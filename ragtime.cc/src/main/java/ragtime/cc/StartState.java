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

import java.util.Arrays;

import java.io.File;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;
import org.polymap.model2.store.no2.No2Store;

import areca.common.Promise;
import areca.common.base.Lazy.RLazy;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateSite;
import ragtime.cc.article.ArticlesState;
import ragtime.cc.model.Article;

/**
 * The start {@link State} of the application.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class StartState {

    private static final Log LOG = LogFactory.getLog( StartState.class );

    public static final ClassInfo<StartState> INFO = StartStateClassInfo.instance();

    private static RLazy<Boolean> repoInitialized = new RLazy<>();

    protected static EntityRepository repo;

    protected UnitOfWork    uow;

    @State.Context
    protected Pageflow      pageflow;

    @State.Context
    protected StateSite     site;


    @State.Init
    public void init() {
        initRepo();
        uow = repo.newUnitOfWork(); // .setPriority( priority );

        pageflow.create( new FrontPage() )
                .putContext( this, Page.Context.DEFAULT_SCOPE )
                .open();
    }


    @State.Action
    public void listArticles() {
        site.createState( new ArticlesState() )
                .putContext( uow, State.Context.DEFAULT_SCOPE )
                .activate();
    }


    protected void initRepo() {
        repoInitialized.supply( () -> {
            var dir = new File( "/tmp/ragtime.cc" );
            dir.mkdir();
            EntityRepository.newConfiguration()
                    .entities.set( Arrays.asList( Article.info ) )
                    .store.set( new No2Store( new File( dir, "main.db" ) ) )
                    .create()
                    .then( newRepo -> {
                        LOG.debug( "Repo: created." );
                        repo = newRepo;

                        return populateRepo();
                    })
                    .waitForResult( __ -> {
                        LOG.info( "Repo: initialized." );
                    });
            return true;
        });
    }


    protected Promise<Submitted> populateRepo() {
        var uow2 = repo.newUnitOfWork();
        return uow2.query( Article.class ).executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Erster Artikel" );
                            proto.content.set( "Hier steht der Text..." );
                        });
                    }
                    LOG.debug( "Repo: Test Article created" );
                    return uow2.submit();
                })
                .onSuccess( submitted -> {
                    LOG.debug( "Repo: submitted." );
                });
    }

}
