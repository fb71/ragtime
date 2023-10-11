/*
 * Copyright (C) 2023, the @authors. All rights reserved.
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
package ragtime.app.ai;

import java.io.IOException;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.json.JSON;

import areca.common.Platform;
import areca.common.Promise;
import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 *
 *
 * @see <a href="https://platform.openai.com/docs/api-reference/">OpenAI API Ref</a>
 * @author Falko Bräutigam
 */
public class OAIImageLab
        extends ImageLab {

    private static final Log LOG = LogFactory.getLog( OAIImageLab.class );

    private static final String KEY1 = "sk-Z8kFZ6bj2pO3j2LNmIwAT";
    private static final String KEY2 = "3BlbkFJ2LMDQl59lwfdxEZ5e3JP";
    public static final String KEY = KEY1 + KEY2;

    // instance *******************************************

    private String      apikey;

    public OAIImageLab( String apikey ) {
        this.apikey = apikey;
    }

    /**
     * @see <a href="https://platform.openai.com/docs/api-reference/images/create">API Ref</a>
     */
    @Override
    public Promise<String> generateImage( String prompt, int n ) {
        var t = Timer.start();
        return Platform.xhr( "POST", "http?uri=https://api.openai.com/v1/images/generations" )
                .addHeader( "Content-Type", "application/json" )
                .addHeader( "Authorization", "Bearer " + apikey )
                .submit( JSON.stringify( GenerateImageRequestBody.defaults( prompt ) ) )
                .map( httpResp -> {
                    LOG.info( "Status: " + httpResp.status() + " (" + t.elapsedHumanReadable() + ")" );
                    if (httpResp.status() >= 300) {
                        throw new IOException( "Unexpected HTTP status code: " + httpResp.status() );
                    }
                    GenerateImageResponseBody resp = JSON.parse( httpResp.text() ).cast();
                    return resp.data()[0].b64_json();
                });
    }

    /**
     *
     */
    protected static abstract class GenerateImageRequestBody
            implements JSObject {

        public static GenerateImageRequestBody defaults( String prompt ) {
            var result = create();
            result.setPrompt( prompt );
            result.setN( 1 );
            result.setSize( "256x256" );
            result.setResponseFormat( "b64_json" );  // url
            return result;
        }

        @JSBody( script = "return {};")
        public static native GenerateImageRequestBody create();

        @JSProperty
        public abstract void setPrompt( String prompt );

        @JSProperty
        public abstract void setN( int n );

        @JSProperty
        public abstract void setSize( String size );

        @JSProperty( value = "response_format" )
        public abstract void setResponseFormat( String responseFormat );
    }

    /**
     *
     */
    protected static abstract class GenerateImageResponseBody
            implements JSObject {

        @JSProperty(value = "created")
        public abstract int created();

        @JSProperty(value = "data")
        public abstract GenerateImageResponseData[] data();
    }

    /**
     *
     */
    protected static abstract class GenerateImageResponseData
            implements JSObject {

        @JSProperty(value = "url")
        public abstract String url();

        @JSProperty(value = "b64_json")
        public abstract String b64_json();
    }
}
