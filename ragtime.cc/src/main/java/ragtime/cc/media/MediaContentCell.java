/*
 * Copyright (C) 2025, the @authors. All rights reserved.
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

import areca.common.Platform;
import areca.common.Timer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.Image;
import areca.ui.component2.Text;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import ragtime.cc.article.ContentPage.ContentPageCell;
import ragtime.cc.article.ContentState.MediaContent;

/**
 * Shows the image of
 * @author Falko Bräutigam
 */
public class MediaContentCell
        extends ContentPageCell<MediaContent> {

    private static final Log LOG = LogFactory.getLog( MediaContentCell.class );

    private static final int SIZE = 360;

    @Override
    protected void create() {
        var media = value.media();
        layout.set( RowLayout.defaults().fillWidth( true ).margins( 10, 10 ) );

        add( new UIComposite() );
        var mimetype = media.mimetype.opt().orElse( "unbekannt" );
        if (mimetype.startsWith( "image" )) {
            add( new Image() {{
                lc( RowConstraints.width( SIZE ).height.set( SIZE ) );
                var t = Timer.start();
                media.thumbnail().size( SIZE, SIZE ).outputFormat( "png" ).create().onSuccess( bytes -> {
                    Platform.schedule( t.remainingMillis( 750 ), () -> { // do not break expand animation
                        if (!isDisposed()) {
                            setData( bytes );
                        }
                    });
                });
            }});
        }
        else {
            add( new Text() {{
                content.set( "Media-Typ: " + mimetype );
            }});
        }
        add( new UIComposite() );
    }

}
