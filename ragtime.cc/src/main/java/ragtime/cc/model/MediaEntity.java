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
import static ragtime.cc.ConcurrentReferenceHashMap.ReferenceType.SOFT;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import org.polymap.model2.ManyAssociation;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Assert;
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
import areca.ui.Size;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import ragtime.cc.AsyncWorker;
import ragtime.cc.ConcurrentReferenceHashMap;
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

    /** */
    public static RConsumer<MediaEntity> defaults() {
        return proto -> {
            proto.permid.set( proto.cpermid.get() );
        };
    }

    /** */
    public static Promise<MediaEntity> getOrCreate( UnitOfWork uow, String name ) {
        return uow.query( MediaEntity.class )
                .where( Expressions.eq( MediaEntity.TYPE.name, name ) ).executeCollect()
                .map( rs -> {
                    Assert.that( rs.size() <= 1 );
                    var entity = !rs.isEmpty()
                            ? rs.get( 0 )
                            : uow.createEntity( MediaEntity.class, proto -> {
                                MediaEntity.defaults().accept( proto );
                                proto.name.set( name );
                            });
                    return entity;
                });
    }

    /** Cache */
    private static Map<String,Promise<byte[]>> thumbnails = new ConcurrentReferenceHashMap<>(
            128, 0.75f, AsyncWorker.MAX_THREADS, SOFT, SOFT, null );


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
    public Promise<List<Article>> articles() {
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


    public ThumbnailBuilder thumbnail() {
        return new ThumbnailBuilder();
    }

    /**
     *
     */
    public class ThumbnailBuilder {
        private Size    size;
        private String  outputFormat;

        public ThumbnailBuilder size( int w, int h ) {
            return size( Size.of( w, h ) );
        }

        public ThumbnailBuilder size( Size s ) {
            this.size = s;
            return this;
        }

        /** {@link ImageIO} image output format: jpg, png */
        public ThumbnailBuilder outputFormat( String format ) {
            this.outputFormat = format;
            return this;
        }

        private String cacheKey() {
            return String.format( "%s|%s|%s", id(), size, outputFormat );
        }

        public Promise<byte[]> create() {
            Assert.notNull( size, "No 'size' specified for thumbnail" );
            Assert.notNull( outputFormat, "No 'imageType' specified for thumbnail" );

            return thumbnails.computeIfAbsent( cacheKey(), cacheKey -> {
                return AsyncWorker.pool( () -> {
                    var t = Timer.start();
                    try (var in = IOUtils.buffer( in() )) {
                        var out = new ByteArrayOutputStream( 64 * 1024 );
                        Thumbnails.fromInputStreams( Arrays.asList( in ) )
                                .size( size.width(), size.height() )
                                .crop( Positions.CENTER )
                                //.imageType( BufferedImage.TYPE_USHORT_565_RGB )
                                //.addFilter( new Canvas( size.width(), size.height(), Positions.CENTER, Color.WHITE ) )
                                .outputFormat( outputFormat )
                                .toOutputStream( out );

                        LOG.debug( "%s: %s (%s)", name.get(), out.size(), t );
                        return out.toByteArray();
                    }
                    catch (IOException e) {
                        throw new RuntimeException( e );
                    }
                });
            });
        }
    }


    /**
     * Unbuffered {@link OutputStream} of the content.
     */
    public OutputStream out() {
        try {
            var f = f();
            if (f.exists()) {
                LOG.warn( "Overwrite: " + name.get() + ". -> FLUSHING CACHE!" );
                thumbnails.clear();
            }
            return /*new BufferedOutputStream(*/ new FileOutputStream( f );
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
                LOG.debug( "FileReader: %s - %s (%s)", f.getName(), size, t );
            }
            catch (Throwable e) {
                promise.completeWithError( e );
            }
        }
    }
}
