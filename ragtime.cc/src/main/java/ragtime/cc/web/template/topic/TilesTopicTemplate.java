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
package ragtime.cc.web.template.topic;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 *
 * @author Falko Bräutigam
 */
public class TilesTopicTemplate
        extends BasicTopicTemplate {

    private static final Log LOG = LogFactory.getLog( TilesTopicTemplate.class );

    @Override
    public String label() {
        return "Tiles";
    }

    @Override
    protected Promise<String> processTopic() {
        return super.processTopic().map( __ -> "tiles.ftl" );
    }

}
