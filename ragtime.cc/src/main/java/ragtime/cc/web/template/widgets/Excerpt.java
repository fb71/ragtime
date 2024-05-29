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

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 *
 * @author Falko Bräutigam
 */
public class Excerpt
        extends RegexParserTextProcessor {

    private static final Log LOG = LogFactory.getLog( Excerpt.class );

    public static final String EXCERPT = "::excerpt::";

    protected String replacement;


    public Excerpt( String replacement ) {
        this.replacement = replacement;
    }


    @Override
    public void process( StringBuilder content, Context ctx ) throws Exception {
        var index = content.indexOf( EXCERPT );
        if (index >= 0) {
            if (replacement != null) {
                content.delete( index, content.length() );
                content.insert( index, replacement );
            }
            else {
                content.delete( index, index + EXCERPT.length() );
            }
        }
    }

}
