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

import org.polymap.model2.Composite;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.PropertyConcern;
import org.polymap.model2.PropertyConcernAdapter;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 * Creates an empty {@link Composite} value for an un-initializes/null
 * {@link Property}. Helps with model schema evolution.
 * <p>
 * Beware: this calls {@link Property#createValue(areca.common.base.Consumer)}
 * which modifies the Entity!
 * <p>
 * The {@link Property} needs to be {@link Nullable} (although it will never return
 * null with this concern).
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class DefaultsCompositeConcern
        extends PropertyConcernAdapter<Composite>
        implements PropertyConcern<Composite> {

    private static final Log LOG = LogFactory.getLog( DefaultsCompositeConcern.class );

    public static final ClassInfo<DefaultsCompositeConcern> info = DefaultsCompositeConcernClassInfo.instance();

    @Override
    public Composite get() {
        var result = _delegate().get();
        if (result == null) {
            result = _delegate().createValue( proto -> {
                LOG.info( "Defaults: %s", _delegate().toString(), proto );
            });
        }
        return result;
    }

    @Override
    public void set( Composite value ) {
        _delegate().set( value );
    }
}