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
package ragtime.cc.web.template;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.core.DirectiveCallPlace;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import ragtime.cc.model.TopicEntity;

/**
 * Provides HTML content created from templates + data/model with FreeMarker and
 * CommonMark.
 *
 * @author Falko Bräutigam
 */
public class TemplateContentProvider
        extends TemplateContentProviderBase {

    private static final Log LOG = LogFactory.getLog( TemplateContentProvider.class );

    /** XXX The templates that are compatible with {@link TemplateContentProvider} */
    public static final List<String> templates = Arrays.asList( "common", "first", "fb71" );

    private static final Pattern    MACRO_CALL = Pattern.compile("<@[^.]*\\.data name=\\\"([^\\\"]+)\\\" model=\"([^\"]+)\" params=\"([^\"]*)\"/>");


    @Override
    protected Promise<Boolean> doProcess() throws Exception {
        var resName = String.join( "/", request.path );

        LOG.info( "Loading template: %s(.ftl)", resName );
        var cfg = TemplateLoader.configuration( config );
        var template = cfg.getTemplate( resName + ".ftl" );

        var data = loadData( template, request.httpRequest, request.uow );
        data.put( "params", new HttpRequestParamsTemplateModel( request.httpRequest ) );
        data.put( "config", new CompositeTemplateModel( config ) );
        data.put( "topics", new QueryTemplateModel( request.uow.query( TopicEntity.class ) ) );

        try (var out = request.httpResponse.getWriter()) {
            template.process( data, out );
        }
        // XXX template models have async stuff
        return done( true );
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
    protected HashMap<Object,Object> loadData( Template template, HttpServletRequest req, UnitOfWork uow ) throws Exception {
        var result = new HashMap<>();
        for (var child : Collections.list( template.getRootTreeNode().children() )) {
            // find macro calls
            if (child instanceof DirectiveCallPlace) {
                var m = MACRO_CALL.matcher( child.toString() );
                if (m.matches()) {
                    var modelParams = new ModelParams();

                    modelParams.addHttpParams( req );

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
