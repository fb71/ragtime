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
package ragtime.cc.web.template.topic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.polymap.model2.query.Expressions;

import areca.common.Promise;
import areca.common.Scheduler.Priority;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateNotFoundException;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.model.TopicTemplateConfigEntity;
import ragtime.cc.web.template.CompositeTemplateModel;
import ragtime.cc.web.template.HttpRequestParamsTemplateModel;
import ragtime.cc.web.template.IterableTemplateModel;
import ragtime.cc.web.template.TemplateContentProviderBase;

/**
 * Delegates to the {@link TopicTemplate} specified in the URL.
 *
 * @author Falko Bräutigam
 */
public class TopicTemplateContentProvider
        extends TemplateContentProviderBase {

    private static final Log LOG = LogFactory.getLog( TopicTemplateContentProvider.class );

    /** XXX The templates compatible with {@link TopicTemplateContentProvider} */
    public static final List<String> templates = Arrays.asList( "topic", "insta", "insta-compact" );


    @Override
    protected Promise<Boolean> doProcess() throws Exception {
        var topicTemplateSite = new TopicTemplate.Site();
        topicTemplateSite.r = request;

        var data = topicTemplateSite.data = new HashMap<Object,Object>();
        data.put( "params", new HttpRequestParamsTemplateModel( request.httpRequest ) );
        data.put( "config", new CompositeTemplateModel( config ) );

        // *.css
        var resName = String.join( "/", request.path );
        if (resName.endsWith( ".css" )) {
            processFtl( resName + ".ftl", data );
            return Promise.completed( true, Priority.MAIN_EVENT_LOOP );
        }

        // topics
        var loadTopics = request.uow.query( TopicEntity.class ).executeCollect().onSuccess( rs -> {
            data.put( "topics", new IterableTemplateModel<>( rs, CompositeTemplateModel::new ) );
        });

        // XXX hack a config
        var topicUrlPart = request.path[0];
        var loadHack = request.uow.query( TopicEntity.class )
                .where( Expressions.eq( TopicEntity.TYPE.urlPart, topicUrlPart ) )
                .singleResult().onSuccess( topic -> {
                    LOG.info( "Hack" );
                    request.uow.createEntity( TopicTemplateConfigEntity.class, proto -> {
                        proto.topic.set( topic );
                        proto.urlPart.set( topic.urlPart.get() );
                        proto.topicTemplateName.set( topic.topicTemplateName.get() );
                    });
                });

        // find TopicTemplateConfigEntity
        LOG.info( "Loading Topic/Config for: %s ...", topicUrlPart );
        var loadTemplate = loadHack
                .then( __ -> {
                    return request.uow.query( TopicTemplateConfigEntity.class )
                            .where( Expressions.eq( TopicTemplateConfigEntity.TYPE.urlPart, topicUrlPart ) )
                            .executeCollect();
                })
                .map( configs -> {
                    if (configs.isEmpty()) {
                        throw new TemplateNotFoundException( topicUrlPart, null, "No such topic: " + topicUrlPart );
                    }
                    if (configs.size() > 1) {
                        throw new RuntimeException( "Multiple configs for: " + topicUrlPart + "(" + configs.size() + ")" );
                    }
                    return configs.get( 0 );
                })
                .then( topicConfig -> {
                    topicTemplateSite.config = topicConfig;
                    data.put( "topicConfig", new CompositeTemplateModel( topicConfig ) );
                    return topicConfig.topic.fetch();
                })
                .onSuccess( topic -> {
                    topicTemplateSite.topic = topic;
                    data.put( "topic", new CompositeTemplateModel( topic ) );
                });

        // process TopicTemplate
        return loadTopics
                .then( __ -> loadTemplate )
                .then( topic -> {
                    return TopicTemplate.forName( topicTemplateSite.config.topicTemplateName.get() )
                            .orElseThrow( () -> new RuntimeException( "No such TopicTemplate: " + topicTemplateSite.config.topicTemplateName.get() ) )
                            .process( topicTemplateSite );
                })
                .map( ftl -> {
                    LOG.info( "Loading topic template: %s", ftl );
                    var cfg = TemplateLoader.configuration( config );
                    var template = cfg.getTemplate( ftl );

                    try (var out = request.httpResponse.getWriter()) {
                        template.process( data, out );
                    }
                    return true;
                });
    }

}
