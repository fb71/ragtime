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

import static java.text.DateFormat.MEDIUM;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import areca.common.base.Consumer.RConsumer;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.Color;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Events.UIEvent;
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
import areca.ui.viewer.CellBuilder;
import areca.ui.viewer.DrillingTreeLayout2;
import areca.ui.viewer.TreeViewer;
import areca.ui.viewer.Viewer;
import areca.ui.viewer.ViewerContext;
import areca.ui.viewer.model.ModelBase;
import ragtime.cc.UICommon;
import ragtime.cc.article.ContentState.ArticleContent;
import ragtime.cc.article.ContentState.ArticleContentEdit;
import ragtime.cc.article.ContentState.MediaContent;
import ragtime.cc.article.ContentState.MediaContentEdit;
import ragtime.cc.article.ContentState.TopicContent;
import ragtime.cc.article.ContentState.TopicContentEdit;
import ragtime.cc.media.MediaCell;
import ragtime.cc.media.MediaContentCell;
import ragtime.cc.web.WebsiteEditPage.WebsiteEditEvent;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ContentPage
        implements CellBuilder<Object> {

    private static final Log LOG = LogFactory.getLog( ContentPage.class );

    public static final ClassInfo<ContentPage> INFO = ContentPageClassInfo.instance();

    protected static final DateFormat df = SimpleDateFormat.getDateTimeInstance( MEDIUM, MEDIUM, Locale.GERMAN );

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected ContentState      state;

//    @Page.Context
//    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    private Action              submitBtn;

    private Map<String,Callable<Boolean>> saveActions = new HashMap<>();

    private boolean             modelChanged;

    protected TreeViewer<Object> tree;

    private Action discardBtn;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Inhalte" );

//        Platform.schedule( 2500, () -> {
//            var path = new ArrayList<>();
//            state.uow
//                    .query( TopicEntity.class ).executeCollect()
//                    .then( rs -> {
//                        path.add( rs.get( 0 ) );
//                        return rs.get( 0 ).articles().executeCollect();
//                    })
//                    .onSuccess( rs -> {
//                        path.add( rs.get( 0 ) );
//                        tree.expandPath( path.toArray() );
//                    });
//        });
//
//        Platform.schedule( 6000, () -> {
//            System.out.println( "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n" );
//            var path = new Object[2];
//            state.uow
//                    .query( TopicEntity.class ).executeCollect()
//                    .then( rs -> {
//                        path[0] = rs.get( 1 );
//                        return rs.get( 1 ).articles().executeCollect();
//                    })
//                    .onSuccess( rs -> {
//                        path[1] = rs.get( 0 );
//                        tree.expandPath( path );
//                    });
//        });

        // action: discard
        site.actions.add( discardBtn = new Action() {{
            description.set( "Änderungen zurücksetzen" );
            type.set( Button.Type.ACTION );
            enabled.set( false );
            //
            handler.set( ev -> {
                state.discardAction().onSuccess( __ -> {
                    modelChanged = false;
                    saveActions.clear();
                    updateSaveEnabled();
                });
            });
        }});
        // action: submit
        site.actions.add( new Action() {{
            submitBtn = this;
            description.set( "Änderungen speichern" );
            //icon.set( UICommon.ICON_SAVE );
            type.set( Button.Type.SUBMIT );
            enabled.set( false );
            //
            handler.set( ev -> {
                for (var action : saveActions.values()) {
                    action.call();
                }
                state.submitAction().onSuccess( __ -> {
                    modelChanged = false;
                    saveActions.clear();
                    updateSaveEnabled();
                });
            });
            // listen to model changes
            state.contentModel
                    .subscribe( ev -> { modelChanged = true; updateSaveEnabled(); })
                    .unsubscribeIf( () -> site.isClosed() );
        }});
        // action: new
        site.actions.add( new Action() {{
            icon.set( "add" );
            description.set( "Neues Topic anlegen" );
            handler.set( ev -> {
                state.createNewTopic();
            });
        }});

        ui.body.layout.set( RowLayout.filled().vertical().margins( 0, 0 ).spacing( 15 ) );

        // TreeViewer
        ui.body.add( new ScrollableComposite() {{
            layout.set( FillLayout.defaults() );

            add( new ViewerContext<>()
                    .model( state.contentModel )
                    .viewer( tree = new TreeViewer<>() {{
                        treeLayout.set( new DrillingTreeLayout2<>() );
                        cellBuilder.set( ContentPage.this );
                        lines.set( true );
                        oddEven.set( false );
                        exclusive.set( false );
                    }})
                    .createAndLoad() );
        }});

        // Website navigation
        EventManager.instance()
                .subscribe( (WebsiteEditEvent ev) -> onWebsiteEditEvent( ev ) )
                .performIf( WebsiteEditEvent.class, ev -> ev.getSource() != ContentPage.this )
                .unsubscribeIf( () -> site.isClosed() );
        return ui;
    }


    protected void onWebsiteEditEvent( WebsiteEditEvent ev ) {
        // topic
        ev.topic().ifPresent( topic -> {
            tree.expandPath( state.contentType( topic ) );
        });
        // article
        ev.article().ifPresent( article -> {
            article.topic.fetch().onSuccess( topic -> {
                tree.expandPath( state.contentType( topic ), state.contentType( article ) );
            });
        });
    }


    protected void updateSaveEnabled() {
        if (modelChanged || !saveActions.isEmpty()) {
            submitBtn.enabled.set( true );
            submitBtn.icon.set( UICommon.ICON_SAVE );
            discardBtn.enabled.set( true );
            discardBtn.icon.set( "undo" );
        }
        else {
            submitBtn.enabled.set( false);
            discardBtn.enabled.set( false);
            discardBtn.icon.set( null );
        }
    }


    protected void registerSaveAction( String name, Callable<Boolean> action ) {
        LOG.debug( "registerSaveAction(): %s", name );
        saveActions.put( name, action );
        //Assert.isNull( previous );
        updateSaveEnabled();
    }


    protected void removeSaveAction( String name ) {
        LOG.debug( "removeSaveAction(): %s", name );
        saveActions.remove( name );
        updateSaveEnabled();
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public UIComponent buildCell( int index, Object value, ModelBase model, Viewer viewer ) {
        var result = (ContentPageCell)null;

        // Topic
        if (value instanceof TopicContentEdit tc) {
            result = new TopicContentEditCell( tc );
        }
        else if (value instanceof TopicContent topic) {
            result = new TopicCell();
        }
        // Article
        else if (value instanceof ArticleContentEdit ac) {
            result = new ArticleContentEditCell();
        }
        else if (value instanceof ArticleContent ac) {
            result = new ArticleCell();
        }
        // Media
        else if (value instanceof MediaContentEdit media) {
            result = new MediaContentCell();
        }
        else if (value instanceof MediaContent media) {
            result = new MediaCell();
        }
        else {
            throw new RuntimeException( "Unhandled value type: " + value );
        }
        result.value = value;
        result.viewer = (TreeViewer<Object>)viewer;
        result.page = this;
        result.pageSite = site;
        result.state = state;
        result.create();
        return result;
    }


    /**
     *
     * @param <V> The type of the model value.
     */
    public static abstract class ContentPageCell<V>
            extends UIComposite {

        /** The model value. */
        protected V                     value;

        protected TreeViewer<Object>    viewer;

        protected ContentPage           page;

        protected PageSite              pageSite;

        protected ContentState          state;

        protected abstract void create();

        /**
         * Adds the given {@link Button} to the action area (toolbar) of this cell.
         * @return
         */
        protected Button addAction( Button btn ) {
            LOG.warn( "addAction(): not yet..." );
            return btn;
        }

        protected void removeAction( Button btn ) {
            LOG.warn( "removeAction(): not yet..." );
        }

        protected void registerSaveAction( Callable<Boolean> action ) {
            page.registerSaveAction( getClass().getName(), action );
        }

        protected void removeSaveAction() {
            page.removeSaveAction( getClass().getName() );
        }

        @Override
        public void dispose() {
            removeSaveAction();
            super.dispose();
        }
    }


    /**
     *
     */
    public static abstract class ExpandableCell<V>
            extends ContentPageCell<V>
            implements TreeViewer.ExpandableCell {

        public static final int     HEIGHT = 54;

        public static final String  SECOND_LINE = "<br/><span class=\"SecondLine\">%s</span>";

        static final RowConstraints RC = RowConstraints.height( HEIGHT ).width.set( HEIGHT );

        protected Button        icon, handle;

        protected UIComposite   content;

        protected boolean       expandable = true;

        protected void create( String _icon, String c, RConsumer<UIComposite> contentBuilder ) {
            lc( RowConstraints.height( HEIGHT ));
            lm( RowLayout.defaults().fillWidth( true ).margins( 5, -1 ).spacing( 5 ) );

            // icon
            icon = add( new Button() {{
                lc( RC );
                icon.set( _icon );
                type.set( Type.NAVIGATE );
                cssClasses.add( "Action" );
                color.set( Color.ofHex( c ));
            }});
            // content
            content = add( new UIComposite() {{
                lm( RowLayout.filled().margins( 0, 11 ) );
                contentBuilder.accept( this );
                if (expandable) {
                    events.on( EventType.CLICK, ev -> {
                        viewer.toggle( value ).onSuccess( expanded -> {
                            onClick( ev, expanded );
                        });
                    });
                }
            }});
            // handle
            handle = add( new Button() {{
                lc( RC );
                type.set( Type.NAVIGATE );
                cssClasses.add( "Action" );
                if (expandable) {
                    icon.set( "keyboard_arrow_down" );
                    events.on( EventType.SELECT, ev -> {
                        viewer.toggle( value ).onSuccess( expanded -> {
                            onClick( ev, expanded );
                        });
                    });
                }
                else {
                    enabled.set( false );
                }
            }});
            if (expandable) {
                cssClasses.add( "Clickable" );
            }
        }

        @Override
        public void updateExpand( boolean expanded ) {
            if (expanded) {
                cssClasses.add( "Expanded" );
                handle.icon.set( "keyboard_arrow_down" );
                onExpand();
            }
            else {
                cssClasses.remove( "Expanded" );
                handle.icon.set( "keyboard_arrow_up" );
                onCollapse();
            }
        }

        protected void onClick( UIEvent ev, boolean expanded ) {}

        protected void onExpand() {}

        protected void onCollapse() {}

        @Override
        protected Button addAction( Button btn ) {
            btn.lc( RC );
            btn.type.set( Button.Type.NAVIGATE );
            btn.cssClasses.add( "Action" );
            components.add( 2, btn ).orElseError();
            return btn;
        }

        @Override
        protected void removeAction( Button btn ) {
            components.remove( btn );
            //Assert.that( components.remove( btn ).isPresent() );
        }
    }


    /**
     *
     */
    protected class TopicContentCell
            extends ExpandableCell<TopicContent> {

        @Override
        protected void create() {
            create( "edit", "#c96e5e", container -> {
                container.add( new Text() {{
                    format.set( Format.HTML );
                    content.set( "Bearbeiten" + SECOND_LINE.formatted( "Topic '" + value.topic().title.get() + "' bearbeiten..." ) );
//                    content.set( "Inhalt bearbeiten...<br/>" +
//                            "<span style=\"font-size:10px; color:#808080;\">" + abbreviate( tc.topic.description.get(), 50 ) + "</span>" );
                }});
            });
        }
    }


//    /**
//     *
//     */
//    protected class ArticleContentCell
//            extends ExpandableCell<ArticleContent> {
//
//        @Override
//        protected void create() {
//            create( "edit", "#5a88b9", container -> {
//                container.add( new Text() {{
//                    format.set( Format.HTML );
//                    content.set( "Bearbeiten" + SECOND_LINE.formatted( "Artikel '" + value.article().title.get() + "' bearbeiten..." ) );
//                }});
//            });
//        }
//    }

}
