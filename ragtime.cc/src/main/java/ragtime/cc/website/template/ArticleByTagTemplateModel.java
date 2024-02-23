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

import static org.polymap.model2.query.Expressions.and;
import static org.polymap.model2.query.Expressions.anyOf;
import static org.polymap.model2.query.Expressions.eq;

import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Assert;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.model.Article;
import ragtime.cc.model.TagEntity;

/**
 * Provides an {@link Article} by specified name of a {@link TagEntity}.
 *
 * @author Falko Bräutigam
 */
public class ArticleByTagTemplateModel
        extends CompositeTemplateModel {

    private static final Log LOG = LogFactory.getLog( ArticleByTagTemplateModel.class );

    public static final String PARAM_TAG = "t";

    public ArticleByTagTemplateModel( ModelParams modelParams, UnitOfWork uow ) {
        var tagQuery = and(
                eq( TagEntity.TYPE.name, modelParams.get( PARAM_TAG ) ),
                eq( TagEntity.TYPE.category, TagEntity.WEBSITE_NAVI ) );

        var rs = uow.query( Article.class )
                .where( anyOf( Article.TYPE.tags, tagQuery ) )
                .executeCollect()
                .waitForResult().get();

        Assert.isEqual( 1, rs.size(), "Wrong number of result: " + rs.size() );

        this.composite = rs.get( 0 );
    }

}
