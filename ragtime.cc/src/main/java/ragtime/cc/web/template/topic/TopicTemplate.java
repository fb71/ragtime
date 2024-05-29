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
package ragtime.cc.web.template.topic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import areca.common.Promise;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import freemarker.template.TemplateNotFoundException;
import ragtime.cc.model.TopicEntity;
import ragtime.cc.web.http.ContentProvider.Request;
import ragtime.cc.web.model.TopicTemplateConfigEntity;

/**
 *
 * @author Falko Bräutigam
 */
public abstract class TopicTemplate {

    private static final Log LOG = LogFactory.getLog( TopicTemplate.class );

    private static final Map<String,Class<? extends TopicTemplate>> available = new HashMap<>() {{
        put( "Basic", BasicTopicTemplate.class );
        put( "Tiles", TilesTopicTemplate.class );
    }};

    /**
     * All available topic template names.
     */
    public static Set<String> availableNames() {
        return available.keySet();
    }

    /**
     * Creates a new instance of the {@link TopicTemplate} with the given name.
     */
    public static Opt<TopicTemplate> forName( String name ) {
        try {
            return Opt.of( available.get( name ) ).map( cl -> cl.getDeclaredConstructor().newInstance() );
        }
        catch (ReflectiveOperationException e) {
            throw new RuntimeException( e );
        }
    }

    /**
     *
     */
    public static class Site {
        public Request r;
        public TopicEntity topic;
        public TopicTemplateConfigEntity config;
        public Map<Object,Object> data;
    }

    // instance *******************************************

    public abstract String label();

    /**
     *
     *
     * @param site
     * @return The *.ftl template to load and process.
     * @throws TemplateNotFoundException
     * @throws Exception
     */
    public abstract Promise<String> process( Site site ) throws TemplateNotFoundException, Exception;

}
