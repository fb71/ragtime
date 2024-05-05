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
package ragtime.cc.article;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.component2.TextField;
import areca.ui.component2.TextField.Type;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.ColorPickerViewer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.form.Form;
import ragtime.cc.UICommon;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TopicPage {

    private static final Log LOG = LogFactory.getLog( TopicPage.class );

    public static final ClassInfo<TopicPage> INFO = TopicPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected TopicEditState    state;

    protected Action            submitBtn;

    private Form                form;


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        ui.init( parent ).title.set( "Topic" );

        form = new Form();
        form.subscribe( ev -> {
            LOG.info( "updateEnabled(): changed = %s, valid = %s", form.isChanged(), form.isValid() );
            boolean enabled = form.isChanged() && form.isValid();
            submitBtn.icon.set( enabled ? "done" : "" );
            submitBtn.enabled.set( enabled );
        });

        ui.body.layout.set( uic.verticalL().fillHeight( true ) );

//        ui.body.add( new Select() {{
//            lc( RowConstraints.height( 35 ) );
//            tooltip.set( "Das Topic dieses Textes" );
//            options.set( Arrays.asList( "Erstens", "Zweitens", "Drittens" ) );
//            value.set( "Zweitens" );
//            events.on( EventType.TEXT, ev -> {
//                LOG.info( "Selected: %s", value.get() );
//            });
//        }});

        ui.body.add( form.newField().label( "Name" )
                .model( new PropertyModel<>( state.topic.name ) )
                .viewer( new TextFieldViewer() )
                .create()
                .lc( RowConstraints.height( 35 ) ) );

        ui.body.add( form.newField().label( "Titel" )
                .model( new PropertyModel<>( state.topic.title ) )
                .viewer( new TextFieldViewer() )
                .create()
                .lc( RowConstraints.height( 35 ) ) );

        ui.body.add( form.newField().label( "Farbe" )
                .model( new PropertyModel<>( state.topic.color ) )
                .viewer( new ColorPickerViewer() )
                .create()
                .lc( RowConstraints.height( 35 ) ) );

        ui.body.add( form.newField() //.label( "Beschreibung" )
                .model( new PropertyModel<>( state.topic.description ) )
                .viewer( new TextFieldViewer().configure( (TextField t) -> {
                    t.multiline.set( true );
                    t.type.set( Type.MARKDOWN );
                }))
                .create()
                .layoutConstraints.set( null ) ); //RowConstraints.height( 300 ) ) );

        form.load();

        // action: submit
        site.actions.add( submitBtn = new Action() {{
            description.set( "Speichern" );
            enabled.set( false );
            handler.set( ev -> {
                form.submit();
                state.submitAction().onSuccess( __ -> {
                    submitBtn.enabled.set( false );
                });
            });
        }});
        return ui;
    }

}
