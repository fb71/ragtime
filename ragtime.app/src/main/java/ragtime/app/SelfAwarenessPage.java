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
import static areca.ui.component2.Events.EventType.SELECT;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RasterLayout;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.PageContainer;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class SelfAwarenessPage {

    private static final Log LOG = LogFactory.getLog( SelfAwarenessPage.class );

    public static final ClassInfo<SelfAwarenessPage> INFO = SelfAwarenessPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected Page.PageSite     site;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Selbstbewusstsein" );

        ui.body.layout.set( RowLayout.filled().orientation( VERTICAL ).margins( Size.of( 15, 15 ) ) );
        ui.body.add( new UIComposite() {{
            layout.set( RasterLayout.withComponentSize( 170, 50 ).spacing( 15 ) );
            add( new Button() {{
                label.set( "Ziele" );
                events.on( SELECT, ev -> {
                    site.createPage( new ImageLabPage() ).open();
                });
            }});
            add( new Button() {{
                label.set( "Bedürfnisse" );
            }});
            add( new Button() {{
                label.set( "Werte" );
            }});

            add( new Button() {{
                label.set( "Gefühle" );
            }});
            add( new Button() {{
                label.set( "Stärken" );
            }});
            add( new Button() {{
                label.set( "Schwächen" );
            }});
        }});
        return ui;
    }

}
