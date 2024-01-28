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
import areca.ui.Action;
import areca.ui.Size;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.Text;
import areca.ui.component2.TextField;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import ragtime.cc.model.Article;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticlesPage {

    private static final Log LOG = LogFactory.getLog( ArticlesPage.class );

    public static final ClassInfo<ArticlesPage> INFO = ArticlesPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected ArticlesState     state;

    @Page.Context
    protected PageSite          site;

    private ScrollableComposite list;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Artikel" );

        ui.body.layout.set( RowLayout.filled().vertical().margins( Size.of( 10, 10 ) ).spacing( 10 ) );

        // actions
        site.actions.add( new Action() {{
            icon.set( "public" );
            description.set( "Web-Seite ansehen" );
            handler.set( ev -> {
                //state.listArticles();
            });
        }});

        // search
        ui.body.add( new TextField() {{
            layoutConstraints.set( RowConstraints.height( 35 ) );
            content.set( state.searchTxt.get() );
            events.on( EventType.TEXT, ev -> {
                state.searchTxt.set( content.get() );
            });
        }});

        // list
        ui.body.add( new ScrollableComposite() {{
            list = this;
            layout.set( RowLayout.filled().vertical().margins( Size.of( 10, 10 ) ).spacing( 5 ) );
            add( new Text() {{
               content.set( "..." );
            }});
            state.articles.subscribe( ev -> refreshArticlesList() )
                    .unsubscribeIf( () -> site.isClosed() );
            refreshArticlesList();
        }});
        return ui;
    }


    protected void refreshArticlesList() {
        list.components.disposeAll();
        state.articles.load( 0, 100 ).onSuccess( opt -> {
            opt.ifPresent( article -> {
                list.add( new ArticleListItem( article ) );
            } );
            opt.ifAbsent( __ -> {
                list.layout();
            });
        });
    }


    /**
     *
     */
    protected class ArticleListItem extends UIComposite {

        public ArticleListItem( Article article ) {
            cssClasses.add( "Button" );
            layoutConstraints.set( RowConstraints.height( 50 ) );
            layout.set( RowLayout.filled().vertical().margins( Size.of( 10, 5 ) ) );
            add( new Text() {{
                //format.set( Format.HTML );
                content.set( article.title.get() );
            }});
            add( new Text() {{
                content.set( article.content.get() );
            }});
            events.on( EventType.SELECT, ev -> {
                state.selected.set( article );
                state.editArticleAction.run();
            });
        }

    }
}