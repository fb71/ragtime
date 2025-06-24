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
package ragtime.cc.web;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.statenaction.State;
import ragtime.cc.BaseState;
import ragtime.cc.UICommon;
import ragtime.cc.web.model.TemplateConfigEntity;

/**
 * A UI {@link State} that handles editing of a {@link TemplateConfigEntity}.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TemplateConfigState
        extends BaseState<TemplateConfigPage> {

    private static final Log LOG = LogFactory.getLog( TemplateConfigState.class );

    public static final ClassInfo<TemplateConfigState> INFO = TemplateConfigStateClassInfo.instance();

    @State.Model
    public Promise<TemplateConfigEntity> config;

    @State.Context
    @Deprecated // XXX
    protected UICommon      uic;


    @State.Init
    public void initAction() {
        super.initAction();
        config = uow.query( TemplateConfigEntity.class ).singleResult();

        createStatePage( new TemplateConfigPage() )
                .putContext( this, Page.Context.DEFAULT_SCOPE )
                .putContext( uic, Page.Context.DEFAULT_SCOPE )
                .open();
    };

    @State.Dispose
    @Override
    public void disposeAction() {
        uow.discard();
        super.disposeAction();
    }

}
