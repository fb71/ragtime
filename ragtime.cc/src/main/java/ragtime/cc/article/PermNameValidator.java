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

import static org.polymap.model2.query.Expressions.and;
import static org.polymap.model2.query.Expressions.eq;
import static org.polymap.model2.query.Expressions.id;
import static org.polymap.model2.query.Expressions.not;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;

import org.polymap.model2.Entity;
import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.viewer.model.Model;
import areca.ui.viewer.transform.Validator;
import ragtime.cc.model.Article;
import ragtime.cc.model.Common;
import ragtime.cc.model.PermNameConcern;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.http.WebsiteServlet;

/**
 *
 * @author Falko Bräutigam
 */
public class PermNameValidator
        extends Validator<String> {

    private static final Log LOG = LogFactory.getLog( PermNameValidator.class );

    private Common entity;

    /**
     *
     *
     * @param entity The {@link Entity} we are checking the permName of
     * @param delegate
     */
    public PermNameValidator( Common entity, Model<String> delegate ) {
        super( delegate );
        this.entity = entity;
    }

    @Override
    public ValidationResult validate( String value ) {
        if (StringUtils.isBlank( value )) {
            return new ValidationResult( "Titel darf nicht leer sein." );
        }
        var permName = PermNameConcern.permName( value );
        if (permName.equals( WebsiteServlet.PATH_HOME )) {
            return new ValidationResult( "'" + WebsiteServlet.PATH_HOME + "' ist reserviert und darf nicht benutzt werden." );
        }
        var count = new MutableInt();
        var t = Timer.start();
        var uow = entity.context.getUnitOfWork();
        return uow.query( Article.class )
                .where( and(
                        eq( Article.TYPE.permName, permName ),
                        not( id( entity ) )
                        ))
                .executeCollect()
                .then( rs -> {
                    count.add( rs.size() );

                    return uow.query( TopicEntity.class )
                            .where( and(
                                    eq( TopicEntity.TYPE.permName, permName ),
                                    not( id( entity ) )
                                    ))
                            .executeCollect();
                })
                .map( rs -> {
                    count.add( rs.size() );
                    LOG.info( "%s -> '%s' : %s (%s)", value, permName, count.getValue(), t );
                    return count.getValue() > 0
                            ? new ValidationResult( "URL existiert bereits: " + permName )
                            : null;
                })
                .waitForResult().orElse( VALID ); // XXX waiting
    }

}
