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

import java.util.EventObject;

import org.polymap.model2.PropertyConcern;
import org.polymap.model2.PropertyConcernAdapter;

import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class PropertyChangeConcern
        extends PropertyConcernAdapter<Comparable>
        implements PropertyConcern<Comparable> {

    private static final Log LOG = LogFactory.getLog( PropertyChangeConcern.class );

    public static final ClassInfo<PropertyChangeConcern> info = PropertyChangeConcernClassInfo.instance();

    @Override
    public Comparable get() {
        return _delegate().get();
    }

    @Override
    public void set( Comparable value ) {
        _delegate().set( value );
        EventManager.instance().publish( new PropertyChangeEvent( this ) );
    }

    /**
     *
     */
    public static class PropertyChangeEvent
            extends EventObject {

        public PropertyChangeEvent( PropertyChangeConcern prop ) {
            super( prop );
            LOG.info( "Changed: %s", prop.info().getName() );
        }

        @Override
        public PropertyChangeConcern getSource() {
            return (PropertyChangeConcern)super.getSource();
        }
    }

}