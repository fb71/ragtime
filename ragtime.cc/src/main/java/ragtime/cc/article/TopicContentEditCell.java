/*
 * Copyright (C) 2025, the @authors. All rights reserved.
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

import static java.lang.String.format;

import areca.common.Platform;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.TextField;
import areca.ui.component2.TextField.Type;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.form.Form;
import areca.ui.viewer.transform.Number2StringTransform;
import ragtime.cc.UICommon;
import ragtime.cc.article.ContentPage.ContentPageCell;
import ragtime.cc.article.ContentState.TopicContentEdit;
import ragtime.cc.media.MediasSelectState;
import ragtime.cc.model.TopicEntity;

/**
 * Provides a tree cell for the {@link ContentPage} that allows to edit a
 * {@link TopicEntity}.
 *
 * @author Falko Bräutigam
 */
class TopicContentEditCell
        extends ContentPageCell<TopicContentEdit> {

    private static final Log LOG = LogFactory.getLog( TopicContentEditCell.class );

    protected TopicContentEdit  tc;

    protected TopicEntity       topic;

    private Form                form;


    public TopicContentEditCell( TopicContentEdit tc ) {
        this.tc = tc;
        this.topic = tc.topic();
    }


    protected void create() {
        lc( RowConstraints.height( 300, 42 ) );

        layout.set( RowLayout.verticals().margins( 15, 22 ).spacing( 15 ).fillWidth( true ).fillHeight( true ) );

        form = new Form();

        // title / color
        add( new UIComposite() {{
            lc( RowConstraints.height( 35 ) );
            layout.set( RowLayout.filled().spacing( 15 ) );

            add( form.newField().label( "Titel" )
                    .description( "Die interne, *eindeutige* Bezeichnung des Topics.\nACHTUNG!: Beim Ändern, ändert sich auch die URL des Topics!" )
                    .viewer( new TextFieldViewer() )
                    .model( new PermNameValidator( topic,
                            new PropertyModel<>( topic.title ) ) )
                    .create() );

            add( form.newField().label( "Position" )
                    .description( "Die Position in der Reihenfolge der Topics" )
                    .viewer( new TextFieldViewer() )
                    .model( new Number2StringTransform(
                            new PropertyModel<>( topic.order ) ) )
                    .create()
                    .lc( RowConstraints.width( 80 ) ) );

//            add( form.newField() //.label( "Farbe" )
//                    .viewer( new ColorPickerViewer() )
//                    .model( new PropertyModel<>( topic.color ) )
//                    .create()
//                    .lc( RowConstraints.width( 50 ) ) );
        }});

        add( form.newField() //.label( "Beschreibung" )
                .model( new PropertyModel<>( topic.description ) )
                .viewer( new TextFieldViewer().configure( (TextField t) -> {
                    t.multiline.set( true );
                    t.type.set( Type.MARKDOWN );
                    TextAutocomplete.process( t, state.uow );
                }))
                .create() );
                //.lc( RowConstraints.height( 150 ) ) );

        // add media
        add( new UIComposite() {{
            lc( RowConstraints.height( 38 ) );
            layout.set( RowLayout.filled().spacing( 15 ) );
            add( new UIComposite() );
            add( new Button() {{
                lc( RowConstraints.width( 60 ) );
                tooltip.set( format( "Bilder/Medien zum diesem Topic hinzufügen", topic.title.get() ) );
                icon.set( "add_photo_alternate" );
                events.on( EventType.SELECT, ev -> {
                    state.site.createState( new MediasSelectState( sel -> tc.addMedias( sel ) ) ).activate();
                });
            }});
        }});

        Platform.schedule( 500, () -> { // avoid PermNameValidator block page open
            form.load();
        });

        form.subscribe( ev -> {
            if (form.isChanged() && form.isValid() ) {
                registerSaveAction( () -> {
                    form.submit();
                    return true;
                });
            }
            else {
                removeSaveAction();
            }
        });

        // submit
        addAction( new Button() {{
            icon.set( UICommon.ICON_SAVE );
            tooltip.set( "Speichern" );
            enabled.set( false );
            events.on( EventType.SELECT, ev -> {
                form.submit();
                state.submitAction().onSuccess( submitted -> {
                    enabled.set( false );
                });
            });
            Runnable updateEnabled = () -> {
                boolean _enabled = /*state.medias.modified() ||*/ (form.isChanged() && form.isValid() );
                enabled.set( _enabled );
            };

            form.subscribe( ev -> updateEnabled.run() );
            //state.medias.subscribe( ev -> updateEnabled.run() );
        }});
    }
}
