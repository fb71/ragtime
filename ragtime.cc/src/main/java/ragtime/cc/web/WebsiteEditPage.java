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
package ragtime.cc.web;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.polymap.model2.runtime.Lifecycle.State.AFTER_SUBMIT;

import java.util.EventObject;
import java.util.regex.Pattern;

import areca.common.MutableInt;
import areca.common.Platform;
import areca.common.Timer;
import areca.common.base.Opt;
import areca.common.base.Sequence;
import areca.common.event.EventCollector;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.NoRuntimeInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.Color;
import areca.ui.Position;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.IFrame;
import areca.ui.component2.IFrame.IFrameMsgEvent;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComponent.CssStyle;
import areca.ui.component2.UIComposite;
import areca.ui.layout.AbsoluteLayout;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import ragtime.cc.BaseState;
import ragtime.cc.HelpPage;
import ragtime.cc.UICommon;
import ragtime.cc.admin.AccountsState;
import ragtime.cc.article.ContentState;
import ragtime.cc.media.MediasState;
import ragtime.cc.model.Article;
import ragtime.cc.model.Common;
import ragtime.cc.model.EntityLifecycleEvent;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.model.TemplateConfigEntity;

/**
 * In-place editing of content and website/template config.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class WebsiteEditPage {

    private static final Log LOG = LogFactory.getLog( WebsiteEditPage.class );

    public static final ClassInfo<WebsiteEditPage> INFO = WebsiteEditPageClassInfo.instance();

    public static final Pattern IFRAME_MSG_PATTERN = Pattern.compile( "([a-z]+)[.](-?[0-9a-z]+):?([a-z]*)" );

    protected static final String WEBSITE_URL = "website/%s/%s?edit=true";


    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected PageSite          site;

    @Page.Context
    protected WebsiteEditState  state;

    @Page.Context
    protected UICommon          uic;

    private BaseState<?>        disposableChildState;

    private IFrameWithEvents    iframe;

    /**
     *
     */
    protected class IFrameWithEvents {

        @SuppressWarnings( "hiding" )
        protected IFrame    iframe = new IFrame();

        protected Timer     skipWebsiteEditEvent = Timer.start();

        public IFrameWithEvents() {
            // IFrame msg
            EventManager.instance()
                    .subscribe( (IFrameMsgEvent ev) -> onIFrameEvent( ev ) )
                    .performIf( IFrameMsgEvent.class, ev -> skipWebsiteEditEvent.elapsedMillis() > 2500 ) //Pageflow.current().topPage() == WebsiteEditPage.this )
                    .unsubscribeIf( () -> iframe.isDisposed() );

            // WebsiteEditEvent from ContentPage
            EventManager.instance()
                    .subscribe( (WebsiteEditEvent ev) -> onContentPageTree( ev ) )
                    .performIf( WebsiteEditEvent.class, ev -> ev.getSource() != WebsiteEditPage.this )
                    .unsubscribeIf( () -> site.isClosed() );

            // Entity submitted -> reload
            var throttle = new EventCollector<>( 100 );
            EventManager.instance()
                    .subscribe( (EntityLifecycleEvent ev) -> {
                        LOG.info( "Submitted: %s", ev.getSource() );
                        throttle.collect( ev, evs -> {
                            reload();

//                            Platform.schedule( 500, () -> {
//                                if (disposableChildState != null
//                                        && !disposableChildState.isDisposed()
//                                        && disposableChildState.page().orNull() == Pageflow.current().topPage()) {
//                                    disposableChildState.disposeAction();
//                                }
//                            });
                        });
                    })
                    .performIf( EntityLifecycleEvent.class, ev -> ev.state == AFTER_SUBMIT )
                    .unsubscribeIf( () -> iframe.isDisposed() );
        }

        public void reload() {
            skipWebsiteEditEvent.restart();
            iframe.reload();
        }

        public void setSrc( String url ) {
            skipWebsiteEditEvent.restart();
            iframe.src.set( url );
        }
    }


    @Page.CreateUI
    public UIComponent createUI( UIComposite parent ) {
        site.prefWidth.set( 800 ).minWidth.set( 550 );
        ui.init( parent ); //.title.set( "Bearbeiten" );

        // check admin
        if (state.account.isAdmin.get()) {
            ui.body.layout.set( RowLayout.filled().margins( 50, 100 ) );
            ui.body.add( new Button() {{
                type.set( Button.Type.NAVIGATE );
                label.set( "Load..." );
                events.on( EventType.SELECT, ev -> {
                    iframe = new IFrameWithEvents() {{
                        setSrc( WEBSITE_URL.formatted( state.account.permid.get(), "home" ) );
                    }};
                    ui.body.components.disposeAll();
                    ui.body.layout.set( new BrowserLayout() );
                    ui.body.add( iframe.iframe );
                    ui.body.layout();

                    // XXX attempt to hide Page header and/or browser bar
                    //iframe.scrollIntoView.set( Vertical.TOP );
                });
            }});
        }
        else {
            iframe = new IFrameWithEvents() {{
                setSrc( WEBSITE_URL.formatted( state.account.permid.get(), "home" ) );
            }};
            ui.body.layout.set( new BrowserLayout() );
            ui.body.add( iframe.iframe );
        }

        // set background for reload transition/blending
        state.uow.query( TemplateConfigEntity.class ).singleResult().onSuccess( config -> {
            ui.body.bgColor.set( Color.ofHex( config.colors.get().pageBackground.get() ) );
        });

        // action: articles
        site.actions.add( new Action() {{
            icon.set( "article" );
            description.set( "Inhalte" );
            handler.set( ev -> state.site.createState( new ContentState() ).activate() );
        }});
        // action: medias
        site.actions.add( new Action() {{
            icon.set( "image" );
            description.set( "Bilder und Medien" );
            handler.set( ev -> state.site.createState( new MediasState() ).activate() );
        }});
//        // action: topics
//        site.actions.add( new Action() {{
//            icon.set( "topic" );
//            description.set( "Topics" );
//            handler.set( ev -> state.site.createState( new TopicsState() ).activate() );
//        }});
        // action: settings
        site.actions.add( new Action() {{
            //order.set( 10 );
            icon.set( "settings" );
            description.set( "Einstellungen" );
            handler.set( ev -> state.site.createState( new TemplateConfigState() ).activate() );
        }});
        // action: refresh
        site.actions.add( new Action() {{
            icon.set( "refresh" );
            description.set( "Web-Seite neu laden" );
            handler.set( ev -> iframe.reload() );
        }});
        // help
        HelpPage.addAction( WebsiteEditPage.class, site );
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


    @NoRuntimeInfo
    protected void onContentPageTree( WebsiteEditEvent ev ) {
        var permName = "";
        if (ev.edited == null) {
            permName = "home";
        }
        else if (ev.edited instanceof TopicEntity topic) {
            permName = topic.permName.get();
        }
        else if (ev.edited instanceof Article article) {
            permName = article.permName.get();
        }
        else {
            throw new RuntimeException( "Unhandled WebsiteEditEvent: " + ev );
        }
        var _permName = permName;
        iframe.iframe.styles.add( CssStyle.of( "transition", "all 0.3s" ) );
        Platform.schedule( 1250, () -> {
            iframe.iframe.opacity.set( 0f );
            iframe.setSrc( WEBSITE_URL.formatted( state.account.permid.get(), _permName ) );
            Platform.schedule( 500, () -> {
                iframe.iframe.opacity.set( 1f );
            });
        });
    }


    @NoRuntimeInfo
    protected void onIFrameEvent( IFrameMsgEvent ev ) {
        LOG.info( "IFrame: '%s' (%s)", ev.msg, state.account.email.get() );
        var match = IFRAME_MSG_PATTERN.matcher( ev.msg );
        match.matches();
        var type = match.group( 1 );
        var id = match.group( 2 );
        var action = defaultIfEmpty( match.group( 3 ), "clicked" );

        var delay = MutableInt.of( 0 );
        if (action.equals( "clicked" ) && (disposableChildState == null || disposableChildState.isDisposed())) {
            disposableChildState = state.site.createState( new ContentState() ).activate();
            delay.set( 2000 );
        }
        else if (action.equals( "clicked" )) {
            delay.set( 0 ); // clicked and ContentPage already present
        }
        else if (action.equals( "loaded" )) {
            delay.set( 500 );
        }

        // article
        if (type.equals( "article" )) {
            var t = Timer.start();
            state.uow.entity( Article.class, id ).onSuccess( article -> {
                Platform.schedule( t.remainingMillis( delay.get() ), () -> {
                    EventManager.instance().publish( new WebsiteEditEvent( WebsiteEditPage.this, article ) );
                });
            });
        }
        // topic
        else if (type.equals( "topic" )) {
            var t = Timer.start();
            state.uow.entity( TopicEntity.class, id ).onSuccess( topic -> {
                Platform.schedule( t.remainingMillis( delay.get() ), () -> {
                    EventManager.instance().publish( new WebsiteEditEvent( WebsiteEditPage.this, topic ) );
                });
            });
        }
        // page.title -> settings
        else if (ev.msg.startsWith( "page." )) {
            disposableChildState = state.site.createState( new TemplateConfigState() ).activate();
        }
        else {
            LOG.warn( "Unhandled msg: %s", ev.msg );
        }

    }


    /**
     *
     */
    public static class WebsiteEditEvent
            extends EventObject {

        public Common    edited;

        /**
         *
         * @param source The {@link Page} that fired the event.
         * @param edited The object that is edited/navigated to.
         */
        public WebsiteEditEvent( Object source, Common edited ) {
            super( source );
            this.edited = edited;
        }

        @SuppressWarnings( "unchecked" )
        public <R> Opt<R> sourceOfType( Class<R> type ) {
            return type.isAssignableFrom( source.getClass() ) ? Opt.of( (R)source ) : Opt.absent();
        }


        public Opt<Article> article() {
            return Opt.of( edited instanceof Article article ? article : null );
        }

        public Opt<TopicEntity> topic() {
            return edited instanceof TopicEntity topic ?  Opt.of( topic ): Opt.absent();
        }
    }

    /**
     *
     */
    public static class BrowserLayout
            extends AbsoluteLayout {

        @Override
        public void layout( UIComposite composite ) {
            super.layout( composite );
            composite.clientSize.opt().ifPresent( size -> {
                var component = Sequence.of( composite.components.get() ).single();

                // on phone oversize so that browser bar and Page header can be scrolled out view
//                var height = size.width() <= 450 && size.height() <= 750
//                        ? size.height() + 110
//                        : size.height();
                var height = size.height();

                component.size.set( Size.of( size.width(), height ) );
                component.position.set( Position.of( 0, 0 ) );
            });
        }
    }

}
