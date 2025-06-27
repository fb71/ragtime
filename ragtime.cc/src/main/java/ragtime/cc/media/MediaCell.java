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

import static org.apache.commons.lang3.StringUtils.abbreviate;

import org.apache.commons.io.FileUtils;
import areca.common.MutableInt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Text;
import ragtime.cc.ConfirmDialog;
import ragtime.cc.UICommon;
import ragtime.cc.article.ContentPage.ExpandableCell;
import ragtime.cc.article.ContentState.MediaContent;

/**
 *
 * @author Falko Bräutigam
 */
public class MediaCell
        extends ExpandableCell<MediaContent> {

    private static final Log LOG = LogFactory.getLog( MediaCell.class );

    @Override
    protected void create() {
        var media = value.media();
        var mime = media.mimetype.opt().orElse( "?" );

        create( "image", "#666666", container -> { //#ada673
            //expandable = false;
            container.tooltip.set( "Media: " + media.name.get() );

            container.add( new Text() {{
                format.set( Format.HTML );
                var name = abbreviate( media.name.get(), 35 );
                content.set( name + SECOND_LINE.formatted( "..." ) );

                var s =  new StringBuilder( 256 ).append( mime );
                media.size()
                        .then( filesize -> {
                            s.append( " - " ).append( FileUtils.byteCountToDisplaySize( filesize ) );
                            return media.articles();
                        })
                        .then( articles -> {
                            s.append( " - Beiträge: " ).append( articles.size() );
                            return media.topics();
                        })
                        .onSuccess( topics -> {
                            s.append( " - Topics: " ).append( topics.size() );
                            if (!isDisposed()) {
                                content.set( name + SECOND_LINE.formatted( s.toString() ) );
                            }
                        });
            }});
        });
        // thumbnail
        icon.styles.add( CssStyle.of( "opacity", "0" ) );
        if (mime.startsWith( "image" )) {
            media.thumbnail().size( 50, 50 ).outputFormat( "png" ).createBase64().onSuccess( base64 -> {
                if (!isDisposed()) {
                    icon.icon.set( null );
                    icon.bgImage.set( base64 );
                    icon.styles.add( CssStyle.of( "background-origin", "content-box" ) );
                    icon.styles.remove( CssStyle.of( "opacity", "0" ) );
                }
            });

//            add( new Image() {{
//                lc( RowConstraints.width( 40 ));
//                Platform.schedule( 1000, () -> {
//                    value.thumbnail().size( 40, 34 ).outputFormat( "png" ).create().onSuccess( bytes -> {
//                        if (!isDisposed()) {
//                            setData( bytes );
//                        }
//                    });
//                });
//            }});
        }
        // delete
        addAction( new Button() {{
            iconStyle.set( IconStyle.OUTLINED );
            icon.set( UICommon.ICON_DELETE );
            tooltip.set( "Löschen" );
            events.on( EventType.SELECT, ev -> {
                var count = new MutableInt( 0 );
                media.articles()
                        .then( rs -> {
                            count.add( rs.size() );
                            return media.topics();
                        })
                        .onSuccess( rs -> {
                            count.add( rs.size() );
                            if (count.intValue() > 1) {
                                value.remove( false );
                            }
                            else {
                                ConfirmDialog.create( "Löschen",
                                        "<b>" + media.name.get() + "</b> - wird in keinem Artikel oder Topic mehr verwendet.<br/><br/>Soll es gelöscht werden?" )
                                        .size.set( Size.of( 320, 230 ) )
                                        .addNoAction( () -> value.remove( false ) )
                                        .addDeleteAction( () -> value.remove( true ) )
                                        .open();
                            }
                        });
            });
        }});
    }

}
