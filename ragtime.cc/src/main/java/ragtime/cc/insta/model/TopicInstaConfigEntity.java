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

import static org.polymap.model2.query.Expressions.is;

import org.polymap.model2.Association;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;

import areca.common.Promise;
import areca.common.Scheduler.Priority;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import ragtime.cc.model.Common;
import ragtime.cc.model.TopicEntity;

/**
 * The connection from a {@link TopicEntity}  and Insta.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TopicInstaConfigEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( TopicInstaConfigEntity.class );

    public static final ClassInfo<TopicInstaConfigEntity> info = TopicInstaConfigEntityClassInfo.instance();

    public static TopicInstaConfigEntity  TYPE;

    /**
     * Computed back association of {@link TopicInstaConfigEntity#topic}
     */
    public static Promise<Opt<TopicInstaConfigEntity>> of( TopicEntity topic ) {
        if (topic != null) {
            return topic.context.getUnitOfWork().query( TopicInstaConfigEntity.class )
                    .where( is( TopicInstaConfigEntity.TYPE.topic, topic ) )
                    .optResult();
        }
        else {
            return Promise.absent( Priority.MAIN_EVENT_LOOP );
        }
    }

    // instance *******************************************

    @Queryable
    public Association<TopicEntity>     topic;

    public Property<String>             username;

    public Property<String>             password;

}
