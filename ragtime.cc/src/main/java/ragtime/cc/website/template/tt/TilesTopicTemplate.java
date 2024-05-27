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
package ragtime.cc.website.template.tt;

import java.util.ArrayList;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query.Order;

import areca.common.Assert;
import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateNotFoundException;
import ragtime.cc.model.Article;
import ragtime.cc.website.template.CompositeTemplateModel;
import ragtime.cc.website.template.IterableAdapterTemplateModel;
import ragtime.cc.website.template.TopicTemplate;

/**
 *
 * @author Falko Bräutigam
 */
public class TilesTopicTemplate
        extends TopicTemplate {

    private static final Log LOG = LogFactory.getLog( TilesTopicTemplate.class );

    @Override
    public String label() {
        return "Tiles";
    }


    @Override
    public Promise<String> process( Site site ) throws TemplateNotFoundException {
        Assert.isEqual( 1, site.r.path.length );

        // article: ?a=<id>
        var articleIdParam = site.r.httpRequest.getParameter( "a" );
        if (articleIdParam != null) {
            return site.r.uow.entity( Article.class, articleIdParam ).map( article -> {
                site.data.put( "article", new CompositeTemplateModel( article ) );
                //processArticleContent( article, site );
                return "article.ftl";
            });
        }
        // topic
        else {
            var result = new ArrayList<Article>( 32 );
            return site.topic.articles()
                    // with order field
                    .andWhere( Expressions.notNull( Article.TYPE.order ) )
                    .orderBy( Article.TYPE.order, Order.ASC )
                    .executeCollect()
                    .then( rs -> {
                        result.addAll( rs );
                        // without order field
                        return site.topic.articles()
                                .andWhere( Expressions.isNull( Article.TYPE.order ) )
                                .orderBy( Article.TYPE.modified, Order.DESC )
                                .executeCollect();
                    })
                    .map( rs -> {
                        result.addAll( rs );
                        site.data.put( "articles", new IterableAdapterTemplateModel<>( result, a -> new CompositeTemplateModel( a ) ) );
                        return "tiles.ftl";
                    });
        }
    }

}
