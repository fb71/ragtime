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

import org.polymap.model2.query.Expressions;
import org.polymap.model2.query.Query;
import org.polymap.model2.query.Query.Order;
import areca.common.Assert;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateAction;
import areca.ui.viewer.model.LazyListModel;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.Pojo;
import ragtime.cc.BaseState;
import ragtime.cc.UICommon;
import ragtime.cc.model.TopicEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TopicsState
        extends BaseState<TopicsPage> {

    private static final Log LOG = LogFactory.getLog( TopicsState.class );

    public static final ClassInfo<TopicsState> INFO = TopicsStateClassInfo.instance();

    /**
     * Model: searchTxt
     */
    @State.Model
    public Model<String>    searchTxt = new Pojo<>( "" );

    @State.Model
    public Model<TopicEntity> selected = new Pojo<>();

    /**
     * Model: topics
     */
    @State.Model
    public LazyListModel<TopicEntity> topics = new EntityListModel<>( TopicEntity.class ) {
        {
            // re-fire events from searchTxt
            searchTxt.subscribe( ev -> fireChangeEvent() ).unsubscribeIf( () -> site.isDisposed() );
            // fire event on Entity change
            fireChangeEventOnEntitySubmit( () -> site.isDisposed() );
        }
        @Override
        protected Query<TopicEntity> query() {
            var searchTxtMatch = Expressions.TRUE;
            if (searchTxt.get().length() > 0) {
                searchTxtMatch = Expressions.or(
                        Expressions.matches( TopicEntity.TYPE.title, searchTxt.get() + "*" ),
                        Expressions.matches( TopicEntity.TYPE.description, searchTxt.get() + "*" ) );
            }
            return uow.query( TopicEntity.class )
                    .where( searchTxtMatch )
                    .orderBy( TopicEntity.TYPE.order, Order.ASC );
        }
    };


    @State.Init
    public void initAction() {
        super.initAction();
        pageflow.create( page = new TopicsPage() )
                .putContext( TopicsState.this, Page.Context.DEFAULT_SCOPE )
                .putContext( site.get( UICommon.class ), Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Action
    public void createTopicAction() {
        site.createState( new TopicCreateState() ).activate();
    }


    @State.Action
    public StateAction<Void> editTopicAction = new StateAction<>() {
        @Override
        public boolean canRun() {
            return selected.$() != null;
        }
        @Override
        public void run( Void arg ) {
            Assert.that( canRun(), "StateAction: !canRun() " );
            site.createState( new TopicEditState() )
                    .putContext( Assert.notNull( selected.$() ), State.Context.DEFAULT_SCOPE )
                    .onChange( ev -> LOG.info( "STATE CHANGE: %s", ev ) )
                    .activate();
        }
    };

}
