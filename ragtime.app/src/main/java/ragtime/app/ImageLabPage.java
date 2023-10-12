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

import static areca.ui.Orientation.VERTICAL;

import org.polymap.model2.runtime.UnitOfWork;

import areca.common.base.Consumer.RConsumer;
import areca.common.event.EventManager;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.common.reflect.ClassInfo;
import areca.common.reflect.RuntimeInfo;
import areca.ui.Size;
import areca.ui.component2.Button;
import areca.ui.component2.Events.EventType;
import areca.ui.component2.TextField;
import areca.ui.component2.UIComponent;
import areca.ui.component2.UIComposite;
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.PageContainer;
import ragtime.app.ai.ImageLab;
import ragtime.app.model.GeneratedImage;
import ragtime.app.model.ModelUpdateEvent;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class ImageLabPage {

    private static final Log LOG = LogFactory.getLog( ImageLabPage.class );

    public static final ClassInfo<ImageLabPage> INFO = ImageLabPageClassInfo.instance();

    @Page.Part
    protected PageContainer     ui;

    @Page.Context
    protected ImageLab          imageLab;

    @Page.Context
    protected UnitOfWork        uow;

    @Page.Context(required = false)
    protected GeneratedImage    generatedImage;

    protected Button            imageBtn;

    protected TextField         promptField;

    protected RConsumer<GeneratedImage> initializer;


    /** For for reflection only */
    protected ImageLabPage() {}


    public ImageLabPage( RConsumer<GeneratedImage> initializer ) {
        this.initializer = initializer;
    }


    @Page.CreateUI
    public UIComponent create( UIComposite parent ) {
        ui.init( parent ).title.set( "Imaginieren ..." );

        ui.body.layout.set( RowLayout.filled().vertical().margins( Size.of( 15, 15 ) ) );
        ui.body.add( new UIComposite() {{
            layout.set( RowLayout.filled().vertical().spacing( 20 ) );
            // prompt
            promptField = add( new TextField() {{
                layoutConstraints.set( RowConstraints.height( 75 ) );
                multiline.set( true );
                tooltip.set( "Beschreiben Sie ein Bild" );
                content.set( generatedImage != null
                        ? generatedImage.prompt.get()
                        : "three little cats in the south of spain");
            }});
            // btn
            add( new Button() {{
                layoutConstraints.set( RowConstraints.height( 50 ) );
                tooltip.set( "Ein neues Bild generieren" );
                icon.set( "arrow_circle_down" );
                events.on( EventType.SELECT, ev -> generateImage() );
            }});
            // image
            imageBtn = add( new Button() {{
                layoutConstraints.set( RowConstraints.height( 360 ) );
                bordered.set( false );
                if (generatedImage != null) {
                    bgImage.set( generatedImage.imageData.get() );
                }
                //label.set( "..." );
            }});
        }});
        return ui;
    }


    protected void generateImage() {
        String prompt = promptField.content.get();
        imageLab.generateImage( prompt, 1 )
                .onSuccess( image -> {
                    LOG.info( "Image: %s bytes", image.length() );
                    imageBtn.bgImage.set( image );

                    if (generatedImage == null) {
                        generatedImage = uow.createEntity( GeneratedImage.class, proto -> {
                            proto.prompt.set( prompt );
                            proto.imageData.set( image );
                            initializer.accept( proto );
                        });
                    }
                    else {
                        generatedImage.prompt.set( prompt );
                        generatedImage.imageData.set( image );
                    }
                    uow.submit().onSuccess( submitted -> {
                        EventManager.instance().publish( new ModelUpdateEvent( uow, submitted ) );
                    });
                });
    }

}
