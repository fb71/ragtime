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

import java.util.Arrays;
import java.util.List;

import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.website.model.TemplateConfigEntity;

/**
 * Processes all "standard" processors.
 *
 * @author Falko Bräutigam
 */
public class TextProcessors
        implements TextProcessor {

    private static final Log LOG = LogFactory.getLog( TextProcessors.class );

    private List<Class<? extends TextProcessor>> cls = Arrays.asList(
            Markdown.class,
            Swiper.class );

    /**
     *
     */
    public static CharSequence process( CharSequence content, TemplateConfigEntity config ) throws Exception {
        var ctx = new Context();
        ctx.config = config;
        var sb = new StringBuilder( content );
        new TextProcessors().process( new StringBuilder( content ), ctx );
        return sb;
    }

    // instance *******************************************

    @Override
    public void process( StringBuilder content, Context ctx ) throws Exception {
        for (Class<? extends TextProcessor> cl : cls) {
            var t = Timer.start();
            var processor = cl.getConstructor().newInstance();
            processor.process( content, ctx );
            LOG.info( "Processor: %s [%s]", cl.getSimpleName(), t );
        }
    }

}
