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

import org.polymap.model2.PropertyConcernAdapter;

import areca.common.Assert;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 * Updates permName in {@link Article} and {@link TopicEntity}.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class UpdatePermNameConcern
        extends PropertyConcernAdapter<String> {

    private static final Log LOG = LogFactory.getLog( UpdatePermNameConcern.class );

    public static final ClassInfo<UpdatePermNameConcern> info = UpdatePermNameConcernClassInfo.instance();

    @Override
    public void set( String value ) {
        _delegate().set( value );

        var entity = context.getEntity();
        var permName = PermNameConcern.permName( value );

        if (entity instanceof Article article) {
            //if (article.permName.opt().isEmpty()) {
                article.permName.set( permName );
            //}
        }
        else if (entity instanceof TopicEntity topic) {
            //if (topic.permName.opt().isEmpty()) {
                topic.permName.set( permName );
            //}
        }
        else {
            Assert.fail( "Unknown entity type: " + entity );
        }
    }

}
