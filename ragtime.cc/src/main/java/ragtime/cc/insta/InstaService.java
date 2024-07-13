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

import static org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus.CREATED;
import static org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus.MODIFIED;
import static org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus.REMOVED;

import org.apache.commons.lang3.mutable.MutableObject;

import org.polymap.model2.runtime.EntityRuntimeContext.EntityStatus;
import org.polymap.model2.runtime.Lifecycle;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Assert;
import areca.common.base.Consumer;
import areca.common.base.Sequence;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.ConfirmDialog;
import ragtime.cc.insta.model.InstaPostEntity;
import ragtime.cc.insta.model.TopicInstaConfigEntity;
import ragtime.cc.model.Article;
import ragtime.cc.model.EntityLifecycleEvent;
import ragtime.cc.web.template.widgets.Abstract;

/**
 * Listen to {@link Article} changes
 *
 * @author Falko Bräutigam
 */
public class InstaService {

    private static final Log LOG = LogFactory.getLog( InstaService.class );

    public static InstaService start() {
        return new InstaService();
    }

    // instance *******************************************

    protected InstaService() {
        // XXX check Topic changes: add/remove insta config
        // XXX

        EventManager.instance()
                .subscribe( (EntityLifecycleEvent ev) -> onArticleSubmit( (Article)ev.getSource(), ev.entityStatus ) )
                .performIf( EntityLifecycleEvent.class, ev ->
                        ev.state == Lifecycle.State.BEFORE_SUBMIT &&
                        ev.getSource() instanceof Article );
    }


    protected void onArticleSubmit( Article article, EntityStatus entityStatus ) {
        LOG.warn( "%s (%s)", article, entityStatus );
        article.topic.fetch()
                .then( topic -> TopicInstaConfigEntity.of( topic ) )
                .onSuccess( opt -> opt.ifPresent( config -> {
                    var uow = article.context.getRepository().newUnitOfWork();
                    InstaPostEntity.of( article, uow ).onSuccess( post -> {
                        if (entityStatus == CREATED && post.isAbsent()) {
                            confirm( "Soll ein neuer Post für diesen Beitrag erstellt werden?", __ ->
                                    createPost( uow, article, config ) );
                        }
                        if (entityStatus == MODIFIED && post.isPresent()) {
                            updatePost( uow, article, config, post.get() );
                        }
                        else if (entityStatus == MODIFIED && post.isAbsent()) {
                            confirm( "Soll dieser Beitrag zu Instagram gepostet werden?", __ ->
                                    createPost( uow, article, config ) );
                        }
                        else if (entityStatus == REMOVED && post.isPresent()) {
                            removePost( uow, article, config, post.get() );
                        }
                        else {
                            throw new IllegalStateException( "Unexpected: entityStatus=" + entityStatus + ", post=" + post );
                        }
                    });
                }));
    }


    private <E extends Exception> void confirm( String msg, Consumer<Void,E> task ) {
        ConfirmDialog.createAndOpen( "Instagram", msg )
                .addCancelAction( () -> {} )
                .addOkAction( () -> {
                    try {
                        task.accept( null );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                });
    }


    private void removePost( UnitOfWork uow, Article article, TopicInstaConfigEntity config, InstaPostEntity post ) {
        LOG.info( "removePost(): %s (%s)", article, article.status() );
        throw new UnsupportedOperationException( "updatePost()" );
    }


    private void updatePost( UnitOfWork uow, Article article, TopicInstaConfigEntity config, InstaPostEntity post ) {
        LOG.info( "updatePost(): %s (%s)", article, article.status() );
        throw new UnsupportedOperationException( "updatePost()" );
    }


    private void createPost( UnitOfWork uow, Article article, TopicInstaConfigEntity config ) throws Exception {
        LOG.warn( "createPost(): %s", article );
        var client = new MutableObject<InstaClient>();
        InstaClient.pooled( config )
                .then( _client -> {
                    client.setValue( Assert.notNull( _client ) );
                    return article.medias.fetchCollect();
                })
                .onSuccess( medias -> {
                    var content = new StringBuilder( article.content.get() );
                    new Abstract( "mehr auf der Website: ..." ).process( content, null );
                    // XXX new Markdown().process( content, ctx );
                    // XXX Swiper

                    var filtered = Sequence.of( medias ).filter( media -> media.mimetype.get().startsWith( "image" ) ).toList();

                    client.getValue().createPost( content.toString(), filtered ).onSuccess( response -> {
                        LOG.warn( "createPost(): post successful: %s", response );

                        uow.createEntity( InstaPostEntity.class, proto -> {
                            proto.article.set( article );
                        });
                        // XXX submit
                    });
                });

    }

}
