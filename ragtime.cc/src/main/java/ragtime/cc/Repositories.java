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
package ragtime.cc;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork.Submitted;
import org.polymap.model2.store.no2.No2Store;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.model.Article;

/**
 *
 * @author Falko Bräutigam
 */
public class Repositories {

    private static final Log LOG = LogFactory.getLog( Repositories.class );

    private static Map<String,EntityRepository> repos = new ConcurrentHashMap<>();


    public static void dispose() {
        for (var repo : repos.values()) {
            repo.close();
        }
        repos.clear();
    }


    /**
     * Gets the main {@link EntityRepository}
     */
    public static EntityRepository mainRepo() {
        return repos.computeIfAbsent( "main", __ -> {
            return initMainRepo();
        });
    }


    protected static EntityRepository initMainRepo() {
        var dir = new File( "/tmp/ragtime.cc" );
        dir.mkdir();
        return EntityRepository.newConfiguration()
                .entities.set( Arrays.asList( Article.info ) )
                .store.set( new No2Store( new File( dir, "main.db" ) ) )
                .create()
                .then( newRepo -> {
                    LOG.debug( "Repo: created." );
                    return populateMainRepo( newRepo ).map( __ -> newRepo );
                })
                .waitForResult().get();
    }


    protected static Promise<Submitted> populateMainRepo( EntityRepository repo ) {
        var uow2 = repo.newUnitOfWork();
        return uow2.query( Article.class ).executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Erster Artikel" );
                            proto.content.set( "Hier steht der Text..." );
                        });
                    }
                    LOG.debug( "Repo: Test Article created" );
                    return uow2.submit();
                })
                .onSuccess( submitted -> {
                    LOG.debug( "Repo: submitted." );
                });
    }

}
