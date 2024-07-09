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
package ragtime.cc.web;

import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Label;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.viewer.SelectViewer;
import ragtime.cc.article.PropertyModel;
import ragtime.cc.article.TopicPageExtension;
import ragtime.cc.web.model.TopicTemplateConfigEntity;
import ragtime.cc.web.template.topic.TopicTemplate;

/**
 *
 * @author Falko Bräutigam
 */
public class WebTopicPageEx
        extends TopicPageExtension {

    private static final Log LOG = LogFactory.getLog( WebTopicPageEx.class );

    @Override
    public void doExtendFormEnd( FormContext ctx ) {
        TopicTemplateConfigEntity.of( ctx.state().topic ).onSuccess( opt -> {
            // doExtendForm
            RConsumer<TopicTemplateConfigEntity> doExtendForm = config -> {
                ctx.formBody().add( new UIComposite() {{
                    lc( RowConstraints.height( 70 ) );
                    layout.set( ctx.uic().vertical().fillHeight( false ) );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Website" ) );

                    add( ctx.form().newField()//.label( "Darstellung" )
                            .description( "Das Layout der Beiträge innerhalb dieses Topics" )
                            .viewer( new SelectViewer( TopicTemplate.availableNames() ) )
                            .model( new PropertyModel<>( config.topicTemplateName ) )
                            .create()
                            .lc( RowConstraints.height( 35 ) ) );
                }});
                ctx.form().load();
                ctx.formBody().layout();
            };

            // present
            opt.ifPresent( config -> {
                doExtendForm.accept( config );
            });

            // absent
            opt.ifAbsent( () -> {
                // add button
                ctx.formBody().add( new UIComposite() {{
                    var parent = this;
                    lc( RowConstraints.height( 38 ) );
                    layout.set( RowLayout.filled().spacing( ctx.uic().space ) );
                    add( new UIComposite() );
                    add( new Button() {{
                        lc( RowConstraints.width( 60 ) );
                        tooltip.set( "Dieses Topic auf der Website veröffentlichen" );
                        icon.set( "public" );
                        events.on( EventType.SELECT, ev -> {
                            parent.dispose();
                            var config = ctx.state().uow.createEntity( TopicTemplateConfigEntity.class, proto -> {
                                proto.topic.set( ctx.state().topic );
                            });
                            doExtendForm.accept( config );
                        });
                    }});
                }});
                ctx.formBody().layout();
            });
        });
    }

}
