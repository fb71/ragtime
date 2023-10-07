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
package ragtime.app;

import static areca.ui.Orientation.VERTICAL;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.Text;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowLayout;
import areca.ui.layout.SwitcherLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.PageContainer;
import ragtime.app.RagtimeApp.PendingUnitOfWork;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class SelfAwarenessPage {

    private static final Log LOG = LogFactory.getLog( SelfAwarenessPage.class );

    public static final ClassInfo<SelfAwarenessPage> INFO = SelfAwarenessPageClassInfo.instance();

    @Page.Context
    protected Page.PageSite     psite;

    @Page.Context
    protected PendingUnitOfWork puow;

    @Page.Part
    protected PageContainer     ui;

    @Page.Part
    protected EmotionsView      emotionsView;

    private SwitcherLayout      switcher;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Bewusst-sein" );

        ui.body.layout.set( RowLayout.filled().orientation( VERTICAL )/*.margins( Size.of( 15, 15 ) )*/ );

        // switcher
        ui.body.add( new UIComposite() {{
            layout.set( switcher = SwitcherLayout.defaults() );
            //bgColor.set( Color.rgb( 34, 35, 36 ) ); // rotate over dark background

            add( emotionsView.create() );
            add( new Text() {{
                content.set( "Bedürfnisse" );
            }});
        }});
        return ui;
    }


    public void flip() {
        switcher.next();
    }
}
