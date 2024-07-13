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

import java.util.List;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;

import areca.common.Assert;
import areca.common.Promise;
import areca.common.Timer;
import areca.common.base.Sequence;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.AsyncWorker;
import ragtime.cc.model.MediaEntity;

/**
 * {@link InstaClient} using private API via {@link IGClient}.
 *
 * @author Falko Bräutigam
 */
public class PrivateInstaClient
        extends InstaClient {

    private static final Log LOG = LogFactory.getLog( PrivateInstaClient.class );

    protected volatile IGClient ig;

    @Override
    public Promise<Boolean> login( String username, String password ) {
        var t = Timer.start();
        return AsyncWorker.start( () -> {
            LOG.info( "Thread start: %s", t );
            Assert.isNull( ig );
            try {
                ig = IGClient.builder().username( username ).password( password ).login();
                LOG.info( "Login: %s", t );
                return true;
            }
            catch (IGLoginException e) {
                return false;
            }
        });
    }


    protected void checkOpen() {
        Assert.notNull( ig, "" );
    }


    @Override
    public Promise<Response> createPost( String caption, List<MediaEntity> images ) {
        checkOpen();
        return images.get( 0 )
                .readFully()
                .then( bytes -> {
                    LOG.info( "uploading: %s bytes", bytes.length );
                    var f = ig.actions().timeline().uploadPhoto( bytes, caption );
                    return new CompletableAdapter<>( f ).map( response -> new Response() );
                });
    }


    public Promise<Response> createPost2( String caption, List<MediaEntity> images ) {
        checkOpen();
        return AsyncWorker
                .start( () -> Sequence.of( images ).map( image -> toPhoto( image )).toList() )
                .then( photos -> {
                    var f = ig.actions().timeline().uploadAlbum( photos, caption );
                    return new CompletableAdapter<>( f ).map( response -> new Response() );
                });
    }


    protected TimelineAction.SidecarInfo toPhoto( MediaEntity image ) {
        try (var in = image.in()) {
            var bytes = IOUtils.toByteArray( in );
            return new TimelineAction.SidecarPhoto( bytes );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }
}
