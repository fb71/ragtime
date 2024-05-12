/*
 * Copyright (C) 2024, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package ragtime.cc.website.http;

import static areca.rt.server.EventLoop.FULLY;
import static org.apache.commons.lang3.StringUtils.split;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;

import areca.common.Assert;
import areca.common.Platform;
import areca.common.Promise;
import areca.common.Scheduler.Priority;
import areca.common.Session;
import areca.common.SessionScoper.ThreadBoundSessionScoper;
import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.rt.server.EventLoop;
import freemarker.template.TemplateNotFoundException;
import ragtime.cc.model.ContentRepo;
import ragtime.cc.website.model.TemplateConfigEntity;
import ragtime.cc.website.template.TemplateContentProvider;
import ragtime.cc.website.template.TopicTemplateContentProvider;

/**
 * Frontend servlet, does {@link Session} management and exception handling and
 * delegates to the actual content provider servlets.
 *
 * @author Falko Bräutigam
 */
public class WebsiteServlet
        extends HttpServlet {

    private static final Log LOG = LogFactory.getLog( WebsiteServlet.class );

    private static final String ATTR_SESSION = "ragtime.cc.website.session";


    protected void error( Throwable e, HttpServletResponse resp ) {
        try (var out = resp.getWriter()) {
            if (e instanceof TemplateNotFoundException) {
                LOG.warn( "Template not found: %s", e.toString() );
                //out.write( "Unter dieser Adresse gibt es nichts." );

                // catch old atlas/polymap customers
                out.write( "<!DOCTYPE HTML>\n"
                        + "<html>\n"
                        + "    <head>\n"
                        + "        <meta charset=\"UTF-8\">\n"
                        + "        <meta http-equiv=\"refresh\" content=\"5; url=https://fb71.org/\">\n"
                        + "        <title>Page does not exist</title>\n"
                        + "    </head>\n"
                        + "    <body style=\"font-family: sans-serif;\">\n"
                        + "        <h1>Page does not exist</h1>\n"
                        + "        <h3>Your are redirected to: <a href='https://fb71.org/'>fb71.org</a></h3>\n"
                        + "    </body>\n"
                        + "</html>" );
            }
            else {
                resp.setStatus( 500 );
                out.write( "Leider ging etwas schief. Die Seite kann nicht angezeigt werden.\n\n" );
                out.write( e.toString() );

                LOG.warn( "Error while processing: %s", e.toString() );
                Platform.rootCause( e ).printStackTrace( System.err );
            }
        }
        catch (IOException ee) {
            ee.printStackTrace( System.err );
        }
    }


    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        try {
            var t = Timer.start();
            var session = new Session();

            ThreadBoundSessionScoper.instance().bind( session, __ -> {
                var eventloop = EventLoop.create();
                Session.setInstance( eventloop );
                eventloop.enqueue( "website request", () -> {
                    try {
                        process( req, resp );
                    }
                    catch (Exception e) {
                        error( e, resp );
                    }
                }, 0 );
                eventloop.execute( FULLY );
            });

            session.dispose();
            LOG.warn( "%s: %s - %s", resp.getStatus(), req.getPathInfo(), t.elapsedHumanReadable() );
        }
        catch (Exception e) {
            error( e, resp );
        }
    }


    protected void process( HttpServletRequest req, HttpServletResponse resp ) throws Exception {
        // find permid in path: /website/XX/home|(media/..)
        var parts = split( req.getPathInfo(), '/' );
        var permid = Integer.parseInt( parts[0] );

        var request = new ContentProvider.Request() {{
            this.httpRequest = req;
            this.httpResponse = resp;
            this.path = parts;
        }};
        request.uow = ContentRepo.waitFor( permid ).newUnitOfWork();

        //LOG.info( "Path: %s", Arrays.toString( parts ) );

        request.uow.query( TemplateConfigEntity.class ).singleResult()
                .then( config -> {
                    request.config = config;

                    // redirect: home
                    if (parts.length == 1) {
                        Assert.that( req.getPathInfo().endsWith( "/" ), "Home URL does not end with a '/'" );
                        resp.sendRedirect( "home" );
                        return Promise.completed( null, Priority.MAIN_EVENT_LOOP );
                    }
                    // media
                    else if (parts.length >= 2 && parts[1].equals( MediaContentProvider.PATH )) {
                        request.path = ArrayUtils.removeAll( parts, 0 , 1);
                        return new MediaContentProvider().process( request );
                    }
                    // template
                    else {
                        request.path = ArrayUtils.remove( parts, 0 );
                        var templateName = config.templateName.get();
                        if (TopicTemplateContentProvider.templates.contains( templateName )) {
                            return new TopicTemplateContentProvider().process( request );
                        }
                        else if (TemplateContentProvider.templates.contains( templateName )) {
                            return new TemplateContentProvider().process( request );
                        }
                        else {
                            throw new RuntimeException( "Configured template not found: " + templateName );
                        }
                    }
                })
                .onError( e -> {
                    error( e, resp );
                })
                .waitForResult();

        request.uow.close();
    }

}
