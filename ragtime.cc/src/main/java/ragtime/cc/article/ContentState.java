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

import static org.polymap.model2.query.Expressions.isAnyOf;
import static org.polymap.model2.query.Expressions.not;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.polymap.model2.Entity;
import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.viewer.TreeViewer;
import areca.ui.viewer.model.LazyTreeModel;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.ModelBaseImpl;
import areca.ui.viewer.model.Pojo;
import ragtime.cc.BaseState;
import ragtime.cc.UICommon;
import ragtime.cc.model.Article;
import ragtime.cc.model.MediaEntity;
import ragtime.cc.model.TopicEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ContentState
        extends BaseState<ContentPage> {

    private static final Log LOG = LogFactory.getLog( ContentState.class );

    public static final ClassInfo<ContentState> INFO = ContentStateClassInfo.instance();

    /**
     * Model: searchTxt
     */
    @State.Model
    public Model<String>        searchTxt = new Pojo<>( "" );

    @State.Model
    public Model<TopicEntity>   selected = new Pojo<>();

    @State.Model
    public ContentModel         contentModel = new ContentModel();


    @State.Init
    public void initAction() {
        super.initAction();
        pageflow.create( page = new ContentPage() )
                .putContext( ContentState.this, Page.Context.DEFAULT_SCOPE )
                .putContext( site.get( UICommon.class ), Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Dispose
    @Override
    public void disposeAction() {
        uow.discard();
        super.disposeAction();
    }


    @State.Action
    public void createNewTopic() {
        uow.createEntity( TopicEntity.class, TopicEntity.defaults().andThen( proto -> {
            proto.title.set( "Thema-" + RandomStringUtils.random( 3, false, true ) );
            proto.description.set( "..." );
        }));
        contentModel.fireChangeEvent();
    }


    @State.Action
    public Promise<Submitted> discardAction() {
        return uow.discard().onSuccess( __ -> contentModel.fireChangeEvent() );
    }


    public Object contentType( Object entity ) {
        if (entity instanceof TopicEntity topic) {
            return new TopicContent( topic );
        }
        else if (entity instanceof Article article) {
            return new ArticleContent( article );
        }
        throw new RuntimeException( "Unhandled: " + entity );
    }


    /**
     *
     */
    class ContentModel
            extends ModelBaseImpl
            implements LazyTreeModel<Object> {

        private Map<Object,? extends Object> itemCache = new HashMap<>();

        @Override
        public Promise<List<? extends Object>> loadChildren( Object item, int first, int max ) {
            // root
            if (item == null) {
                var result = new LinkedList<>();
                return uow.query( TopicEntity.class )
                        .orderBy( TopicEntity.TYPE.order, Order.ASC ).executeCollect()
                        .then( rs -> {
                            result.addAll( rs.stream().map( TopicContent::new ).toList() );
                            return uow.query( Article.class )
                                    // XXX no null check for association yet
                                    .where( not( isAnyOf( Article.TYPE.topic, rs.toArray( TopicEntity[]::new ) ) ) )
                                    .orderBy( Article.TYPE.title, Order.ASC )
                                    .executeCollect();
                        })
                        .map( rs -> {
                            result.addAll( rs.stream().map( ArticleContent::new ).toList() );
                            return result;
                        });
            }
            // Topic
            else if (item instanceof TopicContent tc) {
                var topic = tc.value;
                var result = new LinkedList<Object>();
                return topic.articles().orderBy( Article.TYPE.order, Order.ASC ).executeCollect()
                        .then( rs -> {
                            result.addAll( 0, rs.stream().map( ArticleContent::new ).toList() );
                            return topic.medias.fetchCollect();
                        })
                        .map( rs -> {
                            result.addAll( 0, rs.stream().map( m -> new MediaContent( m, topic ) ).toList() );
                            result.add( 0, new TopicContentEdit( topic ) );
                            return result;
                        });
            }
            // Article
            else if (item instanceof ArticleContent ac) {
                return ac.article().medias.fetchCollect().map( rs -> {
                    var result = new ArrayList<>();
                    result.add( new ArticleContentEdit( ac.article() ) );
                    result.addAll( rs.stream().map( m -> new MediaContent( m, ac.article() ) ).toList() );
                    return result;
                });
            }
            // Media
            else if (item instanceof MediaContentEdit mc) {
                return Promise.async( Collections.emptyList() );
            }
            else if (item instanceof MediaContent mc) {
                return Promise.async( Arrays.asList( new MediaContentEdit( mc.value, mc.parent ) ) );
            }
            else {
                throw new RuntimeException( "Unhandled value type: " + item );
            }
        }

        protected <R> List<R> add( List<R> l, R elm) {
            l.add( 0, elm );
            return l;
        }
    }


    /**
     * @param <V> The type of the #value.
     */
    public abstract class ContentModelItem<V> {

        public V value;

        protected ContentModelItem( V value ) {
            this.value = value;
        }

        public ContentModel contentModel() {
            return contentModel;
        }

        /** Support {@link TreeViewer} to find model changes. */
        public boolean equals( Object other ) {
            return getClass() == other.getClass() && value.equals( ((ContentModelItem)other).value );
        }

        /** Support {@link TreeViewer} to find model changes. */
        public int hashCode() {
            return value.hashCode();
        }
    }

    /**  */
    public class TopicContent
            extends ContentModelItem<TopicEntity> {

        protected TopicContent( TopicEntity topic ) {
            super( topic );
        }

        public TopicEntity topic() {
            return value;
        }

        public void delete() {
            value.context.getUnitOfWork().removeEntity( value );
            contentModel().fireChangeEvent();
        }

        public Article createNewArticle() {
            var article = uow.createEntity( Article.class, proto -> {
                proto.topic.set( topic() );
                proto.title.set( "Beitrag-" + RandomStringUtils.random( 3, false, true ) );
                proto.content.set( "..." );
            });
            contentModel().fireChangeEvent();
            return article;
        }
    }

    /** */
    public class TopicContentEdit
            extends TopicContent {

        protected TopicContentEdit( TopicEntity topic ) {
            super( topic );
        }

        public void addMedias( List<MediaEntity> sel ) {
            sel.forEach( media -> topic().medias.add( media ) );
            contentModel.fireChangeEvent();
        }

    }

    /** */
    public class ArticleContent
            extends ContentModelItem<Article> {

        protected ArticleContent( Article value ) {
            super( value );
        }

        public Article article() {
            return value;
        }

        public void delete() {
            value.context.getUnitOfWork().removeEntity( value );
            contentModel().fireChangeEvent();
        }

        public void moveTopic() {
            contentModel().fireChangeEvent();
        }
    }

    /** */
    public class ArticleContentEdit
            extends ArticleContent {

        protected ArticleContentEdit( Article value ) {
            super( value );
        }

        public void addMedias( List<MediaEntity> sel ) {
            sel.forEach( media -> article().medias.add( media ) );
            contentModel().fireChangeEvent();
        }
    }

    /** */
    public class MediaContent
            extends ContentModelItem<MediaEntity> {

        private Entity parent;

        protected MediaContent( MediaEntity value, Entity parent ) {
            super( value );
            this.parent = parent;
        }

        public MediaEntity media() {
            return value;
        }

        public void remove( boolean delete ) {
            if (parent instanceof TopicEntity topic) {
                topic.medias.remove( media() );
            }
            else if (parent instanceof Article article) {
                article.medias.remove( media() );
            }
            else {
                throw new RuntimeException( "Unhandled parent type: " + parent );
            }
            if (delete) {
                value.context.getUnitOfWork().removeEntity( media() );
            }
            contentModel().fireChangeEvent();
        }
    }

    /** */
    public class MediaContentEdit
            extends MediaContent {

        protected MediaContentEdit( MediaEntity value, Entity parent ) {
            super( value, parent );
        }
    }

}
