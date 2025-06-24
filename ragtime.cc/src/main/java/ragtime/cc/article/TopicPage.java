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
import areca.ui.component2.Button;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.form.Form;
import ragtime.cc.Extensions;
import ragtime.cc.HelpPage;
import ragtime.cc.UICommon;
import ragtime.cc.article.TopicPageExtension.FormContext;

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

            // extensions
//            add( new Text() {{
//                styles.add( CssStyle.of( "color", "#909090" ) );
//                format.set( Format.HTML );
//                content.set( "<em>" //"<hr class=\"Separator\"/>"
//                        + "<h2>Ausgabekanäle</h2>"
//                        + "Das Topic kann mit einem oder mehreren <b>Ausgabekanälen</b> verbunden werden. "
//                        + "Der Ausgabekanel bestimmt ob und wie die Beiträge des Topics dargestellt werden. "
//                        + "Standardmäßig ist jedes Topic mit der <b>Website</b> verbunden."
//                        + "</em>" );
//                }
//                @Override
//                public int computeMinHeight( int width ) {
//                    var lines = (content.get().length() * 7) / width;
//                    return (lines * 17) + 50;
//                }
//            });

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

        // help
        HelpPage.addAction( TopicPage.class, site );
        return ui;
    }

}
