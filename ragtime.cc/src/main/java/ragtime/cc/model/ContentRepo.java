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

import static ragtime.cc.model.ModelVersionEntity.SCHEMA_VERSION_CONTENT;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork.Submitted;
import org.polymap.model2.store.no2.No2Store;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.Workspace;
import ragtime.cc.website.http.WebsiteServlet;
import ragtime.cc.website.model.TemplateConfigEntity;
import ragtime.cc.website.model.WebsiteConfigEntity;
import ragtime.cc.website.template.ArticleTemplateModel;

/**
 *
 * @author Falko Bräutigam
 */
public class ContentRepo {

    private static final Log LOG = LogFactory.getLog( ContentRepo.class );

    private static final boolean CLEAN_ON_STARTUP = false;

    private static Map<Integer,Promise<EntityRepository>> repos = new ConcurrentHashMap<>();


    public static void dispose() {
        repos.entrySet().forEach( entry -> {
            entry.getValue().opt().ifPresent( repo -> repo.close() );
            LOG.info( "permid=%s: closed ", entry.getKey() );
        });
        repos.clear();
    }


    public static EntityRepository waitFor( AccountEntity account ) {
        return instanceOf( account ).waitForResult().get();

    }


    public static Promise<EntityRepository> instanceOf( AccountEntity account ) {
        var permid = account.permid.get();
        return repos.computeIfAbsent( permid, __ -> {
            return create( account.permid.get() ).then( newRepo -> {
                // XXX fix old repos
                return checkAccount( newRepo, account ).map( ___ -> newRepo );
            });
        });
    }


    /**
     * Called from {@link WebsiteServlet} without {@link AccountEntity}.
     */
    public static EntityRepository waitFor( int permid ) {
        return repos.computeIfAbsent( permid, __ -> {
            return create( permid );
        }).waitForResult().get();
    }


    protected static Promise<EntityRepository> create( int permid ) {
        File workspace = Workspace.of( permid );
        if (!workspace.exists()) {
            throw new IllegalArgumentException( "Workspace does not exist for permid: " + permid );
        }
        var dbfile = new File( workspace, "content.db" );
        if (CLEAN_ON_STARTUP) {
            dbfile.delete();
        }
        return EntityRepository.newConfiguration()
                .entities.set( Arrays.asList(
                        AccountEntity.info,
                        Article.info, MediaEntity.info, TagEntity.info,
                        WebsiteConfigEntity.info, TemplateConfigEntity.info,
                        ModelVersionEntity.info ) )
                .store.set( new No2Store( dbfile ) )
                .create()
                .then( newRepo -> {
                    LOG.debug( "Repo: initialized" );
                    return populateContentRepo( newRepo ).map( ___ -> newRepo );
                });
    }


    protected static Promise<Submitted> checkAccount( EntityRepository repo, AccountEntity account ) {
        var uow2 = repo.newUnitOfWork();
        return uow2.query( AccountEntity.class )
                .executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        uow2.createEntity( AccountEntity.class, proto -> {
                            proto.permid.set( account.permid.get() );
                            proto.email.set( account.email.get() );
                            proto.login.set( account.login.get() );
                        });
                    }
                    return uow2.submit();
                });
    }


    protected static Promise<Submitted> populateContentRepo( EntityRepository repo ) {
        var uow2 = repo.newUnitOfWork();
        return uow2.query( Article.class )
                .executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        // model version
                        uow2.createEntity( ModelVersionEntity.class, ModelVersionEntity.defaults( SCHEMA_VERSION_CONTENT ) );
                        // Tags
                        var homeTag = uow2.createEntity( TagEntity.class, proto -> {
                            proto.category.set( TagEntity.WEBSITE_NAVI );
                            proto.name.set( "Home" );
                        });
                        var asideTag = uow2.createEntity( TagEntity.class, proto -> {
                            proto.category.set( TagEntity.WEBSITE_NAVI );
                            proto.name.set( "aside" );
                        });
                        // Article
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Willkommen" );
                            proto.content.set( "## Willkommen\n\n..." );
                            proto.tags.add( homeTag );
                        });
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Impressum" );
                            proto.content.set( "## Impressum\n\n..." );
                        });
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Datenschutz" );
                            proto.content.set( "## Datenschutz\n\n..." );
                        });
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Kasten mit Bild" );
                            proto.content.set( "Kontakt..." );
                            proto.tags.add( asideTag );
                        });
                        // TemplateConfig
                        uow2.createEntity( TemplateConfigEntity.class, proto -> {
                            proto.page.createValue( page -> {
                                page.title.set( "Titel" );
                                page.title2.set( "Untertitel" );
                                page.footer.set( "&copy; ..." );
                            });
                            proto.navItems.createElement( navItem -> {
                                navItem.title.set( "Willkommen" );
                                navItem.href.set( "home" );
                                navItem.order.set( 1 );
                            });
                            proto.navItems.createElement( navItem -> {
                                navItem.title.set( "Datenschutz" );
                                navItem.href.set( String.format( "frontpage?%s=Datenschutz", ArticleTemplateModel.PARAM_TITLE ) );
                                navItem.order.set( 2 );
                            });
                            proto.navItems.createElement( navItem -> {
                                navItem.title.set( "Impressum" );
                                navItem.href.set( String.format( "frontpage?%s=Impressum", ArticleTemplateModel.PARAM_TITLE ) );
                                navItem.order.set( 3 );
                            });
                        });
                    }
                    LOG.debug( "Repo: content created" );
                    return uow2.submit();
                })
                .onSuccess( submitted -> {
                    LOG.debug( "Repo: submitted." );
                });
    }


    /**
     * @deprecated
     */
    protected static Promise<Submitted> populateGienkeRepo( EntityRepository repo, int permid ) {
        var uow2 = repo.newUnitOfWork();
        return uow2.query( Article.class )
                .executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        // model version
                        uow2.createEntity( ModelVersionEntity.class, ModelVersionEntity.defaults( SCHEMA_VERSION_CONTENT ) );
                        // Tags
                        var homeTag = uow2.createEntity( TagEntity.class, proto -> {
                            proto.category.set( TagEntity.WEBSITE_NAVI );
                            proto.name.set( "Home" );
                        });
                        uow2.createEntity( TagEntity.class, proto -> {
                            proto.category.set( TagEntity.WEBSITE_NAVI );
                            proto.name.set( "Kontakt" );
                        });
                        var asideTag = uow2.createEntity( TagEntity.class, proto -> {
                            proto.category.set( TagEntity.WEBSITE_NAVI );
                            proto.name.set( "aside" );
                        });

                        // Article
                        ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Willkommen" );
                            proto.content.set( IOUtils.toString( cl.getResource( "gienke/welcome.md" ), "UTF-8" ) );
                            proto.tags.add( homeTag );
                        });
//                        uow2.createEntity( Article.class, proto -> {
//                            proto.title.set( "Kontakt" );
//                            proto.content.set( "Bild:\n<img src=\"media/friederike_1.jpg\"/>" );
//                            proto.tags.add( kontaktTag );
//                        });
                        var impressum = uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Impressum" );
                            proto.content.set( IOUtils.toString( cl.getResource( "gienke/impressum.md" ), "UTF-8" ) );
                            //proto.tags.add( kontaktTag );
                        });
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Datenschutz" );
                            proto.content.set( IOUtils.toString( cl.getResource( "gienke/datenschutz.md" ), "UTF-8" ) );
                        });
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Kasten mit Bild" );
                            proto.content.set( IOUtils.toString( cl.getResource( "gienke/aside.md" ), "UTF-8" ) );
                            proto.tags.add( asideTag );
                        });

                        // Media
                        uow2.createEntity( MediaEntity.class, proto -> {
                            proto.name.set( "friederike_1.jpg" );
                            proto.mimetype.set( "image/jpeg" );
                            proto.permid.set( permid );
                            try (
                                var in = new URL( "https://www.psychotherapie-gienke.de/wp-content/Bilder/gienke_01.jpg" ).openStream();
                                var out = proto.out();
                            ){
                                IOUtils.copy( in, out );
                            }
                        });

                        // TemplateConfig
                        uow2.createEntity( TemplateConfigEntity.class, proto -> {
                            proto.page.createValue( page -> {
                                page.title.set( "Praxis für Psychotherapie" );
                                page.title2.set( "Dipl.-Psych. Friederike Gienke" );
                                page.footer.set( "&copy; Praxis für Psychotherapie" );
                            });
                            proto.navItems.createElement( navItem -> {
                                navItem.title.set( "Willkommen" );
                                navItem.href.set( "home" );
                            });
                            proto.navItems.createElement( navItem -> {
                                navItem.title.set( "Datenschutz" );
                                navItem.href.set( String.format( "frontpage?%s=Datenschutz", ArticleTemplateModel.PARAM_TITLE ) );
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
