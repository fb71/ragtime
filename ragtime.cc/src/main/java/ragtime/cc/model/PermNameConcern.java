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

import java.text.Normalizer;

import org.polymap.model2.PropertyConcernAdapter;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import ragtime.cc.article.PermNameValidator;

/**
 *
 * @see PermNameValidator
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class PermNameConcern
        extends PropertyConcernAdapter<String> {

    private static final Log LOG = LogFactory.getLog( PermNameConcern.class );

    public static final ClassInfo<PermNameConcern> info = PermNameConcernClassInfo.instance();

    public static String permName( String value ) {
        var result = Normalizer.normalize( value, Normalizer.Form.NFD );
        result = result.replaceAll( "[^\\p{ASCII}]", "" );
        result = result.replaceAll( "[^a-zA-Z0-9]", "-" ).replaceAll( "--+", "-" );
        //result = result.toLowerCase( Locale.ENGLISH );
        LOG.debug( "%s -> %s", value, result );
        return result;
    }

    // instance *******************************************

    @Override
    public void set( String value ) {
        // unique: PermNameValidator
        _delegate().set( value != null ? permName( value ) : null );
    }

    /**
     * Test
     */
    public static void main( String[] args ) {
        var s = "Für Paare";
        System.out.println( "\n" + s + " -> '" + permName( s ) + "'" );

    }
}
