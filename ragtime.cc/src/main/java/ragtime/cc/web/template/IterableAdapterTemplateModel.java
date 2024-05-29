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
public class IterableAdapterTemplateModel<T extends TemplateModel>
        implements TemplateCollectionModel {

    private final Iterable<T> p;

    public IterableAdapterTemplateModel( Iterable<T> p ) {
        this.p = p;
    }

    public <S> IterableAdapterTemplateModel( Iterable<S> p, RFunction<S,T> transform ) {
        this( Sequence.of( p ).map( c -> transform.apply( c ) ).asIterable() );
    }

    @Override
    public TemplateModelIterator iterator() throws TemplateModelException {
        return new TemplateModelIterator() {
            Iterator<T> it = p.iterator();
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
