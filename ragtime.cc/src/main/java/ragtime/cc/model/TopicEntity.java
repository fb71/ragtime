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
package ragtime.cc.model;

import static org.polymap.model2.query.Expressions.anyOf;

import java.util.List;

import org.polymap.model2.DefaultValue;
import org.polymap.model2.Defaults;
import org.polymap.model2.ManyAssociation;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.query.Expressions;

import areca.common.Promise;
import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import ragtime.cc.website.model.TopicTemplateConfigEntity;
import ragtime.cc.website.template.TopicTemplate;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TopicEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( TopicEntity.class );

    public static final ClassInfo<TopicEntity> info = TopicEntityClassInfo.instance();

    public static TopicEntity TYPE;

    public static RConsumer<TopicEntity> defaults() {
        return proto -> {
        };
    }

    // instance *******************************************

    /** Human readable, changable label of this Topic */
    @Queryable
    @DefaultValue( "" )
    public Property<String>     title;

    @DefaultValue( "" )
    public Property<String>     description;

    @Nullable
    @DefaultValue( "#905090" )
    public Property<String>     color;

    /** The position of this item (ascending order) */
    @Defaults
    //@Concerns( PropertyChangeConcern.class )
    public Property<Integer>    order;

    /**
     * The name of the {@link TopicTemplate} to use.
     * @deprecated {@link TopicTemplateConfigEntity#topic}
     */
    @Queryable
    @DefaultValue( "Basic" )
    public Property<String>     topicTemplateName;

    /**
     * The URL path that identifies this {@link #topic}
     * @deprecated {@link TopicTemplateConfigEntity}
     */
    @Queryable
    @Nullable
    public Property<String>     urlPart;

    @Queryable
    public ManyAssociation<TopicEntity> members;

    /**
     * Computed bidi association of {@link #members}.
     */
    public Promise<TopicEntity> parent() {
        return context.getUnitOfWork().query( TopicEntity.class )
                .where( anyOf( TopicEntity.TYPE.members, Expressions.id( id() ) ) )
                .singleResult();
    }

    /**
     * Computed bidi association of {@link Article#topic}.
     */
    public Promise<List<Article>> articles() {
        //LOG.warn( "XXX articles(): ALL!!!!" );
        return context.getUnitOfWork().query( Article.class )
                .where( Expressions.is( Article.TYPE.topic, this ) )
                .executeCollect();
    }

}
