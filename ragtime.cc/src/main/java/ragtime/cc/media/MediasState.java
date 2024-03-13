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

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.FileUpload;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateSite;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.Pojo;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.MediaEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class MediasState {

    private static final Log LOG = LogFactory.getLog( MediasState.class );

    public static final ClassInfo<MediasState> INFO = MediasStateClassInfo.instance();

    @State.Context
    protected StateSite     site;

    @State.Context
    protected Pageflow      pageflow;

    @State.Context
    protected UnitOfWork    uow;

    protected MediasPage    page;

    /**
     * Model: searchTxt
     */
    @State.Model
    public Model<String>    searchTxt = new Pojo<>( "" );

    @State.Model
    public Model<AccountEntity> selected = new Pojo<>();

//    /**
//     * Model: articles
//     */
//    @State.Model
//    public LazyListModel<AccountEntity> accounts = new EntityListModel<>( AccountEntity.class ) {
//        {
//            // re-fire events from searchTxt
//            searchTxt.subscribe( ev -> fireChangeEvent() ).unsubscribeIf( () -> site.isDisposed() );
//            // fire event on Entity change
//            fireChangeEventOnEntitySubmit( () -> site.isDisposed() );
//        }
//        @Override
//        protected Query<AccountEntity> query() {
//            var searchTxtMatch = Expressions.TRUE;
//            if (searchTxt.get().length() > 0) {
//                searchTxtMatch = or(
//                        matches( AccountEntity.TYPE.login, searchTxt.get() + "*" ),
//                        matches( AccountEntity.TYPE.email, searchTxt.get() + "*" ) );
//            }
//            return uow.query( AccountEntity.class )
//                    .where( searchTxtMatch )
//                    .orderBy( AccountEntity.TYPE.lastLogin, Order.DESC );
//        }
//    };


    @State.Init
    public void initAction() {
        pageflow.create( page = new MediasPage() )
                .putContext( MediasState.this, Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Dispose
    public void disposeAction() {
        pageflow.close( page );
        site.dispose();
    };


    @State.Dispose
    public Promise<Submitted> createMediaAction( FileUpload.File f ) {
        uow.createEntity( MediaEntity.class, proto -> {
            MediaEntity.defaults().accept( proto );
            proto.name.set( f.name() );
            proto.mimetype.set( f.mimetype() );
        });
        return uow.submit();
    };

}
