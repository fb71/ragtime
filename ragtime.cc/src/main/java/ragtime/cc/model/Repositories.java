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

import static java.util.Arrays.asList;
import static ragtime.cc.website.template.ArticleByTagTemplateModel.PARAM_TAG;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;

import org.apache.commons.io.IOUtils;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork;
import org.polymap.model2.runtime.UnitOfWork.Submitted;
import org.polymap.model2.store.no2.No2Store;

import areca.common.Promise;
import areca.common.base.Lazy.RLazy;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.statenaction.State;
import ragtime.cc.CCApp;
import ragtime.cc.website.model.TemplateConfigEntity;
import ragtime.cc.website.model.WebsiteConfigEntity;

/**
 *
 * @author Falko Bräutigam
 */
public class Repositories {

    private static final Log LOG = LogFactory.getLog( Repositories.class );

    /** the {@link State.Context} scope of the main {@link EntityRepository} and {@link UnitOfWork}. */
    public static final String SCOPE_MAIN = "main-repository";

    private static RLazy<EntityRepository> mainRepo = new RLazy<>();

    private static Map<Integer,EntityRepository> repos = new ConcurrentHashMap<>();


    public static void init() {
    }


    public static void dispose() {
        mainRepo.ifInitialized( repo -> repo.close() );
        repos.values().forEach( repo -> repo.close() );
        repos.clear();
    }


    /**
     * Gets the main {@link EntityRepository}
     */
    public static EntityRepository mainRepo() {
        return mainRepo.supply( () -> {
            var dbfile = new File( CCApp.workspaceDir(), "main.db" );
            dbfile.delete();

            return EntityRepository.newConfiguration()
                    .entities.set( Arrays.asList( AccountEntity.info ) )
                    .store.set( new No2Store( dbfile ) )
                    .create()
                    .then( newRepo -> {
                        LOG.debug( "Repo: created." );
                        return populateMainRepo( newRepo ).map( __ -> newRepo );
                    })
                    .waitForResult().get();
        });
    }


    protected static Promise<Submitted> populateMainRepo( EntityRepository repo ) {
        var uow = repo.newUnitOfWork();
        return uow.query( AccountEntity.class ).executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        // admin
                        uow.createEntity( AccountEntity.class, proto -> {
                            proto.isAdmin.set( true );
                            proto.login.set( "admin" );
                            proto.setPassword( "admin" );
                            proto.permid.set( 0 );
                            CCApp.workspaceDir( 0 ).mkdir();
                        });
                        // gienke
                        uow.createEntity( AccountEntity.class, proto -> {
                            proto.isAdmin.set( false );
                            proto.login.set( "praxis@psychotherapie-gienke.de" );
                            proto.permid.set( 1 );
                            CCApp.workspaceDir( 1 ).mkdir();
                        });
                    }
                    return uow.submit();
                })
                .onSuccess( submitted -> {
                    LOG.debug( "Repo: submitted." );
                });
    }


    public static EntityRepository repo( int permid ) {
        File workspace = CCApp.workspaceDir( permid );
        if (!workspace.exists()) {
            throw new IllegalArgumentException( "Workspace does not exist for permid: " + permid );
        }
        var dbfile = new File( workspace, "content.db" );
        dbfile.delete();

        return repos.computeIfAbsent( permid, __ -> {
            return EntityRepository.newConfiguration()
                    .entities.set( asList( Article.info, TagEntity.info, WebsiteConfigEntity.info, TemplateConfigEntity.info ) )
                    .store.set( new No2Store( dbfile ) )
                    .create()
                    .then( newRepo -> {
                        LOG.debug( "Repo: created." );
                        return populateGienkeRepo( newRepo ).map( ___ -> newRepo );
                    })
                    .waitForResult().get(); // XXX
        });
    }


    protected static Promise<Submitted> populateGienkeRepo( EntityRepository repo ) {
        var uow2 = repo.newUnitOfWork();
        return uow2.query( Article.class )
                .executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        // Tags
                        var homeTag = uow2.createEntity( TagEntity.class, proto -> {
                            proto.category.set( TagEntity.WEBSITE_NAVI );
                            proto.name.set( "Home" );
                        });
                        var kontaktTag = uow2.createEntity( TagEntity.class, proto -> {
                            proto.category.set( TagEntity.WEBSITE_NAVI );
                            proto.name.set( "Kontakt" );
                        });

                        // Article
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Willkommen" );
                            var welcome = IOUtils.toString( Thread.currentThread().getContextClassLoader().getResource( "welcome.md" ), "UTF-8" );
                            proto.content.set( welcome );
                            proto.tags.add( homeTag );
                        });
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Kontakt" );
                            proto.content.set( "Telefon: ..." );
                            proto.tags.add( kontaktTag );
                        });
                        var impressum = uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Impressum" );
                            proto.content.set( "## Impressum" );
                            //proto.tags.add( kontaktTag );
                        });

                        // TemplateConfig
                        uow2.createEntity( TemplateConfigEntity.class, proto -> {
                            proto.page.createValue( page -> {
                                page.title.set( "Praxis für Psychotherapie" );
                                page.title2.set( "Dipl.-Psych. Friederike Gienke" );
                                page.footer.set( "&copy; Praxis für Psychotherapie" );
                            });
                            proto.navItems.createElement( navItem -> {
                                navItem.title.set( "Home" );
                                navItem.href.set( "home" );
                            });
                            proto.navItems.createElement( navItem -> {
                                navItem.title.set( "Kontakt" );
                                navItem.href.set( String.format( "frontpage?%s=Kontakt", PARAM_TAG ) );
                            });
                            proto.navItems.createElement( navItem -> {
                                navItem.title.set( "Impressum" );
                                navItem.href.set( "article?id=" + impressum.id() );
                            });
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
