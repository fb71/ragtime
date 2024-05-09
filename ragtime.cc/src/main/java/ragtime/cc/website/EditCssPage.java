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
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.TextField;
import areca.ui.component2.TextField.Type;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.form.Form;
import ragtime.cc.UICommon;
import ragtime.cc.article.PropertyModel;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class EditCssPage {

    private static final Log LOG = LogFactory.getLog( EditCssPage.class );

    public static final ClassInfo<EditCssPage> INFO = EditCssPageClassInfo.instance();

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
        ui.init( parent ).title.set( "CSS" );

        ui.body.layout.set( FillLayout.defaults() );
        ui.body.add( new ScrollableComposite() {{
            layout.set( RowLayout.filled().vertical().margins( uic.margins ).spacing( uic.spaceL ) );

            state.config.onSuccess( config -> {
                form = new Form();
                add( form.newField()
                        .viewer( new TextFieldViewer().configure( (TextField t) -> {
                            t.multiline.set( true );
                            t.type.set( Type.CSS );
                        }))
                        .model( new PropertyModel<>( config.css ) )
                        .create()
                        .layoutConstraints.set( null ) );

                form.load();
            });
        }});

        // action: submit
        site.actions.add( submitBtn = new Action() {{
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
//        // action: revert
//        site.actions.add( revertBtn = new Action() {{
//            //icon.set( "undo" );
//            description.set( "Zurücksetzen" );
//            handler.set( ev -> {
//                form.revert();
//            });
//            form.subscribe( l -> {
//                icon.set( form.isChanged() ? "undo" : "" );
//            });
//        }});

        return ui;
    }

}
