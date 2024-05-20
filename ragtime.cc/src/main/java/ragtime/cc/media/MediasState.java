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

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.FileUpload;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.Pojo;
import ragtime.cc.BaseState;
import ragtime.cc.UICommon;
import ragtime.cc.article.EntityListModel;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.MediaEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class MediasState
        extends BaseState<MediasPage> {

    private static final Log LOG = LogFactory.getLog( MediasState.class );

    public static final ClassInfo<MediasState> INFO = MediasStateClassInfo.instance();

    @State.Model
    public Model<String>        searchTxt = new Pojo<>( "" );

    @State.Model
    public Model<AccountEntity> selected = new Pojo<>();

    @State.Model
    public EntityListModel<MediaEntity> medias = new EntityListModel<>( MediaEntity.class ) {
        {
            // re-fire events from searchTxt
            searchTxt.subscribe( ev -> fireChangeEvent() ).unsubscribeIf( () -> site.isDisposed() );
            // fire event on Entity change
            fireChangeEventOnEntitySubmit( () -> site.isDisposed() );
        }
        @Override
        protected Query<MediaEntity> query() {
            var searchTxtMatch = Expressions.TRUE;
            if (searchTxt.get().length() > 0) {
                searchTxtMatch = Expressions.matches( MediaEntity.TYPE.name, searchTxt.get() + "*" );
            }
            return uow.query( MediaEntity.class )
                    .where( searchTxtMatch )
                    .orderBy( MediaEntity.TYPE.name, Order.ASC );
        }
//        @Override
//        public void remove( MediaEntity entity ) {
//            uow.removeEntity( entity );
//            fireChangeEvent();
//        }
    };


    @State.Init
    public void initAction() {
        super.initAction();
        pageflow.create( page = new MediasPage() )
                .putContext( MediasState.this, Page.Context.DEFAULT_SCOPE )
                .putContext( site.get( UICommon.class ), Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Action
    public Promise<Submitted> createMediaAction( FileUpload.File f ) {
        return MediaEntity.getOrCreate( uow, f.name() )
                .map( media -> {
                    media.mimetype.set( f.mimetype() );
                    f.copyInto( media.out() );
                    return media;
                })
                .then( entity -> {
                    return uow.submit();
                });
    }


    @State.Action
    public Promise<Submitted> deleteMediaAction( MediaEntity entity ) {
        uow.removeEntity( entity );
        return uow.submit();
    }

}
