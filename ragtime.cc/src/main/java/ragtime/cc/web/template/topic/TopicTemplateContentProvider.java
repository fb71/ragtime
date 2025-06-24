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
    public static final List<String> TEMPLATES = Arrays.asList( "topic", "insta", "insta-compact", "company" );

    public static final String       TEMPLATE_DEFAULT = TEMPLATES.get( 2 );


    @Override
    protected Promise<Boolean> doProcess() throws Exception {
        var topicTemplateSite = new TopicTemplate.Site();
        topicTemplateSite.r = request;

        var data = topicTemplateSite.data = new HashMap<Object,Object>();
        data.put( "params", new HttpRequestParamsTemplateModel( request.httpRequest ) );
        data.put( "config", new CompositeTemplateModel( config ) );

        // robots/sitemap
        var resName = String.join( "/", request.path );
        if (resName.equals( "robots.txt" ) || resName.equals( "sitemap.txt" )) {
            return request.uow.query( TopicEntity.class ).executeCollect().map( rs -> {
                data.put( "topics", new IterableTemplateModel<>( rs, CompositeTemplateModel::new ) );
                processFtl( resName + ".ftl", data );
                return true;
            });
        }

        // *.css
        if (resName.endsWith( ".css" )) {
            processFtl( resName + ".ftl", data );
            return Promise.completed( true, Priority.MAIN_EVENT_LOOP );
        }

        // /home
        if (resName.equals( WebsiteServlet.PATH_HOME )) {
            return TopicEntity.home( request.uow ).map( topic -> {
                request.httpResponse.sendRedirect( topic.permName.get() );
                return true;
            });
        }

        // XXX filter topics without config
//        var loadTopics = request.uow.query( TopicEntity.class ).execute()
//                .then( opt -> {
//                    return opt.isPresent()
//                            ? TopicTemplateConfigEntity.of( opt.get() )
//                            : Promise.absent( Priority.BACKGROUND );
//                })
//                .reduce( new ArrayList<TopicTemplateConfigEntity>(), (r,opt) -> {
//                    LOG.warn( "Topic: %s", opt );
//                    opt.ifPresent( tconfig -> r.add( tconfig ) );
//                })
//                .map( rs -> {
//                    data.put( "topics", new IterableTemplateModel<>( rs, CompositeTemplateModel::new ) );
//                    return rs;
//                });
        var loadTopics = request.uow.query( TopicEntity.class ).executeCollect()
                .map( rs -> {
                    data.put( "topics", new IterableTemplateModel<>( rs, CompositeTemplateModel::new ) );
                    return rs;
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
                })
                .map( rs -> {
                    if (rs.isEmpty()) {
                        throw new TemplateNotFoundException( permName, null, "No Topic/Article for permName (or Article has no Topic set): " + permName );
                    }
                    if (rs.size() > 1) {
                        throw new TemplateNotFoundException( permName, null, "More than one Articles/Topics for URL: " + permName );
                    }
                    data.put( "topic", new CompositeTemplateModel( rs.get( 0 ) ) );
                    return topicTemplateSite.topic = rs.get( 0 );
                });

        // TopicTemplateConfigEntity
        var loadTopicTemplate = findTopic
                .then( topic -> TopicTemplateConfigEntity.of( topicTemplateSite.topic ) )
                .map( opt -> {
                    if (opt.isAbsent()) {
                        throw new TemplateNotFoundException( permName, null, "Ver&ouml;ffentlichung auf der Website ist nicht konfiguriert f&uuml;r das Topic: " + topicTemplateSite.topic.title.get() );
                    }
                    topicTemplateSite.config = opt.get();
                    data.put( "topicConfig", new CompositeTemplateModel( topicTemplateSite.config ) );
                    return topicTemplateSite.config;
                });

        // process TopicTemplate
        LOG.info( "Loading Topic/Config for: %s ...", permName );
        return loadTopics
                .then( __ -> loadTopicTemplate )
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
