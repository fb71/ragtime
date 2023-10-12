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

import java.util.List;

import org.polymap.model2.query.Expressions;
import org.polymap.model2.runtime.UnitOfWork;

import areca.common.Scheduler.Priority;
import areca.common.base.Consumer.RConsumer;
import areca.common.base.Sequence;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
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

    public static final String FONT_SIZE_HEAD = "18px";

    @Page.Context
    protected Page.PageSite     psite;

    @Page.Context
    protected PendingUnitOfWork puow;

    @Page.Part
    protected SelfAwarenessPage page;


    public UIComponent create() {
        return new ScrollableComposite() {{
            layout.set( RowLayout.filled().vertical().margins( Size.of( 15, 15 ) ) );

            add( new UIComposite() {{
                layoutConstraints.set( RowConstraints.height( 40 ) );
                layout.set( RowLayout.filled() );
                styles.add( CssStyle.of( "font-size", FONT_SIZE_HEAD ) );
                add( new Text() {{
                    content.set( "<u>Gefühle</u>" );
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
                                    add( new GeneratedImageTagBtn( tag, uow ) );
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

    /**
     * A Button that represents a {@link GeneratedImageTag}. Opens
     * {@link ImageLabPage} on select.
     */
    public class GeneratedImageTagBtn
            extends Button {

        private GeneratedImageTag tag;

        private UnitOfWork uow;

        /** Lastly fetched images from {@link #tag} */
        private List<GeneratedImage> tagImages;

        public GeneratedImageTagBtn( GeneratedImageTag tag, UnitOfWork uow ) {
            this.tag = tag;
            this.uow = uow;

            cssClasses.add( "ImageBtn" );
            label.set( "..." );

            update();

            // listen to DB updates
            EventManager.instance()
                    .subscribe( (ModelUpdateEvent ev) -> update() )
                    .performIf( ModelUpdateEvent.class, ev ->
                            ev.isUpdated( tag ) || Sequence.of( tagImages ).anyMatches( img -> ev.isUpdated( img ) ) )
                    .unsubscribeIf( () -> isDisposed() );
        }

        protected void update() {
            tag.images.fetchCollect().priority( Priority.BACKGROUND ).onSuccess( rs -> {
                tagImages = rs;
                // no images yet
                if (rs.isEmpty()) {
                    label.set( tag.label.get() );
                    // FIXME clear() probably does not actually removes the underlaying handler; so
                    // after an update multiple handlers are triggered
                    events.clear().on( EventType.SELECT, ev -> onSelect( null ) );
                }
                // image(s) present
                else {
                    var entity = rs.get( 0 );
                    label.set( tag.label.get() );
                    bgImage.set( entity.imageData.get() );
                    events.clear().on( EventType.SELECT, ev -> onSelect( entity ) );
                }
            });
        }

        protected void onSelect( GeneratedImage entity) {
            RConsumer<GeneratedImage> initializer = proto -> tag.images.add( proto );
            var newPage = psite.createPage( new ImageLabPage( initializer ) )
                    .putContext( uow, Page.Context.DEFAULT_SCOPE );
            if (entity != null) {
                newPage.putContext( entity, Page.Context.DEFAULT_SCOPE );
            }
            newPage.open();
        }
    }

}
