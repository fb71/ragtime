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

import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.Queryable;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class AccountEntity
        extends Common {

    private static final Log LOG = LogFactory.getLog( AccountEntity.class );

    public static final ClassInfo<AccountEntity> info = AccountEntityClassInfo.instance();

    public static AccountEntity TYPE;

    @Queryable
    public Property<Integer>    permid;

    @Queryable
    @Nullable
    public Property<String>     domainName;

    @Queryable
    public Property<String>     login;

    protected Property<String>  pwdHash;

    protected Property<String>  pwdSalt;

    public Property<Boolean>    isAdmin;

    public AccountEntity setPassword( String pwd ) {
        var encrypted = PasswordEncryption.encrypt( pwd );
        pwdHash.set( encrypted.hash );
        pwdSalt.set( encrypted.salt );
        //LOG.info( "PWD: pwd=%s, hash=%s, salt=%s", pwd, encrypted.hash, encrypted.salt );
        return this;
    }

    public boolean checkPassword( String pwd ) {
        var hash = PasswordEncryption.doEncrypt( pwd, pwdSalt.get() );
        //LOG.info( "CHECK: pwd=%s, hash=%s, salt=%s", pwd, hash, pwdSalt.get() );
        return pwdHash.get().equals( hash );
    }
}
