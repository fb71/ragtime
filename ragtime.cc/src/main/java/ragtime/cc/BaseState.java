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
package ragtime.cc;

import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.base.Opt;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.NoRuntimeInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.pageflow.Pageflow.PageBuilder;
import areca.ui.pageflow.PageflowEvent;
import areca.ui.pageflow.PageflowEvent.EventType;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateBuilder;
import areca.ui.statenaction.StateSite;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.MainRepo;

/**
 * The base for all {@link State}s in ragtime.cc
 *
 * @param <P> The type of the page this state controls.
 * @author Falko Bräutigam
 */
@RuntimeInfo
public abstract class BaseState<P> {

    private static final Log LOG = LogFactory.getLog( BaseState.class );

    //public static final ClassInfo<BaseState> INFO = ArticlesStateClassInfo.instance();

    @State.Context
    public StateSite        site;

    @State.Context
    protected Pageflow      pageflow;

    @State.Context
    public UnitOfWork       uow;

    @State.Context( scope=MainRepo.SCOPE )
    public AccountEntity    account;

    @NoRuntimeInfo
    protected P             page;


    public Opt<P> page() {
        return Opt.of( page );
    }


    /**
     * Registers page close listener. Subclasses should call this.
     * */
    @State.Init
    public void initAction() {
        // trigger disposeAction() when page is closed
        EventManager.instance()
                .subscribe( ev -> disposeAction() )
                .performIf( PageflowEvent.class, ev -> ev.type == EventType.PAGE_CLOSED && ev.clientPage == page )
                .unsubscribeIf( () -> isDisposed() );
    };


    /**
     * Creates the {@link Page} of this {@link State}.
     */
    protected PageBuilder createStatePage( @SuppressWarnings( "hiding" ) P page ) {
        this.page = page;
        return pageflow.create( page );
    }


    @State.Dispose
    public void disposeAction() {
        LOG.warn( "disposeAction(): page.isOpen=%s", pageflow.isOpen( page ) );
        if (page != null && pageflow.isOpen( page )) {
            pageflow.close( page );
        }
        uow.discard();
        site.dispose();
        //return true;
    }


    public boolean isDisposed() {
        // site is null if event is handled in unsubscribeIf() in member init
        // which may happen in initializer before site is injected
        return site != null && site.isDisposed();
    }


    @State.Action
    public Promise<Submitted> submitAction() {
        return uow.submit(); //.onSuccess( __ -> disposeAction() );
    }


    /**
     * See {@link StateSite#createState(Object)}
     */
    protected StateBuilder createState( Object newState ) {
        return site.createState( newState );
    }
}
