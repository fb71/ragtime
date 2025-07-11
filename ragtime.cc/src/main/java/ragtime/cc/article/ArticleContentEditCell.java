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
import ragtime.cc.article.ContentState.ArticleContentEdit;
import ragtime.cc.media.MediasSelectState;
import ragtime.cc.model.Article;
import ragtime.cc.Extensions;
import ragtime.cc.article.ContentPage.ContentPageCell;

/**
 * Provides a tree cell for the {@link ContentPage} that allows to edit an
 * {@link Article}.
 *
 * @author Falko Bräutigam
 */
public class ArticleContentEditCell
        extends ContentPageCell<ArticleContentEdit> {

    private static final Log LOG = LogFactory.getLog( ArticleContentEditCell.class );

    private Form form;

    private Button deleteBtn;

    @Override
    protected void create() {
        lc( RowConstraints.height( 450, 68 ) );
        layout.set( RowLayout.verticals().margins( 15, 22 ).spacing( 15 ).fillWidth( true ).fillHeight( true ) );

        form = new Form();

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

        // extensions
        Extensions.ofType( ArticlePageExtension.class ).forEach( ex -> {
            var site = new ArticlePageExtension.ExtensionSite( value.value, pageSite, form, ArticleContentEditCell.this );
            ex.doExtendFormStart( site );
        });

        add( new UIComposite() {{
            lc( RowConstraints.height( 35 ) );
            layout.set( RowLayout.filled().spacing( 15 ) );

            add( form.newField().label( "Name" )
                    .description( "Die interne, eindeutige Bezeichnung des Beitrags.\nACHTUNG: Beim Ändern, ändert sich auch die URL des Beitrages!" )
                    .viewer( new TextFieldViewer() )
                    .model( new PermNameValidator( value.article(),
                            new PropertyModel<>( value.article().title ) ) )
                    .create() );
                    //.lc( RowConstraints.height( 35 ) ) );


            add( form.newField().label( "Position" )
                    .description( "Die Position dieses Beitrags im Topic\nBeiträge ohne Angabe werden nach Modifikationsdatum sortiert" )
                    .viewer( new TextFieldViewer() )
                    .model( new Number2StringTransform(
                            new PropertyModel<>( value.article().order ) ) )
                    .create()
                    .lc( RowConstraints.width( 80 ) ) );
        }});

        add( form.newField()
                .model( new PropertyModel<>( value.article().content ) )
                .viewer( new TextFieldViewer().configure( (TextField t) -> {
                    t.multiline.set( true );
                    t.type.set( Type.MARKDOWN );
                    TextAutocomplete.process( t, state.uow );
                }))
                .create() );
                //.lc( RowConstraints.height( 300 ) ) );

        // actions
        add( new UIComposite() {{
            lc( RowConstraints.height( 35 ) );
            layout.set( RowLayout.verticals().fillWidth( true ).spacing( 5 ) );

            add( new UIComposite() {{
                lc( RowConstraints.height( 38 ) );
                layout.set( RowLayout.filled().spacing( 15 ) );
                add( new UIComposite() );

                // extensions
                Extensions.ofType( ArticlePageExtension.class ).forEach( ex -> {
                    var site = new ArticlePageExtension.ExtensionSite( value.value, pageSite, form, this );
                    ex.doExtendFormEnd( site );
                });

                // add media button
                add( new Button() {{
                    lc( RowConstraints.width( 60 ) );
                    tooltip.set( format( "Bilder/Medien zum diesem Artikel hinzufügen", value.article().title.get() ) );
                    icon.set( "add_photo_alternate" );
                    events.on( EventType.SELECT, ev -> {
                        state.site.createState( new MediasSelectState( sel -> value.addMedias( sel ) ) ).activate();
                    });
                }});
            }});
        }});

        Platform.schedule( 100, () -> { // avoid PermNameValidator block page open
            form.load();
        });
    }
}
