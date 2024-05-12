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
package ragtime.cc.model;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_BYTE_ARRAY;

import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.ArrayUtils;

import org.polymap.model2.ManyAssociation;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus;

import areca.common.Promise;
import areca.common.Promise.Completable;
import areca.common.Session;
import areca.common.Timer;
import areca.common.base.Consumer.RConsumer;
import areca.common.base.Lazy.RLazy;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.rt.server.EventLoop;
import ragtime.cc.Workspace;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class MediaEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( MediaEntity.class );

    public static final ClassInfo<MediaEntity> info = MediaEntityClassInfo.instance();

    public static final String DIR = "media";

    public static MediaEntity TYPE;

    public static RConsumer<MediaEntity> defaults() {
        return proto -> {
            proto.permid.set( proto.cpermid.get() );
        };
    }

    // instance *******************************************

    @Queryable
    public Property<String>             name;

    public Property<String>             mimetype;

    @Queryable
    public ManyAssociation<TagEntity>   tags;

    public Property<Integer>            permid;

    protected RLazy<Integer>            cpermid = new RLazy<>( () ->
            context.getUnitOfWork().query( AccountEntity.class ).singleResult().waitForResult().get().permid.get() );

    /**
     * Computed back association of {@link Article#medias}
     */
    public Promise<List<Article>> article() {
        return context.getUnitOfWork().query( Article.class )
                .where( Expressions.anyOf( Article.TYPE.medias, Expressions.id( id() ) ) )
                .executeCollect();
    }

    @Override
    public void onLifecycleChange( State state ) {
        super.onLifecycleChange( state );
        // delete file
        if (state == State.AFTER_SUBMIT && status() == EntityStatus.REMOVED) {
            var f = f();
            if (f.exists()) {
                f.delete();
            }
            LOG.info( "Removed: %s", f );
        }
//        // remove back association
//        if (state == State.AFTER_REMOVED) {
//            article().onSuccess( articles -> articles.forEach( article -> {
//                LOG.info( "Removing back link: %s", article.title.get() );
//                article.medias.remove( MediaEntity.this );
//            }));
//        }
    }

    /**
     * Unbuffered {@link OutputStream} of the content.
     */
    public OutputStream out() {
        try {
            return /*new BufferedOutputStream(*/ new FileOutputStream( f() );
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Unbuffered {@link InputStream} of the content. Has to be closed by the caller.
     */
    public InputStream in() {
        try {
            return /*new BufferedInputStream(*/ new FileInputStream( f() );
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Async reading the content in chunks.
     */
    public Promise<byte[]> read() {
        return new FileReader( f() ).read();
    }

    /**
     * Async reading the complete content.
     */
    public Promise<byte[]> readFully() {
        return new FileReader( f() ).read()
                .reduce( new ByteArrayOutputStream( 128*1024 ), (buf,chunk) -> {
                    buf.write( chunk != null ? chunk : EMPTY_BYTE_ARRAY );
                })
                .map( buf -> buf.toByteArray() );
    }

    protected File f() {
        var workspace = Workspace.of( permid.get() );
        //var workspace = Workspace.of( cpermid.get() );
        var media = new File( workspace, DIR );
        media.mkdirs();
        return new File( media, name.get() );
    }

    /**
     *
     */
    public static class FileReader
            extends Thread {

        private File f;

        private Completable<byte[]> promise = new Completable<>();

        private EventLoop eventloop;

        public FileReader( File f ) {
            super( "FileReader" );
            setDaemon( true );
            this.f = f;
            this.eventloop = Session.instanceOf( EventLoop.class );
        }

        public Promise<byte[]> read() {
            start();
            return promise;
        }

        @Override
        public void run() {
            try (var in = new FileInputStream( f )) {
                var t = Timer.start();
                var size = 0;
                var buf = new byte[4096];
                for (int c = in.read( buf ); c > -1; c = in.read( buf )) {
                    size += c;
                    var result = ArrayUtils.subarray( buf, 0, c );
                    eventloop.enqueue( "FileReader", () -> promise.consumeResult( result ), 0 );
                }
                eventloop.enqueue( "FileReader", () -> promise.complete( null ), 0 );
                LOG.info( "FileReader: %s - %s (%s)", f.getName(), size, t );
            }
            catch (Throwable e) {
                promise.completeWithError( e );
            }
        }
    }
}
