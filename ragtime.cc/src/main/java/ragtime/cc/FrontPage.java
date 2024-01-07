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

import areca.common.MutableInt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Text;
import areca.ui.component2.TextField;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import ragtime.cc.article.ArticlesPage;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class FrontPage {

    private static final Log LOG = LogFactory.getLog( FrontPage.class );

    public static final ClassInfo<FrontPage> INFO = FrontPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Start!" );

        ui.body.layout.set( RowLayout.verticals().fillWidth( true ).margins( Size.of( 10, 10 ) ).spacing( 10 ) );
        ui.body.add( new Text() {{
            content.set( "New plans, new horizons... :) " );
        }});

        ui.body.add( new TextField() {{
            content.set( "..." );
            events.on( EventType.TEXT, ev -> {
                LOG.info( "TEXT: %s", content.get() );
            });
        }});

        ui.body.add( new Button() {{
            layoutConstraints.set( RowConstraints.height( 60 ) );
            label.set( "Frontpages" );
            events.on( EventType.SELECT, ev -> {
                site.createPage( new StaticContentPage() ).open();
            });
        }});
        ui.body.add( new Button() {{
            var c = new MutableInt();
            label.set( ":|" );
            //Platform.schedule( 3000, () -> label.set( ":) !!!") );
            events.on( EventType.SELECT, ev -> {
                label.set( ":) -- " + c.incrementAndGet() );

                ui.body.add( new Button() {{
                    label.set( ":" + c.getValue() );
                }});
                ui.body.layout();
            });
        }});

        // actions
        site.actions.add( new Action() {{
            icon.set( "feed" );
            description.set( "Artikel" );
            handler.set( ev -> {
                site.createPage( new ArticlesPage() )/*.origin( ev.clientPos() )*/.open();
            });
        }});
        return ui;
    }

}
