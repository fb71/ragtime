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
package ragtime.cc.web.template.topic;

import java.util.ArrayList;
import org.polymap.model2.query.Query.Order;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDateModel;
import ragtime.cc.model.AccountEntity;
import ragtime.cc.model.CalendarEvent;
import ragtime.cc.web.template.IterableTemplateModel;
import ragtime.cc.web.template.MapTemplateModel;

/**
 *
 * @author Falko Bräutigam
 */
public class CalendarTopicTemplate
        extends BasicTopicTemplate {

    private static final Log LOG = LogFactory.getLog( CalendarTopicTemplate.class );

    @Override
    public String label() {
        return "Calendar";
    }

    @Override
    protected Promise<String> processTopic() {
        var period = new IterableTemplateModel<>( new ArrayList<MapTemplateModel>() );
        site.data.put( "period", period );

        var email = site.r.config.email.opt().orElseGet( () -> {
            var account = site.r.uow.query( AccountEntity.class ).singleResult().waitForResult().get();
            return account.email.get();
        });

        return site.r.uow.query( CalendarEvent.class )
                //.where( Expressions.gt( CalendarEvent.TYPE.start, new Date() ) )
                .orderBy( CalendarEvent.TYPE.start, Order.ASC )
                .executeCollect()
                .map( rs -> {
                    for (var ce : rs) {
                        var article = ce.article.fetch().waitForResult().get();
                        var event = new MapTemplateModel( processTopicArticle( article ) );
                        event.delegate.put( "start", new SimpleDate( ce.start.get(), TemplateDateModel.DATETIME ) );
                        event.delegate.put( "email", new SimpleScalar( email ) );
                        period.delegate.add( event );
                    }
                    return "calendar.ftl";
                });
    }

}
