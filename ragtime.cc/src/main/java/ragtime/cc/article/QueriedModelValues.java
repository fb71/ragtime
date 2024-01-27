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
import org.polymap.model2.query.Query;
import org.polymap.model2.runtime.Lifecycle.State;

import areca.common.Assert;
import areca.common.Promise;
import areca.common.base.Opt;
import areca.common.base.Supplier.RSupplier;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.viewer.model.LazyListModel;
import areca.ui.viewer.model.ModelBaseImpl;
import ragtime.cc.model.EntityLifecycleEvent;

/**
 *
 * @author Falko Bräutigam
 */
public class QueriedModelValues<V extends Entity>
        extends ModelBaseImpl
        implements LazyListModel<V> {

    private static final Log LOG = LogFactory.getLog( QueriedModelValues.class );

    protected RSupplier<Query<V>>  supplier;

    /**
     * Override {@link #query()}!
     */
    public QueriedModelValues() {
    }


    public QueriedModelValues( RSupplier<Query<V>> supplier ) {
        this.supplier = Assert.notNull( supplier );
    }


    /**
     * Causes this to {@link #fireChangeEvent()} if {@link Entity}s changed.
     */
    public QueriedModelValues<V> fireChangeEventOnEntitySubmit( RSupplier<Boolean> unsubscribeIf ) {
        EventManager.instance()
                .subscribe( ev -> fireChangeEvent() )
                // XXX check type on every entity
                .performIf( EntityLifecycleEvent.class, ev -> ev.state == State.AFTER_SUBMIT )
                .unsubscribeIf( unsubscribeIf );
        return this;
    }


    protected Query<V> query() {
        return Assert.notNull( supplier, "Override #query() or provide a Supplier in the ctor!" ).supply();
    }

    @Override
    public Promise<Integer> count() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Promise<Opt<V>> load( int first, int max ) {
        return query().execute();
    }

}
