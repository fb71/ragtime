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

import java.util.Locale;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import areca.common.Platform;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.FileUpload;
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

            // upload row
            add( new UIComposite() {{
                layoutConstraints.set( RowConstraints.height( 35 ) );
                layout.set( RowLayout.filled().spacing( uic.space ) );
                add( new FileUpload() {{
                    events.on( EventType.UPLOAD, ev -> {
                        LOG.warn( "Uploaded: %s", data.get().name() );
                        uploaded = data.get();
                        uploadBtn.enabled.set( true );
                    });
                }});

                uploadBtn = add( new Button() {{
                    layoutConstraints.set( RowConstraints.width( 80 ) );
                    //label.set( "Upload" );
                    icon.set( "add" );
                    tooltip.set( "Das ausgewählte File neu anlegen" );
                    type.set( Type.SUBMIT );
                    enabled.set( false );
                    events.on( EventType.SELECT, ev -> {
                        state.createMediaAction( uploaded ).onSuccess( __ -> {
                            enabled.set( false );
                        });
                    });
                }});
            }});

            // list
            add( new ScrollableComposite() {{
                layout.set( FillLayout.defaults() );

                medias = new ViewerContext()
                        .viewer( new CompositeListViewer<MediaEntity>( (media,model) -> new UIComposite() {{
                            LOG.debug( "Creating TableCell for: %s", media );
                            lc( RowConstraints.height( 45 ));
                            layout.set( RowLayout.filled().spacing( uic.space ).margins( 10, 5 ) );
                            var mime = media.mimetype.opt().orElse( "null" );
                            add( new Text() {{
                                format.set( Format.HTML );
                                content.set( media.name.get() + "<br/>" +
                                            "<span style=\"font-size:10px; color:#808080;\">" + mime + "</span>" );
                            }});
                            if (mime.startsWith( "image" )) {
                                add( new Image() {{
                                    lc( RowConstraints.width( 40 ));
                                    Platform.schedule( 850, () -> setData( media.in() ) );
                                }});
                            }
                            add( new Button() {{
                                lc( RowConstraints.width( 40 ));
                                icon.set( "close" );
                                events.on( EventType.SELECT, ev -> {
                                    state.removeMediaAction( media );
                                });
                            }});
                        }}) {{
                            oddEven.set( true );
                            spacing.set( 0 );
                            lines.set( true );
                            onSelect.set( ev -> {
                                LOG.info( "SELECT: %s", ev );
                            });
                        }})
                        .model( state.medias );
                add( medias.createAndLoad() );
            }});
        }});

//        // Submit
//        site.actions.add( new Action() {{
//            description.set( "Speichern" );
//            type.set( Button.Type.SUBMIT );
//            //enabled.set( false );
//            icon.set( "done" );
//            handler.set( ev -> {
//                state.submitAction().onSuccess( __ -> {
//                    enabled.set( false );
//                });
//            });
//            medias.subscribe( ev -> {
//                var _enabled = true; //medias.isChanged() && medias.isValid();
//                enabled.set( _enabled );
//                icon.set( _enabled ? "done" : "" );
//            });
//        }});
        return ui;
    }


    @Page.Close
    public boolean onClose() {
        LOG.info( "onClose()" );
        return state.disposeAction();
    }

}
