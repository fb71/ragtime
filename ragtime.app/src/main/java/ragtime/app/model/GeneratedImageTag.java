/*
 * Copyright (C) 2023, the @authors. All rights reserved.
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
package ragtime.app.model;

import org.polymap.model2.Entity;
import org.polymap.model2.ManyAssociation;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;

import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class GeneratedImageTag
        extends Entity {

    public static final ClassInfo<GeneratedImageTag> INFO = GeneratedImageTagClassInfo.instance();

    public static GeneratedImageTag TYPE;

    public enum TagType {
        EMOTIONAL_CONTEXT
    }

    @Queryable
    public Property<String>     label;

    @Queryable
    public Property<TagType>    type;

    @Queryable
    public ManyAssociation<GeneratedImage> images;
}
