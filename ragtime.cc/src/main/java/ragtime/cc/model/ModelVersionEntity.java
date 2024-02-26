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

import org.polymap.model2.Property;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 * The version of the Model in the store, used for schema updates.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ModelVersionEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( ModelVersionEntity.class );

    public static final ClassInfo<ModelVersionEntity> info = ModelVersionEntityClassInfo.instance();

    /** The current version of the main model */
    public static final Integer VERSION_MAIN = 1;

    /** The current version of the content model */
    public static final Integer VERSION_CONTENT = 1;

    public Property<Integer> version;

}