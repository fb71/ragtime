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

import java.util.concurrent.Callable;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.browser.Window;
import org.teavm.jso.json.JSON;

import areca.common.Platform;
import areca.common.ProgressMonitor;
import areca.common.Promise;
import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Level;
import areca.common.log.LogFactory.Log;
import areca.rt.teavm.PopStateEvent;
import areca.rt.teavm.TeaPlatform;
import areca.rt.teavm.ui.UIComponentRenderer;
import areca.ui.App;
import areca.ui.Size;
import areca.ui.pageflow.Pageflow;

/**
 *
 * @author Falko Bräutigam
 */
public class RagtimeApp
        extends App {

    private static final Log LOG = LogFactory.getLog( RagtimeApp.class );

    public static boolean   debug;

    /**
     *
     */
    public static void main( String[] args ) throws Exception {
        debug = Window.current().getLocation().getSearch().contains( "debug" );
        LOG.info( "Debug: %s", debug );
        LogFactory.DEFAULT_LEVEL = debug ? Level.INFO : Level.WARN;

        Promise.setDefaultErrorHandler( defaultErrorHandler() );
        Platform.impl = new TeaPlatform();
        UIComponentRenderer.start();

        try {
            // Browser history
            Window.current().addEventListener( "popstate", ev -> onBrowserHistoryEvent( ev.cast() ) );

            // UI
            new RagtimeApp().createUI( rootWindow -> {
                rootWindow.size.defaultsTo( () -> {
                    var body = Window.current().getDocument().getBody();
                    var size = Size.of( body.getClientWidth(), body.getClientHeight() - 100 );
                    LOG.debug( "BODY: " + size );
                    return size;
                });

                LOG.info( "URI path: %s", Window.current().getLocation().getPathName() );
                Pageflow.start( rootWindow )
                        .create( new FrontPage() )
                        .open();
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
     *
     */
    public static abstract class BrowserHistoryState
            implements JSObject {

        @JSBody(params = "newState", script = "return {state: newState};")
        public static native BrowserHistoryState create( String newState );

        @JSProperty
        public abstract String getState();
    }

    public static void onPageOpen( String browserHistoryState ) {
        var state = BrowserHistoryState.create( browserHistoryState );
        Window.current().getHistory().pushState( state, "", browserHistoryState );
    }

    public static void closeTopPage() {
        Window.current().getHistory().back();
    }

    /**
     *
     */
    private static void onBrowserHistoryEvent( PopStateEvent ev ) {
        LOG.info( "popstate: %s", JSON.stringify( ev.getState() ) );
        ev.preventDefault();

        catchAll( () -> {
            var pageflow = Pageflow.current();
            pageflow.close( pageflow.pages().first().orElseError() );
            return null;
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
            LOG.warn( "Exception: " + e );
            Throwable rootCause = Platform.rootCause( e );
            LOG.warn( "Root cause: " + rootCause, rootCause );
            if (e instanceof RuntimeException) {
                throw (RuntimeException)rootCause;
            }
            else {
                throw (RuntimeException)rootCause; // XXX
            }
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
                // get a meaningfull stracktrace in TeaVM
                throw (RuntimeException)e;
            }
            else {
                //Pageflow.current().open( new GeneralErrorPage( e ), null );
            }
        };
    }

}
