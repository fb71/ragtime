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
package ragtime.cc.media;

import java.util.HashSet;
import java.util.List;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.FileUpload;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.viewer.model.Pojos;
import ragtime.cc.BaseState;
import ragtime.cc.UICommon;
import ragtime.cc.article.EntityListModel;
import ragtime.cc.model.MediaEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class MediasSelectState
        extends BaseState<MediasSelectPage> {

    private static final Log LOG = LogFactory.getLog( MediasState.class );

    public static final ClassInfo<MediasSelectState> INFO = MediasSelectStateClassInfo.instance();

    public Pojos<MediaEntity> selection = new Pojos<>( new HashSet<>() );

    protected RConsumer<List<MediaEntity>> onApply;


    @State.Model
    public EntityListModel<MediaEntity> medias = new EntityListModel<>( MediaEntity.class ) {
        {
            // fire event on Entity change
            fireChangeEventOnEntitySubmit( () -> site.isDisposed() );
        }
        @Override
        protected Query<MediaEntity> query() {
            var searchTxtMatch = Expressions.TRUE;
            return uow.query( MediaEntity.class )
                    .where( searchTxtMatch )
                    .orderBy( MediaEntity.TYPE.modified, Order.DESC );
        }
    };


    /** {@link RuntimeInfo} */
    protected MediasSelectState() {}

    public MediasSelectState( RConsumer<List<MediaEntity>> onApply ) {
        this.onApply = onApply;
    }


    @State.Init
    public void initAction() {
        super.initAction();
        pageflow.create( page = new MediasSelectPage() )
                .putContext( MediasSelectState.this, Page.Context.DEFAULT_SCOPE )
                .putContext( site.get( UICommon.class ), Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Action
    public void applyAction() {
        onApply.accept( selection.sequence().toList() );

        if (page != null && pageflow.isOpen( page )) {
            pageflow.close( page );
        }
        site.dispose();
    }


    @State.Action
    public Promise<Submitted> createMediaAction( FileUpload.File f ) {
        return MediaEntity.getOrCreate( uow, f.name() )
                .map( media -> {
                    media.mimetype.set( f.mimetype() );
                    f.copyInto( media.out() );
                    return media;
                })
                .then( entity -> {
                    // XXX this also submits chnages from calling state
                    return uow.submit();
                });
    }


    @State.Action
    public Promise<Submitted> deleteMediaAction( MediaEntity entity ) {
        uow.removeEntity( entity );
        return uow.submit();
    }

}
