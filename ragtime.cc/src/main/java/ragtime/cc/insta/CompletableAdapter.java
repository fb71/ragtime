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
package ragtime.cc.insta;

import java.util.concurrent.CompletableFuture;

import areca.common.Platform;
import areca.common.Platform.PollingCommand;
import areca.common.Promise;
import areca.common.Session;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.rt.server.EventLoop;

/**
 *
 * @author Falko Bräutigam
 */
public class CompletableAdapter<T>
        extends Promise.Completable<T> {

    private static final Log LOG = LogFactory.getLog( CompletableAdapter.class );

    public CompletableAdapter( CompletableFuture<T> delegate ) {
        var eventloop = Session.instanceOf( EventLoop.class );
        eventloop.requestPolling();
        delegate.thenAccept( result -> {
                    eventloop.releasePolling( "Insta", () -> {
                        Platform.polling( PollingCommand.STOP );
                        complete( result );
                    }, 0 );
                })
                .exceptionally( e -> {
//                    if (e.getCause() instanceof IGResponseException re) {
//                        LOG.warn( "Error: type=%s,\n    message=%s,\n    feedback=%s",
//                                re.getResponse().getError_type(),
//                                re.getResponse().getMessage(),
//                                re.getResponse().getFeedback_message()
//                                );
//                    }
                    eventloop.releasePolling( "Insta", () -> {
                        Platform.polling( PollingCommand.STOP );
                        completeWithError( e );
                    }, 0 );
                    return null;
                });
    }

}
