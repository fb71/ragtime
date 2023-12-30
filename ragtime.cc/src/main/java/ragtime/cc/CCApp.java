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

    /**
     *
     */
    public static void init() throws Exception {
        ServerApp.init();

        LOG.info( "Debug: %s", debug );
        LogFactory.DEFAULT_LEVEL = debug ? Level.INFO : Level.WARN;
        LogFactory.setClassLevel( CCApp.class, Level.INFO );

        Promise.setDefaultErrorHandler( defaultErrorHandler() );
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