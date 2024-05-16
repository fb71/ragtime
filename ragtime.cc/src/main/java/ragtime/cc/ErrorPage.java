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

import java.io.PrintWriter;
import java.io.StringWriter;

import areca.common.AssertionException;
import areca.common.Platform;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.Button;
import areca.ui.component2.Text;
import areca.ui.component2.TextField;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.component2.Events.EventType;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.pageflow.Pageflow;
import jakarta.mail.MessagingException;

/**
 * The general error page.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ErrorPage {

    private static final Log LOG = LogFactory.getLog( ErrorPage.class );

    public static final ClassInfo<ErrorPage> INFO = ErrorPageClassInfo.instance();

    public static void tryOpen( Throwable e ) {
        var page = new ErrorPage();
        page.error = e;
        try {
            var pageflow = Pageflow.current();
            if (!pageflow.pages().anyMatches( p -> p instanceof ErrorPage )) {
                pageflow.create( page ).open();
            }
            else {
                LOG.warn( "Pageflow already contains an ErrorPage" );
            }
        }
        catch (AssertionException ee) {
            // no Pageflow
        }
        catch (Exception ee) {
            ee.printStackTrace( System.err );
        }
    }


    // instance *******************************************

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    protected Throwable         error;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Problem" );

        ui.body.layout.set( RowLayout.verticals().fillWidth( true ).spacing( 15 ).margins( 15, 15 ) );
        ui.body.add( new Text() {{
            lc( RowConstraints.height( 120 ) );
            format.set( Text.Format.HTML );
            content.set( "<h2>Etwas ging schief :(</h2>"
                    + "<p>"
                    + "Du kannst Bescheid sagen, indem du hier unten eine kleine Nachricht "
                    + "schreibst."
                    + "</p>" );
        }});

        var msg = ui.body.add( new TextField() {{
            lc( RowConstraints.height( 190 ) );
            multiline.set( true );
            content.set( "Lieber Falko, \n\n"
                    + "alles ist ganz fürchterlich!\n"
                    + "Bevor der Fehler kam, habe ich gerade folgendes gemacht:\n\n"
                    + "...\n\n" );
            events.on( EventType.TEXT, ev -> {/*just trigger client send*/} );
        }});

        ui.body.add( new Button() {{
            lc( RowConstraints.height( 40 ) );
            label.set( "Abschicken" );
            //type.set( Button.Type.SUBMIT );
            events.on( EventType.SELECT, ev -> {
                try {
                    var out = new StringWriter( 32*1024 );
                    error.printStackTrace( new PrintWriter( out, true ) );
                    Email.send( "falko@fb71.org", "Fehlermeldung",
                            error.toString() + "\n\n" +
                            msg.content.get() + "\n\n" +
                            out.toString() );
                    label.set( "OK" );
                    enabled.set( false );
                    Platform.schedule( 1000, () -> {
                        site.close();
                    });
                }
                catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
        }});
        return ui;
    }

}
