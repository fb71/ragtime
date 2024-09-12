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

import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import areca.common.base.Opt;
import areca.common.base.Supplier.RSupplier;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateModel;
import ragtime.cc.model.MediaEntity;
import ragtime.cc.web.template.CompositeTemplateModel;
import ragtime.cc.web.template.IterableTemplateModel;

/**
 *
 * @author Falko Bräutigam
 */
public class Swiper
        extends RegexParserTextProcessor {

    private static final Log LOG = LogFactory.getLog( Swiper.class );

    public static final Pattern CAROUSEL = Pattern.compile(
            "::swiper"
            + "([?,]?w=[0-9]+)?"
            + "([?,]?h=[0-9]+)?"
            + "::" );

    protected RSupplier<List<MediaEntity>> medias;

    public Swiper( RSupplier<List<MediaEntity>> medias ) {
        this.medias = medias;
    }

    @Override
    public void process( StringBuilder content, Context ctx ) throws Exception {
        var match = CAROUSEL.matcher( content );
        for (var pos = 0; match.find( pos ); pos = match.start()) {
            var size = new HashMap<String,TemplateModel>();
            size.put( "width", new SimpleNumber( 380 ) );
            size.put( "height", new SimpleNumber( 380 ) );
            Opt.of( match.group( 1 ) ).ifPresent( g -> {
                size.put( "width", new SimpleNumber( Integer.valueOf( substringAfter( g, "w=" ) ) ) );
            });
            Opt.of( match.group( 2 ) ).ifPresent( g -> {
                size.put( "height", new SimpleNumber( Integer.valueOf( substringAfter( g, "h=" ) ) ) );
            });
            ctx.data.put( "size", size );
            ctx.data.put( "medias", new IterableTemplateModel<>( medias.get(), CompositeTemplateModel::new ) );

            content.delete( match.start(), match.end() );
            content.insert( match.start(), processFtl( "swiper.ftl", ctx ) );
        }
    }

}
