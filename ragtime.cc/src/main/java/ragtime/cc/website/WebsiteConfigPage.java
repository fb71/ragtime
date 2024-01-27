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
package ragtime.cc.website;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.Size;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class WebsiteConfigPage {

    private static final Log LOG = LogFactory.getLog( WebsiteConfigPage.class );

    public static final ClassInfo<WebsiteConfigPage> INFO = WebsiteConfigPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected WebsiteConfigState state;

    protected Action            submitBtn;


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        ui.init( parent ).title.set( "Web-Seite" );

        // header
        site.actions.add( submitBtn = new Action() {{
            //icon.set( "done" );
            description.set( "Speichern" );
            handler.set( ev -> {
                state.submitAction.run();
            });
        }});

        ui.body.layout.set( FillLayout.defaults() );
        ui.body.add( new ScrollableComposite() {{
            layout.set( RowLayout.filled().vertical().margins( Size.of( 10, 10 ) ) );

//            add( new TextField() {{
//                multiline.set( true );
//                content.set( state.article.$().content.get() );
//                events.on( EventType.TEXT, ev -> {
//                    LOG.info( "TEXT: %s", content.get() );
//                    state.article.$().content.set( content.get() );
//
//                    state.edited = true;
//                    state.valid = true;
//                    updateEnabled();
//                });
//            }});
        }});
        return ui;
    }


    protected void updateEnabled() {
        LOG.info( "updateEnabled(): %s", state.submitAction.canRun() );
        submitBtn.icon.set( state.submitAction.canRun() ? "done" : "cancel" );
    }


    @Page.Close
    public boolean onClose() {
        LOG.info( "onClose()" );
        return state.disposeAction();
    }
}
