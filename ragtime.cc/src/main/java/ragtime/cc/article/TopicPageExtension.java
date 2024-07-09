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

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.UIComposite;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.viewer.form.Form;
import ragtime.cc.UICommon;

/**
 *
 * @author Falko Bräutigam
 */
public abstract class TopicPageExtension {

    private static final Log LOG = LogFactory.getLog( TopicPageExtension.class );

    public static record FormContext(
            TopicEditState state,
            TopicPage page,
            PageSite pagesite,
            UIComposite formBody,
            Form form,
            UICommon uic ) {
    }

//    public static class FormContext {
//
//        public FormContext( TopicEditState state, TopicPage page, PageSite pagesite, UIComposite formBody, Form form ) {
//            this.state = state;
//            this.page = page;
//            this.pagesite = pagesite;
//            this.formBody = formBody;
//            this.form = form;
//        }
//
//        public TopicEditState state;
//        public TopicPage page;
//        public PageSite pagesite;
//        public UIComposite formBody;
//        public Form form;
//    }

    public void doExtendFormStart( FormContext ctx ) {};

    public void doExtendFormEnd( FormContext ctx ) {};

}
