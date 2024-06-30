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
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.TextField;
import areca.ui.component2.TextField.Type;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.CompositeListViewer;
import areca.ui.viewer.SelectViewer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.ViewerContext;
import areca.ui.viewer.form.Form;
import areca.ui.viewer.transform.Number2StringTransform;
import ragtime.cc.AssociationModel;
import ragtime.cc.ConfirmDialog;
import ragtime.cc.EntityTransform;
import ragtime.cc.HelpPage;
import ragtime.cc.UICommon;
import ragtime.cc.media.MediasPage.MediaListItem;
import ragtime.cc.media.MediasSelectState;
import ragtime.cc.model.MediaEntity;
import ragtime.cc.model.TopicEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticlePage {

    private static final Log LOG = LogFactory.getLog( ArticlePage.class );

    public static final ClassInfo<ArticlePage> INFO = ArticlePageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected ArticleEditState  state;

    private Form                form;


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        ui.init( parent ).title.set( "Beitrag" );

        ui.body.layout.set( FillLayout.defaults() );
        ui.body.add( new ScrollableComposite() {{
            layout.set( uic.verticalL().fillHeight( true ) );

            form = new Form();

            add( new UIComposite() {{
                lc( RowConstraints.height( 35 ) );
                layout.set( RowLayout.filled().spacing( uic.spaceL ) );

                var topics = new EntityTransform<>( state.uow, TopicEntity.class, TopicEntity.TYPE.title,
                        new AssociationModel<>( state.article.$().topic ) );
                add( form.newField()
                        .viewer( new SelectViewer( topics.values(), "Wählen..." ) )
                        .model( topics )
                        .create()
                        .tooltip.set( "Das Topic dieses Textes" ) );

                add( form.newField().label( "Position" )
                        .description( "Die Position dieses Beitrags im Topic\nBeiträge ohne Angabe werden nach Modifikationsdatum sortiert" )
                        .viewer( new TextFieldViewer() )
                        .model( new Number2StringTransform(
                                new PropertyModel<>( state.article.$().order ) ) )
                        .create()
                        .lc( RowConstraints.width( 80 ) ) );
            }});

            add( form.newField().label( "Name" )
                    .description( "Die interne, *eindeutige* Bezeichnung des Beitrags.\nACHTUNG!: Beim Ändern, ändert sich auch die URL des Beitrags!" )
                    .viewer( new TextFieldViewer() )
                    .model( new PermNameValidator( state.uow,
                            new PropertyModel<>( state.article.$().title ) ) )
                    .create()
                    .lc( RowConstraints.height( 35 ) ) );

            add( form.newField()
                    .model( new PropertyModel<>( state.article.$().content ) )
                    .viewer( new TextFieldViewer().configure( (TextField t) -> {
                        t.multiline.set( true );
                        t.type.set( Type.MARKDOWN );
                        TextAutocomplete.process( t, state.uow );
                    }))
                    .create()
                    .lc( RowConstraints.height( 300 ) ) );

//            add( form.newField().label( "URL" )
//                    .viewer( new TextFieldViewer() )
//                    .model( new PropertyModel<>( state.article.$().permName ) )
//                    .create()
//                    .lc( RowConstraints.height( 35 ) ) );

            // medias
            add( new UIComposite() {{
                layout.set( RowLayout.verticals().fillWidth( true ).spacing( 5 ) );

                // add button
                add( new UIComposite() {{
                    lc( RowConstraints.height( 38 ) );
                    layout.set( RowLayout.filled().spacing( uic.space ) );
                    add( new UIComposite() );
                    add( new Button() {{
                        lc( RowConstraints.width( 60 ) );
                        tooltip.set( "Bilder/Medien hinzufügen" );
                        icon.set( "add_photo_alternate" );
                        events.on( EventType.SELECT, ev -> {
                            state.site.createState( new MediasSelectState( sel -> state.addMedias( sel ) ) ).activate();
                        });
                    }});
                }});

                // list
                add( new UIComposite() {{
                    //lc( RowConstraints.height( 70 ) );
                    layout.set( FillLayout.defaults() );
                    var medias = new ViewerContext<>()
                            .viewer( new CompositeListViewer<MediaEntity>( (media,model) -> {
                                return new MediaListItem( media, () -> state.removeMediaAction( media ) );
                            }) {{
                                oddEven.set( true );
                                spacing.set( 0 );
                                lines.set( true );
                                onSelect.set( media -> {
                                    LOG.info( "SELECT: %s", media );
                                });
                            }})
                            .model( state.medias );
                    add( medias.createAndLoad() );
                }});
            }});

            form.load();
        }});

        // action: submit
        site.actions.add( new Action() {{
            description.set( "Speichern" );
            icon.set( UICommon.ICON_SAVE );
            type.set( Button.Type.SUBMIT );
            enabled.set( false );
            handler.set( ev -> {
                form.submit();
                state.submitAction().onSuccess( __ -> {
                    enabled.set( false );
                });
            });
            Runnable updateEnabled = () -> {
                boolean _enabled = state.modelChanged || (form.isChanged() && form.isValid() );
                this.enabled.set( _enabled );
            };

            form.subscribe( ev -> updateEnabled.run() );
            state.medias.subscribe( ev -> updateEnabled.run() );
        }});

        // action: delete
        site.actions.add( new Action() {{
            icon.set( UICommon.ICON_DELETE );
            handler.set( ev -> {
                ConfirmDialog.createAndOpen( "Beitrag",
                        "<b><center>" + state.article.$().title.opt().orElse( "[null]" ) + "</center></b><br/><br/>" )
                        .size.set( Size.of( 320, 200 ) )
                        .addDeleteAction( () -> {
                            state.deleteAction().onSuccess( __ -> {
                                site.close();
                            });
                        });
            });
        }});

        // help
        HelpPage.addAction( ArticlePage.class, site );
        return ui;
    }

}
