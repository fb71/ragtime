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

import org.polymap.model2.Association;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;

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

    @Queryable
    public Association<TopicEntity>     topic;

    /**
     * The URL path that identifies this {@link #topic}
     */
    @Queryable
    public Property<String>             urlPart;

    /**
     * Link to {@link TopicTemplate}
     */
    @Queryable
    public Property<String>             topicTemplateName;

}
