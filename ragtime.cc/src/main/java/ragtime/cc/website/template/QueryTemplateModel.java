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

import org.polymap.model2.Entity;
import org.polymap.model2.query.Query;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;

/**
 *
 * @author Falko Bräutigam
 */
public class QueryTemplateModel
        implements TemplateCollectionModel {

    private static final Log LOG = LogFactory.getLog( QueryTemplateModel.class );

    protected Query<?>      query;


    public QueryTemplateModel( ModelParams modelParams, UnitOfWork uow ) throws ClassNotFoundException {
        @SuppressWarnings( "unchecked" )
        var entityType = (Class<? extends Entity>)Class.forName( modelParams.get( "type" ) );
        this.query = uow.query( entityType );
    }


    public QueryTemplateModel( Query<?> query ) {
        this.query = query;
    }


    @Override
    public TemplateModelIterator iterator() throws TemplateModelException {
        return new TemplateModelIterator() {

            private Iterator<? extends Entity> it = query.executeCollect().waitForResult().get().iterator();

            @Override
            public TemplateModel next() throws TemplateModelException {
                return new CompositeTemplateModel( it.next() );
            }

            @Override
            public boolean hasNext() throws TemplateModelException {
                return it.hasNext();
            }
        };
    }

}
