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
package ragtime.cc.web.http;

import org.apache.commons.io.IOUtils;

import areca.common.Promise;
import areca.common.base.Sequence;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 * Provides robots.txt and sitemap.txt to the {@link WebsiteServlet}.
 *
 * @deprecated In favour of robots.txt.ftl
 * @author Falko Bräutigam
 */
public class SitemapContentProvider
        implements ContentProvider {

    private static final Log LOG = LogFactory.getLog( SitemapContentProvider.class );

    public static boolean canProcess( Request request ) {
        var name = Sequence.of( request.path ).last().get();
        return name.startsWith( "robots" ) || name.startsWith( "sitemap" );
    }


    @Override
    public Promise<Boolean> process( Request request ) throws Exception {
        var name = Sequence.of( request.path ).last().get();
        var content = switch (name) {
            case "robots.txt" -> processRobotsTxt( request );
            case "sitemap.txt" -> processSitemap( request );
            default -> throw new RuntimeException( "Unexpected value: " + request.path[0] );
        };
        try (var out = request.httpResponse.getOutputStream()) {
            IOUtils.write( content, out, "UTF-8" );
        }
        return done( true );
    }


    private String processSitemap( Request request ) {
        request.httpResponse.setContentType( "text/plain" );
        return "";
    }


    protected String processRobotsTxt( Request request ) {
        request.httpResponse.setContentType( "text/plain" );
        var hostname = request.httpRequest.getHeader( "X-Forwarded-Host" ) != null
                ? request.httpRequest.getHeader( "X-Forwarded-Host" )
                : request.httpRequest.getServerName();
        var port = request.httpRequest.getHeader( "X-Forwarded-Port" ) != null
                ? request.httpRequest.getHeader( "X-Forwarded-Port" )
                : request.httpRequest.getServerPort();

        return "User-agent: * \n"
                + "Allow: /\n"
                + "Sitemap: https://" + hostname + ":" + port + "/sitemap.txt";
    }
}
