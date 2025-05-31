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
package ragtime.cc;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
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
import ragtime.cc.model.AccountEntity;
import ragtime.cc.web.template.widgets.Markdown;

/**
 * Help system.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class HelpPage {

    private static final Log LOG = LogFactory.getLog( HelpPage.class );

    public static final ClassInfo<HelpPage> INFO = HelpPageClassInfo.instance();

    /**
     *
     */
    public static void addAction( Class<?> pageClass, PageSite pageSite ) {
        pageSite.actions.add( new Action() {{
            icon.set( "help" );
            description.set( "Hilfe anzeigen" );
            order.set( 100 );
            handler.set( ev -> {
                pageSite.createPage( new HelpPage() )
                        .putContext( pageClass.getSimpleName(), Page.Context.DEFAULT_SCOPE )
                        .open();
            });
        }});
    }

    // instance *******************************************

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected String            key;

    @Page.Context( required = false )
    protected AccountEntity     account;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Hilfe" );

        ui.body.layout.set( RowLayout.filled().vertical().spacing( 15 ).margins( 15, 15 ) );
        ui.body.add( new ScrollableComposite() {{
            layout.set( new FillLayout() );
            cssClasses.add( "HelpPage" );
            add( new Text() {{
                format.set( Text.Format.HTML );
                try {
                    var cl = Thread.currentThread().getContextClassLoader();
                    var resName = "help/" + key + ".md";
                    LOG.info( "Loading: %s", resName );
                    var md = IOUtils.toString( cl.getResource( resName ), "UTF8" );
                    var html = Markdown.render( md );
                    content.set( html );
                }
                catch (IOException e) {
                    throw new RuntimeException( e );
                }
            }});
        }});

        if (account != null) {
            //## Hab ich verstanden!
            //**Unter diesem Text** ist ein Knopf, mit dem Du bestätigen kannst, dass du diese kurze Einleitung gelesen hast. Beim nächsten Start der App wird die Hilfe dann nicht mehr automatisch als erstes eingeblendet.

            ui.body.add( new Button() {{
                lc( RowConstraints.height( 40 ) );
                type.set( Button.Type.SUBMIT );
                label.set( "Ok, kann losgehen!" );
                tooltip.set( "Diesen Hilfetext beim Start nicht mehr automatisch einblenden" );
                events.on( EventType.SELECT, ev -> {
                    account.helpSeen.set( true );
                    account.context.getUnitOfWork().submit().onSuccess( __ -> {
                        site.close();
                    });
                });
            }});
        }
        return ui;
    }

}
