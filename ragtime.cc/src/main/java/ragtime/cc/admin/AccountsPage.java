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
package ragtime.cc.admin;

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
import ragtime.cc.ConfirmDialog;
import ragtime.cc.LoginState;
import ragtime.cc.model.AccountEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class AccountsPage {

    private static final Log LOG = LogFactory.getLog( AccountsPage.class );

    public static final ClassInfo<AccountsPage> INFO = AccountsPageClassInfo.instance();

    protected static final DateFormat df = SimpleDateFormat.getDateTimeInstance( MEDIUM, MEDIUM, Locale.GERMAN );

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected AccountsState     state;

//    @Page.Context
//    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    private ScrollableComposite list;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Accounts" );

        ui.body.layout.set( RowLayout.filled().vertical().margins( 15, 15 ).spacing( 15 ) );

        // list
        ui.body.add( new ScrollableComposite() {{
            layout.set( FillLayout.defaults() );
            add( new ViewerContext<>()
                    .viewer( new CompositeListViewer<AccountEntity>( AccountListItem::new ) {{
                        etag.set( account -> account.modified.get() );
                        lines.set( true );
                        oddEven.set( true );
                        onSelect.set( account -> {
                            state.selected.set( account );
                            state.becomeAccountAction( account );
                        });
                    }})
                    .model( state.accounts )
                    .createAndLoad() );
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
        return ui;
    }


    /**
     *
     */
    protected class AccountListItem extends UIComposite {

        public AccountListItem( AccountEntity account ) {
            lc( RowConstraints.height( 54 ));
            layout.set( RowLayout.filled().margins( 10, 10 ) );
            // title
            add( new Text() {{
                format.set( Format.HTML );
                content.set( account.email.get() + (account.isAdmin.get() ? " (" + account.login.get() + ")" : "") + "<br/>" +
                        "<span style=\"font-size:10px; color:#808080;\">Last login: " + df.format( account.lastLogin.get() ) + "</span>" );
            }});
            add( new Button() {{
                lc( RowConstraints.width( 40 ));
                icon.set( "close" );
                tooltip.set( "Löschen" );
                events.on( EventType.SELECT, ev -> {
                    ConfirmDialog.createAndOpen( "Account", "<b><center>" + account.email.get() + "</center></b>" )
                            .size.set( Size.of( 320, 200 ) )
                            .addDeleteAction( () -> {
                                state.deleteAccountAction( account );
                            });
                });
            }});
        }
    }
}
