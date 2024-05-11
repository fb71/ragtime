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
package ragtime.cc.website.template;

import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import areca.common.base.Sequence;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 * Represents a template (folder) in the classpath.
 *
 * @author Falko Bräutigam
 */
public class TemplateInfo {

    private static final Log LOG = LogFactory.getLog( TemplateInfo.class );

    public static final String      TEMPLATES_BASE_PATH = "templates";

    private static final String     TEMPLATE_INFO_FILE = "template.info.json";

    private static Map<String,TemplateInfo> infos = new HashMap<>();

    static {
        var gson = new GsonBuilder().create();
        // XXX tomcat!? :(
        //var folders = IOUtils.readLines( cl().getResourceAsStream( TEMPLATES_BASE_PATH + "/" ), "UTF8" );
        var folders = new ArrayList<String>() {{
            addAll( TemplateContentProvider.templates );
            addAll( TopicTemplateContentProvider.templates );
        }};

        for (var folder : folders) {
            try {
                var res = cl().getResource( join( "/", TEMPLATES_BASE_PATH, folder, TEMPLATE_INFO_FILE ) );
                var info = gson.fromJson( IOUtils.toString( res, "UTF8" ), TemplateInfo.class );
                info.name = folder;
                infos.put( info.name, info );
                LOG.info( "Template: %s/\t'%s'", info.name, info.title );
            }
            catch (JsonSyntaxException | IOException e) {
                e.printStackTrace();
                throw new RuntimeException( e );
            }
        }
    }

    public static Sequence<TemplateInfo,RuntimeException> all() {
        return Sequence.of( infos.values() );
    }

    /** All templates a user can select */
    public static Sequence<TemplateInfo,RuntimeException> user() {
        return all().filter( t -> StringUtils.isNotBlank( t.title ) );
    }

    /**
     *
     */
    public static TemplateInfo forName( String name ) {
        return infos.get( name );
    }

    protected static ClassLoader cl() {
        //return TemplateInfo.class.getClassLoader();
        return Thread.currentThread().getContextClassLoader();
    }

    // instance *******************************************

    /** The name (of the folder) of this template. */
    public String name;

    public String title;

    public String description;

    @SerializedName( "extends" )
    public String extends_;

    public URL resource( @SuppressWarnings( "hiding" ) String name ) {
        return cl().getResource( join( "/", TEMPLATES_BASE_PATH, this.name, name ) );
    }

    public TemplateInfo parent() {
        return hasParent() ? forName( extends_ ) : null;
    }

    public boolean hasParent() {
        return isNotBlank( extends_ );
    }
}
