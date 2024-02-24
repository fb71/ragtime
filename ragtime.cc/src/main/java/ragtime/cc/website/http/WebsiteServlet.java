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

import static org.apache.commons.lang3.StringUtils.split;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Session;
import areca.common.SessionScoper.ThreadBoundSessionScoper;
import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.rt.server.EventLoop;
import ragtime.cc.CCApp;
import ragtime.cc.model.Repositories;
import ragtime.cc.website.template.TemplateContentProvider;

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
        try {
            resp.setStatus( 500 );
            try (var out = resp.getWriter()) {
                out.write( "Error: " + e );
            }
            e.printStackTrace( System.err );
        }
        catch (IOException ee) {
            e.printStackTrace( System.err );
        }
    }


    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        try {
            var t = Timer.start();
            var session = new Session();

            ThreadBoundSessionScoper.instance().bind( session, __ -> {
                var eventLoop = new EventLoop();
                Session.setInstance( eventLoop );
                eventLoop.enqueue( "website request", () -> {
                    try {
                        process( req, resp );
                    }
                    catch (Exception e) {
                        error( e, resp );
                    }
                }, 0 );
                eventLoop.execute();
            });

            session.dispose();
            LOG.warn( "OK: %s - %s", req.getPathInfo(), t.elapsedHumanReadable() );
        }
        catch (Exception e) {
            error( e, resp );
        }
    }


    protected void process( HttpServletRequest req, HttpServletResponse resp ) throws Exception {
        // find permid in path: /website/XX/home|(media/..)
        var parts = split( req.getPathInfo(), '/' );
        var permid = Integer.parseInt( parts[0] );

        UnitOfWork uow = Repositories.repo( permid ).newUnitOfWork();
        var request = new ContentProvider.Request() {
            @Override
            public HttpServletRequest httpRequest() {
                return req;
            }
            @Override
            public HttpServletResponse httpResponse() {
                return resp;
            }
            @Override
            public UnitOfWork uow() {
                return uow;
            }
            @Override
            public File workspce() {
                return CCApp.workspaceDir( permid );
            }
        };

        //LOG.info( "Path: %s", Arrays.toString( parts ) );
        if (parts.length >= 2 && parts[1].equals( "media" )) {
            new MediaContentProvider().process( request );
        }
        else {
            new TemplateContentProvider().process( request );
        }
        uow.close();
    }

}
