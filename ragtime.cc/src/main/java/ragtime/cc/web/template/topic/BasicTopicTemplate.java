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
import java.util.HashMap;
import java.util.Map;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query.Order;

import areca.common.Assert;
import areca.common.Promise;
import areca.common.Scheduler.Priority;
import areca.common.base.Sequence;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.SimpleCollection;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateNotFoundException;
import ragtime.cc.model.Article;
import ragtime.cc.web.template.CompositeTemplateModel;
import ragtime.cc.web.template.widgets.Excerpt;
import ragtime.cc.web.template.widgets.Markdown;
import ragtime.cc.web.template.widgets.Swiper;
import ragtime.cc.web.template.widgets.TextProcessor;
import ragtime.cc.web.template.widgets.TextProcessor.Context;

/**
 * A basic topic template that renders  two different kind of pages: one for
 * the topic (list) and one for the article (detail). Rendering can be customized by
 * overriding methods.
 *
 * @author Falko Bräutigam
 */
public class BasicTopicTemplate
        extends TopicTemplate {

    private static final Log LOG = LogFactory.getLog( BasicTopicTemplate.class );

    protected TopicTemplate.Site site;


    @Override
    @SuppressWarnings( "hiding" )
    public Promise<String> process( Site site ) throws TemplateNotFoundException {
        Assert.isEqual( 1, site.r.path.length );
        this.site = site;

        // article: ?a=<id>
        var articleIdParam = site.r.httpRequest.getParameter( "a" );
        if (articleIdParam != null) {
            return processArticle( articleIdParam );
        }
        // topic
        else {
            return processTopic();
        }
    }


    @SuppressWarnings( "deprecation" )
    protected Promise<String> processTopic() {
        var ordered = new ArrayList<Article>( 32 );
        return site.topic.articles()
                // with order field
                .andWhere( Expressions.notNull( Article.TYPE.order ) )
                .orderBy( Article.TYPE.order, Order.ASC )
                .executeCollect()
                .then( rs -> {
                    ordered.addAll( rs );
                    // without order field
                    return site.topic.articles()
                            .andWhere( Expressions.isNull( Article.TYPE.order ) )
                            .orderBy( Article.TYPE.modified, Order.DESC )
                            .executeCollect();
                })
                .map( rs -> {
                    ordered.addAll( rs );

                    site.data.put( "articles", new SimpleCollection( Sequence.of( Exception.class, ordered )
                            .map( article -> processTopicArticle( article ) )
                            .asIterable() ) );
                    return "basic.ftl";
                });
    }


    protected Map<String,TemplateModel> processTopicArticle( Article article ) throws Exception {
        var result = new HashMap<String,TemplateModel>();
        result.put( "entity", new CompositeTemplateModel( article ) );
        result.put( "article", new CompositeTemplateModel( article ) );

        var ctx = new TextProcessor.Context() {{ config = site.r.config; }};
        var content = new StringBuilder( 64 * 1024 ).append( article.content.get() );
        processTopicArticle( article, content, ctx );
        result.put( "content", new SimpleScalar( content.toString() ) );
        return result;
    }


    protected void processTopicArticle( Article article, StringBuilder content, Context ctx ) throws Exception {
        new Excerpt( processArticleLink( article ) ).process( content, ctx );
        new Markdown().process( content, ctx );
        new Swiper( () -> article.medias.fetchCollect().waitForResult().get() ).process( content, ctx );
    }


    protected Promise<String> processArticle( String articleId ) {
        var ctx = new TextProcessor.Context() {{ config = site.r.config; }};
        var content = new StringBuilder( 64 * 1024 ); // C64! :)

        return site.r.uow.entity( Article.class, articleId )
                .then( article -> processArticle( article, content, ctx ) );
    }


    protected Promise<String> processArticle( Article article, StringBuilder content, Context ctx ) throws Exception {
        site.data.put( "article", new CompositeTemplateModel( article ) );
        content.append( article.content.get() );

        new Excerpt( null ).process( content, ctx );
        new Markdown().process( content, ctx );
        new Swiper( () -> article.medias.fetchCollect().waitForResult().get() ).process( content, ctx );

        site.data.put( "content", new SimpleScalar( content.toString() ) );
        return Promise.completed( "article.ftl", Priority.MAIN_EVENT_LOOP );
    }


    @SuppressWarnings( "deprecation" )
    protected String processArticleLink( Article article ) {
        // '${topic.urlPart}?a=${article.entity.id}'
        return String.format( "<a href=\"%s?a=%s\">Weiter...</a>",
                article.topic.fetch().waitForResult().get().urlPart.get(), // XXX waitForResult()
                article.id() );
    }


    @Override
    public String label() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

}
