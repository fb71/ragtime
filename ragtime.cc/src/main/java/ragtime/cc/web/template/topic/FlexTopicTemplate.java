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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query.Order;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateHashModel;
import ragtime.cc.model.Article;
import ragtime.cc.web.template.IterableTemplateModel;
import ragtime.cc.web.template.MapTemplateModel;

/**
 *
 * @author Falko Bräutigam
 */
public class FlexTopicTemplate
        extends BasicTopicTemplate {

    private static final Log LOG = LogFactory.getLog( FlexTopicTemplate.class );

    @Override
    public String label() {
        return "Flex";
    }

    @Override
    protected Promise<String> processTopic() {
        var rows = new IterableTemplateModel<>( new ArrayList<IterableTemplateModel>() );
        site.data.put( "rows", rows );
        return site.topic.articles()
                // query order field
                .andWhere( Expressions.notNull( Article.TYPE.order ) )
                .orderBy( Article.TYPE.order, Order.ASC )
                .executeCollect()
                .then( rs -> {
                    var rowBase = Integer.MIN_VALUE;
                    var row = new IterableTemplateModel<>( new ArrayList<TemplateHashModel>() );
                    for (var article : rs) {
                        if (article.order.get() / 10 > rowBase) {
                            rowBase = article.order.get() / 10;
                            row = new IterableTemplateModel<>( new ArrayList<TemplateHashModel>() );
                            rows.delegate.add( row );
                        }
                        row.delegate.add( new MapTemplateModel( processTopicArticle( article ) ) );
                    }

                    // query without order
                    return site.topic.articles()
                            .andWhere( Expressions.isNull( Article.TYPE.order ) )
                            .orderBy( Article.TYPE.modified, Order.DESC )
                            .executeCollect();
                })
                .map( rs -> {
                    //
                    for (var article : rs) {
                        var row = new IterableTemplateModel<>( asList( new MapTemplateModel( processTopicArticle( article ) ) ) );
                        rows.delegate.add( row );
                    }

                    return "flex.ftl";
                });
    }


}
