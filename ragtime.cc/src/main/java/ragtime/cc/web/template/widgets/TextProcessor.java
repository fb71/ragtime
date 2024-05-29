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
package ragtime.cc.web.template.widgets;

import java.util.HashMap;
import java.util.Map;

import ragtime.cc.model.Article;
import ragtime.cc.web.model.TemplateConfigEntity;

/**
 * Processing (change, replace, decorate) {@link Article} content *before* the main
 * template (*.ftl) is processed.
 *
 * @author Falko Bräutigam
 */
public interface TextProcessor {

    /**
     * Carries the context of {@link TextProcessor#process(StringBuilder, Context)}
     */
    public class Context {
        public TemplateConfigEntity config;
        public Map<Object,Object> data = new HashMap<>();
    }

    /**
     *
     */
    public void process( StringBuilder content, Context ctx ) throws Exception;

}
