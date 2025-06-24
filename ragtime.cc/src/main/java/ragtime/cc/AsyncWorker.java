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

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import areca.common.Promise;
import areca.common.Promise.Completable;
import areca.common.Session;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.rt.server.EventLoop;

/**
 *
 * @author Falko Bräutigam
 */
public class AsyncWorker<R>
        extends Thread {

    private static final Log LOG = LogFactory.getLog( AsyncWorker.class );

    public static final int MAX_THREADS = Runtime.getRuntime().availableProcessors() * 2;

    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor( MAX_THREADS, MAX_THREADS,
            30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new AsyncWorkerThreadFactory() )
            {{
                //allowCoreThreadTimeOut( true );
            }};

    private static class AsyncWorkerThreadFactory
            implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger( 1 );
        private AtomicInteger poolThreadNumber = new AtomicInteger( 1 );
        private String namePrefix = "AsyncWorker-pool" + poolNumber.getAndIncrement() + "-";
        private ThreadGroup group = Thread.currentThread().getThreadGroup();

        public Thread newThread( Runnable r ) {
            var t = new Thread( group, r, namePrefix + poolThreadNumber.getAndIncrement() );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );
            return t;
        }
    }

    private static class CallerBlocks
            implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution( Runnable task, ThreadPoolExecutor executor ) {
            // this will block if the queue is full
            try { executor.getQueue().put( task ); } catch (InterruptedException e) {}
            if (executor.isShutdown()) {
               throw new RejectedExecutionException( "Task " + task + " rejected from " + executor );
            }
         }
    }


    public static void dispose() {
        pool.shutdown();
    }


    /**
     * Starts an async worker task. The worker {@link Thread} is picked up from a
     * global thread pool.
     */
    public static <RR> Promise<RR> pool( Callable<RR> work ) {
        LOG.debug( "Pool: threads=%s, tasks=%s", pool.getPoolSize(), pool.getQueue().size() );
        var promise = new Completable<RR>();
        var eventloop = Session.instanceOf( EventLoop.class );
        eventloop.requestPolling();
        pool.execute( () -> {
            try {
                var result = work.call();
                eventloop.releasePolling( "AsyncWorker", () -> {
                    promise.complete( result );
                }, 0 );
            }
            catch (Throwable e) {
                eventloop.releasePolling( "AsyncWorker", () -> {
                    promise.completeWithError( e );
                }, 0 );
            }
        });
        return promise;
    }


    /**
     * Starts an async worker task. A worker {@link Thread} is newly created
     * for every call of this method.
     */
    public static <RR> Promise<RR> start( Callable<RR> work ) {
        return new AsyncWorker<>( work ).promise;
    }


    private static final AtomicInteger threadNumber = new AtomicInteger( 1 );

    // instance *******************************************

    private Completable<R> promise = new Completable<>();

    private EventLoop eventloop;

    private Callable<R> work;


    public AsyncWorker( Callable<R> work ) {
        super( "AsyncWorker-" + threadNumber.getAndIncrement() );
        setDaemon( true );
        this.work = work;

        this.eventloop = Session.instanceOf( EventLoop.class );
        this.eventloop.requestPolling();

        start();
    }


    @Override
    public void run() {
        try {
            var result = work.call();
            eventloop.releasePolling( "AsyncWorker", () -> {
                promise.complete( result );
            }, 0 );
        }
        catch (Throwable e) {
            promise.completeWithError( e );
        }
    }

}
