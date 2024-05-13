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

import static java.text.DateFormat.MEDIUM;

import java.util.Locale;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Link;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.Text;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.CompositeListViewer;
import areca.ui.viewer.ViewerContext;
import ragtime.cc.LoginState;
import ragtime.cc.model.Article;
import ragtime.cc.website.WebsiteEditState;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticlesPage {

    private static final Log LOG = LogFactory.getLog( ArticlesPage.class );

    public static final ClassInfo<ArticlesPage> INFO = ArticlesPageClassInfo.instance();

    protected static final DateFormat df = SimpleDateFormat.getDateTimeInstance( MEDIUM, MEDIUM, Locale.GERMAN );

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected ArticlesState     state;

//    @Page.Context
//    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    private ScrollableComposite list;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Beiträge" );

        // action: new
        site.actions.add( new Action() {{
            order.set( 9 );
            icon.set( "add" );
            description.set( "Neuen Artikel/Text anlegen" );
            handler.set( ev -> state.createArticleAction() );
        }});
        // action: settings
        site.actions.add( new Action() {{
            order.set( 10 );
            icon.set( "settings" );
            description.set( "Einstellungen" );
            handler.set( ev -> state.openSettingsAction() );
        }});
        // action: logout
        site.actions.add( new Action() {{
            order.set( 0 );
            icon.set( "logout" );
            description.set( state.account.login.get() + "\nAnmeldedaten löschen\nBeim nächsten Start neu anmelden" );
            handler.set( ev -> {
                LoginState.logout( state.account ).onSuccess( __ -> {
                    ui.body.components.disposeAll();
                    ui.body.add( new Text() {{
                        content.set( "Logout complete. Reload browser!" );
                    }});
                    ui.body.layout();
                });
            });
        }});
        if (state.account.isAdmin.get()) {
            // action: settings
            site.actions.add( new Action() {{
                order.set( 20 );
                icon.set( "face4" );
                description.set( "Accounts" );
                handler.set( ev -> state.openAccountsAction() );
            }});
        }
        // action: medias
        site.actions.add( new Action() {{
            icon.set( "image" );
            description.set( "Medien" );
            handler.set( ev -> state.openMediasAction() );
        }});
        // action: topics
        site.actions.add( new Action() {{
            icon.set( "topic" );
            description.set( "Topics" );
            handler.set( ev -> state.site.createState( new TopicsState() ).activate() );
        }});

        ui.body.layout.set( RowLayout.filled().vertical().margins( Size.of( 22, 22 ) ).spacing( 15 ) );

        // website links
        ui.body.add( new UIComposite() {{
            lc( RowConstraints.height( 30 ) );
            layout.set( RowLayout.filled().spacing( 20 ) );
            add( new Button() {{
                lc( RowConstraints.width( 180 ) );
                label.set( "Web-Seite bearbeiten..." );
                tooltip.set( "Web-Seite durch Click auf einzelne Element bearbeiten" );
                events.on( EventType.SELECT, ev -> {
                    // XXX
                    state.site.createState( new WebsiteEditState() ).activate();
                });
            }});
            add( new UIComposite() );
            add( new Link() {{
                lc( RowConstraints.width( 150 ) );
                content.set( "Web-Seite ansehen..." );
                tooltip.set( "Die Web-Seite in einem neuen Browser-Fenster öffnen" );
                href.set( String.format( "website/%s/home", state.account.permid.get() ) );
            }});
        }});

//        // search
//        ui.body.add( new TextField() {{
//            layoutConstraints.set( RowConstraints.height( 35 ) );
//            content.set( state.searchTxt.get() );
//            events.on( EventType.TEXT, ev -> {
//                state.searchTxt.set( content.get() );
//            });
//        }});

        // list
        ui.body.add( new ScrollableComposite() {{
            layout.set( FillLayout.defaults() );

            add( new ViewerContext<>()
                    .viewer( new CompositeListViewer<Article>( (article,model) -> new ListItem( article ) ) {{
                        lines.set( true );
                        oddEven.set( true );
                        onSelect.set( article -> {
                            state.selected.set( article );
                            state.editArticleAction.run();
                        });
                    }})
                    .model( state.articles )
                    .createAndLoad() );
        }});
        return ui;
    }


    /**
     *
     */
    protected class ListItem extends UIComposite {

        protected ListItem( Article article ) {
            lc( RowConstraints.height( 54 ));
            layout.set( RowLayout.filled().margins( 10, 10 ) );
            add( new Text() {{
                format.set( Format.HTML );
                content.set( article.title.get() + "<br/>" +
                        "<span style=\"font-size:10px; color:#808080;\">" + df.format( article.modified.get() ) + "</span>" );
            }});
//            Platform.schedule( 2000, () -> {
//                var comments = RandomUtils.nextInt( 1, 8 );
//                if (comments < 3) {
//                    addDecorator( new Badge() {{
//                        content.set( "" + comments );
//                        tooltip.set( "Ungelesene Kommentare: " + comments );
//                    }});
//                }
//            });
        }
    }


//    /**
//     *
//     */
//    protected class ArticleListItem extends Button {
//
//        public ArticleListItem( Article article ) {
//            layoutConstraints.set( RowConstraints.height( 50 ) );
//            layout.set( RowLayout.filled().vertical().margins( 10, 7 ).spacing( 8 ) );
//            bordered.set( false );
//            add( new Text() {{
//                //format.set( Format.HTML );
//                content.set( article.title.get() );
//            }});
//            add( new Text() {{
//                content.set( "Geändert: " + df.format( article.modified.get() ) );
//                styles.add( CssStyle.of( "font-size", "10px") );
//                styles.add( CssStyle.of( "color", "#707070") );
//                enabled.set( false );
//            }});
//            events.on( EventType.SELECT, ev -> {
//                state.selected.set( article );
//                state.editArticleAction.run();
//            });
//        }
//    }
}
