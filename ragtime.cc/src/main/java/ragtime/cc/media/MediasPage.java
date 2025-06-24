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

import static java.text.DateFormat.MEDIUM;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import java.util.Locale;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import areca.common.Platform;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.FileUpload.File;
import areca.ui.component2.Image;
import areca.ui.component2.ScrollableComposite;
import areca.ui.component2.Text;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.FillLayout;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Page.PageSite;
import areca.ui.pageflow.PageContainer;
import areca.ui.viewer.CompositeListViewer;
import areca.ui.viewer.ViewerBuilder;
import areca.ui.viewer.ViewerContext;
import ragtime.cc.ConfirmDialog;
import ragtime.cc.UICommon;
import ragtime.cc.model.MediaEntity;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class MediasPage {

    private static final Log LOG = LogFactory.getLog( MediasPage.class );

    public static final ClassInfo<MediasPage> INFO = MediasPageClassInfo.instance();

    protected static final DateFormat df = SimpleDateFormat.getDateTimeInstance( MEDIUM, MEDIUM, Locale.GERMAN );

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected MediasState       state;

    @Page.Context
    protected UICommon          uic;

    @Page.Context
    protected PageSite          site;

    private Button              uploadBtn;

    private File                uploaded;

    private ViewerBuilder       medias;


    @Page.CreateUI
    @SuppressWarnings( "unchecked" )
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Medien" );

        ui.body.layout.set( uic.verticalL() );
        ui.body.add( new UIComposite() {{
            layout.set( RowLayout.filled().vertical().spacing( uic.space ) );

//            // upload row
//            add( new UIComposite() {{
//                layoutConstraints.set( RowConstraints.height( 35 ) );
//                layout.set( RowLayout.filled().spacing( uic.space ) );
//                add( new FileUpload() {{
//                    events.on( EventType.UPLOAD, ev -> {
//                        LOG.warn( "Uploaded: %s", data.get().name() );
//                        uploaded = data.get();
//                        uploadBtn.enabled.set( true );
//                    });
//                }});
//
//                uploadBtn = add( new Button() {{
//                    layoutConstraints.set( RowConstraints.width( 80 ) );
//                    //label.set( "Upload" );
//                    icon.set( "add" );
//                    tooltip.set( "Das ausgewählte File neu anlegen" );
//                    type.set( Type.SUBMIT );
//                    enabled.set( false );
//                    events.on( EventType.SELECT, ev -> {
//                        state.createMediaAction( uploaded ).onSuccess( __ -> {
//                            enabled.set( false );
//                        });
//                    });
//                }});
//            }});

            // list
            add( new ScrollableComposite() {{
                layout.set( FillLayout.defaults() );

                medias = new ViewerContext()
                        .viewer( new CompositeListViewer<MediaEntity>( (media) -> {
                            return new MediaListItem( media, () -> {
                                ConfirmDialog.createAndOpen( "Media", "<b><center>" + media.name.get() + "</center></b>" )
                                        .size.set( Size.of( 320, 200 ) )
                                        .addDeleteAction( () -> {
                                            state.deleteMediaAction( media );
                                        });
                            });
                        }) {{
                            etag.set( media -> media.modified.get() );
                            oddEven.set( true );
                            spacing.set( 0 );
                            lines.set( true );
                            onSelect.set( media -> {
                                LOG.info( "SELECT: %s", media );
                            });
                        }})
                        .model( state.medias );
                add( medias.createAndLoad() );
            }});
        }});
        return ui;
    }


    /**
     *
     */
    public static class MediaListItem
            extends UIComposite {

        public MediaListItem( MediaEntity media, Runnable removeAction ) {
            LOG.debug( "Creating TableCell for: %s", media );
            lc( RowConstraints.height( 54 ));
            layout.set( RowLayout.filled().spacing( 10 ).margins( 10, 10 ) );
            var mime = media.mimetype.opt().orElse( "null" );
            add( new Text() {{
                tooltip.set( media.name.get() );
                format.set( Format.HTML );
//                content.set( StringUtils.abbreviate( media.name.get(), 35 ) + "<br/>" +
//                        "<span style=\"font-size:10px; color:#808080;\">" + mime + "</span>" );

                var s =  "%s<br/><span style=\"font-size:10px; color:#808080;\">%s - Beiträge: %s</span>";
                var name = abbreviate( media.name.get(), 35 );
                content.set( String.format( s, name, mime, "?" ) );

                Platform.schedule( 1000, () -> {
                    media.articles().onSuccess( articles -> {
                        if (!isDisposed()) {
                            content.set( String.format( s, name, mime, articles.size() ) );
                        }
                    });
                });
            }});
            if (mime.startsWith( "image" )) {
                add( new Image() {{
                    lc( RowConstraints.width( 40 ));
                    Platform.schedule( 1000, () -> {
                        media.thumbnail().size( 40, 34 ).outputFormat( "png" ).create().onSuccess( bytes -> {
                            if (!isDisposed()) {
                                setData( bytes );
                            }
                        });
                    });
                }});
            }
            add( new Button() {{
                lc( RowConstraints.width( 40 ));
                icon.set( UICommon.ICON_DELETE );
                tooltip.set( "Löschen" );
                events.on( EventType.SELECT, ev -> {
                    removeAction.run();
                });
            }});
        }
    }
}
