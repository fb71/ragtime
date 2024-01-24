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
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateSite;
import ragtime.cc.article.ArticlesState;

/**
 * The start {@link State} of the application.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class StartState {

    private static final Log LOG = LogFactory.getLog( StartState.class );

    public static final ClassInfo<StartState> INFO = StartStateClassInfo.instance();

    protected UnitOfWork    uow;

    @State.Context
    protected Pageflow      pageflow;

    @State.Context
    protected StateSite     site;


    @State.Init
    public void init() {
        var repo = Repositories.mainRepo();
        uow = repo.newUnitOfWork(); // .setPriority( priority );

        pageflow.create( new FrontPage() )
                .putContext( this, Page.Context.DEFAULT_SCOPE )
                .open();
    }


    @State.Action
    public void listArticles() {
        site.createState( new ArticlesState() )
                .putContext( uow, State.Context.DEFAULT_SCOPE )
                .activate();
    }

}
