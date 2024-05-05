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
package ragtime.cc;

import java.util.List;

import org.polymap.model2.Entity;
import org.polymap.model2.Property;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.PropertyInfo;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.base.Sequence;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.transform.TransformingModelBase;

/**
 * Transforms {@link Entity}s into value of a given {@link Property}.
 *
 * @author Falko Bräutigam
 */
public class EntityTransform<M extends Entity,V>
        extends TransformingModelBase<Model<M>,V>
        implements Model<V> {

    private static final Log LOG = LogFactory.getLog( EntityTransform.class );

    private PropertyInfo<V> prop;

    private Class<M> entityType;

    private UnitOfWork uow;

    private Property<V> p;

    public EntityTransform( UnitOfWork uow, Class<M> entityType, Property<V> prop, Model<M> delegate ) {
        super( delegate );
        this.p = prop;
        this.prop = prop.info();
        this.entityType = entityType;
        this.uow = uow;
    }

    @Override
    public V get() {
        var result = delegate.get();
        return result != null ? ((Property<V>)prop.get( result )).get() : null;
    }

    @Override
    public void set( V value ) {
        if (value != null) {
//            var t = Expressions.template( entityType, uow.repo() );
//            var p = t.info().getProperty( prop.getName() ).get;
            var entity = uow.query( entityType )
                    .where( Expressions.eq( p, value ) )
                    .singleResult().waitForResult().get();

            delegate.set( entity );
        }
    }

    public List<V> values() {
        var rs = uow.query( entityType ).executeCollect().waitForResult().get();
        return Sequence.of( rs ).map( entity -> ((Property<V>)prop.get( entity )).get() ).toList();
    }
}
