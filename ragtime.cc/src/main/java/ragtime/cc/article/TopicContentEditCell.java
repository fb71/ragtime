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

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.TextField;
import areca.ui.component2.TextField.Type;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.viewer.ColorPickerViewer;
import areca.ui.viewer.CompositeListViewer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.ViewerContext;
import areca.ui.viewer.form.Form;
import areca.ui.viewer.transform.Number2StringTransform;
import ragtime.cc.UICommon;
import ragtime.cc.article.ContentPage.ContentPageCell;
import ragtime.cc.article.ContentPage.TopicContentEdit;
import ragtime.cc.media.MediasPage.MediaListItem;
import ragtime.cc.model.MediaEntity;
import ragtime.cc.model.TopicEntity;

/**
 * Provides a tree cell for the {@link ContentPage} that allows to edit a
 * {@link TopicEntity}.
 *
 * @author Falko Bräutigam
 */
class TopicContentEditCell
        extends ContentPageCell {

    private static final Log LOG = LogFactory.getLog( TopicContentEditCell.class );

    protected TopicEntity   topic;

    private Form            form;


    public TopicContentEditCell( TopicContentEdit tc ) {
        this.topic = tc.topic();
    }


    protected void create() {
        layout.set( RowLayout.verticals().margins( 10, 22 ).spacing( 15 ).fillWidth( true ) );

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

            add( form.newField() //.label( "Farbe" )
                    .viewer( new ColorPickerViewer() )
                    .model( new PropertyModel<>( topic.color ) )
                    .create()
                    .lc( RowConstraints.width( 50 ) ) );
        }});

        add( form.newField() //.label( "Beschreibung" )
                .model( new PropertyModel<>( topic.description ) )
                .viewer( new TextFieldViewer().configure( (TextField t) -> {
                    t.multiline.set( true );
                    t.type.set( Type.MARKDOWN );
                    TextAutocomplete.process( t, uow );
                }))
                .create()
                .lc( RowConstraints.height( 200 ) ) );

        add( form.newField().label( "Reihenfolge" )
                .viewer( new TextFieldViewer() )
                .model( new Number2StringTransform(
                        new PropertyModel<>( topic.order ) ) )
                .create()
                .lc( RowConstraints.height( 35 ) ) );

        // medias
        add( new UIComposite() {{
            //lc( RowConstraints.height( 100 ) );
            layout.set( RowLayout.verticals().fillWidth( true ).spacing( 5 ) );

            // add button
            add( new UIComposite() {{
                lc( RowConstraints.height( 38 ) );
                layout.set( RowLayout.filled().spacing( 15 ) );
                add( new UIComposite() );
                add( new Button() {{
                    lc( RowConstraints.width( 60 ) );
                    tooltip.set( "Bilder/Medien hinzufügen" );
                    icon.set( "add_photo_alternate" );
                    events.on( EventType.SELECT, ev -> {
                        //state.site.createState( new MediasSelectState( sel -> state.addMedias( sel ) ) ).activate();
                    });
                }});
            }});

            // list
            var medias = new ViewerContext<>()
                    .viewer( new CompositeListViewer<MediaEntity>( media -> {
                        return new MediaListItem( media, () -> {} ); //state.removeMediaAction( media ) );
                    }) {{
                        oddEven.set( true );
                        spacing.set( 0 );
                        lines.set( true );
                        onSelect.set( media -> {
                            LOG.info( "SELECT: %s", media );
                        });
                        onLayout.set( c -> TopicContentEditCell.this.layout() );
                    }})
                    .model( new EntityAssocListModel<>( topic.medias ) );
            add( medias.createAndLoad() );
        }});

        form.load();

        // submit
        addAction( new Button() {{
            icon.set( UICommon.ICON_SAVE );
            tooltip.set( "Speichern" );
            enabled.set( false );
            events.on( EventType.SELECT, ev -> {
                form.submit();
                submit().onSuccess( __ -> {
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
