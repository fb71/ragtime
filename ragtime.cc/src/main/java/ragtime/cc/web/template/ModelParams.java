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
package ragtime.cc.web.template;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import areca.common.Assert;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 *
 * @author Falko Bräutigam
 */
public class ModelParams
        extends HashMap<String,String> {

    private static final Log LOG = LogFactory.getLog( ModelParams.class );

    /**
     *
     */
    public void addHttpParams( HttpServletRequest request ) {
        for (var entry : request.getParameterMap().entrySet()) {
            put( entry.getKey(), entry.getValue()[0] );
        }
    }

    public void addMacroParams( String group ) {
        for (String kv : StringUtils.split( group, "," )) {
            put( substringBefore( kv, "=" ), substringAfter( kv, "=" ) );
        }
    }

    @Override
    public String get( Object key ) {
        return Assert.notNull( super.get( key ), "No such ModelParam: '" + key + "' (" + toString() + ")" );
    }

    public Opt<String> opt( Object key ) {
        return Opt.of( super.get( key ) );
    }

}
