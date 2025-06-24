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

import static areca.ui.component2.Events.EventType.SELECT;

import java.util.Date;

import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.Action;
import areca.ui.component2.Button;
import areca.ui.component2.DatePicker.DateTime;
import areca.ui.layout.RowConstraints;
import areca.ui.viewer.DatePickerViewer;
import areca.ui.viewer.transform.Date2StringTransform;
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
    public void doExtendFormEnd( ExtensionSite site ) {
        CalendarEvent.of( site.article() ).onSuccess( opt -> {
            // doExtendForm
            RConsumer<CalendarEvent> doExtendForm = ce -> {
                site.formBody().components.add( 1, site.form().newField().label( "Termin: Start" )
                        .description( "Startzeit des Termins" )
                        .viewer( new DatePickerViewer() )
                        .model( new Date2StringTransform( DateTime.DATETIME,
                                new PropertyModel<>( ce.start ) ) )
                        .create()
                        .lc( RowConstraints.height( 35 ) ) );

                site.form().load();
                site.formBody().layout();
            };

            // absent: action add
            opt.ifAbsent( () -> {
                site.formBody().components.add( 1, new Button() {{
                    //lc( RowConstraints.height( 35 ) );
                    lc( RowConstraints.width( 60 ) );

                    tooltip.set( "Diesen Beitrag zu einem Termin machen" );
                    icon.set( "event" );
                    events.on( SELECT, ev -> {
                        //enabled.set( false );
                        dispose();
                        var ce = site.uow().createEntity( CalendarEvent.class, proto -> {
                            proto.article.set( site.article() );
                            proto.start.set( new Date() );
                        });
                        doExtendForm.accept( ce );
                    });
                }});
                site.formBody().layout();
            });

            // present: form
            opt.ifPresent( ce -> {
                doExtendForm.accept( ce );

                // action: remove
                site.pagesite().actions.add( new Action() {{
                    description.set( "Termin vom Beitrag entfernen\nDer Beitrag selber bleibt bestehen" );
                    icon.set( "event_busy" );
                    handler.set( ev -> {
                        enabled.set( false );
                        site.uow().removeEntity( ce );
                        site.uow().submit().onSuccess( __ -> {
                            //state.disposeAction();
                        });
                    });
                }});
            });
        });
    }

}
