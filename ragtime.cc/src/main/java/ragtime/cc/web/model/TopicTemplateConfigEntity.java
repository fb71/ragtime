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
package ragtime.cc.web.model;

import static org.polymap.model2.query.Expressions.is;

import org.polymap.model2.Association;
import org.polymap.model2.DefaultValue;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;

import areca.common.Promise;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import ragtime.cc.model.Common;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.template.topic.TopicTemplate;

/**
 * The connection from a {@link TopicEntity} to its representation in the website.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TopicTemplateConfigEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( TopicTemplateConfigEntity.class );

    public static final ClassInfo<TopicTemplateConfigEntity> info = TopicTemplateConfigEntityClassInfo.instance();

    public static TopicTemplateConfigEntity  TYPE;

    /**
     * Computed back association of {@link TopicTemplateConfigEntity#topic}
     */
    public static Promise<Opt<TopicTemplateConfigEntity>> of( TopicEntity topic ) {
        return topic.context.getUnitOfWork().query( TopicTemplateConfigEntity.class )
                .where( is( TopicTemplateConfigEntity.TYPE.topic, topic ) )
                .optResult();
    }

    // instance *******************************************

    @Queryable
    public Association<TopicEntity>     topic;

    /**
     * Link to {@link TopicTemplate}
     */
    @Queryable
    @DefaultValue( "Basic" )
    public Property<String>             topicTemplateName;

}
