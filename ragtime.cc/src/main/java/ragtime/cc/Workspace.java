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

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

import areca.common.Assert;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.model.AccountEntity;

/**
 *
 * @author Falko Bräutigam
 */
public class Workspace {

    private static final Log LOG = LogFactory.getLog( Workspace.class );

    private static File baseDir;

    static {
        baseDir = CCApp.config.workspaceBase.startsWith( "/" )
                ? new File( CCApp.config.workspaceBase )
                : new File( SystemUtils.getUserHome(), CCApp.config.workspaceBase );
        LOG.info( "Workspace: %s", baseDir.getAbsolutePath() );
    }

    public static File baseDir() {
       return Assert.notNull( baseDir, "Workspace not yet initialized." );
    }

    public static void create( AccountEntity account ) {
        of( account ).mkdir();
    }

    public static File of( AccountEntity account ) {
        return of( account.permid.get() );
    }

    public static File of( int permid ) {
        return new File( baseDir(), Integer.toString( permid ) );
    }

//    /**
//     *
//     * @param account The permanent ID of the {@link AccountEntity}.
//     */
//    static void init( AccountEntity account ) {
//        Session.setInstance( new Workspace( account.permid.get() ) );
//    }
//
//    /**
//     * The {@link Workspace} of the current {@link Session} - if the user has
//     * logged in and there is a {@link AccountEntity}.
//     */
//    public static Workspace current() {
//        return Session.instanceOf( Workspace.class );
//    }
//
//    // instance *******************************************
//
//    protected int permid;
//
//
//    public Workspace( int permid ) {
//        this.permid = permid;
//    }
//
//
//    public File dir() {
//        return new File( baseDir, Integer.toString( permid ) );
//    }

}
