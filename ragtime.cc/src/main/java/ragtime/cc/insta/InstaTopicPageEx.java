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
package ragtime.cc.insta;

import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Label;
import areca.ui.component2.Text;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.viewer.TextFieldViewer;
import ragtime.cc.article.PropertyModel;
import ragtime.cc.article.TopicPageExtension;
import ragtime.cc.insta.model.TopicInstaConfigEntity;

/**
 * Extends {@link TopicPage} by means of managing {@link TopicInstaConfigEntity}.
 *
 * @author Falko Bräutigam
 */
public class InstaTopicPageEx
        extends TopicPageExtension {

    private static final Log LOG = LogFactory.getLog( InstaTopicPageEx.class );

    @Override
    public void doExtendFormEnd( FormContext ctx ) {
        TopicInstaConfigEntity.of( ctx.state().topic ).onSuccess( opt -> {
            // doExtendForm
            RConsumer<TopicInstaConfigEntity> doExtendForm = config -> {
                ctx.formBody().add( new UIComposite() {{
                    //lc( RowConstraints.height( 70 ) );
                    layout.set( ctx.uic().verticalL().fillHeight( false ) );
                    cssClasses.add( "MessageCard" );
                    addDecorator( new Label().content.set( "Instagram" ) );

                    add( ctx.form().newField( "iuser" ).label( "Name" )
                            .description( "Der Login-Name bei Instagram" )
                            .viewer( new TextFieldViewer() )
                            .model( new PropertyModel<>( config.username ) )
                            .create()
                            .lc( RowConstraints.height( 35 ) ) );

                    add( ctx.form().newField( "ipwd" ).label( "Passwort" )
                            .viewer( new TextFieldViewer() )
                            .model( new PropertyModel<>( config.password ) )
                            .create()
                            .lc( RowConstraints.height( 35 ) ) );

                    add( new Button() {{
                        label.set( "Testen" );
                        tooltip.set( "Die eingegebenen Login-Daten bei Instagram testen" );
                        events.on( EventType.SELECT, ev -> {
                            label.set( "Login wird getestet..." );
                            var username = ctx.form().field( "iuser" ).<String>currentValue();
                            var password = ctx.form().field( "ipwd" ).<String>currentValue();
                            InstaClient.newInstance().login( username, password ).onSuccess( loggedIn -> {
                                label.set( loggedIn ? "Login: Ok" : "Login: Nicht korrekt" );
                                tooltip.set( "Login-Daten erneut testen" );
                            });
                        });
                    }});
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
                    add( new Text() {{
                        //enabled.set( false );
                        styles.add( CssStyle.of( "color", "#808080" ) );
                        format.set( Format.HTML );
                        content.set( "<em>Die Beiträge eines Topics können automatisch auf Instagram veröffentlicht werden.</em>" );
                    }});
                    add( new Button() {{
                        lc( RowConstraints.width( 60 ) );
                        tooltip.set( "Dieses Topic auf der Instagram veröffentlichen" );
                        icon.set( "share" );
                        events.on( EventType.SELECT, ev -> {
                            parent.dispose();
                            var config = ctx.state().uow.createEntity( TopicInstaConfigEntity.class, proto -> {
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
