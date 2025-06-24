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

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus;
import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Size;
import areca.ui.component2.FileUpload;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.viewer.model.Pojos;
import ragtime.cc.BaseState;
import ragtime.cc.ConfirmDialog;
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
        createStatePage( new MediasSelectPage() )
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
    public void createMediaAction( FileUpload.File f ) {
        MediaEntity.getOrCreate( uow, f.name() ).onSuccess( media -> {
            // mime check
            if (StringUtils.isBlank( f.mimetype() )) {
                ConfirmDialog.create( "MimeType",
                        "Der Type der Datei kann nicht ermittelt werden.<br/>Ist die Endung des Dateinamens korrekt?" )
                        .addOkAction( () -> LOG.info( "" ) )
                        .open();
            }
            // status/name check
            else if (!media.status().equals( EntityStatus.CREATED )) {
                ConfirmDialog.create( "Name",
                        "<center><b>" + media.name.get() + "</b><br/><br/>Existiert bereits und wird überschrieben!</center>" )
                        .size.set( Size.of( 320, 200 ) )
                        .addCancelAction( () -> LOG.info( "cancelled" ) )
                        .addOkAction( () -> createMedia( f, media ) )
                        .open();
            }
            //
            else {
                createMedia( f, media );
            }
        });
    }


    protected void createMedia( FileUpload.File f, MediaEntity media ) {
        try {
            media.mimetype.set( f.mimetype() );
            f.copyInto( media.out() );

            // we are working on behalf of another state...
            //uow.submit();
            medias.fireChangeEvent();
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


//    @State.Action
//    public Promise<Submitted> deleteMediaAction( MediaEntity entity ) {
//        uow.removeEntity( entity );
//    }

}
