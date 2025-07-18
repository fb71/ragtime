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

import javax.servlet.ServletContext;

import org.polymap.model2.store.no2.No2Store;

import areca.common.Platform;
import areca.common.ProgressMonitor;
import areca.common.Promise;
import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Level;
import areca.common.log.LogFactory.Log;
import areca.rt.server.EventLoop;
import areca.rt.server.ServerApp;
import areca.rt.server.ServerBrowserHistoryStrategy;
import areca.rt.server.servlet.ArecaUIServer;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import ragtime.cc.calendar.CalendarEventArticlePageEx;
import ragtime.cc.insta.InstaTopicPageEx;
import ragtime.cc.model.ContentRepo;
import ragtime.cc.model.MainRepo;
import ragtime.cc.web.WebTopicPageEx;
import ragtime.cc.web.template.TemplateInfo;

/**
 *
 * @author Falko Bräutigam
 */
public class CCApp
        extends ServerApp {

    private static final Log LOG = LogFactory.getLog( CCApp.class );

    private static final int DB_VERSION = 7;

    public static boolean debug = true;

    public static CCAppConfig config;


    /**
     * Called by the {@link ArecaUIServer} once, when the servlet is loaded.
     */
    public static void init( ServletContext ctx ) throws Exception {
        LOG.info( "Debug: %s", debug );
        LogFactory.DEFAULT_LEVEL = debug ? Level.INFO : Level.WARN;
        LogFactory.setClassLevel( CCApp.class, Level.INFO );
        LogFactory.setPackageLevel( No2Store.class, Level.DEBUG );
        //LogFactory.setPackageLevel( State.class, Level.DEBUG );
        //LogFactory.setClassLevel( UIEventCollector.class, Level.DEBUG );

        //System.setProperty( "org.slf4j.simpleLogger.logFile", "System.out" );

        // Mostly assertions
        Promise.setDefaultErrorHandler( defaultErrorHandler() );
        EventLoop.setDefaultErrorHandler( defaultErrorHandler() ); // process click events

        config = CCAppConfig.instance;

        // check/fail on startup
        LOG.info( "Templates: ..." );
        TemplateInfo.all();

        // extensions
        Extensions.register( CalendarEventArticlePageEx.class );
        Extensions.register( WebTopicPageEx.class );
        Extensions.register( InstaTopicPageEx.class );
    }


    public static void dispose() {
        LOG.warn( "DISPOSE " );
        ContentRepo.dispose();
        MainRepo.dispose();
        AsyncWorker.dispose();
    }


    // instance *******************************************

    @Override
    public void createUI() {
        try {
            LOG.info( "createUI(): ..." );

            createUI( rootWindow -> {
                Page.DEFAULT_PAGE_WIDTH = UICommon.PAGE_WIDTH;
                Page.DEFAULT_PAGE_WIDTH_MIN = UICommon.PAGE_WIDTH_MIN;
                var pageflow = Pageflow.start( rootWindow );

                State.start( new LoginState() )
                        .putContext( pageflow, State.Context.DEFAULT_SCOPE )
                        .putContext( new UICommon(), State.Context.DEFAULT_SCOPE )
                        //.putContext( new CCAppStatePageMapping( pageflow ), State.Context.DEFAULT_SCOPE )
                        .activate();

                //InstaService.start();

                ServerBrowserHistoryStrategy.start( pageflow );

//                // track current account for ErrorPage
//                EventManager.instance()
//                        .subscribe( (StateChangeEvent ev) -> {
//                            LOG.debug( "Current account: %s", ev.stateSite.opt( AccountEntity.class, MainRepo.SCOPE )
//                                    .map( a -> a.email.get() ).orElse( "[???]" ) );
//                        })
//                        .performIf( StateChangeEvent.class, ev -> true );
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
     * The {@link Promise#defaultErrorHandler}
     */
    private static RConsumer<Throwable> defaultErrorHandler() {
        return (Throwable e) -> {
            if (e instanceof ProgressMonitor.CancelledException || e instanceof Promise.CancelledException) {
                LOG.info( "Operation cancelled." );
                return;
            }
            if (debug) {
                LOG.warn( "defaultErrorHandler(): %s", e.toString() );
                e.printStackTrace( System.out );
                //throw rootCauseForTeaVM( e );
            }
            // no ErrorPage if image scaling produced an OOM
            if (!Error.class.isInstance( Platform.rootCause( e ) )) {
                ErrorPage.tryOpen( e );
            }
        };
    }

    /**
     * Get a meaningfull stracktrace in TeaVM
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
