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
package ragtime.cc.website.template;

import static org.apache.commons.lang3.StringUtils.split;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Assert;
import areca.common.Session;
import areca.common.SessionScoper.ThreadBoundSessionScoper;
import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.rt.server.EventLoop;
import freemarker.cache.ClassTemplateLoader;
import freemarker.core.DirectiveCallPlace;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
import freemarker.template.Version;
import ragtime.cc.model.Repositories;
import ragtime.cc.website.model.TemplateConfigEntity;

/**
 * Processes templates + data/model with FreeMarker and CommonMark to provide a
 * website.
 *
 * @author Falko Bräutigam
 */
public class WebsiteServlet
        extends HttpServlet {

    private static final String ATTR_SESSION = "ragtime.cc.website.session";

    private static final Log LOG = LogFactory.getLog( WebsiteServlet.class );

    private static final Pattern        MACRO_CALL = Pattern.compile("<@[^.]*\\.data name=\\\"([^\\\"]+)\\\" model=\"([^\"]+)\" params=\"([^\"]*)\"/>");

    private Configuration               cfg;


    @Override
    public void init() throws ServletException {
        try {
            Version v2_3_32 = new Version( 2, 3, 32 );
            cfg = new Configuration( v2_3_32 );

            cfg.setTemplateLoader( new ClassTemplateLoader( Thread.currentThread().getContextClassLoader(), "templates" ) );

            cfg.setDefaultEncoding( "ISO-8859-1" );
            cfg.setLocale( Locale.GERMAN );
            cfg.setTemplateExceptionHandler( TemplateExceptionHandler.RETHROW_HANDLER );
            LOG.info( "initialized" );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public void destroy() {
        LOG.warn( "DISPOSE " );
        Repositories.dispose();
    }


    /**
     *
     */
    protected Session session( HttpSession httpSession ) {
        var session = (Session)httpSession.getAttribute( ATTR_SESSION );
        synchronized (httpSession) {
            if (session == null) {
                LOG.info( "Session: START" );
                session = new Session();
                httpSession.setAttribute( ATTR_SESSION, session );

                ThreadBoundSessionScoper.instance().bind( session, __ -> {
                    Session.setInstance( new EventLoop() );
                });
            }
        }
        return Assert.notNull( session );
    }


    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        try {
            var t = Timer.start();
            var httpSession = req.getSession( true );
            var session = session( httpSession );

            ThreadBoundSessionScoper.instance().bind( session, __ -> {
                process( req, resp );
            });
            LOG.warn( "processed (%s)", t.elapsedHumanReadable() );
        }
        catch (Exception e) {
            resp.setStatus( 500 );
            try (var out = resp.getWriter()) {
                out.write( "" + e );
            }
            e.printStackTrace();
        }
    }


    protected void process( HttpServletRequest request, HttpServletResponse resp ) throws Exception {
        var uow = Repositories.mainRepo().newUnitOfWork();

        // find permid in path
        var parts = split( request.getPathInfo(), '/' );
        if (parts.length == 2) {
            var permid = Integer.parseInt( parts[0] );
            uow = Repositories.repo( permid ).newUnitOfWork();
        }
        // find from domain name
        else {
            throw new RuntimeException( "Domain name mapping is not yet implemented." );
        }

        var templateName = StringUtils.substringAfterLast( request.getPathInfo(), "/" );
        LOG.info( "Loading template: %s(.ftl)", templateName );
        var template = cfg.getTemplate( templateName + ".ftl" );

        var data = loadData( template, request, uow );
        data.put( "params", new HttpRequestParamsTemplateModel( request ) );
        data.put( "config", loadTemplateConfig( uow ) );

        try (var out = resp.getWriter()) {
            template.process( data, out );
        }
        uow.close();
    }


    protected Object loadTemplateConfig( UnitOfWork uow ) {
        var config = uow.query( TemplateConfigEntity.class ).executeCollect().waitForResult().get().get( 0 );
        return new CompositeTemplateModel( config );
    }

    @Deprecated
    protected Object templateConfig() {
        return new HashMap<>() {{
            put( "nav_items", new ArrayList<>() {{
                add( new HashMap<>() {{ put( "title", "Home"); put( "href", "frontpage"); }});
                add( new HashMap<>() {{ put( "title", "Impressum"); put( "href", "impressum"); }});
            }});
        }};
    }


    /**
     * Initialize/load {@link TemplateModel}s defined inside the template via the
     * <@data ...> macro declared in commons.ftl.
     * <p/>
     * Typical macro call:
     * <pre>
     * <@c.data name="articles" model="QueryTemplateModel" params="type=ragtime.cc.model.Article"/>
     * </pre>
     */
    @SuppressWarnings({"deprecation", "unchecked"})
    protected HashMap<Object,Object> loadData( Template template, HttpServletRequest request, UnitOfWork uow ) throws Exception {
        var result = new HashMap<>();
        for (var child : Collections.list( template.getRootTreeNode().children() )) {
            // find macro calls
            if (child instanceof DirectiveCallPlace) {
                var m = MACRO_CALL.matcher( child.toString() );
                if (m.matches()) {
                    var modelParams = new ModelParams();

                    modelParams.addHttpParams( request );

                    // macro params
                    String name = m.group( 1 );
                    String modelName = m.group( 2 );
                    modelParams.addMacroParams( m.group( 3 ) );
                    LOG.info( "MACRO: name=%s model=%s params=%s", name, modelName, modelParams );

                    // Entity by id
                    if (modelName.equals( EntityByIdTemplateModel.class.getSimpleName() )) {
                        result.put( name, new EntityByIdTemplateModel( modelParams, uow ) );
                    }
                    // Article by tag
                    else if (modelName.equals( ArticleByTagTemplateModel.class.getSimpleName() )) {
                        result.put( name, new ArticleByTagTemplateModel( modelParams, uow ) );
                    }
                    // Query
                    else if (modelName.equals( QueryTemplateModel.class.getSimpleName() )) {
                        result.put( name, new QueryTemplateModel( modelParams, uow ) );
                    }
                    else {
                        throw new RuntimeException( "more work! : " + modelName );
                    }
                }
            }
        }
        return result;
    }

}
