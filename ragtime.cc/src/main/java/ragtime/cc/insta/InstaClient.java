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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import areca.common.Assert;
import areca.common.Promise;
import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.AsyncWorker;
import ragtime.cc.insta.model.TopicInstaConfigEntity;
import ragtime.cc.model.MediaEntity;

/**
 * The interface for the actual interaction with IG API.
 *
 * @author Falko Bräutigam
 */
public class InstaClient {

    private static final Log LOG = LogFactory.getLog( InstaClient.class );

    private static Map<String,Promise<InstaClient>> clients = new HashMap<>();

    protected static InstaClient newClient() {
        return new InstaClient();
    }

    /**
     * Returns a pooled instance that is already logged in with user of the given
     * config.
     */
    public static Promise<InstaClient> pooled( TopicInstaConfigEntity config ) {
        var key = config.username.get() + config.password.get();
        return clients.computeIfAbsent( key, __ -> {
            var client = newClient();
            return client.login( config.username.get(), config.password.get() )
                    .map( loggedIn -> {
                        Assert.that( loggedIn, "..." );
                        return client;
                    });
        });
    }

    /**
     * Return a new instance for testing connection/login.
     */
    public static InstaClient newInstance() {
        return newClient();
    }

    /**
     *
     */
    public static class Response {

    }

    // instance *******************************************

    public Promise<Boolean> login( String username, String password ) {
        var t = Timer.start();
        return AsyncWorker.start( () -> {
            LOG.info( "Thread start: %s", t );
            LOG.warn( "Login: %s/%s", username, password );
            Thread.sleep( 1000 );
            return true;
        });
    }


    public Promise<Response> createPost( String caption, List<MediaEntity> images ) {
        return AsyncWorker.start( () -> {
            LOG.warn( "Caption: %s", caption );
            LOG.warn( "Images: %s", images.size() );
            Thread.sleep( 1000 );
            return new Response();
        });
    }

}
