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
package ragtime.cc.website.template;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateModelException;

/**
 * Markdown parse and render functionality.
 *
 * @author Falko Bräutigam
 */
public class Markdown {

    private static final Log LOG = LogFactory.getLog( Markdown.class );

    public static Parser         parser = Parser.builder().build();

    public static HtmlRenderer   renderer = HtmlRenderer.builder().build();


    public static String render( String markdown ) throws TemplateModelException {
        Node document = parser.parse( markdown );
        return renderer.render( document );
    }
}
