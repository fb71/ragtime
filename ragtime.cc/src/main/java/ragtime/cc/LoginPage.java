/*
 * Copyright (C) 2023, the @authors. All rights reserved.
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

import areca.common.AssertionException;
import areca.common.Platform;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Link;
import areca.ui.component2.Text;
import areca.ui.component2.TextField;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.MaxWidthLayout;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.form.Form;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class LoginPage {

    private static final Log LOG = LogFactory.getLog( LoginPage.class );

    public static final ClassInfo<LoginPage> INFO = LoginPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected LoginState        state;

    protected Text              responseTxt;

    protected TextField         loginField;

    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "fb71.org" );

        ui.body.layout.set( MaxWidthLayout.width( 320 ) );
        ui.body.add( new UIComposite() {{
            layout.set( RowLayout.verticals().fillWidth( true ).margins( Size.of( 0, 50 ) ).spacing( 30 ) );

            var form = new Form();
            add( form.newField().label( "EMail" )
                    .model( state.login )
                    .viewer( new TextFieldViewer().configure( (TextField f) -> {
                        //f.type.set( TextField.Type.USERNAME );
                        loginField = f;
                    }))
                    .create() );

            add( form.newField().label( "Passwort" )
                    .model( state.pwd )
                    .viewer( new TextFieldViewer() ) // .configure( (TextField f) -> f.type.set( TextField.Type.PASSWORD ) ) )
                    .create() );
            form.load();

            // Passwort vergessen
            add( new Link() {{
                layoutConstraints.set( RowConstraints.height( 20 ) );
                content.set( "Passwort vergessen... " );
                styles.add( CssStyle.of( "text-align", "right" ) );
                tooltip.set( "Sendet ein neues Passwort\nan die oben eingetragene Adresse" );
                events.on( EventType.SELECT, ev -> {
                    var email = loginField.content.$();
                    responseTxt.content.set( "EMail wird versendet..." );
                    Platform.schedule( 500, () -> {
                        state.sendNewPasswordAction( email )
                            .onSuccess( __ -> {
                                responseTxt.content.set( "EMail wurde versendet an:<br/><b>" + email + "</b>" );
                            })
                            .onError( e -> {
                                responseTxt.content.set( "<b>EMail konnte nicht versandt werden.</b><br/>" + e.getMessage() );
                                e.printStackTrace();
                            });
                    });
                });
            }});

            // Login
            add( new Button() {{
                layoutConstraints.set( RowConstraints.height( 40 ) );
                type.set( Button.Type.SUBMIT );
                label.set( "Login" );
                enabled.set( false );
                form.subscribe( ev -> enabled.set( form.isChanged() && form.isValid() ) );
                events.on( EventType.SELECT, ev -> {
                    form.submit();
                    state.loginAction().onError( e -> {
                        LOG.info( "Login failed: %s", e.getMessage() );
                        if (e instanceof AssertionException) {
                            e.printStackTrace();
                        }
                        responseTxt.content.set( "EMail oder Passwort sind nicht korrekt" );
                    });
                });
            }});

            // Register
            add( new Button() {{
                layoutConstraints.set( RowConstraints.height( 40 ) );
                label.set( "Registrieren" );
                tooltip.set( "Registriert einen neuen Nutzer für\ndie oben eingegebene EMail-Adresse.\nDas Passwort wird an diese Adresse verschickt." );
                //enabled.set( false );
                //form.subscribe( ev -> enabled.set( form.isChanged() && form.isValid() ) );
                events.on( EventType.SELECT, ev -> {
                    if (!form.isChanged() || !form.isValid()) {
                        ConfirmDialog.create( "EMail", "Bitte gib eine gültige Adresse im Feld <b>EMail</b> ein.<br/>Das Passwort wird an diese Adresse geschickt." )
                                .size.set( Size.of(  330, 190 ) )
                                .addOkAction( () -> LOG.info( "ok" ) )
                                .open();
                    }
                    else {
                        responseTxt.content.set( "Neuer Nutzer wird erstellt..." );
                        Platform.schedule( 500, () -> {
                            var email = loginField.content.$();
                            state.registerAction( email )
                                    .onSuccess( __ -> {
                                        responseTxt.content.set( "EMail wurde versendet an:<br/><b>" + email + "</b>" );
                                    })
                                    .onError( e -> {
                                        responseTxt.content.set( "<b>Nutzer konnte nicht angelegt werden.</b><br/>" + e.getMessage() );
                                        e.printStackTrace();
                                    });
                        });
                    }
                });
            }});
            //
            responseTxt = add( new Text() {{
                format.set( Format.HTML );
            }});
        }});
        return ui;
    }

}
