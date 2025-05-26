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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.polymap.model2.Entity;
import org.polymap.model2.query.Query.Order;

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


    /**
     *
     */
    class ContentModel
            extends ModelBaseImpl
            implements LazyTreeModel<Object> {

        private Map<Object,? extends Object> itemCache = new HashMap<>();

//        protected ContentModel() {
//            EventManager.instance()
//                    .subscribe( ev -> onPropertyChange( (PropertyChangeEvent)ev ) )
//                    .performIf( PropertyChangeEvent.class, ev -> true )
//                    .unsubscribeIf( () -> isDisposed() );
//
//        }
//
//        protected void onPropertyChange( PropertyChangeEvent ev ) {
//            LOG.warn( "%s", ev );
//        }

        @Override
        public Promise<List<? extends Object>> loadChildren( Object item, int first, int max ) {
            // root
            if (item == null) {
                return uow.query( TopicEntity.class )
                        .orderBy( TopicEntity.TYPE.order, Order.ASC )
                        .executeCollect().map( ArrayList::new ); // XXX
            }
            // Topic
            else if (item instanceof TopicEntity topic) {
                var result = new LinkedList<Object>();
                return topic.articles().executeCollect()
                        .then( rs -> {
                            result.addAll( 0, rs );
                            return topic.medias.fetchCollect();
                        })
                        .map( rs -> {
                            result.addAll( 0, rs.stream().map( m -> new MediaContent( m, topic ) ).toList() );
                            result.add( 0, new TopicContentEdit( topic ) );
                            return result;
                        });
            }
            else if (item instanceof TopicContent tc) {
                return Promise.async( asList( new TopicContentEdit( tc.topic() ) ) );
            }
            // Article
            else if (item instanceof Article article) {
                return article.medias.fetchCollect().map( rs -> {
                    return new ArrayList<>() {{
                        add( new ArticleContentEdit( article ) );
                        addAll( rs.stream().map( m -> new MediaContent( m, article ) ).toList() );
                    }};
                });
            }
            // Media
            else if (item instanceof MediaContent mc) {
                return Promise.async( Collections.emptyList() );
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
        public boolean equals( Object obj ) {
            return obj instanceof ContentModelItem other ? value.equals( other.value ) : false;
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


}
