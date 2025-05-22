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

import java.util.ArrayList;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Property;
import areca.ui.component2.Property.ReadWrite;
import areca.ui.component2.Property.ReadWrites;
import areca.ui.component2.Text;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.DialogContainer;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;

/**
 * A general dialog.
 *
 * @author Falko Bräutigam
 */
public class ConfirmDialog
        extends Page {

    private static final Log LOG = LogFactory.getLog( ConfirmDialog.class );

    /**
     *
     *
     * @param title {@link #title}
     * @param msg {@link #msg}
     * @return Newly created instance.
     */
    public static ConfirmDialog createAndOpen( String title, String msg ) {
        return create( title, msg ).open();
    }

    public static ConfirmDialog create( String title, String msg ) {
        return new ConfirmDialog().title.set( title ).msg.set( msg );
    }


    // instance *******************************************

    public ReadWrite<ConfirmDialog,String> title = Property.rw( this, "title" );

    /** The size of the dialog. Defaults to: 320x200 */
    public ReadWrite<ConfirmDialog,Size> size = Property.rw( this, "size", Size.of( 300, 200 ) );

    /** HTML content of the dialog. */
    public ReadWrite<ConfirmDialog,String> msg = Property.rw( this, "msg" );

    public ReadWrites<ConfirmDialog,UIComponent> actions = Property.rws( this, "actions", new ArrayList<>() );

    protected DialogContainer ui;


    public ConfirmDialog addDeleteAction( Runnable task ) {
        actions.add( new Button() {{
            label.set( "Löschen" );
            type.set( Button.Type.SUBMIT );
            events.on( EventType.SELECT, ev -> {
                pageSite.close();
                task.run();
            });
        }});
        return this;
    }


    public ConfirmDialog addOkAction( Runnable task ) {
        actions.add( new Button() {{
            label.set( "Ok" );
            //type.set( Button.Type.SUBMIT );
            events.on( EventType.SELECT, ev -> {
                pageSite.close();
                task.run();
            });
        }});
        return this;
    }


    public ConfirmDialog addNoAction( Runnable task ) {
        actions.add( new Button() {{
            label.set( "Nein" );
            //type.set( Button.Type.SUBMIT );
            events.on( EventType.SELECT, ev -> {
                pageSite.close();
                task.run();
            });
        }});
        return this;
    }


    public ConfirmDialog addCancelAction( Runnable task ) {
        actions.add( new Button() {{
            label.set( "Abbrechen" );
            type.set( Button.Type.NAVIGATE );
            events.on( EventType.SELECT, ev -> {
                pageSite.close();
                task.run();
            });
        }});
        return this;
    }


    public ConfirmDialog open() {
        Pageflow.current().create( this ).open();
        return this;
    }


    @Override
    protected UIComponent onCreateUI( UIComposite parent ) {
        this.ui = new DialogContainer( this, parent );
        //ui.init( parent );
        title.onInitAndChange( (v,__) -> ui.title.set( v ) );
        size.onInitAndChange( (v,__) -> ui.dialogSize.set( v ) );

        ui.body.layout.set( RowLayout.filled().spacing( 15 ).margins( 15, 15 ) );
        ui.body.add( new Text() {{
            format.set( Text.Format.HTML );
            msg.onInitAndChange( (v,__) -> content.set( v ) );
        }});
        actions.onInitAndChange( (btns,__) -> {
            btns.forEach( btn -> ui.actions.add( btn ) );
        });
        return ui;
    }

}
