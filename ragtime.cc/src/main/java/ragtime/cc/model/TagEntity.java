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
import org.polymap.model2.Queryable;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TagEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( TagEntity.class );

    public static final ClassInfo<TagEntity> info = TagEntityClassInfo.instance();

    public static TagEntity TYPE;

    /** Category: Website frontpage navigation/menu entries */
    public static final String WEBSITE_NAVI = "Website:Navigation";

    @Queryable
    public Property<String> name;

    @Queryable
    public Property<String> category;

    public Property<String> description;

}
