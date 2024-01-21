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

import static org.polymap.model2.query.Expressions.matches;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Assert;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.modeladapter.LazyModelValues;
import areca.ui.modeladapter.ModelValue;
import areca.ui.modeladapter.Pojo;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateAction;
import areca.ui.statenaction.StateSite;
import ragtime.cc.model.Article;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticlesState {

    private static final Log LOG = LogFactory.getLog( ArticlesState.class );

    public static final ClassInfo<ArticlesState> INFO = ArticlesStateClassInfo.instance();

    @State.Context
    protected StateSite     site;

    @State.Context
    protected Pageflow      pageflow;

    @State.Context
    protected UnitOfWork    uow;

    protected ArticlesPage  page;

    /**
     * Model: searchTxt
     */
    @State.Model
    public ModelValue<String>   searchTxt = new Pojo<>( "" );

    @State.Model
    public ModelValue<Article>  selected = new Pojo<>();

    /**
     * Model: articles
     */
    @State.Model
    public LazyModelValues<Article> articles = new QueriedModelValues<>() {
        {
            fireIfChanged( searchTxt );
        }
        @Override
        protected Query<Article> query() {
            var searchTxtMatch = Expressions.TRUE;
            if (searchTxt.get().length() > 0) {
                searchTxtMatch = Expressions.or(
                        matches( Article.TYPE.title, searchTxt.get() + "*" ),
                        matches( Article.TYPE.content, searchTxt.get() + "*" ) );
            }
            return uow.query( Article.class )
                    .where( searchTxtMatch )
                    .orderBy( Article.TYPE.modified, Order.DESC );
        }
    };


    @State.Init
    public void initAction() {
        pageflow.create( page = new ArticlesPage() )
                .putContext( ArticlesState.this, Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Dispose
    public void disposeAction() {
        pageflow.close( page );
        site.dispose();
    };


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
