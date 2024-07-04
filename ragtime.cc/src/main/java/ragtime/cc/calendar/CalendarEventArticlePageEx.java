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
package ragtime.cc.calendar;

import java.util.Date;

import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.Action;
import areca.ui.component2.DatePicker.DateTime;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.viewer.DatePickerViewer;
import areca.ui.viewer.form.Form;
import areca.ui.viewer.transform.Date2StringTransform;
import ragtime.cc.article.ArticleEditState;
import ragtime.cc.article.ArticlePage;
import ragtime.cc.article.ArticlePageExtension;
import ragtime.cc.article.PropertyModel;
import ragtime.cc.model.CalendarEvent;

/**
 *
 * @author Falko Bräutigam
 */
public class CalendarEventArticlePageEx
        extends ArticlePageExtension {

    private static final Log LOG = LogFactory.getLog( CalendarEventArticlePageEx.class );

    @Override
    public void doExtendFormEnd( ArticleEditState state, ArticlePage page, PageSite pagesite, Form form, UIComposite formBody ) {
        CalendarEvent.of( state.article.$() ).onSuccess( opt -> {
            //
            RConsumer<CalendarEvent> doExtendForm = ce -> {
                formBody.components.add( 0, form.newField().label( "Termin: Start" )
                        .description( "Startzeit des Termins" )
                        .viewer( new DatePickerViewer() )
                        .model( new Date2StringTransform( DateTime.DATETIME,
                                new PropertyModel<>( ce.start ) ) )
                        .create()
                        .lc( RowConstraints.height( 35 ) ) );

                form.load();
                formBody.layout();
            };

            // absent: action add
            opt.ifAbsent( () -> {
                pagesite.actions.add( new Action() {{
                    description.set( "Diesen Beitrag zu einem Termin machen" );
                    icon.set( "event" );
                    handler.set( ev -> {
                        enabled.set( false );
                        var ce = state.uow.createEntity( CalendarEvent.class, proto -> {
                            proto.article.set( state.article.$() );
                            proto.start.set( new Date() );
                        });
                        doExtendForm.accept( ce );
                    });
                }});
            });

            // present: form
            opt.ifPresent( ce -> {
                doExtendForm.accept( ce );

                // action: remove
                pagesite.actions.add( new Action() {{
                    description.set( "Termin vom Beitrag entfernen\nDer Beitrag selber bleibt bestehen" );
                    icon.set( "event_busy" );
                    handler.set( ev -> {
                        enabled.set( false );
                        state.uow.removeEntity( ce );
                        state.uow.submit().onSuccess( __ -> {
                            state.disposeAction();
                        });
                    });
                }});
            });
        });
    }

}
