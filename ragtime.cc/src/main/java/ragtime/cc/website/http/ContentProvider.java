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
package ragtime.cc.website.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.polymap.model2.runtime.UnitOfWork;

/**
 * Provide content (template/HTML, media, etc.) to the {@link WebsiteServlet}.
 *
 * @author Falko Bräutigam
 */
public interface ContentProvider {

    public void process( Request request ) throws Exception;

    public static class Request {

        public HttpServletRequest httpRequest;

        public HttpServletResponse httpResponse;

        public UnitOfWork uow;

        /** The parts of the URI path specific for this provider. */
        public String[] path;
    }
}
