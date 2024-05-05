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

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import ragtime.cc.BaseState;
import ragtime.cc.UICommon;
import ragtime.cc.model.TopicEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TopicEditState
        extends BaseState<TopicPage> {

    private static final Log LOG = LogFactory.getLog( TopicEditState.class );

    public static final ClassInfo<TopicEditState> INFO = TopicEditStateClassInfo.instance();

    @State.Model
    @State.Context(required = false)
    public TopicEntity topic;


    @State.Init
    public void initAction() {
        super.initAction();
        pageflow.create( page = new TopicPage() )
                .putContext( this, Page.Context.DEFAULT_SCOPE )
                .putContext( site.get( UICommon.class ), Page.Context.DEFAULT_SCOPE )
                .open();
    };

}
