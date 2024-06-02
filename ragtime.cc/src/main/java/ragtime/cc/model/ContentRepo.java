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
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.polymap.model2.runtime.EntityRepository;
import org.polymap.model2.runtime.UnitOfWork.Submitted;
import org.polymap.model2.store.no2.No2Store;

import areca.common.Promise;
import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.Workspace;
import ragtime.cc.web.http.WebsiteServlet;
import ragtime.cc.web.model.TemplateConfigEntity;
import ragtime.cc.web.model.TopicTemplateConfigEntity;
import ragtime.cc.web.model.WebsiteConfigEntity;
import ragtime.cc.web.template.ArticleTemplateModel;
import ragtime.cc.web.template.topic.TopicTemplateContentProvider;

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


    public static Promise<EntityRepository> of( AccountEntity account ) {
        var permid = account.permid.get();
        return repos.computeIfAbsent( permid, __ -> {

            // XXX testing
//            if (account.email.get().equals( "f.braeutigam@polymap.de" )) {
//                FileUtils.deleteQuietly( Workspace.of( permid ) );
//                Workspace.of( permid ).mkdir();
//            }

            return initRepo( permid )
                    .then( repo -> checkInitAccount( repo, account ).map( ___ -> repo ) )
                    .then( repo -> checkInitContent( repo, account ).map( ___ -> repo ) );
        });
    }


    /**
     * Called from {@link WebsiteServlet} without {@link AccountEntity}.
     */
    public static Promise<EntityRepository> of( int permid ) {
        return repos.computeIfAbsent( permid, __ -> {
            return initRepo( permid );
        });
    }


    protected static Promise<EntityRepository> initRepo( int permid ) {
        var workspace = Workspace.of( permid );
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
                        Article.info, MediaEntity.info, TagEntity.info, TopicEntity.info,
                        WebsiteConfigEntity.info, TemplateConfigEntity.info, TopicTemplateConfigEntity.info,
                        ModelVersionEntity.info ) )
                .store.set( new No2Store( dbfile ) )
                .create();
    }


    protected static Promise<Submitted> checkInitAccount( EntityRepository repo, AccountEntity account ) {
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


    protected static Promise<Submitted> checkInitContent( EntityRepository repo, AccountEntity account ) {
        var uow2 = repo.newUnitOfWork();
        return uow2.query( Article.class ).executeCollect()
                .then( rs -> {
                    if (rs.size() == 0) {
                        // model version
                        uow2.createEntity( ModelVersionEntity.class, ModelVersionEntity.defaults( SCHEMA_VERSION_CONTENT ) );
                        // Medias
                        //var sanddorn = uow2.createEntity( MediaEntity.class, defaultMedia( "sanddorn.jpeg" ));
                        var wohnung = uow2.createEntity( MediaEntity.class, defaultMedia( "wohnung.jpeg" ));
                        var areca = uow2.createEntity( MediaEntity.class, defaultMedia( "areca.jpeg" ));
                        // Topics
                        var main = uow2.createEntity( TopicEntity.class, proto -> {
                            proto.title.set( "Willkommen" );
                            proto.description.set( defaults( "mainTopic.md" ) );
                            proto.order.set( 1 );
                            proto.medias.add( wohnung );
                            proto.urlPart.set( "home" );
                        });
                        var legal = uow2.createEntity( TopicEntity.class, proto -> {
                            proto.title.set( "Rechtliches" );
                            proto.description.set( defaults( "legalTopic.md" ) );
                            proto.order.set( 2 );
                            proto.medias.add( wohnung );
                            proto.urlPart.set( "legal" );
                        });
                        // Article
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Bedienung" );
                            proto.content.set( defaults( "bedienung.md" ) );
                            proto.topic.set( main );
                        });
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Impressum" );
                            proto.content.set( defaults( "impressum.md" ) );
                            proto.topic.set( legal );
                            proto.order.set( 1 );
                        });
                        uow2.createEntity( Article.class, proto -> {
                            proto.title.set( "Datenschutz" );
                            proto.content.set( defaults( "datenschutz.md" ) );
                            proto.topic.set( legal );
                            proto.order.set( 2 );
                        });
                        // TemplateConfig
                        uow2.createEntity( TemplateConfigEntity.class, proto -> {
                            proto.templateName.set( TopicTemplateContentProvider.templates.get( 0 ) );
                            proto.bannerImage.set( areca );
                            proto.leadImage.set( wohnung );
                            proto.colors.get().headerBackground.set( "#e0ddd2");
                            proto.colors.get().headerForeground.set( "#ffffff");
                            proto.colors.get().pageBackground.set( "#f0f0f0");
                            proto.colors.get().pageForeground.set( "#3d3e3d");
                            proto.colors.get().footerBackground.set( "#d5d1c3");
                            proto.colors.get().footerForeground.set( "#3d3e3d");
                            proto.colors.get().accent.set( "#52784f");
                            proto.colors.get().link.set( "#db7e00" );
                            proto.page.createValue( page -> {
                                page.title.set( "Meine Website" );
                                page.title2.set( "Hier sollte etwas anderes stehen" );
                                page.footer.set( "&copy; ..." );
                            });
                            proto.footerNavItems.createElement( navItem -> {
                                navItem.title.set( "Datenschutz" );
                                navItem.href.set( String.format( "frontpage?%s=Datenschutz", ArticleTemplateModel.PARAM_TITLE ) );
                                navItem.order.set( 2 );
                            });
                            proto.footerNavItems.createElement( navItem -> {
                                navItem.title.set( "Impressum" );
                                navItem.href.set( String.format( "frontpage?%s=Impressum", ArticleTemplateModel.PARAM_TITLE ) );
                                navItem.order.set( 3 );
                            });
                            proto.css.set( defaults( "config-default.css" ) );
                        });
                    }
                    LOG.debug( "Repo: content created" );
                    return uow2.submit();
                })
                .onSuccess( submitted -> {
                    LOG.debug( "Repo: submitted." );
                });
    }


    protected static String defaults( String res ) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return IOUtils.toString( cl.getResource( "defaults/" + res ), "UTF-8" );
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }
    }


    protected static RConsumer<MediaEntity> defaultMedia( String res ) {
        return proto -> {
            MediaEntity.defaults().accept( proto );
            proto.mimetype.set( "image/" + StringUtils.substringAfterLast( res, "." ) );
            proto.name.set( res );

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try (
                var out = proto.out();
                var in = cl.getResourceAsStream( "defaults/" + res )
            ){
                IOUtils.copy( in, out );
            }
            catch (IOException e) {
                throw new RuntimeException( e );
            }
        };
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
