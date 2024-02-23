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
package ragtime.cc.website.template;

import org.polymap.model2.Entity;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 * Provides an {@link Entity} by specified type and id.
 *
 * @author Falko Bräutigam
 */
public class EntityByIdTemplateModel
        extends CompositeTemplateModel {

    private static final Log LOG = LogFactory.getLog( EntityByIdTemplateModel.class );


    public EntityByIdTemplateModel( ModelParams modelParams, UnitOfWork uow ) throws ClassNotFoundException {
        @SuppressWarnings( "unchecked" )
        var entityType = (Class<? extends Entity>)Class.forName( modelParams.get( "type" ) );
        this.composite = uow.entity( entityType, modelParams.get( "id" ) ).waitForResult().get();
    }

}
