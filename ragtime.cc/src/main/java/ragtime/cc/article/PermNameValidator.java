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
package ragtime.cc.article;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.transform.Validator;
import ragtime.cc.model.Article;
import ragtime.cc.model.PermNameConcern;
import ragtime.cc.model.TopicEntity;

/**
 *
 * @author Falko Bräutigam
 */
public class PermNameValidator
        extends Validator<String> {

    private static final Log LOG = LogFactory.getLog( PermNameValidator.class );

    private UnitOfWork uow;

    public PermNameValidator( UnitOfWork uow, Model<String> delegate ) {
        super( delegate );
        this.uow = uow;
    }

    @Override
    public ValidationResult validate( String value ) {
        if (StringUtils.isBlank( value )) {
            return new ValidationResult( "Titel darf nicht leer sein." );
        }
        var permName = PermNameConcern.permName( value );
        var count = new MutableInt();
        var t = Timer.start();
        return uow.query( Article.class )
                .where( Expressions.eq( Article.TYPE.permName, permName ) )
                .executeCollect()
                .then( rs -> {
                    count.add( rs.size() );

                    return uow.query( TopicEntity.class )
                            .where( Expressions.eq( TopicEntity.TYPE.permName, permName ) )
                            .executeCollect();
                })
                .map( rs -> {
                    count.add( rs.size() );
                    LOG.info( "%s -> '%s' : %s (%s)", value, permName, count.getValue(), t );
                    return count.getValue() > 0
                            ? new ValidationResult( "URL existiert bereits: " + permName )
                            : null;
                })
                .waitForResult().orElse( VALID );
    }

}
