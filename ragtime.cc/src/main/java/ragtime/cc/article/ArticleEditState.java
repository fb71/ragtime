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

import java.util.List;

import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.FileUpload.File;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.viewer.model.Model;
import ragtime.cc.BaseState;
import ragtime.cc.UICommon;
import ragtime.cc.model.Article;
import ragtime.cc.model.MediaEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticleEditState
        extends BaseState<ArticlePage> {

    private static final Log LOG = LogFactory.getLog( ArticleEditState.class );

    public static final ClassInfo<ArticleEditState> INFO = ArticleEditStateClassInfo.instance();

    @State.Context
    @Deprecated
    protected UICommon      uic;

    @State.Model
    @State.Context(required = false)
    public Model<Article>   article = new EntityModel<>();

    @State.Model
    public EntityAssocListModel<MediaEntity> medias;

    public boolean          modelChanged;


    @State.Init
    public void initAction() {
        super.initAction();

        medias = new EntityAssocListModel<>( article.get().medias );

        createStatePage( new ArticlePage() )
                .putContext( this, Page.Context.DEFAULT_SCOPE )
                .putContext( uic, Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Action
    public void createMediaAction( File uploaded ) {
        MediaEntity.getOrCreate( uow, uploaded.name() ).onSuccess( media -> {
            media.mimetype.set( uploaded.mimetype() );
            uploaded.copyInto( media.out() );
            article.get().medias.add( media );

            modelChanged = true;
            medias.fireChangeEvent();
        });
    }


    public void removeMediaAction( MediaEntity media ) {
        //uow.removeEntity( media );
        article.get().medias.remove( media );

        modelChanged = true;
        medias.fireChangeEvent();
    }


    public void addMedias( List<MediaEntity> add ) {
        add.forEach( media -> article.get().medias.add( media ) );
        modelChanged = true;
        medias.fireChangeEvent();
    }


    public Promise<Submitted> deleteAction() {
        uow.removeEntity( article.$() );
        return uow.submit();
    }

}
