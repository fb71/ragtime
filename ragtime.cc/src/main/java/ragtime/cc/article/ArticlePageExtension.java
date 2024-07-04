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

import areca.ui.component2.UIComposite;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.viewer.form.Form;

/**
 *
 * @author Falko Bräutigam
 */
public abstract class ArticlePageExtension {

    public void doExtendFormStart( ArticleEditState state, ArticlePage page, PageSite pagesite, UIComposite formBody ) {};

    public void doExtendFormEnd( ArticleEditState state, ArticlePage page, PageSite pageSite, Form form, UIComposite formBody ) {};

}
