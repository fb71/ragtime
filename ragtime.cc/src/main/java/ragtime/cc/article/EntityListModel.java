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

import java.util.Iterator;

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
import areca.ui.viewer.model.ListModel;
import areca.ui.viewer.model.ModelBaseImpl;
import areca.ui.viewer.model.ModifiableListModel;
import ragtime.cc.model.EntityLifecycleEvent;

/**
 *
 * @author Falko Bräutigam
 */
public class EntityListModel<V extends Entity>
        extends ModelBaseImpl
        implements LazyListModel<V>, ListModel<V> {

    private static final Log LOG = LogFactory.getLog( EntityListModel.class );

    protected RSupplier<Query<V>>   query;

    protected Class<V>              resultType;

    /**
     * Override {@link #query()}!
     */
    public EntityListModel( Class<V> resultType ) {
        this.resultType = resultType;
    }


    public EntityListModel( Class<V> resultType, RSupplier<Query<V>> query ) {
        this( resultType );
        this.query = Assert.notNull( query );
    }


    protected Query<V> query() {
        return Assert.notNull( query, "Override #query() or provide a Supplier in the ctor!" ).supply();
    }


    /**
     *
     * <p>
     * This does not follow {@link ModifiableListModel} ...
     */
    public void remove( V entity ) {
        throw new UnsupportedOperationException( "remove() is not implemented" );
    }


    /**
     * Causes this model to {@link #fireChangeEvent()} if {@link Entity}s changed.
     */
    public EntityListModel<V> fireChangeEventOnEntitySubmit( RSupplier<Boolean> unsubscribeIf ) {
        EventManager.instance()
                .subscribe( ev -> fireChangeEvent() )
                .performIf( EntityLifecycleEvent.class, ev ->
                        ev.state == State.AFTER_SUBMIT && resultType.isInstance( ev.getSource() ) )
                .unsubscribeIf( unsubscribeIf );
        return this;
    }


    @Override
    public Iterator<V> iterator() {
        return query().executeCollect().waitForResult().get().iterator(); // XXX
    }


    @Override
    public int size() {
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Promise<Integer> count() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Promise<Opt<V>> load( int first, int max ) {
        return query().execute();
    }

}
