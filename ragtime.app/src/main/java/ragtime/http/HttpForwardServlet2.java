/*
 * Copyright (C) 2021, the @authors. All rights reserved.
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
package ragtime.http;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Arrays;
import java.util.Base64;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * https://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
 *
 * @author Falko Bräutigam
 */
public class HttpForwardServlet2 extends HttpServlet {

    @Override
    public void init() throws ServletException {
        log( "" + getClass().getSimpleName() + " init..." );
    }

    int c = 0; // ich bin müde :)

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        try {
            var uri = new StringBuilder( req.getParameter( "uri" ) );

            c = 0;
            req.getParameterMap().forEach( (key,values) -> {
                if (!key.equals( "uri" )) {
                    uri.append( c++ == 0 ? "?" : "&" ).append( String.format( "%s=%s", key, values[0] ) );
                }
            });
            debug( "URI: %s %s", req.getMethod(), uri );

            // connection
            HttpURLConnection conn = (HttpURLConnection)URI.create( uri.toString() ).toURL().openConnection();

            // authentication
            if (req.getHeader( "X-auth-username" ) != null) {
                String auth = req.getHeader( "X-auth-username" ) + ":" + req.getHeader( "X-auth-password" );
                byte[] encodedAuth = Base64.getEncoder().encode( auth.getBytes( UTF_8 ) );
                String basic = "Basic " + new String( encodedAuth );
                conn.setRequestProperty( "Authorization", basic );
            }

            // headers
            req.getHeaderNames().asIterator().forEachRemaining( name -> {
                if (!name.startsWith( "X" )) {
                    conn.setRequestProperty( name, req.getHeader( name ) );
                    //debug( "Request Header: %s: %s", name, req.getHeader( name ) );
                }
            });

            // method -> send
            conn.setRequestMethod( req.getMethod() );
            if (Arrays.asList("POST", "REPORT", "PUT").contains( req.getMethod() )) {
                conn.setDoOutput( true );
                copyAndClose( req.getInputStream(), conn.getOutputStream() );
            }

            resp.setStatus( conn.getResponseCode() );
            conn.getHeaderFields().forEach( (name,values) -> {
                //debug( "Response Header: %s: %s", name, values );
                if (name == null || name.equals( "WWW-Authenticate" )) {
                    // return 401 code but suppress the WWW-Authenticate header
                    // in order to prevent browser popup asking for credentials
                }
                else {
                    resp.addHeader( name, values.get( 0 ) ); // FIXME
                }
            });
            if (conn.getResponseCode() < 299) {
                if ("BASE64".equals( req.getParameter( "_encode_" ) )) {
                    debug( "Encode: BASE64..." );
                    var bytes = new ByteArrayOutputStream( 4*4096 );
                    var in = new BufferedInputStream( req.getInputStream() );
                    for (var b = in.read(); b > -1; b = in.read()) {
                        bytes.write( b );
                    }
                    copyAndClose(
                            new ByteArrayInputStream( Base64.getEncoder().encode( bytes.toByteArray() ) ),
                            resp.getOutputStream() );
                }
                else {
                    copyAndClose( conn.getInputStream(), resp.getOutputStream() );
                }
            }
            else {
                copyAndClose( conn.getErrorStream(), resp.getOutputStream() );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected static void copyAndClose( InputStream in, OutputStream out) throws IOException {
        try (
            var _in = in;
            var _out = out
        ) {
            var buf = new byte[4096];
            for (int c = in.read( buf ); c != -1; c = in.read( buf )) {
                out.write( buf, 0, c );
            }
            out.flush();
        }
    }


    protected void debug( String msg, Object... args ) {
        System.out.println( args.length == 0 ? msg : String.format( msg, args ) );
    }

}
