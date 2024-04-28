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
import java.io.FileReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import com.google.gson.GsonBuilder;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 *
 * @author Falko Bräutigam
 */
public class CCAppConfig {

    private static final Log LOG = LogFactory.getLog( CCAppConfig.class );

    public static final String FILENAME = "ragtime.conf";

    public static final CCAppConfig instance;

    static {
        var f = new File( SystemUtils.getUserHome(), "." + FILENAME );
        if (!f.exists()) {
            LOG.info( "not found: " + f );
            f = FileUtils.getFile( SystemUtils.getUserHome(), FILENAME );
        }
        if (!f.exists()) {
            LOG.info( "not found: " + f );
            f = FileUtils.getFile( SystemUtils.getUserHome(), ".config", FILENAME );
        }
        if (!f.exists()) {
            LOG.info( "not found: " + f );
            f = new File( SystemUtils.getUserDir(), FILENAME );
        }
        if (!f.exists()) {
            LOG.info( "not found: " + f );
            f = new File( "/etc", FILENAME );
        }
        if (!f.exists()) {
            LOG.info( "not found: " + f );
            LOG.warn( "No app config found: " + FILENAME );
            throw new RuntimeException( "No app config found: " + FILENAME );
        }
        try {
            LOG.warn( "Loading config: %s", f );
            var gson = new GsonBuilder().create();
            instance = gson.fromJson( IOUtils.buffer( new FileReader( f, StandardCharsets.UTF_8 ) ), CCAppConfig.class );
        }
        catch (Exception e) {
            throw new RuntimeException( "Error reading: " + f );
        }
    }

    // instance *******************************************

    public String workspaceBase;

    public String smtpHost;
    public String smtpPort;
    public String smtpUser;
    public String smtpPassword;

    public String igUser;
    public String igPassword;

}
