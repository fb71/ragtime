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

import static org.polymap.model2.query.Expressions.eq;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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

        var findMedia = StringUtils.containsOnly( name, "0123456789" )
                 ? request.uow.entity( MediaEntity.class, name )
                 : request.uow.query( MediaEntity.class ).where( eq( MediaEntity.TYPE.name, name ) ).singleResult();

        return findMedia.map( media -> {
            request.httpResponse.setContentType( media.mimetype.get() );
            try (
                var in = IOUtils.buffer( media.in() );
                var out = request.httpResponse.getOutputStream();
            ){
                //
                if (request.httpRequest.getParameterMap().isEmpty()) {
                    IOUtils.copy( in, out );
                }
                // thumbnail
                else {
                    var w = Integer.parseInt( request.httpRequest.getParameter( "w" ) );
                    var h = Integer.parseInt( request.httpRequest.getParameter( "h" ) );
                    var encoder = StringUtils.substringAfterLast( media.mimetype.get(), "/" );

                    var bytes = media.thumbnail().size( w, h ).outputFormat( encoder ).create().waitForResult().get();
                    out.write( bytes );
                }
            }
            return true;
        });
    }

}
