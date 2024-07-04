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
package ragtime.cc.model;

import java.util.Date;
import org.polymap.model2.Association;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.query.Expressions;

import areca.common.Promise;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class CalendarEvent
        extends Common {

    private static final Log LOG = LogFactory.getLog( CalendarEvent.class );

    public static final ClassInfo<CalendarEvent> info = CalendarEventClassInfo.instance();

    public static CalendarEvent TYPE;

    /**
     * Computed back association of {@link Article#medias}
     */
    public static Promise<Opt<CalendarEvent>> of( Article article ) {
        return article.context.getUnitOfWork().query( CalendarEvent.class )
                .where( Expressions.is( CalendarEvent.TYPE.article, article ) )
                .optResult();
    }

    // instance *******************************************

    @Queryable
    public Property<Date>               start;

    @Queryable
    public Property<Date>               end;

    @Nullable
    @Queryable
    public Association<Article>         article;

}
