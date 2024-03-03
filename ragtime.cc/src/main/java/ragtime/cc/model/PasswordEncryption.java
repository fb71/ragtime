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
package ragtime.cc.model;

import java.util.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.polymap.model2.PropertyConcern;
import org.polymap.model2.PropertyConcernAdapter;

import areca.common.base.Lazy;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class PasswordEncryption
        extends PropertyConcernAdapter<String>
        implements PropertyConcern<String> {

    private static final Log LOG = LogFactory.getLog( PasswordEncryption.class );

    public static final ClassInfo<PasswordEncryption> info = PasswordEncryptionClassInfo.instance();

    private static Charset charset = StandardCharsets.ISO_8859_1;

    private static SecureRandom random = new SecureRandom();

    private static Lazy<SecretKeyFactory,Exception> skf = new Lazy<>( () -> SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA1" ) );

    @Override
    public String get() {
        return _delegate().get();
    }

    @Override
    public void set( String value ) {
        throw new RuntimeException( "@Concern aspect not supported" );
        //_delegate().set( encrypt( value ) );
    }

    /**
     * Encrypts the given password.
     * @return Hash of the given password.
     */
    public static String doEncrypt( String password, String salt ) {
        try {
            var spec = new PBEKeySpec( password.toCharArray(), decode( salt ), 65536, 128 );
            var hash = skf.$().generateSecret( spec ).getEncoded();
            return encode( hash );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Encrypts the given password with a random salt.
     */
    public static Encrypted encrypt( String password ) {
        try {
            var salt = new byte[16];
            random.nextBytes( salt );

            var encrypted = doEncrypt( password, encode( salt ) );
            return new Encrypted( encrypted, encode( salt ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    public static String encode( byte[] b ) {
        return new String( Base64.getEncoder().encode( b ), charset );
    }

    public static byte[] decode( String s ) {
        return Base64.getDecoder().decode( s.getBytes( charset ) );
    }

    public static class Encrypted {
        public String hash;
        public String salt;

        protected Encrypted( String hash, String salt ) {
            this.hash = hash;
            this.salt = salt;
        }
    }
}