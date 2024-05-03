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
package ragtime.cc.insta;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 *
 * @author Falko Bräutigam
 */
public class IGExperiments {

    private static final Log LOG = LogFactory.getLog( IGExperiments.class );

//    public static void main() throws IGLoginException {
//        //
//        var t = Timer.start();
//        IGClient ig = IGClient.builder()
//                .username( CCApp.config.igUser )
//                .password( CCApp.config.igPassword )
//                .login();
//
//        LOG.info( "Login: (%s)", t );
//
//        var monster = new File( "/home/falko/Dokumente/Briefcase/monster-freelancer.jpg" );
//        Assert.that( monster.exists() );
//        var falko = new File( "/home/falko/Dokumente/Briefcase/falko-freelancer.jpg" );
//        Assert.that( falko.exists() );
//        var img = new File( "/home/falko/Bilder/2023/IMG_20230122_190757.jpg" );
//        Assert.that( img.exists() );
//
//        var profile = ig.getSelfProfile();
////        ig.actions().account()
////                .setProfilePicture( new File( "/home/falko/Dokumente/Briefcase/falko-freelancer.jpg" ) )
////                .thenAccept( response -> {
////                    LOG.info( "Upload complete ((%s)", t );
////                    LOG.info( "IG: username=%s, url=%s", profile.getUsername(), profile.getProfile_pic_url() );
////                })
////                .exceptionally( e -> {
////                    LOG.warn( "Upload failed: %s", ((IGResponseException)e.getCause()).getMessage() );
////                    return null;
////                });
//
////        ig.actions().account().setBio( "Wizard & Crew" )
////                .thenAccept( response -> {
////                    LOG.info( "Bio complete (%s)", t );
////                });
//
////        ig.actions().timeline().uploadPhoto( falko, "Another photo..." )
////                .thenAccept( response -> {
////                    LOG.info( "Timeline post complete (%s)", t );
////                })
////                .exceptionally( e -> {
////                    LOG.warn( "Post failed: %s", e.getCause().toString() );
////                    return null;
////                });
//
//        ig.actions().story().uploadPhoto( falko )
//                .thenAccept( response -> {
//                    LOG.info( "Story post complete (%s)", t );
//                })
//                .exceptionally( e -> {
//                    LOG.warn( "Post failed: %s", e.getCause().toString() );
//                    return null;
//                });
//
//        LOG.info( "IG: username=%s", profile.getUsername() );
//    }
}
