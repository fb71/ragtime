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
package ragtime.cc.media;

import static java.text.DateFormat.MEDIUM;

import java.util.Locale;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.FileUpload;
import areca.ui.component2.FileUpload.File;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class MediasPage {

    private static final Log LOG = LogFactory.getLog( MediasPage.class );

    public static final ClassInfo<MediasPage> INFO = MediasPageClassInfo.instance();

    protected static final DateFormat df = SimpleDateFormat.getDateTimeInstance( MEDIUM, MEDIUM, Locale.GERMAN );

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected MediasState     state;

//    @Page.Context
//    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    private ScrollableComposite list;

    private File                uploaded;

    private Button              btn;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Medien" );

        ui.body.layout.set( RowLayout.filled().vertical().margins( Size.of( 22, 22 ) ).spacing( 15 ) );

        ui.body.add( new FileUpload() {{
            events.on( EventType.UPLOAD, ev -> {
                LOG.warn( "Uploaded: %s", data.get().name() );
                uploaded = data.get();
                btn.enabled.set( true );
            });
        }});

        btn = ui.body.add( new Button() {{
            label.set( "Submit" );
            type.set( Type.SUBMIT );
            enabled.set( false );
            events.on( EventType.SELECT, ev -> {
               state.createMediaAction( uploaded ).onSuccess( __ -> {
                   enabled.set( false );
               });
            });
        }});

//        // list
//        ui.body.add( new ScrollableComposite() {{
//            list = this;
//            layout.set( RowLayout.filled().vertical().spacing( 10 ) );
//            add( new Text() {{
//               content.set( "..." );
//            }});
//            state.accounts.subscribe( ev -> refreshArticlesList() )
//                    .unsubscribeIf( () -> site.isClosed() );
//            refreshArticlesList();
//        }});
        return ui;
    }


//    protected void refreshArticlesList() {
//        list.components.disposeAll();  // XXX race cond.
//        state.accounts.load( 0, 100 ).onSuccess( opt -> {
//            opt.ifPresent( article -> {
//                list.add( new AccountListItem( article ) );
//            } );
//            opt.ifAbsent( __ -> {
//                list.layout();
//            });
//        });
//    }
//
//
//    /**
//     *
//     */
//    protected class AccountListItem extends Button {
//
//        public AccountListItem( AccountEntity account ) {
//            layoutConstraints.set( RowConstraints.height( 50 ) );
//            layout.set( RowLayout.filled().vertical().margins( 10, 10 ).spacing( 8 ) );
//            bordered.set( false );
//            add( new Text() {{
//                //format.set( Format.HTML );
//                content.set( account.email.get() +
//                        (account.isAdmin.get() ? " (" + account.login.get() + ")" : "") );
//            }});
//            add( new Text() {{
//                content.set( "Login: " + df.format( account.lastLogin.get() ) );
//                styles.add( CssStyle.of( "font-size", "10px") );
//                styles.add( CssStyle.of( "color", "#707070") );
//                enabled.set( false );
//            }});
//            events.on( EventType.SELECT, ev -> {
//                state.selected.set( account );
//                state.becomeAccountAction( account );
//            });
//        }
//
//    }
}
