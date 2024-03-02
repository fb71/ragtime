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
package ragtime.cc.website;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Label;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.form.Form;
import ragtime.cc.UICommon;
import ragtime.cc.article.PropertyModel;
import ragtime.cc.website.model.TemplateConfigEntity;

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
        ui.init( parent ).title.set( "Web-Seite" );

        ui.body.layout.set( FillLayout.defaults() );
        ui.body.add( new ScrollableComposite() {{
            layout.set( RowLayout.filled().vertical().margins( uic.margins ).spacing( uic.spaceL ) );

            TemplateConfigEntity config = state.config.get();

            form = new Form();

            // PageConfig
            add( new UIComposite() {{
                layout.set( RowLayout.filled().vertical().margins( uic.marginsL ).spacing( uic.spaceL ) );
                bordered.set( true );
                cssClasses.add( "MessageCard" );
                addDecorator( new Label().content.set( "Seite" ) );

                add( form.newField().label( "Titel" )
                        .viewer( new TextFieldViewer() )
                        .model( new PropertyModel<>( config.page.get().title ) )
                        .create() );
                add( form.newField().label( "Titel 2" )
                        .viewer( new TextFieldViewer() )
                        .model( new PropertyModel<>( config.page.get().title2 ) )
                        .create() );
                add( form.newField().label( "Fusszeile" )
                        .viewer( new TextFieldViewer() )
                        .model( new PropertyModel<>( config.page.get().footer ) )
                        .create() );
            }});
            // NavItems
            add( new UIComposite() {{
                layout.set( RowLayout.verticals().fillWidth( true ).margins( uic.marginsL ).spacing( uic.spaceL ) );
                bordered.set( true );
                cssClasses.add( "MessageCard" );
                addDecorator( new Label().content.set( "Navigation / Menüs" ) ).get();

                for (var navItem : config.navItems) {
                    add( new UIComposite() {{
                        var itemRow = this;
                        layoutConstraints.set( RowConstraints.height( 35 ) );
                        layout.set( RowLayout.filled().spacing( uic.space ) );

                        add( form.newField().label( "Titel" )
                                .viewer( new TextFieldViewer() )
                                .model( new PropertyModel<>( navItem.title ) )
                                .create() );
                        add( form.newField().label( "Ziel" )
                                .viewer( new TextFieldViewer() )
                                .model( new PropertyModel<>( navItem.href ) )
                                .create() );

                        add( new Button() {{
                            layoutConstraints.set( RowConstraints.width( 50 ) );
                            bordered.set( false );
                            icon.set( "delete" );
                            tooltip.set( "Diesen Eintrag löschen" );
                            events.on( EventType.SELECT, ev -> {
                                //state.
                                var itemRowParent = itemRow.parent();
                                itemRow.dispose();
                                itemRowParent.layout();
                            });
                        }});
                    }});
                }
            }});
            form.load();
        }});

        // action: submit
        site.actions.add( submitBtn = new Action() {{
            //icon.set( "done" );
            description.set( "Speichern" );
            handler.set( ev -> {
                form.submit();
                state.submitAction.run();
            });
            form.subscribe( l -> {
                icon.set( form.isChanged() && form.isValid() && state.submitAction.canRun() ? "done" : "cancel" );
            });
        }});
        // action: revert
        site.actions.add( revertBtn = new Action() {{
            //icon.set( "undo" );
            description.set( "Zurücksetzen" );
            handler.set( ev -> {
                form.revert();
            });
            form.subscribe( l -> {
                icon.set( form.isChanged() ? "undo" : "" );
            });
        }});

        return ui;
    }


    @Page.Close
    public boolean onClose() {
        LOG.info( "onClose()" );
        return state.disposeAction();
    }
}
