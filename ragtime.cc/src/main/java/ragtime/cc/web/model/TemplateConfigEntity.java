/*
 * Copyright (C) 2024-25, the @authors. All rights reserved.
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
package ragtime.cc.web.model;

import org.polymap.model2.Association;
import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Concerns;
import org.polymap.model2.DefaultValue;
import org.polymap.model2.Defaults;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import ragtime.cc.model.Common;
import ragtime.cc.model.DefaultsCompositeConcern;
import ragtime.cc.model.MediaEntity;
import ragtime.cc.web.template.TemplateContentProvider;

/**
 * The configuration of the main/common(?) template.
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class TemplateConfigEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( TemplateConfigEntity.class );

    public static final ClassInfo<TemplateConfigEntity> info = TemplateConfigEntityClassInfo.instance();

    public static TemplateConfigEntity  TYPE;

    /** The template (name) to be used for the website. */
    @DefaultValue( "first" )
    public Property<String>             templateName;

    public Property<PageConfig>         page;

    @Nullable
    public Association<MediaEntity>     favicon;

    public CollectionProperty<NavItem>  navItems;

    @Defaults
    public CollectionProperty<NavItem>  footerNavItems;

    @Nullable
    @Concerns( DefaultsCompositeConcern.class )
    public Property<Colors>             colors;

    @Nullable
    public Association<MediaEntity>     bannerImage;

    @Nullable
    public Association<MediaEntity>     leadImage;

    /**
     * Project specific CSS.
     * Loaded as config.css from page.ftl via {@link TemplateContentProvider}.
     * Default comes from src/main/resources/templates/config-default.css.
     */
    @DefaultValue( ":root {\n  /*--bs-body-bg: white;*/\n}" )
    public Property<String>             css;

    /**
     * Project specific JS.
     * Loaded as config.css from page.ftl via {@link TemplateContentProvider}
     */
    @DefaultValue( "" )
    public Property<String>             js;

    /**
     * Configurable EMail address used by {@link CalendarTopicTemplate}.
     * <p>
     * This is an quick-and-dirty solution.
     */
    @Nullable
    public Property<String>             email;

}
