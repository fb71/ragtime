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
package ragtime.cc.website.model;

import org.polymap.model2.Composite;
import org.polymap.model2.DefaultValue;
import org.polymap.model2.Property;

import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

@RuntimeInfo
public class Colors
        extends Composite {

    public static final ClassInfo<Colors> info = ColorsClassInfo.instance();

    public static Colors    TYPE;

    @DefaultValue( "#f0f0f0" )
    public Property<String>     pageBackground;

    @DefaultValue( "#212529" )
    public Property<String>     pageForeground;

    @DefaultValue( "#f0eff6" )
    public Property<String>     headerBackground;

    @DefaultValue( "#212529" )
    public Property<String>     headerForeground;

    @DefaultValue( "#695ea1" )
    public Property<String>     accent;

    @DefaultValue( "#7767c5" )
    public Property<String>     link;
}