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

import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Assert;
import areca.common.base.Consumer.RConsumer;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
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

    public String           searchTxt;

    public Article          selected;


    @State.Init
    public void initAction() {
        pageflow.create( page = new ArticlesPage() )
                .putContext( ArticlesState.this, Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Dispose
    public void disposeAction() {
        pageflow.close( page );
    };


    @State.Action
    public void createArticleAction() {
        site.createState( new ArticleCreateState() ).activate();
    }


    @State.Action
    public StateAction<Void> editArticleAction = new StateAction<>() {
        @Override
        public boolean canRun() {
            return selected != null;
        }
        @Override
        public void run( Void arg ) {
            Assert.that( canRun(), "StateAction: !canRun() " );
            site.createState( new ArticleEditState() )
                    .putContext( Assert.notNull( selected ), State.Context.DEFAULT_SCOPE )
                    .activate();
        }
    };


    public void articles( RConsumer<Opt<Article>> consumer ) {
        uow.query( Article.class )
                .orderBy( Article.TYPE.modified, Order.DESC )
                .execute().onSuccess( opt -> consumer.accept( opt ) );
    }
}
