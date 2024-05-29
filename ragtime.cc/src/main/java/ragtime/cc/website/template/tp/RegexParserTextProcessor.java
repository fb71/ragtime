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
package ragtime.cc.website.template.tp;

import org.apache.commons.io.output.StringBuilderWriter;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.website.template.TemplateContentProviderBase.TemplateLoader;

/**
 *
 * @author Falko Bräutigam
 */
public abstract class RegexParserTextProcessor
        implements TextProcessor {

    private static final Log LOG = LogFactory.getLog( RegexParserTextProcessor.class );

    /**
     *
     */
    protected String processFtl( String ftl, Context ctx ) throws Exception {
        var cfg = TemplateLoader.configuration( ctx.config );
        var template = cfg.getTemplate( ftl );

        var out = new StringBuilderWriter();
        template.process( ctx.data, out );
        return out.getBuilder().toString();
    }

}
