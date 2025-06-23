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
package ragtime.cc.article;

import static java.lang.String.format;

import java.util.ArrayList;

import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Platform;
import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.TextField;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.Article;
import ragtime.cc.model.MediaEntity;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.http.MediaContentProvider;
import ragtime.cc.web.http.WebsiteServlet;

/**
 * Topic/Article/Media/Widget snippets for article/topic texts.
 *
 * @author Falko Bräutigam
 */
public class TextAutocomplete {

    private static final Log LOG = LogFactory.getLog( TextAutocomplete.class );

    /**
     *
     */
    public static void process( TextField tf, UnitOfWork uow ) {
        var t = Timer.start();
        var result = new ArrayList<String>();
        Platform.schedule( 1000, () -> null )
                // widgets
                .then( __ -> {
                    result.add( "::swiper::" );
                    result.add( "::swiper?w=<Breite>,h=<Höhe>::" );
                    result.add( "::abstract::" );

                    return uow.query( AccountEntity.class ).optResult();
                })
                .then( opt -> {
                    opt.ifPresent( account -> {
                        result.add( "----" );
                        result.add( String.format( "[EMail](mailto:%s)", account.email.get() ) );
                    });
                    result.add( "----" );

                    // XXX orderBy() does not work for newly created Topic
                    return uow.query( TopicEntity.class ).executeCollect();
                })
                // topics
                .then( rs -> {
                    result.add( format( "[%s](%s)", "Start", WebsiteServlet.PATH_HOME ) );
                    rs.stream()
                            .filter( topic -> topic.permName.opt().isPresent() )
                            .sorted( (t1,t2) -> t1.title.get().compareTo( t2.title.get() ) )
                            .forEach( topic -> result.add( format( "[%s](%s)", topic.title.get(), topic.permName.get() ) ) );
                    result.add( "----" );

                    // XXX orderBy() does not work for newly created Article
                    return uow.query( Article.class ).executeCollect();
                })
                // articles
                .then( rs -> {
                    rs.stream()
                            .filter( article -> article.permName.opt().isPresent() )
                            .sorted( (t1,t2) -> t1.title.get().compareTo( t2.title.get() ) )
                            .forEach( article -> result.add( format( "[%s](%s)", article.title.get(), article.permName.get() ) ) );
                    result.add( "----" );

                    return uow.query( MediaEntity.class )
                            .orderBy( MediaEntity.TYPE.name, Order.DESC )
                            .executeCollect();
                })
                // medias
                .onSuccess( rs -> {
                    for (var media : rs) {
                        result.add( String.format( "![%s](%s)", media.name.get(), MediaContentProvider.PATH + "/" + media.id() ) );
                    }

                    LOG.debug( "autocomplete: %s [%s]", result.size(), t );
                    if (!tf.isDisposed()) {
                        tf.autocomplete.set( result );
                    }
                    else {
                        LOG.warn( "autocomplete: TextField already disposed" );
                    }
                });
    }

}
