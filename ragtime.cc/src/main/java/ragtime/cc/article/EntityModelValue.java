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

import org.polymap.model2.Entity;
import org.polymap.model2.runtime.Lifecycle.State;

import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.modeladapter.ModelValue;
import ragtime.cc.model.EntityLifecycleEvent;

/**
 *
 * @author Falko Bräutigam
 */
public class EntityModelValue<V extends Entity>
        extends ModelValue<V> {

    private static final Log LOG = LogFactory.getLog( EntityModelValue.class );

    private V entity;

    public EntityModelValue() {
        EventManager.instance()
                .subscribe( ev -> fireChangeEvent() )
                .performIf( EntityLifecycleEvent.class, ev ->
                        ev.state == State.AFTER_SUBMIT && ev.belongsTo( entity ) )
                .unsubscribeIf( () -> isDisposed() );
    }

    @Override
    public V get() {
        return entity;
    }

    @Override
    public void set( V entity ) {
        this.entity = entity;
        fireChangeEvent();
    }

}
