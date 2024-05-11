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

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Concerns;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.Lifecycle.State;
import org.polymap.model2.runtime.PropertyInfo;

import areca.common.Assert;
import areca.common.base.Consumer.RConsumer;
import areca.common.base.Sequence;
import areca.common.base.Supplier.RSupplier;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.viewer.model.ListModel;
import areca.ui.viewer.model.ModelBaseImpl;
import areca.ui.viewer.model.ModifiableListModel;
import ragtime.cc.model.EntityLifecycleEvent;
import ragtime.cc.model.PropertyChangeConcern.PropertyChangeEvent;

/**
 *
 * @author Falko Bräutigam
 */
public class EntityCompositeListModel<V extends Composite>
        extends ModelBaseImpl
        implements ListModel<V> {

    private static final Log LOG = LogFactory.getLog( EntityCompositeListModel.class );

    protected CollectionProperty<V> coll;

    protected Class<? extends Entity> entityType;

    protected PropertyInfo<? extends Comparable> orderBy;


    public EntityCompositeListModel( Class<? extends Entity> entityType, CollectionProperty<V> coll ) {
        this.entityType = entityType;
        this.coll = coll;
    }


    public EntityCompositeListModel<V> orderBy(
            Property<? extends Comparable> newOrderBy,
            RSupplier<Boolean> unsubscribeIf ) {

        this.orderBy = newOrderBy.info();
        Assert.that( orderBy.getAnnotation( Concerns.class ) != null, "No (PropertyChange)@Concern on Property: " + orderBy );
        EventManager.instance()
                .subscribe( ev -> fireChangeEvent() )
                .performIf( PropertyChangeEvent.class, ev -> ev.getSource().info() == orderBy )
                .unsubscribeIf( unsubscribeIf );
        return this;
    }


    /**
     * Causes this model to {@link #fireChangeEvent()} if {@link Entity}s changed.
     */
    public EntityCompositeListModel<V> fireChangeEventOnEntitySubmit( RSupplier<Boolean> unsubscribeIf ) {
        EventManager.instance()
                .subscribe( ev -> fireChangeEvent() )
                .performIf( EntityLifecycleEvent.class, ev ->
                        ev.state == State.AFTER_SUBMIT && entityType.isInstance( ev.getSource() ) )
                .unsubscribeIf( unsubscribeIf );
        return this;
    }


    @Override
    public int size() {
        return coll.size();
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public Iterator<V> iterator() {
        var l = Sequence.of( coll ).toList();
        if (orderBy != null) {
            l.sort( (e1, e2) -> {
                var p1 = (Property<? extends Comparable>)orderBy.get( e1 );
                var p2 = (Property<? extends Comparable>)orderBy.get( e2 );
                return p1.get().compareTo( p2.get() );
            });
        }
        //LOG.info( "Sorted: %s", l );
        return l.iterator();
    }


    /**
     * Creates a new {@link Composite} in the underlying {@link Entity}.
     * <p>
     * <b>Beware:</b> This modifies the underlying {@link Entity}!
     * <p>
     * There is no way to create a {@link Composite} without the
     * Entity/{@link CollectionProperty} is belongs to. So this does not follow the
     * {@link ModifiableListModel} interface.
     */
    public void createElement( RConsumer<V> initializer ) {
        coll.createElement( initializer );
        fireChangeEvent();
    }


    /**
     * Removes a new {@link Composite} in the underlying {@link Entity}.
     * <p>
     * <b>Beware:</b> This modifies the underlying {@link Entity}!
     */
    public void removeElement( V elm ) {
        coll.remove( elm );
        fireChangeEvent();
    }

}
