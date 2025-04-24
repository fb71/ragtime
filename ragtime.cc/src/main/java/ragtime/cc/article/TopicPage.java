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
import areca.ui.component2.Text;
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
import areca.ui.viewer.ColorPickerViewer;
import areca.ui.viewer.CompositeListViewer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.ViewerContext;
import areca.ui.viewer.form.Form;
import areca.ui.viewer.transform.Number2StringTransform;
import ragtime.cc.ConfirmDialog;
import ragtime.cc.Extensions;
import ragtime.cc.HelpPage;
import ragtime.cc.UICommon;
import ragtime.cc.article.TopicPageExtension.FormContext;
import ragtime.cc.media.MediasPage.MediaListItem;
import ragtime.cc.media.MediasSelectState;
import ragtime.cc.model.MediaEntity;

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

    protected Form              form;

    protected UIComposite       formBody;


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        ui.init( parent ).title.set( "Topic" );

        ui.body.layout.set( FillLayout.defaults() );
        ui.body.add( new ScrollableComposite() {{
            formBody = layout.set( uic.verticalL().fillHeight( false ) );

            form = new Form();

            // title / color
            add( new UIComposite() {{
                lc( RowConstraints.height( 35 ) );
                layout.set( RowLayout.filled().spacing( uic.space ) );

                add( form.newField().label( "Titel" )
                        .description( "Die interne, *eindeutige* Bezeichnung des Topics.\nACHTUNG!: Beim Ändern, ändert sich auch die URL des Topics!" )
                        .viewer( new TextFieldViewer() )
                        .model( new PermNameValidator( state.topic,
                                new PropertyModel<>( state.topic.title ) ) )
                        .create() );

                add( form.newField() //.label( "Farbe" )
                        .viewer( new ColorPickerViewer() )
                        .model( new PropertyModel<>( state.topic.color ) )
                        .create()
                        .lc( RowConstraints.width( 50 ) ) );
            }});

            add( form.newField() //.label( "Beschreibung" )
                    .model( new PropertyModel<>( state.topic.description ) )
                    .viewer( new TextFieldViewer().configure( (TextField t) -> {
                        t.multiline.set( true );
                        t.type.set( Type.MARKDOWN );
                        TextAutocomplete.process( t, state.uow );
                    }))
                    .create()
                    .lc( RowConstraints.height( 200 ) ) );

            add( form.newField().label( "Reihenfolge" )
                    .viewer( new TextFieldViewer() )
                    .model( new Number2StringTransform(
                            new PropertyModel<>( state.topic.order ) ) )
                    .create()
                    .lc( RowConstraints.height( 35 ) ) );

            // medias
            add( new UIComposite() {{
                //lc( RowConstraints.height( 100 ) );
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
                var medias = new ViewerContext<>()
                        .viewer( new CompositeListViewer<MediaEntity>( media -> {
                            return new MediaListItem( media, () -> state.removeMediaAction( media ) );
                        }) {{
                            oddEven.set( true );
                            spacing.set( 0 );
                            lines.set( true );
                            onSelect.set( media -> {
                                LOG.info( "SELECT: %s", media );
                            });
                            onLayout.set( c -> formBody.layout() );
                        }})
                        .model( state.medias );
                add( medias.createAndLoad() );
            }});

            // extensions
            //add( new Separator() );
            add( new Text() {{
                //lc( RowConstraints.height( 100 ) );
                //enabled.set( false );
                styles.add( CssStyle.of( "color", "#808080" ) );
                format.set( Format.HTML );
                content.set( "<em>" //"<hr class=\"Separator\"/>"
                        + "<h2>Ausgabekanäle</h2>"
                        + "Das Topic kann mit einem oder mehreren <b>Ausgabekanälen</b> verbunden werden. "
                        + "Der Ausgabekanel bestimmt ob und wie die Beiträge des Topics dargestellt werden. "
                        + "Standardmäßig ist jedes Topic mit der <b>Website</b> verbunden."
                        + "</em>" );
                }
                @Override
                public int computeMinHeight( int width ) {
                    var lines = (content.get().length() * 7) / width;
                    return (lines * 17) + 50;
                }
            });

            var ctx = new FormContext( state, TopicPage.this, site, formBody, form, uic );
            Extensions.ofType( TopicPageExtension.class ).forEach( ex -> ex.doExtendFormEnd( ctx ) );

            form.load();
        }});

        // action: submit
        site.actions.add( submitBtn = new Action() {{
            type.set( Button.Type.SUBMIT );
            icon.set( UICommon.ICON_SAVE );
            description.set( "Speichern" );
            enabled.set( false );
            handler.set( ev -> {
                form.submit();
                state.submitAction().onSuccess( __ -> {
                    submitBtn.enabled.set( false );
                });
            });
            Runnable updateEnabled = () -> {
                boolean _enabled = state.medias.modified() || (form.isChanged() && form.isValid() );
                this.enabled.set( _enabled );
            };

            form.subscribe( ev -> updateEnabled.run() );
            state.medias.subscribe( ev -> updateEnabled.run() );
        }});

        // action: delete
        site.actions.add( new Action() {{
            icon.set( UICommon.ICON_DELETE );
            handler.set( ev -> {
                state.topic.articles().executeCollect().onSuccess( articles -> {
                    ConfirmDialog.createAndOpen( "Topic",
                            "<b>" + state.topic.title.get() + "</b><br/><br/>" +
                            "Enthält " + articles.size() + " Beiträge. Diese werden nicht gelöscht." )
                            .size.set( Size.of( 360, 200 ) )
                            .addDeleteAction( () -> {
                                state.deleteAction().onSuccess( __ -> {
                                    site.close();
                                });
                    });
                });
            });
        }});

        // help
        HelpPage.addAction( TopicPage.class, site );
        return ui;
    }

}
