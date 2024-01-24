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

import org.polymap.model2.Composite;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import ragtime.cc.model.Format;
import ragtime.cc.model.Format.FormatType;

/**
 * A {@link TemplateModel} of a {@link Composite}.
 *
 * @author Falko Bräutigam
 */
public class CompositeTemplateModel
        implements TemplateHashModel {

    private static final Log LOG = LogFactory.getLog( CompositeTemplateModel.class );

    protected static Parser         markdownParser = Parser.builder().build();

    protected static HtmlRenderer   markdownRenderer = HtmlRenderer.builder().build();

    // instance *******************************************

    protected Composite composite;


    public CompositeTemplateModel( Composite composite ) {
        this.composite = composite;
    }

    @Override
    public TemplateModel get( String key ) throws TemplateModelException {
        // "id"
        if (key.equals( "id" )) {
            return new SimpleScalar( ((Entity)composite).id().toString() );
        }
        var prop = composite.info().getProperty( key );
        // single
        if (prop.getMaxOccurs() == 1) {
            Property<?> p = (Property<?>)prop.get( composite );
            // String
            if (String.class.isAssignableFrom( prop.getType() )) {
                var a = prop.getAnnotation( Format.class );
                var format = a != null ? a.value() : FormatType.PLAIN;
                switch (format) {
                    case MARKDOWN: {
                        Node document = markdownParser.parse( (String)p.get() );
                        return new SimpleScalar( markdownRenderer.render( document ) );
                    }
                    case PLAIN:
                        return new SimpleScalar( (String)p.get() );
                    default:
                        throw new RuntimeException( "Not yet: " + format );
                }
            }
            // Composite
            else if (Composite.class.isAssignableFrom( prop.getType() )) {
                return new CompositeTemplateModel( (Composite)p.get() );
            }
        }
        throw new RuntimeException( "Not yet: " + prop);
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return composite.info().getProperties().isEmpty();
    }

}
