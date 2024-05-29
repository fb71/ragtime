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

import java.util.Map;

import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;

import org.apache.commons.io.input.CharSequenceReader;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 * Markdown parse and render functionality.
 *
 * @author Falko Bräutigam
 */
public class Markdown
        implements TextProcessor {

    private static final Log LOG = LogFactory.getLog( Markdown.class );

    /* Both are thread-safe */
    public static Parser         parser = Parser.builder().build();

    public static HtmlRenderer   renderer = HtmlRenderer.builder()
            .attributeProviderFactory( new AttributeFactory() )
            .escapeHtml( false )
            .build();

    /**
     * Renders the given markdown to HTML.
     */
    public static String render( String markdown ) {
        Node doc = parser.parse( markdown );
        return renderer.render( doc );
    }

    /**
     * Adds target="_blanc" to {@link Link} nodes.
     */
    protected static class AttributeFactory
            implements AttributeProviderFactory {

        @Override
        public AttributeProvider create( AttributeProviderContext context ) {
            return new AttributeProvider() {
                @Override
                public void setAttributes( Node node, String tagName, Map<String,String> attributes ) {
                    if (node instanceof Link l) {
                        if (l.getDestination().startsWith( "http" )) {
                            attributes.put( "target", "_blank" );
                            attributes.put( "rel", "noopener" );
                        }
                    }
                }
            };
        }
    }

    // instance *******************************************

    @Override
    public void process( StringBuilder content, Context ctx ) throws Exception {
        Node doc = parser.parseReader( new CharSequenceReader( content ) );
        content.setLength( 0 );
        renderer.render( doc, content );
    }
}
