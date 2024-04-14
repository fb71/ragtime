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
package ragtime.cc.website;

import static areca.ui.pageflow.PageflowEvent.EventType.PAGE_CLOSED;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateSite;
import ragtime.cc.UICommon;
import ragtime.cc.website.model.NavItem;
import ragtime.cc.website.model.TemplateConfigEntity;

/**
 * A UI {@link State} that handles editing of a {@link TemplateConfigEntity}.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TemplateConfigState {

    private static final Log LOG = LogFactory.getLog( TemplateConfigState.class );

    public static final ClassInfo<TemplateConfigState> INFO = TemplateConfigStateClassInfo.instance();

    @State.Context
    protected StateSite     site;

    @State.Context
    protected Pageflow      pageflow;

    protected TemplateConfigPage page;

    @State.Context
    protected UnitOfWork    uow;

    @State.Model
    public TemplateConfigEntity config;

    @State.Context
    @Deprecated // XXX
    protected UICommon      uic;


    @State.Init
    public void initAction() {
        uow.query( TemplateConfigEntity.class ).singleResult().onSuccess( c -> {
            config = c;

            pageflow.create( page = new TemplateConfigPage() )
                    .putContext( this, Page.Context.DEFAULT_SCOPE )
                    .putContext( uic, Page.Context.DEFAULT_SCOPE )
                    .open();

            page.site.subscribe( PAGE_CLOSED, ev -> disposeAction() );
//            EventManager.instance()
//                    .subscribe( ev -> disposeAction() )
//                    .performIf( PageflowEvent.class, ev -> ev.type == PAGE_CLOSED && ev.page.get() == page)
//                    .unsubscribeIf( () -> site.isDisposed() );
        });
    };


    @State.Dispose
    public void disposeAction() {
        if (!page.site.isClosed()) {
            page.site.close();
        }
        uow.discard();
        site.dispose();
        //return true;
    }


    @State.Action
    public Promise<Submitted> submitAction() {
        return uow.submit(); //.onSuccess( __ -> disposeAction() );
    }


    @State.Action
    public void addFooterNavItem() {
        config.footerNavItems.createElement( NavItem.defaults() );
    };

}
