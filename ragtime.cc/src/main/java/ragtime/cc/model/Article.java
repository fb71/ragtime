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

import org.polymap.model2.Association;
import org.polymap.model2.Concerns;
import org.polymap.model2.ManyAssociation;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import org.polymap.model2.store.no2.Fulltext;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class Article
        extends Common {

    private static final Log LOG = LogFactory.getLog( Article.class );

    public static final ClassInfo<Article> info = ArticleClassInfo.instance();

    public static Article TYPE;

    /**
     * An normalized, simplified, URL compatible name that identifies this Article,
     * auto updated when {@link #title} changes.
     */
    @Queryable
    @Concerns( PermNameConcern.class )
    public Property<String>             permName;

    @Queryable
    @Concerns( UpdatePermNameConcern.class )
    public Property<String>             title;

    @Queryable
    @Fulltext
    @Format( Format.FormatType.MARKDOWN )
    public Property<String>             content;

    @Queryable
    public ManyAssociation<TagEntity>   tags;

    /**
     * The position (ascending order) of this {@link Article} in the list
     * of articles of the associated {@link TopicEntity}.
     */
    @Queryable
    @Nullable
    public Property<Integer>            order;

    @Nullable
    @Queryable
    public Association<TopicEntity>     topic;

    @Queryable
    public ManyAssociation<MediaEntity> medias;


    /**
     * remove back association
     */
    @Override
    public void onLifecycleChange( State state ) {
        super.onLifecycleChange( state );
        if (state == State.AFTER_REMOVED) {
            // CalendarEvent
            CalendarEvent.of( this ).waitForResult( opt -> opt.ifPresent( ce -> {
                LOG.info( "Removing back link: %s", ce );
                context.getUnitOfWork().removeEntity( ce );
            }));
        }
    }

}
