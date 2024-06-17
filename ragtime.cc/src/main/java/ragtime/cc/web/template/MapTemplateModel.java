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

import java.util.Map;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 *
 * @author Falko Bräutigam
 */
public class MapTemplateModel
        implements TemplateHashModel {

    private static final Log LOG = LogFactory.getLog( MapTemplateModel.class );

    public Map<String,TemplateModel> delegate;


    public MapTemplateModel( Map<String,TemplateModel> delegate ) {
        this.delegate = delegate;
    }

    @Override
    public TemplateModel get( String key ) throws TemplateModelException {
        return delegate.get( key );
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return delegate.isEmpty();
    }

}
