/*
 * Copyright (C) 2023, the @authors. All rights reserved.
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
package ragtime.app.model;

import java.util.EventObject;

import org.polymap.model2.Entity;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 * Thrown when the model has changed and submitted.
 *
 * @author Falko Bräutigam
 */
public class ModelUpdateEvent
        extends EventObject {

    private static final Log LOG = LogFactory.getLog( ModelUpdateEvent.class );

    public Submitted submitted;

    public ModelUpdateEvent( UnitOfWork source, Submitted submitted ) {
        super( source );
        this.submitted = submitted;
    }

    @Override
    public UnitOfWork getSource() {
        return (UnitOfWork)super.getSource();
    }

    public boolean isUpdated( Entity entity ) {
        return submitted.modifiedIds.contains( entity.id() ) || submitted.removedIds.contains( entity.id() );
    }
}
