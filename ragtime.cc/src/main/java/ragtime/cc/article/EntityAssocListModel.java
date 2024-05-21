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
import org.polymap.model2.ManyAssociation;

import areca.common.Platform;
import areca.common.Promise;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.viewer.model.LazyListModel;
import areca.ui.viewer.model.ModelBaseImpl;

/**
 *
 * @param <V>
 * @author Falko Bräutigam
 */
public class EntityAssocListModel<V extends Entity>
        extends ModelBaseImpl
        implements LazyListModel<V> {

    private static final Log LOG = LogFactory.getLog( EntityAssocListModel.class );

    protected ManyAssociation<V> assoc;

    protected boolean modified;

    public EntityAssocListModel( ManyAssociation<V> assoc ) {
        this.assoc = assoc;
    }

    public boolean modified() {
        return modified;
    }

    @Override
    public Promise<Integer> count() {
        throw new RuntimeException( "not implemented." );
    }

    @Override
    public Promise<Opt<V>> load( int first, int max ) {
        LOG.info( "Load: %s", assoc.info().getName() );
        return assoc.fetch()
                .onSuccess( opt -> LOG.info( "sending: %s", opt ) )
                // XXX ManyAssociation.fetch() does not seem to return absent as last element
                .join( Platform.schedule( 200, () -> {
                    LOG.info( "sending: complete/absent" );
                    return Opt.absent();
                }));
    }

    @Override
    public void fireChangeEvent() {
        modified = true;

        super.fireChangeEvent();
    }

}