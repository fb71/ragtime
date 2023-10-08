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

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.app.model.GeneratedImageTag.TagType;

/**
 *
 * @author Falko Bräutigam
 */
public class ModelInitializer {

    private static final Log LOG = LogFactory.getLog( ModelInitializer.class );

    private UnitOfWork uow;


    public Promise<Submitted> initModel( EntityRepository repo ) {
        uow = repo.newUnitOfWork();
        return createDefaultEntities()
                .then( __ -> {
                    return uow.submit();
                });
    }


    protected Promise<?> createDefaultEntities() {
        return uow.query( GeneratedImageTag.class ).executeCollect().map( rs -> {
            if (rs.isEmpty()) {
                uow.createEntity( GeneratedImageTag.class, proto -> {
                    proto.type.set( TagType.EMOTIONAL_CONTEXT );
                    proto.label.set( "Beziehung" );
                } );
                uow.createEntity( GeneratedImageTag.class, proto -> {
                    proto.type.set( TagType.EMOTIONAL_CONTEXT );
                    proto.label.set( "Familie" );
                } );
                uow.createEntity( GeneratedImageTag.class, proto -> {
                    proto.type.set( TagType.EMOTIONAL_CONTEXT );
                    proto.label.set( "Kinder" );
                } );
                uow.createEntity( GeneratedImageTag.class, proto -> {
                    proto.type.set( TagType.EMOTIONAL_CONTEXT );
                    proto.label.set( "Arbeit" );
                } );
                LOG.info( "Default entities created." );
            }
            return uow;
        });
    }
}
