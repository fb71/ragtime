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

import javax.servlet.http.HttpServletRequest;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Provides the params and headers from the given {@link HttpServletRequest}.
 *
 * @author Falko Bräutigam
 */
public class HttpRequestParamsTemplateModel
        implements TemplateHashModel {

    private static final Log LOG = LogFactory.getLog( HttpRequestParamsTemplateModel.class );

    private HttpServletRequest request;

    public HttpRequestParamsTemplateModel( HttpServletRequest request ) {
        this.request = request;
    }

    @Override
    public TemplateModel get( String key ) throws TemplateModelException {
        String value = request.getParameter( key );
        if (value == null) {
            value = request.getHeader( key );
        }
        LOG.debug( "%s: %s", key, value );
        return new SimpleScalar( value );
    }

    @Override
    public boolean isEmpty() throws TemplateModelException {
        return request.getParameterMap().isEmpty() && request.getHeaderNames().hasMoreElements();
    }

}
