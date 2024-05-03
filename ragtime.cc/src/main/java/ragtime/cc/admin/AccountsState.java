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
package ragtime.cc.admin;

import static org.polymap.model2.query.Expressions.matches;
import static org.polymap.model2.query.Expressions.or;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.viewer.model.LazyListModel;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.Pojo;
import ragtime.cc.BaseState;
import ragtime.cc.article.ArticlesState;
import ragtime.cc.article.EntityListModel;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.ContentRepo;
import ragtime.cc.model.MainRepo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class AccountsState
        extends BaseState<AccountsPage> {

    private static final Log LOG = LogFactory.getLog( AccountsState.class );

    public static final ClassInfo<AccountsState> INFO = AccountsStateClassInfo.instance();

    @State.Context(scope = MainRepo.SCOPE)
    protected UnitOfWork    mainUow;

    /**
     * Model: searchTxt
     */
    @State.Model
    public Model<String>    searchTxt = new Pojo<>( "" );

    @State.Model
    public Model<AccountEntity> selected = new Pojo<>();

    /**
     * Model: articles
     */
    @State.Model
    public LazyListModel<AccountEntity> accounts = new EntityListModel<>( AccountEntity.class ) {
        {
            // re-fire events from searchTxt
            searchTxt.subscribe( ev -> fireChangeEvent() ).unsubscribeIf( () -> site.isDisposed() );
            // fire event on Entity change
            fireChangeEventOnEntitySubmit( () -> site.isDisposed() );
        }
        @Override
        protected Query<AccountEntity> query() {
            var searchTxtMatch = Expressions.TRUE;
            if (searchTxt.get().length() > 0) {
                searchTxtMatch = or(
                        matches( AccountEntity.TYPE.login, searchTxt.get() + "*" ),
                        matches( AccountEntity.TYPE.email, searchTxt.get() + "*" ) );
            }
            return mainUow.query( AccountEntity.class )
                    .where( searchTxtMatch )
                    .orderBy( AccountEntity.TYPE.lastLogin, Order.DESC );
        }
    };


    @State.Init
    public void initAction() {
        super.initAction();
        pageflow.create( page = new AccountsPage() )
                .putContext( AccountsState.this, Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Action
    public void becomeAccountAction( @SuppressWarnings( "hiding" ) AccountEntity account ) {
        var contentRepo = ContentRepo.waitFor( account );
        var contentUow = contentRepo.newUnitOfWork();
        site.createState( new ArticlesState() )
                .putContext( account, MainRepo.SCOPE )
                .putContext( contentRepo, State.Context.DEFAULT_SCOPE )
                .putContext( contentUow, State.Context.DEFAULT_SCOPE )
                .activate();
    }

}
