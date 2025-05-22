/*
 * Copyright (C) 2025, the @authors. All rights reserved.
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

import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Text;
import areca.ui.statenaction.State;
import ragtime.cc.UICommon;
import ragtime.cc.article.ContentPage.ExpandableCell;
import ragtime.cc.model.TopicEntity;

/**
 *
 * @author Falko Bräutigam
 */
class TopicCell
        extends ExpandableCell<TopicEntity> {

    @Override
    protected void create() {
        create( "topic", "#c96e5e", container -> {
            container.tooltip.set( "Thema: " + value.title.get() );
            container.add( new Text() {{
                format.set( Format.HTML );
                content.set( value.title.get() + "<br/>..." );
                value.articles().executeCollect().onSuccess( articles -> {
                    content.set( value.title.get() + SECOND_LINE.formatted( "Beiträge: " + articles.size() ) );
                });
            }});
        });
    }


    @Override
    protected void onExpand() {
        super.onExpand();
        // delete
        addAction( new Button() {{
            //icon.set( "close" );
            icon.set( UICommon.ICON_DELETE );
            tooltip.set( "Löschen" );
            events.on( EventType.SELECT, ev -> {
                //removeAction.run();
            });
        }});
        // settings
        addAction( new Button() {{
            icon.set( "settings" );
            tooltip.set( "Einstellungen bearbeiten" );
            events.on( EventType.SELECT, ev -> {
                state.site.createState( new TopicEditState() )
                        .putContext( value, State.Context.DEFAULT_SCOPE )
                        .activate();
            });
        }});

//        addAction( new Button() {{
//            tooltip.set( "Bilder/Medien hinzufügen" );
//            icon.set( "add_photo_alternate" );
//            events.on( EventType.SELECT, ev -> {
//                state.site.createState( new MediasSelectState( sel -> addMedias( sel ) ) ).activate();
//            });
//        }});
    }


    @Override
    protected void onCollapse() {
        super.onCollapse();
    }
}