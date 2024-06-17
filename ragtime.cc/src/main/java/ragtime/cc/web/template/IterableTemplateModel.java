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

import java.util.Iterator;

import areca.common.base.Function.RFunction;
import areca.common.base.Sequence;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;

/**
 * {@link Iterable} -> {@link TemplateCollectionModel}
 *
 * @author Falko Bräutigam
 */
public class IterableTemplateModel<T extends TemplateModel, C extends Iterable<T>>
        implements TemplateCollectionModel {

    public final C delegate;

    public IterableTemplateModel( C p ) {
        this.delegate = p;
    }

    @SuppressWarnings( "unchecked" )
    public <S> IterableTemplateModel( Iterable<S> p, RFunction<S,T> transform ) {
        this( (C)Sequence.of( p ).map( transform ).asIterable() );
    }

    @Override
    public TemplateModelIterator iterator() throws TemplateModelException {
        return new TemplateModelIterator() {
            Iterator<T> it = delegate.iterator();
            @Override
            public TemplateModel next() throws TemplateModelException {
                return it.next();
            }
            @Override
            public boolean hasNext() throws TemplateModelException {
                return it.hasNext();
            }
        };
    }
}
