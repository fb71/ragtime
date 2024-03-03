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

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Assert;
import areca.common.Platform;
import areca.common.Promise;
import areca.common.base.Sequence;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.rt.server.servlet.ArecaUIServer;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateSite;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.Pojo;
import ragtime.cc.article.ArticlesState;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.PasswordEncryption;
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

    public static final String COOKIE_NAME = "ragtime.cc.me";

    public static final String COOKIE_DELIM = "-";

    @State.Context
    protected Pageflow      pageflow;

    @State.Context
    protected StateSite     site;

    @State.Context
    protected UICommon      uic;

    protected EntityRepository repo;

    protected UnitOfWork    uow;

    @State.Model
    public Model<String>    login = new Pojo<>( "" );

    @State.Model
    public Model<String>    pwd = new Pojo<>( "" );


    @State.Init
    public void init() {
        repo = Repositories.mainRepo();
        uow = repo.newUnitOfWork(); // .setPriority( priority );

        Platform.async( () -> {
            var remembered = remembered();
            if (remembered != null) {
                LOG.warn( "Remembered: %s", remembered );
                advanceState( remembered );
            }
            else {
                pageflow.create( new LoginPage() )
                        .putContext( this, Page.Context.DEFAULT_SCOPE )
                        .putContext( uic, Page.Context.DEFAULT_SCOPE )
                        .open();
            }
        });
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

                    account.lastLogin.set( new Date() );
                    storeNewCookie( account );
                    uow.submit();

                    advanceState( account );
                });
    }


    protected void advanceState( AccountEntity account ) {
        var contentRepo = Repositories.repo( account.permid.get() );
        var contentUow = contentRepo.newUnitOfWork();
        site.createState( new ArticlesState() )
                .putContext( account, Repositories.SCOPE_MAIN )
                .putContext( repo, Repositories.SCOPE_MAIN )
                .putContext( uow, Repositories.SCOPE_MAIN )
                .putContext( contentRepo, State.Context.DEFAULT_SCOPE )
                .putContext( contentUow, State.Context.DEFAULT_SCOPE )
                .activate();
    }


    protected AccountEntity remembered() {
        var r = ArecaUIServer.currentRequest.get();
        //Sequence.of( r.request.getCookies() ).forEach( c -> LOG.info( "Cookie: %s", c.getName() ) );

        return Sequence.of( r.request.getCookies() )
                .first( c -> c.getName().equals( COOKIE_NAME ) )
                .map( presented -> {
                    var parts = StringUtils.split( presented.getValue(), COOKIE_DELIM );
                    if (parts.length == 2) {
                        var rs = uow.query( AccountEntity.class )
                                .where( Expressions.eq( AccountEntity.TYPE.rememberMeSalt, parts[1] ) )
                                .executeCollect()
                                .waitForResult().get();

                        Assert.that( rs.size() < 2 );
                        if (rs.size() == 1) {
                            var encrypted = PasswordEncryption.doEncrypt( parts[0], parts[1] );
                            if (rs.get( 0 ).rememberMe.get().equals( encrypted )) {
                                return rs.get( 0 );
                            }
                        }
                    }
                    return null;
                })
                .orNull();
    }

    protected void storeNewCookie( AccountEntity account ) {
        var value = RandomStringUtils.random( 24, true, true );
        var encrypted = PasswordEncryption.encrypt( value );

        var cookie = new Cookie( COOKIE_NAME, value + COOKIE_DELIM + encrypted.salt);
        cookie.setHttpOnly( true );
        cookie.setMaxAge( (int)TimeUnit.DAYS.toSeconds( 180 ) );
        ArecaUIServer.currentRequest.get().response.addCookie( cookie );

        account.rememberMe.set( encrypted.hash );
        account.rememberMeSalt.set( encrypted.salt );
    }


    public static Promise<Submitted> logout( AccountEntity account ) {
        var uow = account.context.getUnitOfWork();
        account.rememberMe.set( "" );
        return uow.submit().onSuccess( __ -> {
            var r = ArecaUIServer.currentRequest.get();
            r.request.getSession( false ).invalidate();
            //r.response.sendRedirect( "#" );
        });
    }

}
