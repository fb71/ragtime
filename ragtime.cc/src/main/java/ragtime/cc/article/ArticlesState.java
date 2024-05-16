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

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.Query.Order;

import areca.common.Assert;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateAction;
import areca.ui.viewer.model.LazyListModel;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.Pojo;
import ragtime.cc.BaseState;
import ragtime.cc.admin.AccountsState;
import ragtime.cc.media.MediasState;
import ragtime.cc.model.Article;
import ragtime.cc.website.TemplateConfigState;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticlesState
        extends BaseState<ArticlesPage> {

    private static final Log LOG = LogFactory.getLog( ArticlesState.class );

    public static final ClassInfo<ArticlesState> INFO = ArticlesStateClassInfo.instance();

    /**
     * Model: searchTxt
     */
    @State.Model
    public Model<String>    searchTxt = new Pojo<>( "" );

    @State.Model
    public Model<Article>   selected = new Pojo<>();

    /**
     * Model: articles
     */
    @State.Model
    public LazyListModel<Article> articles = new EntityListModel<>( Article.class ) {
        {
            // re-fire events from searchTxt
            searchTxt.subscribe( ev -> fireChangeEvent() ).unsubscribeIf( () -> site.isDisposed() );
            // fire event on Entity change
            fireChangeEventOnEntitySubmit( () -> site.isDisposed() );
        }
        @Override
        protected Query<Article> query() {
            var searchTxtMatch = Expressions.TRUE;
            if (searchTxt.get().length() > 0) {
                searchTxtMatch = Expressions.or(
                        Expressions.matches( Article.TYPE.title, searchTxt.get() + "*" ),
                        Expressions.matches( Article.TYPE.content, searchTxt.get() + "*" ) );
            }
            return uow.query( Article.class )
                    .where( searchTxtMatch )
                    .orderBy( Article.TYPE.title, Order.ASC );
        }
    };


    @State.Init
    public void initAction() {
        super.initAction();
        pageflow.create( page = new ArticlesPage() )
                .putContext( ArticlesState.this, Page.Context.DEFAULT_SCOPE )
                .open();
    };


    public void openAccountsAction() {
        site.createState( new AccountsState() ).activate();
    }


    @State.Action
    public void openSettingsAction() {
        site.createState( new TemplateConfigState() ).activate();
    }

    public void openMediasAction() {
        site.createState( new MediasState() ).activate();
    }

    @State.Action
    public void createArticleAction() {
        site.createState( new ArticleCreateState() ).activate();
    }


    @State.Action
    public StateAction<Void> editArticleAction = new StateAction<>() {
        @Override
        public boolean canRun() {
            return selected.$() != null;
        }
        @Override
        public void run( Void arg ) {
            Assert.that( canRun(), "StateAction: !canRun() " );
            site.createState( new ArticleEditState() )
                    .putContext( Assert.notNull( selected.$() ), State.Context.DEFAULT_SCOPE )
                    .onChange( ev -> LOG.info( "STATE CHANGE: %s", ev ) )
                    .activate();
        }
    };

}
