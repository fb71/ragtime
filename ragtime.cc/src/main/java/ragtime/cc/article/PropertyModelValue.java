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
package ragtime.cc.article;

import org.polymap.model2.Property;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.ModelBaseImpl;

/**
 *
 * @author Falko Bräutigam
 */
public class PropertyModelValue<V>
        extends ModelBaseImpl
        implements Model<V> {

    private static final Log LOG = LogFactory.getLog( PropertyModelValue.class );

    private Property<V> prop;

    public PropertyModelValue( Property<V> prop ) {
        this.prop = prop;
    }

    @Override
    public V get() {
        return prop.get();
    }

    @Override
    public void set( V value ) {
        prop.set( value );
    }

}
