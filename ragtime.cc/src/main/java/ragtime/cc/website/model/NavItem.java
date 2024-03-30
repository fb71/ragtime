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
import org.polymap.model2.Concerns;
import org.polymap.model2.Defaults;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import areca.common.base.Consumer.RConsumer;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import ragtime.cc.model.PropertyChangeConcern;

@RuntimeInfo
public class NavItem
        extends Composite {

    public static final ClassInfo<NavItem> info = NavItemClassInfo.instance();

    public static NavItem       TYPE;

    public static RConsumer<NavItem> defaults() {
        return proto -> {
            proto.title.set( "Neuer Eintrag" );
            proto.href.set( "frontpage?n=..." );
        };
    }

    public Property<String>     title;

    public Property<String>     href;

    @Nullable
    public Property<String>     description;

    /** The position of this item (ascending order) */
    @Defaults
    @Concerns( PropertyChangeConcern.class )
    public Property<Integer>    order;

    @Override
    public int hashCode() {
        return System.identityHashCode( this );
    }

    @Override
    public boolean equals( Object obj ) {
        return this == obj;
    }

}