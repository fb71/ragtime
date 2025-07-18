/*
 * Copyright (C) 2024-2025, the @authors. All rights reserved.
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
package ragtime.cc.web;

import areca.common.Platform;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Label;
import areca.ui.component2.Link;
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
import areca.ui.viewer.ColorPickerViewer;
import areca.ui.viewer.CompositeListViewer;
import areca.ui.viewer.SelectViewer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.form.Form;
import areca.ui.viewer.model.ListModelBase;
import areca.ui.viewer.transform.Number2StringTransform;
import ragtime.cc.HelpPage;
import ragtime.cc.UICommon;
import ragtime.cc.article.EntityAssocModel;
import ragtime.cc.article.EntityCompositeListModel;
import ragtime.cc.article.PropertyModel;
import ragtime.cc.web.http.WebsiteServlet;
import ragtime.cc.web.model.NavItem;
import ragtime.cc.web.model.TemplateConfigEntity;
import ragtime.cc.web.template.TemplateInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TemplateConfigPage {

    private static final Log LOG = LogFactory.getLog( TemplateConfigPage.class );

    public static final ClassInfo<TemplateConfigPage> INFO = TemplateConfigPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected TemplateConfigState state;

    @Page.Context
    protected UICommon          uic;

    protected Action            submitBtn;

    protected Action            revertBtn;

    protected Form              form;


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        ui.init( parent ).title.set( "Einstellungen" );

        ui.body.layout.set( FillLayout.defaults() );
        ui.body.add( new ScrollableComposite() {{
            layout.set( RowLayout.verticals().fillWidth( true ).margins( uic.marginsL ).spacing( uic.spaceL ) );

            form = new Form();

            state.config.onSuccess( config -> {
                // Template
                add( new UIComposite() {{
                    layout.set( uic.vertical().fillHeight( false ) );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Design-Vorlage" ) );

//                    var templates = TemplateInfo.user().map( t -> t.name ).toArray( String[]::new );
//                    add( form.newField().label( String.format( "Name (%s)", String.join(", ", templates ) ) )
//                            .viewer( new TextFieldViewer() )
//                            .model( new PropertyModel<>( config.templateName ) )
//                            .create() );

                    var templates = TemplateInfo.user().map( t -> t.name ).toList();
                    add( form.newField()
                            .viewer( new SelectViewer( templates ) )
                            .model( new PropertyModel<>( config.templateName ) )
                            .create() );

                    // website link
                    add( new UIComposite() {{
                        lc( RowConstraints.height( 20 ) );
                        layout.set( RowLayout.filled().spacing( 20 ) );
                        add( new UIComposite() );
                        add( new Link() {{
                            lc( RowConstraints.width( 140 ) );
                            content.set( "Web-Seite ansehen..." );
                            tooltip.set( "Die Web-Seite in einem neuen Browser-Fenster öffnen" );
                            href.set( String.format( "website/%s/%s", state.account.permid.get(), WebsiteServlet.PATH_HOME ) );
                        }});
                    }});
                }});

                // PageConfig
                add( new UIComposite() {{
                    layout.set( uic.verticalL().fillHeight( false ) );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Seite" ) );

                    add( new UIComposite() {{
                        lc( RowConstraints.height( 32 ) ); //Text.DEFAULT_HEIGHT ) );
                        layout.set( RowLayout.filled().spacing( uic.spaceL ) );
                        add( form.newField().label( "Titel" )
                                .viewer( new TextFieldViewer() )
                                .model( new PropertyModel<>( config.page.get().title ) )
                                .create() );
                        add( form.newField()/*.label( "favicon" )*/
                                .description( "Browser-Bild\nGröße: 16x16 oder 32x32" )
                                .viewer( new MediaPickerViewer( state.site ) )
                                .model( new EntityAssocModel<>( config.favicon ) )
                                .create()
                                .lc( RowConstraints.width( 32 ) ) );
                    }});
                    add( form.newField().label( "Titel 2" )
                            .viewer( new TextFieldViewer() )
                            .model( new PropertyModel<>( config.page.get().title2 ) )
                            .create() );
                    add( form.newField().label( "Fusszeile" )
                            .viewer( new TextFieldViewer() )
                            .model( new PropertyModel<>( config.page.get().footer ) )
                            .create() );
                }});

                // Bilder
                add( new UIComposite() {{
                    lc( RowConstraints.height( 100 ) );
                    layout.set( uic.verticalL().margins( uic.spaceL, 10 ).spacing( uic.space ) );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Bilder" ) );

                    add( new UIComposite() {{
                        layout.set( RowLayout.filled().spacing( uic.spaceL ) );
                        add( form.newField()/*.label( "Lead" )*/
                                .viewer( new MediaPickerViewer( state.site ) )
                                .model( new EntityAssocModel<>( config.leadImage ) )
                                .create() );
                        add( form.newField()/*.label( "Banner" )*/
                                .viewer( new MediaPickerViewer( state.site ) )
                                .model( new EntityAssocModel<>( config.bannerImage ) )
                                .create() );
                    }});
                }});

                // Colors
                add( new UIComposite() {{
                    //layoutConstraints.set( RowConstraints.height( 220 ) );
                    layout.set( uic.verticalL().fillHeight( false ).spacing( uic.space ) );
                    //bordered.set( true );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Farben" ) );

                    add( new UIComposite() {{
                        layout.set( RowLayout.defaults().fillWidth( true ).spacing( uic.space ) );
                        add( form.newField().label( "Hintergrund" )
                                .viewer( new ColorPickerViewer() )
                                .model( new PropertyModel<>( config.colors.get().pageBackground ) )
                                .create() );
                        add( form.newField().label( "Text" )
                                .viewer( new ColorPickerViewer() )
                                .model( new PropertyModel<>( config.colors.get().pageForeground ) )
                                .create() );
                    }});

                    add( new UIComposite() {{
                        layout.set( RowLayout.defaults().fillWidth( true ).spacing( uic.space ) );
                        add( form.newField().label( "Kopf - Hintergrund" )
                                .viewer( new ColorPickerViewer() )
                                .model( new PropertyModel<>( config.colors.get().headerBackground ) )
                                .create() );
                        add( form.newField().label( "Kopf - Text" )
                                .viewer( new ColorPickerViewer() )
                                .model( new PropertyModel<>( config.colors.get().headerForeground ) )
                                .create() );
                    }});

                    add( new UIComposite() {{
                        layout.set( RowLayout.defaults().fillWidth( true ).spacing( uic.space ) );
                        add( form.newField().label( "Footer - Hintergrund" )
                                .viewer( new ColorPickerViewer() )
                                .model( new PropertyModel<>( config.colors.get().footerBackground ) )
                                .create() );
                        add( form.newField().label( "Footer - Text" )
                                .viewer( new ColorPickerViewer() )
                                .model( new PropertyModel<>( config.colors.get().footerForeground ) )
                                .create() );
                    }});

                    add( new UIComposite() {{
                        layout.set( RowLayout.defaults().fillWidth( true ).spacing( uic.space ) );
                        add( form.newField().label( "Akzent" )
                                .viewer( new ColorPickerViewer() )
                                .model( new PropertyModel<>( config.colors.get().accent ) )
                                .create() );
                        add( form.newField().label( "Link" )
                                .viewer( new ColorPickerViewer() )
                                .model( new PropertyModel<>( config.colors.get().link ) )
                                .create() );
                    }});
                }});

                // NavItems
                add( new UIComposite() {{
                    layout.set( uic.verticalL().fillHeight( false ) );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Navigation" ) ).get();

                    var items = new EntityCompositeListModel<>( TemplateConfigEntity.class, config.navItems )
                            .orderBy( NavItem.TYPE.order, () -> isDisposed() );

                    add( form.newField()
                            .viewer( new CompositeListViewer<NavItem>( NavItemEditor::new ) {{
                                spacing.set( 20 );
                            }})
                            .model( items )
                            .create() );

                    add( new Button() {{
                        icon.set( "add" );
                        tooltip.set( "Neues Element hinzufügen" );
                        events.on( EventType.SELECT, ev -> {
                            items.createElement( NavItem.defaults() );
                            ui.body.layout();
                        });
                    }});
                }});

                // Footer NavItems
                add( new UIComposite() {{
                    layout.set( uic.verticalL().fillHeight( false ) );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Footer Navigation" ) ).get();

                    var items = new EntityCompositeListModel<>( TemplateConfigEntity.class, config.footerNavItems )
                            .orderBy( NavItem.TYPE.order, () -> isDisposed() );

                    add( form.newField()
                            .viewer( new CompositeListViewer<NavItem>( NavItemEditor::new ) {{
                                spacing.set( 20 );
                            }})
                            .model( items )
                            .create() );

                    add( new Button() {{
                        //layoutConstraints.set( RowConstraints.height( 40 ) );
                        icon.set( "add" );
                        tooltip.set( "Neues Element hinzufügen" );
                        events.on( EventType.SELECT, ev -> {
                            items.createElement( NavItem.defaults() );
                            ui.body.layout();
                        });
                    }});
                }});

                // EMail
                add( new UIComposite() {{
                    layout.set( uic.verticalL().fillHeight( false ) );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Kalender" ) ).get();

                    add( form.newField().label( "EMail  (" + state.account.email.get() + ")" )
                            .description( "EMail-Adresse für die 'Termin buchen' Funktion.\nNur angeben, wenn abweichend vom Standard!" )
                            .viewer( new TextFieldViewer() )
                            .model( new PropertyModel<>( config.email ) )
                            .create() );
                }});

                // separator
                add( new Text().layoutConstraints.set( RowConstraints.height( 10 ) ) );

                ui.body.layout();

                // split rendering and help browser (?)
                Platform.schedule( 750, () -> {
                    form.load();
                    ui.body.layout();
                });
            });
        }});

        // action: submit
        site.actions.add( submitBtn = new Action() {{
            //order.set( 10 );
            description.set( "Speichern" );
            type.set( Button.Type.SUBMIT );
            enabled.set( false );
            handler.set( ev -> {
                form.submit();
                state.submitAction().onSuccess( __ -> {
                    enabled.set( false );
                });
            });
            form.subscribe( ev -> {
                var _enabled = form.isChanged() && form.isValid();
                enabled.set( _enabled );
                icon.set( _enabled ? UICommon.ICON_SAVE : "" );
            });
        }});

        // action: CSS
        site.actions.add( new Action() {{
            icon.set( "code" );
            description.set( "CSS bearbeiten" );
            handler.set( ev -> {
                site.createPage( new EditCssPage() ).open();
            });
        }});

        // help
        HelpPage.addAction( TemplateConfigPage.class, site );
        return ui;
    }


    /**
     *
     */
    protected class NavItemEditor extends UIComposite {

        public NavItemEditor( NavItem item, ListModelBase<NavItem> model ) {
            layoutConstraints.set( RowConstraints.height( 30 ) );
            layout.set( RowLayout.defaults().fillWidth( true ).spacing( uic.space ) );

            add( form.newField().label( "Titel" )
                    .viewer( new TextFieldViewer() )
                    .model( new PropertyModel<>( item.title ) )
                    .createAndLoad() );
            add( form.newField().label( "Ziel" )
                    .viewer( new TextFieldViewer() )
                    .model( new PropertyModel<>( item.href ) )
                    .createAndLoad() );
            add( form.newField().label( "Pos" )
                    .viewer( new TextFieldViewer() )
                    .model( new Number2StringTransform(
                            new PropertyModel<>( item.order ) ) )
                    .createAndLoad()
                    .layoutConstraints.set( RowConstraints.width( 35 ) ) );

            add( new Button() {{
                layoutConstraints.set( RowConstraints.width( 40 ) );
                icon.set( "close" );
                tooltip.set( "Dieses Element löschen" );
                events.on( EventType.SELECT, ev -> {
                    ((EntityCompositeListModel<NavItem>)model).removeElement( item );
                    ui.body.layout();
                });
            }});
        }
    }


//    @Page.Close
//    public boolean onClose() {
//        LOG.info( "onClose()" );
//        //Platform.async( () -> state.disposeAction() );
//        state.disposeAction();
//        return true;
//    }
}
