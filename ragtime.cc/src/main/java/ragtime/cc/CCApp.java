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

import java.util.concurrent.Callable;

import java.io.File;

import javax.servlet.ServletContext;

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
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.Repositories;

/**
 *
 * @author Falko Bräutigam
 */
public class CCApp
        extends ServerApp {

    private static final Log LOG = LogFactory.getLog( CCApp.class );

    private static final int DB_VERSION = 7;

    private static boolean debug = true;

    private static File workspaceDir;


    public static void init( ServletContext ctx ) throws Exception {
        LOG.info( "Debug: %s", debug );
        LogFactory.DEFAULT_LEVEL = debug ? Level.INFO : Level.WARN;
        LogFactory.setClassLevel( CCApp.class, Level.INFO );
        LogFactory.setPackageLevel( No2Store.class, Level.DEBUG );
        LogFactory.setPackageLevel( State.class, Level.DEBUG );
        //LogFactory.setClassLevel( UIEventCollector.class, Level.DEBUG );

        Promise.setDefaultErrorHandler( defaultErrorHandler() );

//        File tmp = (File)ctx.getAttribute( "javax.servlet.context.tempdir" );
//        File dbBaseDir = new File( tmp, "../ragtime.cc " );
        workspaceDir = new File( System.getProperty( "user.home" ), "servers/workspace-ragtime.cc" );
        workspaceDir.mkdirs();
        LOG.info( "Workspace: %s", workspaceDir.getAbsolutePath() );

        Repositories.init();
    }


    public static void dispose() {
        LOG.warn( "DISPOSE " );
        Repositories.dispose();
    }


    /**
     * The workspace directory of the given {@link AccountEntity#permid}.
     *
     * @param permid The permanent ID of the {@link AccountEntity}.
     */
    public static File workspaceDir( int permid ) {
        return new File( workspaceDir, Integer.toString( permid ) );
    }

    public static File workspaceDir() {
        return workspaceDir;
    }

    // instance *******************************************

    @Override
    public void createUI() {
        try {
            LOG.info( "createUI(): ..." );
            createUI( rootWindow -> {
                rootWindow.layout.set( MaxWidthLayout.width( 750 ).fillHeight.set( true ) );
                var pageflowContainer = rootWindow.add( new UIComposite() {{
                    cssClasses.add( "MaxWidth" );
                }});
                rootWindow.layout();

                var pageflow = Pageflow.start( pageflowContainer );

                State.start( new LoginState() )
                        .putContext( pageflow, State.Context.DEFAULT_SCOPE )
                        .putContext( new UICommon(), State.Context.DEFAULT_SCOPE )
                        //.putContext( new CCAppStatePageMapping( pageflow ), State.Context.DEFAULT_SCOPE )
                        .activate();

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
                LOG.warn( "defaultErrorHandler(): ", e );
                e.printStackTrace( System.err );
                //throw rootCauseForTeaVM( e );
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
