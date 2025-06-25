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

import areca.common.Platform;
import areca.common.event.EventManager;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Events.UIEvent;
import areca.ui.component2.Text;
import areca.ui.statenaction.State;
import ragtime.cc.UICommon;
import ragtime.cc.article.ContentPage.ExpandableCell;
import ragtime.cc.article.ContentState.TopicContent;
import ragtime.cc.model.Article;
import ragtime.cc.model.EntityLifecycleEvent;
import ragtime.cc.web.WebsiteEditPage.WebsiteEditEvent;
import org.polymap.model2.runtime.Lifecycle;

/**
 *
 * @author Falko Bräutigam
 */
class TopicCell
        extends ExpandableCell<TopicContent> {

    private Button deleteBtn;

    private Button settingsBtn;

    private Button articleBtn;


    @Override
    protected void create() {
        var topic = value.topic();
        create( "topic", "#c96e5e", container -> {
            container.tooltip.set( "Topic: " + topic.title.get() );
            container.add( new Text() {{
                format.set( Format.HTML );
                content.set( topic.title.get() + "<br/>..." );

                Runnable update = () -> {
                    topic.articles().executeCollect().onSuccess( articles -> {
                        content.set( topic.title.get() + SECOND_LINE.formatted( "Beiträge: " + articles.size() ) );
                    });
                };
                update.run();
                EventManager.instance()
                        .subscribe( ev -> update.run() )
                        .performIf( EntityLifecycleEvent.class, ev ->
                                ev.state == Lifecycle.State.AFTER_SUBMIT &&
                                (ev.getSource() == topic || ev.getSource() instanceof Article a && a.topic.fetch().waitForResult().orNull() == topic))
                        .unsubscribeIf( () -> isDisposed() );
            }});
        });
    }


    @Override
    protected void onClick( UIEvent ev, boolean expanded ) {
        // load null (home) if topic is collapsed
        EventManager.instance().publish( new WebsiteEditEvent( page, expanded ? value.topic() : null ) );
    }


    @Override
    protected void onExpand() {
        var topic = value.topic();

        // delete
        deleteBtn = addAction( new Button() {{
            iconStyle.set( IconStyle.OUTLINED );
            icon.set( UICommon.ICON_DELETE );
            tooltip.set( "Topic Löschen" );
            events.on( EventType.SELECT, ev -> {
                value.delete();
            });
        }});
        // settings
        settingsBtn = addAction( new Button() {{
            iconStyle.set( IconStyle.OUTLINED );
            icon.set( "view_agenda" );
            tooltip.set( "Einstellungen bearbeiten" );
            events.on( EventType.SELECT, ev -> {
                state.site.createState( new TopicEditState() )
                        .putContext( topic, State.Context.DEFAULT_SCOPE )
                        .activate();
            });
        }});
        // add article
        articleBtn = addAction( new Button() {{
            iconStyle.set( IconStyle.OUTLINED );
            icon.set( "note_add" );
            tooltip.set( "Neuen Beitrag anlegen im Topic '%s'".formatted( topic.title.get() ) );
            events.on( EventType.SELECT, ev -> {
                var article = value.createNewArticle();
                Platform.schedule( 2000, () -> {
                    viewer.expand( state.contentType( article ) );
                });
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
        removeAction( deleteBtn );
        removeAction( settingsBtn );
        removeAction( articleBtn );
        super.onCollapse();
    }

}