/*
 * Copyright (C) 2023, the @authors. All rights reserved.
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
package ragtime.app;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.base.Consumer.RConsumer;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Orientation;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Link;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.Text;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RasterLayout;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import ragtime.app.RagtimeApp.PendingUnitOfWork;
import ragtime.app.model.GeneratedImage;
import ragtime.app.model.GeneratedImageTag;
import ragtime.app.model.GeneratedImageTag.TagType;
import ragtime.app.model.ModelUpdateEvent;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class EmotionsView {

    private static final Log LOG = LogFactory.getLog( EmotionsView.class );

    public static final ClassInfo<EmotionsView> INFO = EmotionsViewClassInfo.instance();

    @Page.Context
    protected Page.PageSite     psite;

    @Page.Context
    protected PendingUnitOfWork puow;

    @Page.Part
    protected SelfAwarenessPage page;


    public UIComponent create() {
        return new ScrollableComposite() {{
            layout.set( RowLayout.filled().orientation( Orientation.VERTICAL ).margins( Size.of( 15, 15 ) ) );

            add( new UIComposite() {{
                layoutConstraints.set( RowConstraints.height( 30 ) );
                layout.set( RowLayout.filled() );
                add( new Text() {{
                    content.set( "<u><b>Gefühle</b></u>" );
                    format.set( Format.HTML );
                }});
                add( new Link() {{
                    content.set( "Bedürfnisse" );
                    events.on( EventType.SELECT, ev -> page.flip() );
                    styles.add( CssStyle.of( "text-align", "right" ) );
                }});
            }});

            add( new UIComposite() {{
                layout.set( RasterLayout.withComponentSize( 108, 100 ).spacing( 15 ) );
                //layout.set( RasterLayout.withColums( 2 ).spacing( 15 ) );

                puow.whenAvailable( uow -> {
                    uow.query( GeneratedImageTag.class )
                            .where( Expressions.eq( GeneratedImageTag.TYPE.type, TagType.EMOTIONAL_CONTEXT ) )
                            .executeCollect()
                            .onSuccess( rs -> {
                                for (var tag : rs) {
                                    add( createImageLabBtn( uow, tag ) );
                                }
                                add( new Button() {{
                                    icon.set( "add" );
                                    tooltip.set( "Eine andere Situation hinzufügen..." );
                                }});
                                parent().layout();
                            });
                });
            }});
        }};
    }

    protected Button createImageLabBtn( UnitOfWork uow, GeneratedImageTag tag ) {
        var btn = new Button() {{
            cssClasses.add( "ImageBtn" );
            label.set( "..." );
        }};
        RConsumer<GeneratedImage> initializer = proto -> tag.images.add( proto );
        tag.images.fetchCollect().onSuccess( rs -> {
            if (rs.isEmpty()) {
                btn.label.set( tag.label.get() );
                btn.events.on( EventType.SELECT, ev -> {
                    psite.createPage( new ImageLabPage( initializer ) )
                            .putContext( uow, Page.Context.DEFAULT_SCOPE )
                            .open();
                } );
            }
            else {
                btn.label.set( null );
                btn.image.set( rs.get( 0 ).imageData.get() );
                btn.events.on( EventType.SELECT, ev -> {
                    psite.createPage( new ImageLabPage( initializer ) )
                            .putContext( uow, Page.Context.DEFAULT_SCOPE )
                            .putContext( rs.get( 0 ), Page.Context.DEFAULT_SCOPE )
                            .open();
                } );
            }
        });
        EventManager.instance()
                .subscribe( (ModelUpdateEvent ev) -> {
                    btn.label.set( "[updated]" );
                })
                .performIf( ev -> ev instanceof ModelUpdateEvent )
                .unsubscribeIf( () -> btn.isDisposed() );
        return btn;
    }
}
