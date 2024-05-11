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
package ragtime.cc.website.template.tt;

import areca.common.Promise;
import areca.common.base.Sequence;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateNotFoundException;
import ragtime.cc.website.template.CompositeTemplateModel;
import ragtime.cc.website.template.IterableAdapterTemplateModel;
import ragtime.cc.website.template.TopicTemplate;

/**
 *
 * @author Falko Bräutigam
 */
public class BasicTopicTemplate
        extends TopicTemplate {

    private static final Log LOG = LogFactory.getLog( BasicTopicTemplate.class );

    @Override
    public String label() {
        return "Basic";
    }


    @Override
    public Promise<String> process( Site site ) throws TemplateNotFoundException {
        // topic home
        if (site.r.path.length == 1) {
            return site.topic.articles().map( articles -> {
                site.data.put( "articles", new IterableAdapterTemplateModel<>( articles, a -> new CompositeTemplateModel( a ) ) );

                var rike = Sequence.of( articles ).first( a -> a.title.get().equalsIgnoreCase( "rike" ) ).orElseError();
                site.data.put( "article", new CompositeTemplateModel( rike ) );
                return "basic.ftl";
            });
        }
        // article
        else if (site.r.path.length == 2) {
            throw new RuntimeException( "We need: " + site.r.path[1] );
        }
        else {
            throw new TemplateNotFoundException( "", null, "" );
        }
    }

}
