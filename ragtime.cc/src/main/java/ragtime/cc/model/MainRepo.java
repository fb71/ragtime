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

import static ragtime.cc.model.ModelVersionEntity.SCHEMA_VERSION_MAIN;

import java.util.Arrays;

import java.io.File;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;
import org.polymap.model2.store.no2.No2Store;

import areca.common.Promise;
import areca.common.base.Lazy.RLazy;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.CCApp;
import ragtime.cc.Workspace;

/**
 *
 * @author Falko Bräutigam
 */
public class MainRepo {

    private static final Log LOG = LogFactory.getLog( MainRepo.class );

    /**
     * The {@link State.Context} scope of the main {@link EntityRepository} and
     * {@link UnitOfWork}.
     */
    public static final String SCOPE = "main-repository";

    private static final boolean CLEAN_ON_STARTUP = false;

    private static RLazy<Promise<EntityRepository>> instance = new RLazy<>();


    public static void dispose() {
        instance.ifInitialized( promise -> {
            promise.opt().ifPresent( repo -> repo.close() );
            LOG.info( "closed" );
        });
    }


    /**
     * The global main {@link EntityRepository}.
     */
    public static Promise<EntityRepository> instance() {
        return instance.supply( () -> {
            var dbfile = new File( Workspace.baseDir(), "main.db" );
            if (CLEAN_ON_STARTUP) {
                dbfile.delete();
            }
            return EntityRepository.newConfiguration()
                    .entities.set( Arrays.asList( AccountEntity.info, ModelVersionEntity.info ) )
                    .store.set( new No2Store( dbfile ) )
                    .create()
                    .then( newRepo -> {
                        LOG.debug( "Repo: opened" );
                        return checkInitMainRepo( newRepo ).map( __ -> newRepo );
                    })
                    .onSuccess( submitted -> {
                        LOG.debug( "Repo: initialized" );
                    });
        });
    }


    protected static Promise<Submitted> checkInitMainRepo( EntityRepository repo ) {
        var uow = repo.newUnitOfWork();  // must not be closed as we are giving back promise
        return uow.query( ModelVersionEntity.class ).executeCollect()
                .then( rs -> {
                    // model version
                    if (rs.size() == 0) {
                        uow.createEntity( ModelVersionEntity.class, ModelVersionEntity.defaults( SCHEMA_VERSION_MAIN ) );
                    }

                    return uow.query( AccountEntity.class )
                            .where( Expressions.eq( AccountEntity.TYPE.login, CCApp.config.adminLogin ) )
                            .executeCollect();
                })
                .then( rs -> {
//                    // fix wrong admins
//                    for (var admin : rs) {
//                        if (admin.permid.get() != 0) {
//                            LOG.warn( "FIX: admin: %s", admin.permid.get() );
//                            uow.removeEntity( admin );
//                        }
//                    }
                    if (rs.size() == 1) {
                        rs.get( 0 ).setPassword( CCApp.config.adminPassword );
                    }
                    else if (rs.isEmpty()) {
                        uow.createEntity( AccountEntity.class, proto -> {
                            proto.isAdmin.set( true );
                            proto.login.set( CCApp.config.adminLogin );
                            proto.email.set( CCApp.config.adminEMail );
                            proto.setPassword( CCApp.config.adminPassword );
                            proto.permid.set( 0 );
                            Workspace.create( proto );
                        });
                    }
                    return uow.submit();
                });
    }


    public static int nextPermid( UnitOfWork uow ) {
        return uow.query( ModelVersionEntity.class ).singleResult()
                .map( mv -> {
                    mv.nextPermid.set( mv.nextPermid.get() + 1 );
                    LOG.info( "Next permid: %s", mv.nextPermid.get() );
                    return mv;
                })
                .waitForResult().get()
                .nextPermid.get();
    }


}
