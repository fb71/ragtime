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
package ragtime.app;

import java.util.List;
import java.util.concurrent.Callable;

import org.teavm.jso.browser.Window;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.store.tidbstore.IDBStore;

import areca.common.Platform;
import areca.common.ProgressMonitor;
import areca.common.Promise;
import areca.common.base.Consumer.RConsumer;
import areca.common.base.Supplier.RSupplier;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Level;
import areca.common.log.LogFactory.Log;
import areca.rt.teavm.SimpleBrowserHistoryStrategy;
import areca.rt.teavm.TeaApp;
import areca.ui.component2.UIComposite;
import areca.ui.layout.MaxWidthLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import ragtime.app.ai.OAIImageLab;
import ragtime.app.model.GeneratedImage;
import ragtime.app.model.GeneratedImageTag;
import ragtime.app.model.ModelInitializer;

/**
 *
 * @author Falko Bräutigam
 */
public class RagtimeApp
        extends TeaApp {

    private static final Log LOG = LogFactory.getLog( RagtimeApp.class );

    private static final int DB_VERSION = 7;

    /** Default space in the UI */
    public static final int SPACE = 15;

    public static boolean           debug;

    private static EntityRepository repo;

    private static UnitOfWork       uow;

    /**
     *
     */
    public static class PendingUnitOfWork
            extends Pending<UnitOfWork> {

        public PendingUnitOfWork( RSupplier<UnitOfWork> supplier ) {
            super( supplier );
        }
    }

    /**
     *
     */
    public static void main( String[] args ) throws Exception {
        debug = Window.current().getLocation().getSearch().contains( "debug" );
        LOG.info( "Debug: %s", debug );
        LogFactory.DEFAULT_LEVEL = debug ? Level.INFO : Level.WARN;
        LogFactory.setClassLevel( RagtimeApp.class, Level.INFO );

        Promise.setDefaultErrorHandler( defaultErrorHandler() );

        try {
            // init
            var app = new RagtimeApp();

            // database
            EntityRepository.newConfiguration()
                    .entities.set( List.of( GeneratedImage.INFO, GeneratedImageTag.INFO ) )
                    .store.set( new IDBStore( "ragtime.app", DB_VERSION, true ) )
                    .create()
                    .then( newRepo -> {
                        RagtimeApp.repo = newRepo;
                        return new ModelInitializer().initModel( repo );
                    })
                    .onSuccess( __ -> {
                        LOG.info( "Database and model repo initialized." );
                        uow = repo.newUnitOfWork();
                    });

            // UI
            app.createUI( rootWindow -> {
                String pathName = Window.current().getLocation().getPathName();
                LOG.info( "URI path: %s", pathName );

                rootWindow.layout.set( MaxWidthLayout.width( 500 ).fillHeight.set( true ) );
                var pageflowContainer = rootWindow.add( new UIComposite() {{
                    cssClasses.add( "MaxWidth" );
                }});
                rootWindow.layout();

                Pageflow.start( pageflowContainer )
                        .create( new SelfAwarenessPage() )
                        .putContext( new OAIImageLab( OAIImageLab.KEY ), Page.Context.DEFAULT_SCOPE )
                        .putContext( new PendingUnitOfWork( () -> uow ), Page.Context.DEFAULT_SCOPE )
                        .open();

                SimpleBrowserHistoryStrategy.start( Pageflow.current() );
            });
        }
        catch (Throwable e) {
            LOG.info( "Exception: %s -->", e );
            Throwable rootCause = Platform.rootCause( e );
            LOG.info( "Root cause: %s : %s", rootCause, rootCause.getMessage() );
            throw (Exception)rootCause;
        }
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
