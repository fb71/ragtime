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

import org.polymap.model2.runtime.UnitOfWork;

import areca.ui.component2.UIComposite;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.viewer.form.Form;
import ragtime.cc.model.Article;

/**
 *
 * @author Falko Bräutigam
 */
public abstract class ArticlePageExtension {

    /**
     *
     */
    public static record ExtensionSite( Article article, PageSite pagesite, Form form, UIComposite formBody ) {

        public UnitOfWork uow() {
            return article.context.getUnitOfWork();
        }
    }

    public void doExtendFormStart( ExtensionSite site ) {};

    public void doExtendFormEnd( ExtensionSite site ) {};

}
