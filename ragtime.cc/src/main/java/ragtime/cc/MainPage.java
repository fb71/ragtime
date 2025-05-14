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
package ragtime.cc;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.polymap.model2.runtime.Lifecycle.State.AFTER_SUBMIT;

import areca.common.Platform;
import areca.common.event.EventCollector;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.IFrame;
import areca.ui.component2.IFrame.IFrameMsgEvent;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import ragtime.cc.admin.AccountsState;
import ragtime.cc.article.ArticleEditState;
import ragtime.cc.article.ContentState;
import ragtime.cc.article.TopicEditState;
import ragtime.cc.model.Article;
import ragtime.cc.model.EntityLifecycleEvent;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.TemplateConfigState;

/**
 * The main page providing website and editing.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class MainPage {

    private static final Log LOG = LogFactory.getLog( MainPage.class );

    public static final ClassInfo<MainPage> INFO = MainPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected MainState         state;

    @Page.Context
    protected UICommon          uic;

    protected WebsitePart       websitePart = new WebsitePart();

//    @Page.Part
//    protected ArticlesTopicsPage treePart;

    private BaseState<?>        disposableChildState;


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        //site.prefWidth.set( 500 );

        ui.init( parent ); //.title.set( "Bearbeiten" );

        ui.body.layout.set( FillLayout.defaults() );
//        ui.body.layout.set( SwitcherLayout.defaults() );
//        ui.body.bgColor.set( Color.rgb( 36, 36, 35 ) ); // rotate over dark background

        ui.body.add( websitePart.create() );
//        ui.body.add( treePart.createAsPart() );

        // action: settings
        site.actions.add( new Action() {{
            //order.set( 10 );
            icon.set( "settings" );
            description.set( "Einstellungen" );
            handler.set( ev -> state.site.createState( new TemplateConfigState() ).activate() );
        }});
//        // action: refresh
//        site.actions.add( new Action() {{
//            icon.set( "refresh" );
//            description.set( "Web-Seite neu laden" );
//            handler.set( ev -> iframe.reload() );
//        }});
        // help
        HelpPage.addAction( MainPage.class, site );
        // accounts
        if (state.account.isAdmin.get()) {
            site.actions.add( new Action() {{
                order.set( 20 );
                icon.set( "manage_accounts" );
                description.set( "Accounts" );
                handler.set( ev -> state.site.createState( new AccountsState() ).activate() );
            }});
        }
        return ui;
    }


    /**
     *
     */
    protected class WebsitePart {

        public UIComponent create() {
            var container = new UIComposite();

            var iframe = new IFrame() {{
                src.set( String.format( "website/%s/home?edit=true", state.account.permid.get() ) );

                // IFrame msg
                EventManager.instance()
                        .subscribe( (IFrameMsgEvent ev) -> onEditableClick2( ev ) )
                        .performIf( IFrameMsgEvent.class, ev -> true ) //Pageflow.current().topPage() == WebsiteEditPage.this ) // FIXME
                        .unsubscribeIf( () -> isDisposed() );

                // Entity submitted -> reload
                var throttle = new EventCollector<>( 100 );
                EventManager.instance()
                        .subscribe( (EntityLifecycleEvent ev) -> {
                            LOG.info( "Submitted: %s", ev.getSource() );
                            throttle.collect( ev, evs -> {
                                reload();

                                Platform.schedule( 500, () -> {
                                    if (disposableChildState != null
                                            && !disposableChildState.isDisposed()
                                            && disposableChildState.page().orNull() == Pageflow.current().topPage()) {
                                        disposableChildState.disposeAction();
                                    }
                                });
                            });
                        })
                        .performIf( EntityLifecycleEvent.class, ev -> ev.state == AFTER_SUBMIT )
                        .unsubscribeIf( () -> isDisposed() );
            }};

            // check admin
            if (state.account.isAdmin.get()) {
                container.layout.set( RowLayout.filled().margins( 50, 100 ) );
                container.add( new Button() {{
                    type.set( Button.Type.NAVIGATE );
                    label.set( "Load..." );
                    events.on( EventType.SELECT, ev -> {
                        container.components.disposeAll();
                        container.layout.set( new FillLayout() );
                        container.add( iframe );
                        container.layout();

                        // XXX attempt to hide Page header and/or browser bar
                        //iframe.scrollIntoView.set( Vertical.TOP );
                    });
                }});
            }
            else {
                container.layout.set( new FillLayout() );
                container.add( iframe );
            }
            return container;
        }


        protected void onEditableClick2( IFrameMsgEvent ev ) {
            LOG.info( "IFrame: %s (%s)", ev.msg, state.account.email.get() );
            var id = substringAfter( ev.msg, "." );
            disposableChildState = state.site.createState( new ContentState() )
                    .activate();
        }


        protected void onEditableClick( IFrameMsgEvent ev ) {
            LOG.info( "IFrame: %s (%s)", ev.msg, state.account.email.get() );
            var id = substringAfter( ev.msg, "." );
            // article
            if (ev.msg.startsWith( "article." )) {
                state.uow.entity( Article.class, id ).onSuccess( article -> {
                    disposableChildState = state.site.createState( new ArticleEditState() )
                            .putContext( article, State.Context.DEFAULT_SCOPE )
                            .activate();
                });
            }
            // page.title -> settings
            else if (ev.msg.startsWith( "page." )) {
                disposableChildState = state.site.createState( new TemplateConfigState() ).activate();
            }
            // topic
            else if (ev.msg.startsWith( "topic." )) {
                state.uow.entity( TopicEntity.class, id ).onSuccess( topic -> {
                    disposableChildState = state.site.createState( new TopicEditState() )
                            .putContext( topic, State.Context.DEFAULT_SCOPE )
                            .activate();
                });
            }
            else {
                LOG.warn( "Unhandled msg: %s", ev.msg );
            }
        }

    }

}
