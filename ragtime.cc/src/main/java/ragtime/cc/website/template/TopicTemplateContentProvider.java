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
import ragtime.cc.website.model.TopicTemplateConfigEntity;
import ragtime.cc.website.template.tt.BasicTopicTemplate;

/**
 * Processes... {@link TopicTemplate}.
 *
 * @author Falko Bräutigam
 */
public class TopicTemplateContentProvider
        extends TemplateContentProviderBase {

    private static final Log LOG = LogFactory.getLog( TopicTemplateContentProvider.class );

    /** XXX The templates that are compatible with {@link TopicTemplateContentProvider} */
    public static final List<String> templates = Arrays.asList( "insta" );


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
            data.put( "topics", new IterableAdapterTemplateModel<>( rs, t -> new CompositeTemplateModel( t ) ) );
        });

        // hack a config
        var loadHack = request.uow.query( TopicEntity.class ).executeCollect().onSuccess( rs -> {
            LOG.info( "Hack" );
            request.uow.createEntity( TopicTemplateConfigEntity.class, proto -> {
                proto.topic.set( rs.get( 0 ) );
                proto.urlPart.set( "home" );
                proto.topicTemplateName.set( BasicTopicTemplate.class.getSimpleName() );
            });
        });

        // find TopicTemplateConfigEntity
        var topicUrlPart = request.path[0];
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
                    var topicTemplate = TopicTemplate.forKey( topicTemplateSite.config.topicTemplateName.get() );
                    return topicTemplate.process( topicTemplateSite );
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
