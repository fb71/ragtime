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
import areca.ui.component2.TextField;
import areca.ui.component2.TextField.Type;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.viewer.SelectViewer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.form.Form;
import areca.ui.viewer.transform.Number2StringTransform;
import ragtime.cc.AssociationModel;
import ragtime.cc.EntityTransform;
import ragtime.cc.article.ContentPage.ArticleContentEdit;
import ragtime.cc.article.ContentPage.ContentPageCell;
import ragtime.cc.model.TopicEntity;

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

    @Override
    protected void create() {
        layout.set( RowLayout.verticals().margins( 10, 22 ).spacing( 15 ).fillWidth( true ) );

        form = new Form();

        add( new UIComposite() {{
            lc( RowConstraints.height( 35 ) );
            layout.set( RowLayout.filled().spacing( 15 ) );

            var topics = new EntityTransform<>( uow, TopicEntity.class, TopicEntity.TYPE.title,
                    new AssociationModel<>( value.article().topic ) );
            add( form.newField()
                    .viewer( new SelectViewer( topics.values(), "Wählen..." ) )
                    .model( topics )
                    .create()
                    .tooltip.set( "Das Topic dieses Textes" ) );

            add( form.newField().label( "Position" )
                    .description( "Die Position dieses Beitrags im Topic\nBeiträge ohne Angabe werden nach Modifikationsdatum sortiert" )
                    .viewer( new TextFieldViewer() )
                    .model( new Number2StringTransform(
                            new PropertyModel<>( value.article().order ) ) )
                    .create()
                    .lc( RowConstraints.width( 80 ) ) );
        }});

        add( form.newField().label( "Name" )
                .description( "Die interne, eindeutige Bezeichnung des Beitrags.\nACHTUNG: Beim Ändern, ändert sich auch die URL des Beitrages!" )
                .viewer( new TextFieldViewer() )
                .model( new PermNameValidator( value.article(),
                        new PropertyModel<>( value.article().title ) ) )
                .create()
                .lc( RowConstraints.height( 35 ) ) );

        add( form.newField()
                .model( new PropertyModel<>( value.article().content ) )
                .viewer( new TextFieldViewer().configure( (TextField t) -> {
                    t.multiline.set( true );
                    t.type.set( Type.MARKDOWN );
                    TextAutocomplete.process( t, uow );
                }))
                .create()
                .lc( RowConstraints.height( 300 ) ) );
    }
}
