/*
 * Copyright (C) 2020, the @authors. All rights reserved.
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

import java.util.Date;

import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.model2.runtime.Lifecycle;

import areca.common.event.EventListener;
import areca.common.event.EventManager;
import areca.common.event.EventManager.EventHandlerInfo;
import areca.common.reflect.RuntimeInfo;
import ragtime.cc.website.http.WebsiteServlet;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public abstract class Common
        extends Entity
        implements Lifecycle {

    @Queryable
    public Property<Date> created;

    @Queryable
    public Property<Date> modified;


    @Override
    public void onLifecycleChange( State state ) {
        if (state == State.AFTER_CREATED) {
            var now = new Date();
            created.set( now );
            modified.set( now );
        }
        if (state == State.BEFORE_SUBMIT && status() != EntityStatus.REMOVED ) {
            modified.set( new Date() );

            // XXX brute force
            WebsiteServlet.clearCache();
        }
        // XXX this send ALL lifecycle events - although we are just using AFTER_SUBMIT;
        // except for REMOVE, which we need because there is no other way to find out (after submit);
        // would be cool to have a good way to check actual modifications/remove/create
        EventManager.instance().publish( new EntityLifecycleEvent( this, state ) );
    }


    /**
     * Registers a listener for {@link EntityLifecycleEvent}s with the given {@link State}.
     */
    public EventHandlerInfo onLifecycle( State state, EventListener<EntityLifecycleEvent> l ) {
        return EventManager.instance().subscribe( l )
                .performIf( EntityLifecycleEvent.class, ev ->
                        ev.state == state && ev.getSource() == Common.this );
    }

}
