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

import java.io.IOException;

import org.polymap.model2.Entity;
import org.polymap.model2.ManyAssociation;

import areca.common.Platform;
import areca.common.Promise;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.FileUpload.File;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.viewer.model.LazyListModel;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.ModelBaseImpl;
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

    @State.Context(required = false)
    @State.Model
    public Model<Article>   article = new EntityModel<>();

    @State.Model
    public AssocListModel<MediaEntity> medias;


    @State.Init
    public void initAction() {
        super.initAction();

        medias = new AssocListModel<>( article.get().medias );

        pageflow.create( page = new ArticlePage() )
                .putContext( this, Page.Context.DEFAULT_SCOPE )
                .putContext( uic, Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Action
    public void createMediaAction( File uploaded ) {
        uow.createEntity( MediaEntity.class, proto -> {
            try {
                MediaEntity.defaults().accept( proto );
                proto.name.set( uploaded.name() );
                proto.mimetype.set( uploaded.mimetype() );
                uploaded.copyInto( proto.out() );

                article.get().medias.add( proto );
            }
            catch (IOException e) {
                // XXX
                throw new RuntimeException( e );
            }
        });
        medias.fireChangeEvent();
    }


    public void removeMediaAction( MediaEntity media ) {
        uow.removeEntity( media );
        medias.fireChangeEvent();
    }


    /**
     *
     */
    public static class AssocListModel<V extends Entity>
            extends ModelBaseImpl
            implements LazyListModel<V> {

        protected ManyAssociation<V> assoc;

        public AssocListModel( ManyAssociation<V> assoc ) {
            this.assoc = assoc;
        }

        @Override
        public Promise<Integer> count() {
            throw new RuntimeException( "not implemented." );
        }

        @Override
        public Promise<Opt<V>> load( int first, int max ) {
            LOG.info( "Load: %s", assoc.info().getName() );
            return assoc.fetch()
                    // XXX ManyAssociation.fetch() does not seem to return absent as last element
                    .join( Platform.schedule( 1000, () -> Opt.absent() ) );
        }

        @Override
        public void fireChangeEvent() {
            super.fireChangeEvent();
        }

    }

}
