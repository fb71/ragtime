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
package ragtime.cc.web.template.widgets;

import java.util.regex.Pattern;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 *
 * @author Falko Bräutigam
 */
public class fb71
        implements TextProcessor {

    private static final Log LOG = LogFactory.getLog( fb71.class );

    public static final Pattern FB71 = Pattern.compile( ":+fb71:+" );

    @Override
    public void process( StringBuilder content, Context ctx ) throws Exception {
        var match = FB71.matcher( content );
        for (var pos = 0; match.find( pos ); pos = match.start()) {
            content.delete( match.start(), match.end() );
            content.insert( match.start(), "fb71.net" );
        }
    }

}
