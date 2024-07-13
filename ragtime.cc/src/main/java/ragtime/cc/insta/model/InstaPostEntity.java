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
package ragtime.cc.insta.model;

import org.polymap.model2.Association;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Promise;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import ragtime.cc.model.Article;
import ragtime.cc.model.Common;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class InstaPostEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( InstaPostEntity.class );

    public static final ClassInfo<InstaPostEntity> info = InstaPostEntityClassInfo.instance();

    public static InstaPostEntity TYPE;

    /**
     * Computed back association.
     */
    public static Promise<Opt<InstaPostEntity>> of( Article article, UnitOfWork uow ) {
        return uow.query( InstaPostEntity.class )
                .where( Expressions.is( InstaPostEntity.TYPE.article, article ) )
                .optResult();
    }

    // instance *******************************************

    @Queryable
    public Association<Article> article;

    @Queryable
    public Property<String>     instaRef;

    @Nullable
    public Property<String>     text;

}
