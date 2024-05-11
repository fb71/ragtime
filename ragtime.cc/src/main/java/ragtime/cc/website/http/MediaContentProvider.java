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
package ragtime.cc.website.http;

import org.apache.commons.io.IOUtils;
import org.polymap.model2.query.Expressions;

import areca.common.Promise;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import ragtime.cc.model.MediaEntity;

/**
 * Provides {@link MediaEntity} content to the {@link WebsiteServlet}.
 *
 * @author Falko Bräutigam
 */
public class MediaContentProvider
        implements ContentProvider {

    private static final Log LOG = LogFactory.getLog( MediaContentProvider.class );

    public static final String PATH = "media";

    @Override
    public Promise<Boolean> process( Request request ) throws Exception {
        var name = String.join(  "/", request.path );
        return request.uow.query( MediaEntity.class )
                .where( Expressions.eq( MediaEntity.TYPE.name, name ) )
                .singleResult()
                .map( media -> {
                    request.httpResponse.setContentType( media.mimetype.get() );
                    try (
                        var in = media.in();
                        var out = request.httpResponse.getOutputStream();
                    ) {
                        IOUtils.copy( in, out );
                    }
                    return true;
                });
    }

}
