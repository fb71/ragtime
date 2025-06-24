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

import static areca.ui.component2.Events.EventType.SELECT;
import static ragtime.cc.article.ContentPage.df;

import org.polymap.model2.runtime.Lifecycle.State;

import areca.common.Platform;
import areca.common.base.Supplier.RSupplier;
import areca.common.event.EventManager;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Events.UIEvent;
import areca.ui.component2.Text;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.DialogContainer;
import areca.ui.pageflow.Page;
import areca.ui.viewer.SelectViewer;
import areca.ui.viewer.form.Form;
import ragtime.cc.AssociationModel;
import ragtime.cc.EntityTransform;
import ragtime.cc.UICommon;
import ragtime.cc.article.ContentPage.ExpandableCell;
import ragtime.cc.article.ContentState.ArticleContent;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.WebsiteEditPage.WebsiteEditEvent;

/**
 *
 */
class ArticleCell
        extends ExpandableCell<ArticleContent> {

    private Button deleteBtn;

    private Button topicBtn;


    @Override
    protected void create() {
        var article = value.article();
        create( "description", "#5a88b9", container -> {
            container.add( new Text() {{
                format.set( Format.HTML );
                RSupplier<String> title = () -> article.title.get() + SECOND_LINE.formatted( df.format( article.modified.get() ) );
                content.set( title.get() );
                article.onLifecycle( State.AFTER_SUBMIT, ev -> content.set( title.get() ) ).unsubscribeIf( () -> isDisposed() );
            }});
        });
    }


    @Override
    protected void onClick( UIEvent ev, boolean expanded ) {
        if (expanded) {
            EventManager.instance().publish( new WebsiteEditEvent( page, value.article() ) );
        }
        else {
            value.article().topic.fetch().onSuccess( topic -> {
                EventManager.instance().publish( new WebsiteEditEvent( page, topic ) );
            });
        }
    }


    @Override
    protected void onExpand() {
        super.onExpand();

        // delete
        deleteBtn = addAction( new Button() {{
            icon.set( UICommon.ICON_DELETE );
            tooltip.set( "Beitrag löschen" );
            events.on( EventType.SELECT, ev -> {
                value.delete();
            });
        }});
        // move topic
        topicBtn = addAction( new Button() {{
            icon.set( "drive_file_move" );
            tooltip.set( "Beitrag in ein anderes Topic verschieben" );
            events.on( EventType.SELECT, ev -> {
                pageSite.createPage( new MoveTopicDialog() ).open();
            });
        }});
    }


    @Override
    protected void onCollapse() {
        removeAction( deleteBtn );
        removeAction( topicBtn );
        super.onCollapse();
    }

    /**
     *
     */
    protected class MoveTopicDialog extends Page {

        @Override
        protected UIComponent onCreateUI( UIComposite parent ) {
            pageSite.isDialog.set( true );
            var ui = new DialogContainer( this, parent );
            ui.title.set( "Neues Topic" );
            ui.dialogSize.set( Size.of( 320, 200 ) );

            ui.body.layout.set( RowLayout.filled().vertical().spacing( 15 ).margins( 15, 15 ) );
//            ui.body.add( new Text() {{
//                format.set( Text.Format.HTML );
//                content.set( "..." );
//            }});

            var form = new Form();

            var topics = new EntityTransform<>( state.uow, TopicEntity.class, TopicEntity.TYPE.title,
                    new AssociationModel<>( value.article().topic ) );

            ui.body.add( form.newField()
                    .viewer( new SelectViewer( topics.values(), "-Keinem Topic zugeordnet-" ) )
                    .model( topics )
                    .create()
                    .tooltip.set( "Das Topic dieses Beitrags" ) );

            ui.actions.add( new Button() {{
                type.set( Button.Type.SUBMIT );
                label.set( "Verschieben" );
                events.on( SELECT, ev -> {
                    form.submit();
                    value.moveTopic();
                    pageSite.close();
                });
            }});

            Platform.schedule( 100, () -> form.load() );
            return ui;
        }
    }

}