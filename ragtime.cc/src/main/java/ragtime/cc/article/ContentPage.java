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

import static areca.common.Scheduler.Priority.BACKGROUND;
import static java.text.DateFormat.MEDIUM;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.polymap.model2.query.Query.Order;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.Color;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
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
import areca.ui.viewer.DrillingTreeLayout;
import areca.ui.viewer.TreeViewer;
import areca.ui.viewer.Viewer;
import areca.ui.viewer.ViewerContext;
import areca.ui.viewer.model.LazyTreeModel;
import areca.ui.viewer.model.ModelBase;
import ragtime.cc.MainPage;
import ragtime.cc.model.Article;
import ragtime.cc.model.TopicEntity;

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
    protected PageContainer ui;

    @Page.Context
    protected ContentState  state;

//    @Page.Context
//    protected UICommon          uic;

    @Page.Context
    protected PageSite      site;

    private Map<Class<?>,ExpandableCell>  expanded = new HashMap<>();

    private Action          submitBtn;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Inhalte" );

        // action: new
        site.actions.add( new Action() {{
            icon.set( "add" );
            description.set( "Neues Topic anlegen" );
            //handler.set( ev -> state.createTopicAction() );
        }});

        ui.body.layout.set( RowLayout.filled().vertical().margins( Size.of( 10, 10 ) ).spacing( 15 ) );

//        // search
//        ui.body.add( new TextField() {{
//            layoutConstraints.set( RowConstraints.height( 35 ) );
//            content.set( state.searchTxt.get() );
//            events.on( EventType.TEXT, ev -> {
//                state.searchTxt.set( content.get() );
//            });
//        }});

        ui.body.add( createAsPart() );
        return ui;
    }

    /**
     * Init as a part of {@link MainPage}.
     */
    public UIComponent createAsPart() {
        return new ScrollableComposite() {{
            layout.set( FillLayout.defaults() );

            add( new ViewerContext<>()
                    .model( new ContentModel() )
                    .viewer( new TreeViewer<>() {{
                        treeLayout.set( new DrillingTreeLayout<>() );
                        cellBuilder.set( ContentPage.this );
                        lines.set( true );
                        oddEven.set( false );
                        exclusive.set( false );
                    }})
                    .createAndLoad() );
        }};
    }


    /** */
    protected static record TopicContent( TopicEntity topic ) { }

    /** */
    static record TopicContentEdit( TopicEntity topic ) { }

    /** */
    protected static record ArticleContent( Article article ) { }

    /** */
    protected static record ArticleContentEdit( Article article ) { }


    /**
     *
     */
    class ContentModel implements LazyTreeModel<Object> {

        @Override
        public Promise<List<? extends Object>> loadChildren( Object item, int first, int max ) {
            // root
            if (item == null) {
                return state.uow.query( TopicEntity.class )
                        .orderBy( TopicEntity.TYPE.order, Order.ASC )
                        .executeCollect().map( ArrayList::new ); // XXX
            }
            // Topic
            else if (item instanceof TopicEntity topic) {
                return topic.articles().executeCollect()
                        .map( ArrayList<Object>::new )
                        .map( rs -> add( rs, new TopicContent( topic ) ) );
            }
            else if (item instanceof TopicContent tc) {
                return Promise.completed( singletonList( new TopicContentEdit( tc.topic ) ), BACKGROUND );
            }
            // Article
            else if (item instanceof Article article) {
                return Promise.completed( singletonList( new ArticleContentEdit( article ) ), BACKGROUND );
            }
//            else if (item instanceof ArticleContent ac) {
//                return Promise.completed( singletonList( new ArticleContentEdit( ac.article ) ), BACKGROUND );
//            }
            else {
                throw new RuntimeException( "Unhandled value type: " + item );
            }
        }

        protected <R> List<R> add( List<R> l, R elm) {
            l.add( 0, elm );
            return l;
        }
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public UIComponent buildCell( int index, Object value, ModelBase model, Viewer viewer ) {
        var result = (ContentPageCell)null;

        // Topic
        if (value instanceof TopicEntity topic) {
            result = new TopicCell() {{ value = topic; }};
        }
        else if (value instanceof TopicContent tc) {
            result = new TopicContentCell() {{ value = tc; }};
        }
        else if (value instanceof TopicContentEdit tc) {
            result = new TopicContentEditCell( tc );
        }
        // Article
        else if (value instanceof Article article) {
            result = new ArticleCell() {{ value = article; }};
        }
        else if (value instanceof ArticleContent ac) {
            result = new ArticleContentCell() {{ value = ac; }};
        }
        else if (value instanceof ArticleContentEdit ac) {
            result = new ArticleContentEditCell() {{ value = ac; }};
        }
        else {
            throw new RuntimeException( "Unhandled value type: " + value );
        }
        result.viewer = (TreeViewer<Object>)viewer;
        result.pageSite = site;
        result.uow = state.uow;
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

        protected PageSite              pageSite;

        protected UnitOfWork            uow;

        protected abstract void create();

        protected void addAction( Button btn ) {
            throw new RuntimeException( "not yet..." );
        }

        protected Promise<Submitted> submit() {
            return uow.submit(); // XXX
        }
    }


    /**
     *
     */
    public static abstract class ExpandableCell<V>
            extends ContentPageCell<V> {

        static final int        HEIGHT = 54;

        static final RowConstraints RC = RowConstraints.height( HEIGHT ).width.set( HEIGHT );

        static final String     SECOND_LINE = "<br/><span style=\"font-size:10px; color:#8a8a8a;\">%s</span>";

        protected Button        icon, handle;

        protected UIComposite   content;

        protected void create( String _icon, String c, RConsumer<UIComposite> contentBuilder ) {
            cssClasses.add( "Clickable" );
            lc( RowConstraints.height( HEIGHT ));
            lm( RowLayout.defaults().fillWidth( true ).spacing( 5 ) );

            if (viewer.isExpanded( value )) {
                styles.add( CssStyle.of( "background-color", "#252525" ) );
            }
            // icon
            icon = add( new Button() {{
                lc( RC );
                icon.set( _icon );
                type.set( Type.NAVIGATE );
                color.set( Color.ofHex( c ));
                //styles.add( CssStyle.of( "color", "#808080" ) );
            }});
            // content
            content = add( new UIComposite() {{
                lm( RowLayout.filled().margins( 0, 10 ) );
                contentBuilder.accept( this );
                events.on( EventType.CLICK, ev -> {
                    toggle( handle );
                });
            }});
            // handle
            handle = add( new Button() {{
                lc( RC );
                icon.set( "keyboard_arrow_down" );
                type.set( Type.NAVIGATE );
                events.on( EventType.SELECT, ev -> {
                    toggle( handle );
                });
            }});
        }

        private void toggle( Button btn ) {
            if (!viewer.isExpanded( value )) {
                //expanded.put( value.getClass(), ExpandableCell.this );
                ExpandableCell.this.styles.add( CssStyle.of( "background-color", "#252525" ) );
                //ExpandableCell.this.styles.add( CssStyle.of( "font-weight", "bold" ) );
                //ExpandableCell.this.styles.add( CssStyle.of( "background-color", "var(--basic-accent2-color)" ) );
                btn.icon.set( "keyboard_arrow_up" );
                viewer.expand( value );
            }
            else {
                //expanded.remove( value );
                ExpandableCell.this.styles.remove( CssStyle.of( "background-color", "" ) );
                ExpandableCell.this.styles.remove( CssStyle.of( "font-weight", "" ) );
                btn.icon.set( "keyboard_arrow_down" );
                viewer.collapse( value );
            }
        }

        @Override
        protected void addAction( Button btn ) {
            btn.lc( RC );
            btn.type.set( Button.Type.NAVIGATE );
            components.add( 2, btn ).orElseError();
        }
    }


    /**
     *
     */
    class TopicCell
            extends ExpandableCell<TopicEntity> {

        @Override
        protected void create() {
            create( "topic", "#c96e5e", container -> {
                container.tooltip.set( "Topic: " + value.title.get() );
                container.add( new Text() {{
                    format.set( Format.HTML );
                    content.set( value.title.get() + "<br/>..." );
                    value.articles().executeCollect().onSuccess( articles -> {
                        content.set( value.title.get() + SECOND_LINE.formatted( "Beiträge: " + articles.size() ) );
                    });
                }});
            });
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
                    content.set( "Bearbeiten" + SECOND_LINE.formatted( "Topic '" + value.topic.title.get() + "' bearbeiten..." ) );
//                    content.set( "Inhalt bearbeiten...<br/>" +
//                            "<span style=\"font-size:10px; color:#808080;\">" + abbreviate( tc.topic.description.get(), 50 ) + "</span>" );
                }});
            });
        }
    }

    /**
     *
     */
    protected class ArticleCell
            extends ExpandableCell<Article> {

        @Override
        protected void create() {
            create( "description", "#5a88b9", container -> {
                container.add( new Text() {{
                    format.set( Format.HTML );
                    content.set( value.title.get() + "<br/>" +
                            "<span style=\"font-size:10px; color:#808080;\">" + df.format( value.modified.get() ) + "</span>" );
                }});
            });
        }
    }

    /**
     *
     */
    protected class ArticleContentCell
            extends ExpandableCell<ArticleContent> {

        @Override
        protected void create() {
            create( "edit", "#5a88b9", container -> {
                container.add( new Text() {{
                    format.set( Format.HTML );
                    content.set( "Bearbeiten" + SECOND_LINE.formatted( "Artikel '" + value.article.title.get() + "' bearbeiten..." ) );
                }});
            });
        }
    }

}
