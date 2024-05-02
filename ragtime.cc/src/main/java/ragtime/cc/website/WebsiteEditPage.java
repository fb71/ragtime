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

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.polymap.model2.runtime.Lifecycle.State.AFTER_SUBMIT;

import org.polymap.model2.query.Expressions;

import areca.common.Platform;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.IFrame;
import areca.ui.component2.IFrame.IFrameMsgEvent;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.statenaction.State;
import ragtime.cc.UICommon;
import ragtime.cc.article.ArticleEditState;
import ragtime.cc.model.Article;
import ragtime.cc.model.EntityLifecycleEvent;

/**
 * In-Place editing of content and website/template config.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class WebsiteEditPage {

    private static final Log LOG = LogFactory.getLog( WebsiteEditPage.class );

    public static final ClassInfo<WebsiteEditPage> INFO = WebsiteEditPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected WebsiteEditState  state;

    @Page.Context
    protected UICommon          uic;

    private ArticleEditState    disposableChildState;


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        ui.init( parent ).title.set( "Bearbeiten" );

        ui.body.layout.set( FillLayout.defaults() );
        ui.body.add( new IFrame() {{
            //layout.set( RowLayout.filled().vertical().margins( uic.margins ).spacing( uic.spaceL ) );
            src.set( String.format( "website/%s/home?edit=true", state.account.permid.get() ) );

            // IFrame msg
            EventManager.instance()
                    .subscribe( (IFrameMsgEvent ev) -> onEditableClick( ev ) )
                    .performIf( IFrameMsgEvent.class, ev -> true )
                    .unsubscribeIf( () -> isDisposed() );

            // Entity submitted -> reload
            EventManager.instance()
                    .subscribe( (EntityLifecycleEvent ev) -> {
                        LOG.info( "Submitted: %s", ev.getSource() );
                        reload();
                        Platform.schedule( 750, () -> {
                            if (disposableChildState != null && !disposableChildState.isDisposed()) {
                                disposableChildState.disposeAction();
                            }
                        });
                    })
                    .performIf( EntityLifecycleEvent.class, ev -> ev.state == AFTER_SUBMIT )
                    .unsubscribeIf( () -> isDisposed() );
        }});
        return ui;
    }


    protected void onEditableClick( IFrameMsgEvent ev ) {
        LOG.info( "msg: %s", ev.msg );
        // article
        if (ev.msg.startsWith( "article." )) {
            state.uow.query( Article.class )
                    .where( Expressions.id( substringAfter( ev.msg, "." ) ) )
                    .singleResult()
                    .onSuccess( article -> {
                        disposableChildState = state.site.createState( new ArticleEditState() )
                                .putContext( article, State.Context.DEFAULT_SCOPE )
                                .activate();
                    });
        }
        // page.title -> settings
        else if (ev.msg.startsWith( "page." )) {
            state.site.createState( new TemplateConfigState() ).activate();
        }
        else {
            LOG.warn( "Unhandled msg: %s", ev.msg );
        }
    }

}
