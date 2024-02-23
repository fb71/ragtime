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
package ragtime.cc;

import javax.security.auth.login.LoginException;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Assert;
import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateSite;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.Pojo;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.Repositories;

/**
 * The start {@link State} of the application.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class LoginState {

    private static final Log LOG = LogFactory.getLog( LoginState.class );

    public static final ClassInfo<LoginState> INFO = LoginStateClassInfo.instance();

    @State.Context
    protected Pageflow      pageflow;

    @State.Context
    protected StateSite     site;

    @State.Context
    protected UICommon      uic;

    protected EntityRepository repo;

    protected UnitOfWork    uow;

    @State.Model
    public Model<String>    login = new Pojo<>( "admin" );

    @State.Model
    public Model<String>    pwd = new Pojo<>( "admin" );


    @State.Init
    public void init() {
        repo = Repositories.mainRepo();
        uow = repo.newUnitOfWork(); // .setPriority( priority );

        pageflow.create( new LoginPage() )
                .putContext( this, Page.Context.DEFAULT_SCOPE )
                .putContext( uic, Page.Context.DEFAULT_SCOPE )
                .open();
    }


    @State.Action
    public Promise<AccountEntity> loginAction() {
        return uow.query( AccountEntity.class )
                .where( Expressions.eq( AccountEntity.TYPE.login, login.get() ) )
                .executeCollect()
                .map( rs -> {
                    Assert.that( rs.size() <= 1 );
                    if (rs.isEmpty()) {
                        throw new LoginException( "No such login: '" + login.get() + "'" );
                    }
                    return rs.get( 0 );
                })
                .onSuccess( account -> {
                    if (!account.checkPassword( pwd.$() )) {
                        throw new LoginException( "Wrong pwd for login: " + login.get() );
                    }

                    var contentRepo = Repositories.repo( account.permid.get() );
                    var contentUow = contentRepo.newUnitOfWork();
                    site.createState( new StartState() )
                            .putContext( account, Repositories.SCOPE_MAIN )
                            .putContext( repo, Repositories.SCOPE_MAIN )
                            .putContext( uow, Repositories.SCOPE_MAIN )
                            .putContext( contentRepo, State.Context.DEFAULT_SCOPE )
                            .putContext( contentUow, State.Context.DEFAULT_SCOPE )
                            .activate();
                });
    }

}
