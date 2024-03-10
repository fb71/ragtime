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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

import org.polymap.model2.runtime.UnitOfWork;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.cache.ClassTemplateLoader;
import freemarker.core.DirectiveCallPlace;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;
import ragtime.cc.website.http.ContentProvider;
import ragtime.cc.website.model.TemplateConfigEntity;

/**
 * Provides HTML content created from templates + data/model with FreeMarker and
 * CommonMark.
 *
 * @author Falko Bräutigam
 */
public class TemplateContentProvider
        implements ContentProvider {

    private static final Log LOG = LogFactory.getLog( TemplateContentProvider.class );

    public static final int         BUFFER_SIZE = 32 * 1024;

    private static final Pattern    MACRO_CALL = Pattern.compile("<@[^.]*\\.data name=\\\"([^\\\"]+)\\\" model=\"([^\"]+)\" params=\"([^\"]*)\"/>");

    private static Configuration    cfg;


    static {
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
    public void process( Request request ) throws Exception {
        var templateName = String.join( "/", request.path );
        request.httpResponse.setBufferSize( BUFFER_SIZE );

        // skip *(.css).map
        if (templateName.endsWith( ".map" )) {
            request.httpResponse.setStatus( 404 );
            return;
        }
        // stream resource (*.css, *.woff, ...)
        var res = getClass().getClassLoader().getResource( "templates/" + templateName );
        if (res != null) {
            try (
                var in = res.openStream();
                var out = request.httpResponse.getOutputStream();
            ) {
                IOUtils.copy( in, out, BUFFER_SIZE );
            }
            return;
        }

        // config.css
        var config = request.uow.query( TemplateConfigEntity.class ).singleResult().waitForResult().get();
        if (templateName.equals( "config.css" )) {
            try (var out = request.httpResponse.getWriter()) {
                IOUtils.copy( new StringReader( config.css.get() ), out );
            }
            return;
        }

        try {
            // load template
            LOG.info( "Loading template: %s(.ftl)", templateName );
            var template = cfg.getTemplate( templateName + ".ftl" );

            var data = loadData( template, request.httpRequest, request.uow );
            data.put( "params", new HttpRequestParamsTemplateModel( request.httpRequest ) );
            data.put( "config", new CompositeTemplateModel( config ) );

            try (var out = request.httpResponse.getWriter()) {
                template.process( data, out );
            }
        }
        catch (TemplateNotFoundException e) {
            request.httpResponse.setStatus( 404 );
            try (var out = request.httpResponse.getWriter()) {
                out.write( "Unter dieser Adresse gibt es nichts." );
            }
        }
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
                    LOG.info( "Data macro: name=%s model=%s params=%s", name, modelName, modelParams );

                    // Entity by id
                    if (modelName.equals( EntityByIdTemplateModel.class.getSimpleName() )) {
                        result.put( name, new EntityByIdTemplateModel( modelParams, uow ) );
                    }
                    // Article by tag
                    else if (modelName.equals( ArticleTemplateModel.class.getSimpleName() )) {
                        result.put( name, new ArticleTemplateModel( modelParams, uow ) );
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
