/*
 * Copyright (C) 2024, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package ragtime.cc.website.template;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.StringReader;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;
import ragtime.cc.website.http.ContentProvider;
import ragtime.cc.website.model.TemplateConfigEntity;

/**
 *
 *
 * @author Falko Bräutigam
 */
public abstract class TemplateContentProviderBase
        implements ContentProvider {

    private static final Log LOG = LogFactory.getLog( TemplateContentProviderBase.class );

    public static final int         BUFFER_SIZE = 32 * 1024;

    protected Request request;

    protected TemplateConfigEntity config;

    /**
     *
     */
    protected abstract Promise<Boolean> doProcess()
            throws TemplateNotFoundException, Exception;


    @Override
    public Promise<Boolean> process( @SuppressWarnings( "hiding" ) Request request ) throws Exception {
        this.request = request;
        this.config = request.config;

        request.httpResponse.setBufferSize( BUFFER_SIZE );

        // Edit mode: preserve param in session
        var editParam = request.httpRequest.getParameter( "edit" );
        if (editParam != null) {
            var session = request.httpRequest.getSession( true );
            session.setAttribute( "edit", editParam );
        }

        // skip *(.css).map
        var resName = String.join( "/", request.path );
        if (resName.endsWith( ".map" )) {
            request.httpResponse.setStatus( 404 );
            return done( true );
        }

        // stream resource (*.css, *.woff, ...)
        // XXX thread loader?
        var res = getClass().getClassLoader().getResource( "templates/" + resName );
        if (res != null) {
            try (
                var in = res.openStream();
                var out = request.httpResponse.getOutputStream();
            ){
                IOUtils.copy( in, out, BUFFER_SIZE );
            }
            return done( true );
        }

        // config.css
        if (resName.equals( "config.css" )) {
            try (var out = request.httpResponse.getWriter()) {
                IOUtils.copy( new StringReader( config.css.get() ), out );
            }
            return done( true );
        }

        return doProcess();
    }


    protected void processFtl( String name, Map<Object,Object> data ) throws Exception {
        LOG.info( "Loading template: %s", name );
        var cfg = TemplateLoader.configuration( config );
        var template = cfg.getTemplate( name );

        try (var out = request.httpResponse.getWriter()) {
            template.process( data, out );
        }
    }


    /**
     * {@link Configuration} factory and template loader that...
     */
    public static class TemplateLoader
            extends ClassTemplateLoader {

        private static Map<String,Configuration> cfg = new ConcurrentHashMap<>();

        public static Configuration configuration( TemplateConfigEntity config ) {
            return cfg.computeIfAbsent( config.templateName.get(), templateName -> {
                try {
                    Version v2_3_32 = new Version( 2, 3, 32 );
                    var result = new Configuration( v2_3_32 );
                    result.setDefaultEncoding( "ISO-8859-1" );
                    result.setLocale( Locale.GERMAN );
                    result.setTemplateExceptionHandler( TemplateExceptionHandler.RETHROW_HANDLER );
                    //result.setTemplateLookupStrategy( );
                    result.setLocalizedLookup( false );

                    result.setTemplateLoader( new TemplateLoader( templateName ) );
                    LOG.warn( "Configuration initialized: %s", templateName );
                    return result;
                }
                catch (Exception e) {
                    throw new RuntimeException( e );
                }
            });
        }

        // instance ***************************************

        private TemplateInfo    template;

        protected TemplateLoader( String templateName ) {
            super( TemplateInfo.cl(), TemplateInfo.TEMPLATES_BASE_PATH );
            this.template = TemplateInfo.forName( templateName );
        }

        @Override
        protected URL getURL( String name ) {
            var t = template;
            var result = (URL)null;
            while (result == null && t != null) {
                result = t.resource( name );
                t = result == null ? t.parent() : t;
            }
            //LOG.warn( "TEMPLATE: %s (%s)", name, t != null ? t.name : "not found" );
            return result;
        }
    }

}