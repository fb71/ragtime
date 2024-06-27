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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;

import areca.common.Promise;
import areca.common.Scheduler.Priority;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateNotFoundException;
import ragtime.cc.model.Article;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.http.WebsiteServlet;
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
    public static final List<String> templates = Arrays.asList( "topic", "insta", "insta-compact", "company" );


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

        // /home
        if (resName.equals( WebsiteServlet.PATH_HOME )) {
            return request.uow.query( TopicEntity.class )
                    .orderBy( TopicEntity.TYPE.order, Query.Order.ASC )
                    .executeCollect()
                    .map( topics -> {
                        request.httpResponse.sendRedirect( topics.get( 0 ).permName.get() );
                        return true;
                    });
        }

        // topics
        var loadTopics = request.uow.query( TopicEntity.class ).executeCollect().onSuccess( rs -> {
            data.put( "topics", new IterableTemplateModel<>( rs, CompositeTemplateModel::new ) );
        });

        // permName -> topic/article
        var permName = request.path[0];
        var permNameInTopic = request.uow
                .query( TopicEntity.class )
                .where( Expressions.eq( TopicEntity.TYPE.permName, permName ) )
                .optResult();
        var permNameInArticle = request.uow
                .query( Article.class )
                .where( Expressions.eq( Article.TYPE.permName, permName ) )
                .optResult()
                .thenOpt( article -> {
                    topicTemplateSite.article = article;
                    return article.get().topic.fetch();
                });
        var findTopic = permNameInTopic
                .join( permNameInArticle )
                .reduce( new ArrayList<TopicEntity>(), (r,opt) -> {
                    //LOG.info( "Topic: %s", opt.map( t -> t.permName.get() ) );
                    opt.ifPresent( t -> r.add( t ) );
                });

        // XXX hack a config
        var hackTopicConfig = findTopic.map( rs -> {
            if (rs.isEmpty()) {
                throw new TemplateNotFoundException( permName, null, "No Topic/Article for permName (or Article has no Topic set): " + permName );
            }
            if (rs.size() > 1) {
                throw new TemplateNotFoundException( permName, null, "Mehrere Beitraege/Topics haben die URL: " + permName );
            }
            var topic = rs.get( 0 );
            LOG.info( "Hack" );
            topicTemplateSite.config = request.uow.createEntity( TopicTemplateConfigEntity.class, proto -> {
                proto.topic.set( topic );
                proto.topicTemplateName.set( topic.topicTemplateName.get() );
            });
            topicTemplateSite.topic = topic;
            return topic;
        });

        // find TopicTemplateConfigEntity
        LOG.info( "Loading Topic/Config for: %s ...", permName );
        var loadTemplate = hackTopicConfig
                // XXX
                .map( __ -> Arrays.asList( topicTemplateSite.config ) )
//                .then( topic -> {
//                    return request.uow.query( TopicTemplateConfigEntity.class )
//                            .where( Expressions.is( TopicTemplateConfigEntity.TYPE.topic, topic ) )
//                            .executeCollect();
//                })
                .map( configs -> {
                    if (configs.isEmpty()) {
                        throw new TemplateNotFoundException( permName, null, "No such topic: " + permName );
                    }
                    if (configs.size() > 1) {
                        throw new RuntimeException( "Multiple configs for: " + permName + "(" + configs.size() + ")" );
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
                    LOG.info( "Loading template: %s", ftl );
                    var cfg = TemplateLoader.configuration( config );
                    var template = cfg.getTemplate( ftl );

                    try (var out = request.httpResponse.getWriter()) {
                        template.process( data, out );
                    }
                    return true;
                });
    }

}
