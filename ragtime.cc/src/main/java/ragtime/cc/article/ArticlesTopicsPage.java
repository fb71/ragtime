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
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.polymap.model2.query.Query.Order;

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
import areca.ui.viewer.CellBuilder;
import areca.ui.viewer.ColorPickerViewer;
import areca.ui.viewer.CompositeListViewer;
import areca.ui.viewer.DrillingTreeLayout;
import areca.ui.viewer.SelectViewer;
import areca.ui.viewer.TextFieldViewer;
import areca.ui.viewer.TreeViewer;
import areca.ui.viewer.Viewer;
import areca.ui.viewer.ViewerContext;
import areca.ui.viewer.form.Form;
import areca.ui.viewer.model.LazyTreeModel;
import areca.ui.viewer.model.ListModelBase;
import areca.ui.viewer.model.ModelBase;
import areca.ui.viewer.model.Pojos;
import areca.ui.viewer.transform.Number2StringTransform;
import ragtime.cc.AssociationModel;
import ragtime.cc.EntityTransform;
import ragtime.cc.MainPage;
import ragtime.cc.UICommon;
import ragtime.cc.media.MediasPage.MediaListItem;
import ragtime.cc.model.Article;
import ragtime.cc.model.MediaEntity;
import ragtime.cc.model.TopicEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ArticlesTopicsPage
        implements CellBuilder<Object> {

    private static final Log LOG = LogFactory.getLog( ArticlesTopicsPage.class );

    public static final ClassInfo<ArticlesTopicsPage> INFO = ArticlesTopicsPageClassInfo.instance();

    protected static final DateFormat df = SimpleDateFormat.getDateTimeInstance( MEDIUM, MEDIUM, Locale.GERMAN );

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected ArticlesTopicsState state;

//    @Page.Context
//    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    private Map<Class<?>,ExpandableCell>  expanded = new HashMap<>();

    private Action            submitBtn;


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
                    .model( new ArticlesTopicsModel() )
                    .viewer( new TreeViewer<>() {{
                        treeLayout.set( new DrillingTreeLayout<>() );
                        cellBuilder.set( ArticlesTopicsPage.this );
                        lines.set( true );
                        oddEven.set( false );
                        exclusive.set( false );
                    }})
                    .createAndLoad() );
        }};
    }

    /**
     *
     */
    class ArticlesTopicsModel implements LazyTreeModel<Object> {

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


    //@Override
    public ListModelBase<?> buildModel( Object selected, TreeViewer<Object> viewer ) {
        // root
        if (selected == null) {
            return new EntityListModel<>( TopicEntity.class, () ->
                    state.uow.query( TopicEntity.class ).orderBy( TopicEntity.TYPE.order, Order.ASC ) );
        }
        // Topic
        else if (selected instanceof TopicEntity topic) {
            return new Pojos<Object>( new ArrayList<Object>() {{
                add( new TopicContent( topic ) );
                addAll( topic.articles().executeCollect().waitForResult().get() );
            }});
        }
        else if (selected instanceof TopicContent tc) {
            return new Pojos<Object>( new ArrayList<Object>() {{
                add( new TopicContentEdit( tc.topic ) );
            }});
        }
        // Article
        else if (selected instanceof Article article) {
            return new Pojos<>( new ArrayList<>() {{
                add( new ArticleContent( article ) );
                //addAll( topic.articles().executeCollect().waitForResult().get() );
            }});
        }
        else if (selected instanceof ArticleContent ac) {
            return new Pojos<>( singleton( new ArticleContentEdit( ac.article ) ) );
        }
        else {
            throw new RuntimeException( "Unhandled value type: " + selected );
        }
    }


    /** */
    protected static record TopicContent( TopicEntity topic ) { }

    /** */
    protected static record TopicContentEdit( TopicEntity topic ) { }

    /** */
    protected static record ArticleContent( Article article ) { }

    /** */
    protected static record ArticleContentEdit( Article article ) { }


    @Override
    @SuppressWarnings( "unchecked" )
    public UIComponent buildCell( int index, Object value, ModelBase model, Viewer viewer ) {
        // Topic
        if (value instanceof TopicEntity topic) {
            return new TopicCell( topic, (TreeViewer<Object>)viewer );
        }
        else if (value instanceof TopicContent tc) {
            return new TopicContentCell( tc, (TreeViewer<Object>)viewer );
        }
        else if (value instanceof TopicContentEdit tc) {
            return new TopicContentEditCell( tc, (TreeViewer<Object>)viewer );
        }
        // Article
        else if (value instanceof Article article) {
            return new ArticleCell( article, (TreeViewer<Object>)viewer );
        }
        else if (value instanceof ArticleContent ac) {
            return new ArticleContentCell( ac, (TreeViewer<Object>)viewer );
        }
        else if (value instanceof ArticleContentEdit ac) {
            return new ArticleContentEditCell( ac, (TreeViewer<Object>)viewer );
        }
        else {
            throw new RuntimeException( "Unhandled value type: " + value );
        }
    }

    /**
     *
     */
    public abstract class ExpandableCell
            extends UIComposite {

        static int HEIGHT = 54;

        static final RowConstraints RC = RowConstraints.height( HEIGHT ).width.set( HEIGHT );

        static final String     SECOND_LINE = "<br/><span style=\"font-size:10px; color:#808080;\">%s</span>";

        protected Button        icon, handle;

        protected UIComposite   content;

        protected void init( String _icon, String c, Object value, TreeViewer<Object> viewer, RConsumer<UIComposite> contentBuilder ) {
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
                    toggle( value, viewer, handle );
                });
            }});
            // handle
            handle = add( new Button() {{
                lc( RC );
                icon.set( "keyboard_arrow_down" );
                type.set( Type.NAVIGATE );
                events.on( EventType.SELECT, ev -> {
                    toggle( value, viewer, handle );
                });
            }});
        }

        private void toggle( Object value, TreeViewer<Object> viewer, Button btn ) {
            if (!viewer.isExpanded( value )) {
                expanded.put( value.getClass(), ExpandableCell.this );
                ExpandableCell.this.styles.add( CssStyle.of( "background-color", "#252525" ) );
                //ExpandableCell.this.styles.add( CssStyle.of( "font-weight", "bold" ) );
                //ExpandableCell.this.styles.add( CssStyle.of( "background-color", "var(--basic-accent2-color)" ) );
                btn.icon.set( "keyboard_arrow_up" );
                viewer.expand( value );
            }
            else {
                expanded.remove( value );
                ExpandableCell.this.styles.remove( CssStyle.of( "background-color", "" ) );
                ExpandableCell.this.styles.remove( CssStyle.of( "font-weight", "" ) );
                btn.icon.set( "keyboard_arrow_down" );
                viewer.collapse( value );
            }
        }

        protected Button addAction( Button btn ) {
            btn.lc( RC );
            btn.type.set( Button.Type.NAVIGATE );
            return components.add( 2, btn ).orElseError();
        }
    }

    /**
     *
     */
    protected class TopicCell extends ExpandableCell {

        protected TopicCell( TopicEntity topic, TreeViewer<Object> viewer ) {
            init( "topic", "#c96e5e", topic, viewer, container -> {
                container.tooltip.set( "Topic: " + topic.title.get() );
                container.add( new Text() {{
                    format.set( Format.HTML );
                    content.set( topic.title.get() + "<br/>..." );
                    topic.articles().executeCollect().onSuccess( articles -> {
                        content.set( topic.title.get() + SECOND_LINE.formatted( "Beiträge: " + articles.size() ) );
                    });
                }});
            });
        }
    }

    /**
     *
     */
    protected class TopicContentCell extends ExpandableCell {

        protected TopicContentCell( TopicContent tc, TreeViewer<Object> viewer ) {
            init( "edit", "#c96e5e", tc, viewer, container -> {
                container.add( new Text() {{
                    format.set( Format.HTML );
                    content.set( "Bearbeiten" + SECOND_LINE.formatted( "Topic '" + tc.topic.title.get() + "' bearbeiten..." ) );
//                    content.set( "Inhalt bearbeiten...<br/>" +
//                            "<span style=\"font-size:10px; color:#808080;\">" + abbreviate( tc.topic.description.get(), 50 ) + "</span>" );
                }});
            });
        }
    }

   /**
    *
    */
   protected class TopicContentEditCell extends UIComposite {

       private Form form;

       protected TopicContentEditCell( TopicContentEdit tc, TreeViewer<Object> viewer ) {
           layout.set( RowLayout.verticals().margins( 10, 22 ).spacing( 15 ).fillWidth( true ) );

           form = new Form();

           // title / color
           add( new UIComposite() {{
               lc( RowConstraints.height( 35 ) );
               layout.set( RowLayout.filled().spacing( 15 ) );

               add( form.newField().label( "Titel" )
                       .description( "Die interne, *eindeutige* Bezeichnung des Topics.\nACHTUNG!: Beim Ändern, ändert sich auch die URL des Topics!" )
                       .viewer( new TextFieldViewer() )
                       .model( new PermNameValidator( tc.topic,
                               new PropertyModel<>( tc.topic.title ) ) )
                       .create() );

               add( form.newField() //.label( "Farbe" )
                       .viewer( new ColorPickerViewer() )
                       .model( new PropertyModel<>( tc.topic.color ) )
                       .create()
                       .lc( RowConstraints.width( 50 ) ) );
           }});

           add( form.newField() //.label( "Beschreibung" )
                   .model( new PropertyModel<>( tc.topic.description ) )
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
                           new PropertyModel<>( tc.topic.order ) ) )
                   .create()
                   .lc( RowConstraints.height( 35 ) ) );

           // medias
           add( new UIComposite() {{
               //lc( RowConstraints.height( 100 ) );
               layout.set( RowLayout.verticals().fillWidth( true ).spacing( 5 ) );

               // add button
               add( new UIComposite() {{
                   lc( RowConstraints.height( 38 ) );
                   layout.set( RowLayout.filled().spacing( 15 ) );
                   add( new UIComposite() );
                   add( new Button() {{
                       lc( RowConstraints.width( 60 ) );
                       tooltip.set( "Bilder/Medien hinzufügen" );
                       icon.set( "add_photo_alternate" );
                       events.on( EventType.SELECT, ev -> {
                           //state.site.createState( new MediasSelectState( sel -> state.addMedias( sel ) ) ).activate();
                       });
                   }});
               }});

               // list
               var medias = new ViewerContext<>()
                       .viewer( new CompositeListViewer<MediaEntity>( media -> {
                           return new MediaListItem( media, () -> {} ); //state.removeMediaAction( media ) );
                       }) {{
                           oddEven.set( true );
                           spacing.set( 0 );
                           lines.set( true );
                           onSelect.set( media -> {
                               LOG.info( "SELECT: %s", media );
                           });
                           onLayout.set( c -> TopicContentEditCell.this.layout() );
                       }})
                       .model( new EntityAssocListModel<>( tc.topic.medias ) );
               add( medias.createAndLoad() );
           }});

           form.load();

           // submit
           var parentCell = expanded.get( TopicContent.class );
           parentCell.addAction( new Button() {{
               icon.set( UICommon.ICON_SAVE );
               tooltip.set( "Speichern" );
               enabled.set( false );
               events.on( EventType.SELECT, ev -> {
                   form.submit();
                   state.submitAction().onSuccess( __ -> {
                       submitBtn.enabled.set( false );
                   });
               });
               Runnable updateEnabled = () -> {
                   boolean _enabled = /*state.medias.modified() ||*/ (form.isChanged() && form.isValid() );
                   enabled.set( _enabled );
               };

               form.subscribe( ev -> updateEnabled.run() );
               //state.medias.subscribe( ev -> updateEnabled.run() );
           }});
       }
   }

   /**
    *
    */
   protected class ArticleCell extends ExpandableCell {

       protected ArticleCell( Article article, TreeViewer<Object> viewer ) {
           init( "description", "#5a88b9", article, viewer, container -> {
               container.add( new Text() {{
                   format.set( Format.HTML );
                   content.set( article.title.get() + "<br/>" +
                           "<span style=\"font-size:10px; color:#808080;\">" + df.format( article.modified.get() ) + "</span>" );
               }});
           });
       }
   }

   /**
    *
    */
   protected class ArticleContentCell extends ExpandableCell {

       protected ArticleContentCell( ArticleContent ac, TreeViewer<Object> viewer ) {
           init( "edit", "#5a88b9", ac, viewer, container -> {
               container.add( new Text() {{
                   format.set( Format.HTML );
                   content.set( "Bearbeiten" + SECOND_LINE.formatted( "Artikel '" + ac.article.title.get() + "' bearbeiten..." ) );
               }});
           });
       }
   }

   /**
    *
    */
   protected class ArticleContentEditCell extends UIComposite {

       private Form form;

       protected ArticleContentEditCell( ArticleContentEdit ac, TreeViewer<Object> viewer ) {
           layout.set( RowLayout.verticals().margins( 10, 22 ).spacing( 15 ).fillWidth( true ) );

           form = new Form();

           add( new UIComposite() {{
               lc( RowConstraints.height( 35 ) );
               layout.set( RowLayout.filled().spacing( 15 ) );

               var topics = new EntityTransform<>( state.uow, TopicEntity.class, TopicEntity.TYPE.title,
                       new AssociationModel<>( ac.article.topic ) );
               add( form.newField()
                       .viewer( new SelectViewer( topics.values(), "Wählen..." ) )
                       .model( topics )
                       .create()
                       .tooltip.set( "Das Topic dieses Textes" ) );

               add( form.newField().label( "Position" )
                       .description( "Die Position dieses Beitrags im Topic\nBeiträge ohne Angabe werden nach Modifikationsdatum sortiert" )
                       .viewer( new TextFieldViewer() )
                       .model( new Number2StringTransform(
                               new PropertyModel<>( ac.article.order ) ) )
                       .create()
                       .lc( RowConstraints.width( 80 ) ) );
           }});

           add( form.newField().label( "Name" )
                   .description( "Die interne, eindeutige Bezeichnung des Beitrags.\nACHTUNG: Beim Ändern, ändert sich auch die URL des Beitrages!" )
                   .viewer( new TextFieldViewer() )
                   .model( new PermNameValidator( ac.article,
                           new PropertyModel<>( ac.article.title ) ) )
                   .create()
                   .lc( RowConstraints.height( 35 ) ) );

           add( form.newField()
                   .model( new PropertyModel<>( ac.article.content ) )
                   .viewer( new TextFieldViewer().configure( (TextField t) -> {
                       t.multiline.set( true );
                       t.type.set( Type.MARKDOWN );
                       TextAutocomplete.process( t, state.uow );
                   }))
                   .create()
                   .lc( RowConstraints.height( 300 ) ) );
       }
   }

}
