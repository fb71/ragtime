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
package ragtime.cc.article;

import org.polymap.model2.Association;
import org.polymap.model2.Entity;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.model.ModelBaseImpl;

/**
 *
 * @author Falko Bräutigam
 */
public class EntityAssocModel<V extends Entity>
        extends ModelBaseImpl
        implements Model<V> {

    private static final Log LOG = LogFactory.getLog( EntityAssocModel.class );

    private Association<V> prop;

    public EntityAssocModel( Association<V> prop ) {
        this.prop = prop;
    }

    @Override
    public V get() {
        return prop.fetch().waitForResult().orNull();
    }

    @Override
    public void set( V value ) {
        prop.set( value );
    }

}
