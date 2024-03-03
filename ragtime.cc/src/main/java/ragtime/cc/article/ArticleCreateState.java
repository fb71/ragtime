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
import areca.ui.statenaction.State;
import ragtime.cc.model.Article;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticleCreateState
        extends ArticleEditState {

    private static final Log LOG = LogFactory.getLog( ArticleCreateState.class );

    @SuppressWarnings( "hiding" )
    public static final ClassInfo<ArticleCreateState> INFO = ArticleCreateStateClassInfo.instance();

    @State.Init
    @Override
    public void initAction() {
        article.set( uow.createEntity( Article.class, proto -> {
            proto.title.set( "Neuer Text" );
            proto.content.set( "..." );
        }));
        super.initAction();
    };

}
