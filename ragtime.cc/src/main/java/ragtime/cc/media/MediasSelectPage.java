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
package ragtime.cc.media;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Action;
import areca.ui.Position;
import areca.ui.component2.Badge;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.FileUpload;
import areca.ui.component2.Image;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.component2.UIElement;
import areca.ui.layout.AbsoluteLayout;
import areca.ui.layout.FillLayout;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.CompositeGridViewer;
import areca.ui.viewer.ViewerBuilder;
import areca.ui.viewer.ViewerContext;
import ragtime.cc.UICommon;
import ragtime.cc.model.MediaEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class MediasSelectPage {

    private static final Log LOG = LogFactory.getLog( MediasSelectPage.class );

    public static final ClassInfo<MediasSelectPage> INFO = MediasSelectPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected MediasSelectState state;

    @Page.Context
    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    private ViewerBuilder       medias;

    private Map<MediaEntity,UIElement> selected = new HashMap<>();


    @Page.CreateUI
    @SuppressWarnings( "unchecked" )
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Medien" );

        ui.body.layout.set( uic.vertical() );
        ui.body.add( new UIComposite() {{
            layout.set( RowLayout.filled().vertical().spacing( uic.space ) );

            // upload row
            add( new UIComposite() {{
                layoutConstraints.set( RowConstraints.height( 35 ) );
                layout.set( RowLayout.filled().spacing( uic.space ) );
                add( new FileUpload() {{
                    events.on( EventType.UPLOAD, ev -> {
                        state.createMediaAction( data.get() );
                    });
                }});
            }});

            // list
            add( new ScrollableComposite() {{
                layout.set( FillLayout.defaults() );
                medias = new ViewerContext()
                        .viewer( new CompositeGridViewer<MediaEntity>( MediaGridItem::new ) {{
                            columns.set( 5 );
                            spacing.set( 5 );
                            onSelect.set( media -> {
                                LOG.info( "SELECT: %s", media );
                                selected.compute( media, (__,deco) -> {
                                    if (deco == null) {
                                        state.selection.add( media );
                                        var cell = components.get( media );
                                        return cell.addDecorator( new Badge() {{
                                            content.set( "X" );
                                        }}).get();
                                    }
                                    else {
                                        state.selection.remove( media );
                                        deco.dispose();
                                        return null;
                                    }
                                });
                            });
                        }})
                        .model( state.medias );
                add( medias.createAndLoad() );
            }});
        }});

        // Submit
        site.actions.add( new Action() {{
            description.set( "Auswahl übernehmen" );
            icon.set( UICommon.ICON_CHECK );
            type.set( Button.Type.SUBMIT );
            enabled.set( false );
            handler.set( ev -> {
                state.applyAction();
                enabled.set( false );
            });
            state.selection.subscribe( ev -> {
                enabled.set( state.selection.size() > 0 );
            });
        }});
        return ui;
    }

    /**
     *
     */
    public static class MediaGridItem
            extends UIComposite {

        public MediaGridItem( MediaEntity media ) {
            tooltip.set( media.name.get() );
            layout.set( new AbsoluteLayout() {
                @Override
                public void layout( UIComposite composite ) {
                    composite.clientSize.opt().ifPresent( s -> {
                        composite.components.forEach( child -> {
                            if (child instanceof Image img && !Objects.equals( img.size.opt().orNull(), s )) {
                                LOG.debug( "thumbnail: %s", s );
                                media.thumbnail().size( s ).outputFormat( "png" ).create().onSuccess( bytes -> {
                                    if (!img.isDisposed()) {
                                        img.setData( bytes );
                                    }
                                });
                            }
                            child.size.set( s );
                            child.position.set( Position.of( 0, 0 ) );
                        });
                    });
                }
            });
            var mime = media.mimetype.opt().orElse( "null" );
            if (mime.startsWith( "image" )) {
                add( new Image() {{
                    styles.add( CssStyle.of( "border-radius", "3px" ) );
                }});
            }
        }
    }


    /**
     *
     */
    public static class MediaGridItem2
            extends Image {

        public MediaGridItem2( MediaEntity media ) {
            var mime = media.mimetype.opt().orElse( "null" );
            if (mime.startsWith( "image" )) {
                styles.add( CssStyle.of( "border-radius", "3px" ) );
                media.thumbnail().size( 100, 100 ).outputFormat( "png" ).create().onSuccess( bytes -> {
                    setData( bytes );
                });
            }
        }
    }
}
