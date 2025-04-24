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

import java.util.Locale;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.Size;
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
import areca.ui.viewer.CompositeListViewer;
import areca.ui.viewer.ViewerContext;
import ragtime.cc.model.TopicEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TopicsPage {

    private static final Log LOG = LogFactory.getLog( TopicsPage.class );

    public static final ClassInfo<TopicsPage> INFO = TopicsPageClassInfo.instance();

    protected static final DateFormat df = SimpleDateFormat.getDateTimeInstance( MEDIUM, MEDIUM, Locale.GERMAN );

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected TopicsState       state;

//    @Page.Context
//    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    private ScrollableComposite list;


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Topics" );

        // action: new
        site.actions.add( new Action() {{
            icon.set( "add" );
            description.set( "Neues Topic anlegen" );
            handler.set( ev -> state.createTopicAction() );
        }});

        ui.body.layout.set( RowLayout.filled().vertical().margins( Size.of( 22, 22 ) ).spacing( 15 ) );

//        // search
//        ui.body.add( new TextField() {{
//            layoutConstraints.set( RowConstraints.height( 35 ) );
//            content.set( state.searchTxt.get() );
//            events.on( EventType.TEXT, ev -> {
//                state.searchTxt.set( content.get() );
//            });
//        }});

        // list
        ui.body.add( new ScrollableComposite() {{
            layout.set( FillLayout.defaults() );

            add( new ViewerContext<>()
                    .viewer( new CompositeListViewer<TopicEntity>( ListItem::new ) {{
                        etag.set( topic -> topic.modified.get() );
                        lines.set( true );
                        oddEven.set( true );
                        onSelect.set( topic -> {
                            state.selected.set( topic );
                            state.editTopicAction.run();
                        });
                    }})
                    .model( state.topics )
                    .createAndLoad() );
        }});
        return ui;
    }


    /**
     *
     */
    protected class ListItem extends UIComposite {

        protected ListItem( TopicEntity topic ) {
            lc( RowConstraints.height( 54 ) );
            layout.set( RowLayout.filled().margins( 10, 10 ) );
            add( new Text() {{
                format.set( Format.HTML );
                content.set( topic.title.get() + "<br/>" +
                        "<span style=\"font-size:10px; color:#808080;\">" + df.format( topic.modified.get() ) + "</span>" );
            }});
        }
    }

}
