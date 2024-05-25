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
package ragtime.cc.website;

import java.util.List;

import areca.common.Assert;
import areca.common.base.Opt;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.Image;
import areca.ui.component2.UIComponent;
import areca.ui.statenaction.StateSite;
import areca.ui.viewer.Viewer;
import areca.ui.viewer.model.Model;
import ragtime.cc.media.MediasSelectState;
import ragtime.cc.model.MediaEntity;

/**
 *
 * @author Falko Bräutigam
 */
public class MediaPickerViewer
        extends Viewer<Model<MediaEntity>> {

    private static final Log LOG = LogFactory.getLog( MediaPickerViewer.class );

    protected Image img;

    protected StateSite site;

    protected MediaEntity selection;


    public MediaPickerViewer( StateSite site ) {
        this.site = site;
    }

    @Override
    public UIComponent create() {
        Assert.isNull( img );
        img = new Image() {{
            events.on( EventType.SELECT, ev -> {
                site.createState( new MediasSelectState( sel -> onSelect( sel ) ) ).activate();
            });
            if (configurator != null) {
                configurator.accept( this );
            }
        }};
        model.subscribe( ev -> load() ).unsubscribeIf( () -> img.isDisposed() );
        return img;
    }

    protected void onSelect( List<MediaEntity> newSelection ) {
        var previous = selection;
        selection = newSelection.isEmpty() ? null : newSelection.get( 0 );
        if (selection != null) {
            selection.thumbnail().size( img.size.$() ).outputFormat( "png" ).create().onSuccess( bytes -> {
                img.setData( bytes );
            });
        }
        fireEvent( selection, previous );
    }

    @Override
    protected boolean isDisposed() {
        return Assert.notNull( img, "No field has been created yet for this viewer." ).isDisposed();
    }

    @Override
    public String store() {
        model.set( selection );
        return selection != null ? (String)selection.id() : null;
    }

    @Override
    public String load() {
        Opt.of( model.get() ).ifPresent( media -> {
            selection = media;
            LOG.info( "img: %s", img.size.$() );
            media.thumbnail().size( img.size.$() ).outputFormat( "png" ).create().onSuccess( bytes -> {
                img.setData( bytes );
            });
        });
        return selection != null ? (String)selection.id() : null;
    }

}
