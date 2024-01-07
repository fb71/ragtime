/*
 * Copyright (C) 2023, the @authors. All rights reserved.
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
import java.util.concurrent.Callable;

import java.io.File;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;
import org.polymap.model2.store.no2.No2Store;

import areca.common.Platform;
import areca.common.ProgressMonitor;
import areca.common.Promise;
import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Level;
import areca.common.log.LogFactory.Log;
import areca.rt.server.ServerApp;
import areca.ui.component2.UIComposite;
import areca.ui.layout.MaxWidthLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import ragtime.cc.model.Article;

/**
 *
 * @author Falko Bräutigam
 */
public class CCApp
        extends ServerApp {

    private static final Log LOG = LogFactory.getLog( CCApp.class );

    private static final int DB_VERSION = 7;

    /** Default space in the UI */
    public static final int SPACE = 15;

    private static boolean debug = true;

    private volatile boolean init;

    public static EntityRepository repo;

    public static UnitOfWork uow;


    public static void init() throws Exception {
        ServerApp.init();

        LOG.info( "Debug: %s", debug );
        LogFactory.DEFAULT_LEVEL = debug ? Level.INFO : Level.WARN;
        LogFactory.setClassLevel( CCApp.class, Level.INFO );

        Promise.setDefaultErrorHandler( defaultErrorHandler() );
    }


    public static void dispose() {
        LOG.warn( "DISPOSE " );
        if (repo != null) {
            repo.close();
        }
    }

    // instance *******************************************

    public CCApp() {
        if (!init) {
            init = true;
            var dir = new File( "/tmp/ragtime.cc" );
            dir.mkdir();
            EntityRepository.newConfiguration()
                    .entities.set( Arrays.asList( Article.info ) )
                    .store.set( new No2Store( new File( dir, "main.db" ) ) )
                    .create()
                    .then( newRepo -> {
                        LOG.debug( "Repo: created." );
                        repo = newRepo;
                        uow = newRepo.newUnitOfWork(); // .setPriority( priority );

                        return populateRepo();
                    })
                    .waitForResult( __ -> {
                        LOG.info( "Repo: initialized." );
                    });

        }
    }


    @Override
    public void createUI() {
        try {
            LOG.info( "createUI(): ..." );
            createUI( rootWindow -> {
                rootWindow.layout.set( MaxWidthLayout.width( 500 ).fillHeight.set( true ) );
                var pageflowContainer = rootWindow.add( new UIComposite() {{
                    cssClasses.add( "MaxWidth" );
                }});
                rootWindow.layout();

                Pageflow.start( pageflowContainer )
                        .create( new FrontPage() )
                        .putContext( uow, Page.Context.DEFAULT_SCOPE )
                        .open();

                //SimpleBrowserHistoryStrategy.start( Pageflow.current() );
            });
        }
        catch (Throwable e) {
            LOG.info( "Exception: %s -->", e );
            Throwable rootCause = Platform.rootCause( e );
            LOG.info( "Root cause: %s : %s", rootCause, rootCause.getMessage() );
            rootCause.printStackTrace();
        }
    }


    protected Promise<Submitted> populateRepo() {
        var uow2 = repo.newUnitOfWork();
        return uow2.query( Article.class ).executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "test" );
                            proto.content.set( "Hier steht der Text..." );
                        });
                    }
                    LOG.info( "Repo: Test Article created" );
                    return uow2.submit();
                })
                .onSuccess( submitted -> {
                    LOG.info( "Repo: submitted." );
                });
    }


    /**
     * Helps to handle exceptions in any code that is not handled
     * by default (UI clicks are handled by default for example).
     */
    public static <R> R catchAll( Callable<R> callable ) {
        try {
            return callable.call();
        }
        catch (Throwable e) {
            throw rootCauseForTeaVM( e );
        }
    }

    /**
     *
     */
    private static RConsumer<Throwable> defaultErrorHandler() {
        return (Throwable e) -> {
            if (e instanceof ProgressMonitor.CancelledException || e instanceof Promise.CancelledException) {
                LOG.info( "Operation cancelled." );
            }
            else if (debug) {
                throw rootCauseForTeaVM( e );
            }
            else {
                //Pageflow.current().open( new GeneralErrorPage( e ), null );
            }
        };
    }

    /**
     * get a meaningfull stracktrace in TeaVM
     */
    private static RuntimeException rootCauseForTeaVM( Throwable e ) {
        LOG.warn( "Exception: " + e );
        Throwable rootCause = Platform.rootCause( e );
        LOG.warn( "Root cause: " + rootCause, rootCause );
        if (e instanceof RuntimeException) {
            return (RuntimeException)rootCause;
        }
        else {
            return (RuntimeException)rootCause; // XXX
        }
    }

}
