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
package ragtime.cc.website.model;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class WebsiteConfigEntity
        extends Entity {

    private static final Log LOG = LogFactory.getLog( WebsiteConfigEntity.class );

    public static final ClassInfo<WebsiteConfigEntity> info = WebsiteConfigEntityClassInfo.instance();

    public static WebsiteConfigEntity   TYPE;

    public Property<PageConfig>         page;

    public CollectionProperty<NavItem>  navItems;

}
