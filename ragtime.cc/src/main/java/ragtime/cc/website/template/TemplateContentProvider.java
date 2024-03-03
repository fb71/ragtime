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

import java.io.BufferedWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import org.polymap.model2.runtime.UnitOfWork;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.cache.ClassTemplateLoader;
import freemarker.core.DirectiveCallPlace;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
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

    private static final Pattern        MACRO_CALL = Pattern.compile("<@[^.]*\\.data name=\\\"([^\\\"]+)\\\" model=\"([^\"]+)\" params=\"([^\"]*)\"/>");

    private static Configuration        cfg;


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


    public void process( Request request ) throws Exception {
        var templateName = StringUtils.substringAfterLast( request.httpRequest().getPathInfo(), "/" );
        LOG.info( "Loading template: %s(.ftl)", templateName );
        var template = cfg.getTemplate( templateName + ".ftl" );

        var data = loadData( template, request.httpRequest(), request.uow() );
        data.put( "params", new HttpRequestParamsTemplateModel( request.httpRequest() ) );
        data.put( "config", loadTemplateConfig( request.uow() ) );

        try (var out = new BufferedWriter( request.httpResponse().getWriter() )) {
            template.process( data, out );
        }
    }


    protected Object loadTemplateConfig( UnitOfWork uow ) {
        var config = uow.query( TemplateConfigEntity.class ).singleResult().waitForResult().get();
        return new CompositeTemplateModel( config );
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
