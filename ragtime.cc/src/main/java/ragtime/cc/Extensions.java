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

import java.util.ArrayList;
import java.util.List;

import areca.common.Assert;
import areca.common.base.Sequence;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 * Very simple extension mechanism. Later we may use pf4j.org.
 *
 * @author Falko Bräutigam
 */
public class Extensions {

    private static final Log LOG = LogFactory.getLog( Extensions.class );

    private static List<Class<?>> extensions = new ArrayList<>();

    public static void register( Class<?> type ) {
        Assert.that( !extensions.contains( type ) );
        extensions.add( type );
    }

    @SuppressWarnings( "unchecked" )
    public static final <R> List<R> ofType( Class<R> type ) {
        try {
            return Sequence.of( Exception.class, extensions )
                    .filter( type::isAssignableFrom )
                    .map( ex -> (R)ex.getConstructor().newInstance() )
                    .toList();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
}
