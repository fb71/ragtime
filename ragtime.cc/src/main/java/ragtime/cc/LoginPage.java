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

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
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


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Login" );

        ui.body.layout.set( MaxWidthLayout.width( 320 ) );
        ui.body.add( new UIComposite() {{
            layout.set( RowLayout.verticals().fillWidth( true ).margins( Size.of( 0, 50 ) ).spacing( 30 ) );

            var form = new Form();
            add( form.newField().label( "Login" )
                    .model( state.login )
                    .viewer( new TextFieldViewer() )
                    .create());

            add( form.newField().label( "Password" )
                    .model( state.pwd )
                    .viewer( new TextFieldViewer() )
                    .create());
            form.load();

            add( new Button() {{
                layoutConstraints.set( RowConstraints.height( 40 ) );
                label.set( "Login" );
                events.on( EventType.SELECT, ev -> {
                    form.submit();
                    state.loginAction().onError( e -> {
                        LOG.info( "Login failed: %s", e.getMessage() );
                    });
                });
            }});
        }});
        return ui;
    }

}
