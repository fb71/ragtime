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

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import org.polymap.model2.CollectionProperty;
import org.polymap.model2.Composite;
import org.polymap.model2.Entity;
import org.polymap.model2.Property;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.Color;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
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

    protected Composite composite;

    /**
     * Just for subclass initialization.
     */
    protected CompositeTemplateModel() {
    }


    public CompositeTemplateModel( Composite composite ) {
        this.composite = composite;
    }


    @Override
    public TemplateModel get( String _key ) throws TemplateModelException {
        var key = StringUtils.substringBefore( _key, "@" );
        var convert = StringUtils.substringAfter( _key, "@" );
        //LOG.debug( "%s -> %s@%s", _key, key, convert );

        // "id"
        if (key.equals( "id" )) {
            return new SimpleScalar( ((Entity)composite).id().toString() );
        }
        var prop = composite.info().getProperty( key );
        // single
        if (prop.getMaxOccurs() == 1) {
            var p = (Property<?>)prop.get( composite );
            // String
            if (String.class.isAssignableFrom( prop.getType() )) {
                var a = prop.getAnnotation( Format.class );
                var format = a != null ? a.value() : FormatType.PLAIN;
                switch (format) {
                    case MARKDOWN: return new SimpleScalar( Markdown.render( (String)p.get() ) );
                    case PLAIN: return new SimpleScalar( convert( p.get(), convert ) );
                    default: throw new RuntimeException( "Not yet: " + format );
                }
            }
            // Composite
            else if (Composite.class.isAssignableFrom( prop.getType() )) {
                return new CompositeTemplateModel( (Composite)p.get() );
            }
        }
        // list
        else {
            var p = (CollectionProperty<?>)prop.get( composite );
            // Composite
            if (Composite.class.isAssignableFrom( prop.getType() )) {
                return new TemplateCollectionModel() {
                    @Override
                    public TemplateModelIterator iterator() throws TemplateModelException {
                        return new TemplateModelIterator() {
                            Iterator it = p.iterator();
                            @Override
                            public TemplateModel next() throws TemplateModelException {
                                return new CompositeTemplateModel( (Composite)it.next() );
                            }
                            @Override
                            public boolean hasNext() throws TemplateModelException {
                                return it.hasNext();
                            }
                        };
                    }
                };
            }
        }
        throw new RuntimeException( "Not yet: " + prop);
    }

    /**
     * First simple attempt to support type and other conversions.
     */
    protected String convert( Object value, String convert ) {
        if (convert.equalsIgnoreCase( "rgb" )) {
            var c = Color.ofHex( (String)value );
            return String.format( "%s,%s,%s", c.r, c.g, c.b );
        }
        else {
            return (String)value;
        }
    }


    @Override
    public boolean isEmpty() throws TemplateModelException {
        return composite.info().getProperties().isEmpty();
    }

}
