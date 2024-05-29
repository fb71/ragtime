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

import org.polymap.model2.runtime.UnitOfWork;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateAction;
import areca.ui.statenaction.StateSite;
import areca.ui.viewer.model.Model;
import ragtime.cc.article.EntityModel;
import ragtime.cc.web.model.WebsiteConfigEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class WebsiteConfigState {

    private static final Log LOG = LogFactory.getLog( WebsiteConfigState.class );

    public static final ClassInfo<WebsiteConfigState> INFO = WebsiteConfigStateClassInfo.instance();

    @State.Context
    protected StateSite     site;

    @State.Context
    protected Pageflow      pageflow;

    protected WebsiteConfigPage page;

    @State.Context
    protected UnitOfWork    uow;

    @State.Context
    @State.Model
    public Model<WebsiteConfigEntity> config = new EntityModel<>();

    public boolean          edited;

    public boolean          valid;


    @State.Init
    public void initAction() {
        pageflow.create( page = new WebsiteConfigPage() )
                .putContext( this, Page.Context.DEFAULT_SCOPE )
                .open();
    };


    @State.Dispose
    public boolean disposeAction() {
        if (!page.site.isClosed()) {
            pageflow.close( page );
        }
        uow.discard();
        site.dispose();
        return true;
    }


    @State.Action
    public StateAction<Void> submitAction = new StateAction<>() {
        @Override
        public boolean canRun() {
            return edited && valid;
        }
        @Override
        public void run( Void arg ) {
            uow.submit().onSuccess( __ -> disposeAction() );
        }
    };

}
