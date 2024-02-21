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

import static java.lang.Math.max;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.component2.Button;
import areca.ui.component2.Tag;
import areca.ui.component2.UIComposite;
import areca.ui.layout.CloudLayout;
import areca.ui.layout.RowConstraints;

/**
 *
 * @author Falko Bräutigam
 */
public class ImageAnnotationsCloud
         extends UIComposite {

    private static final Log LOG = LogFactory.getLog( ImageAnnotationsCloud.class );


    public ImageAnnotationsCloud() {
        layout.set( CloudLayout.withSpacing( RagtimeApp.SPACE ).componentHeight.set( 50 ) );

        add( new AnnotationBtn( "Emotion", "ausgeglichen, leicht") {{
        }});
        add( new AnnotationBtn( "Umfeld", "Urlaub") {{
        }});
        add( new AnnotationBtn( "Zeit", "...") {{
        }});
        add( new AnnotationBtn( "Ort", "...") {{
        }});
        add( new AnnotationBtn( "Nochwas", "") {{
        }});
        add( new AnnotationBtn( "???", "") {{
        }});
        add( new AnnotationBtn( "??? - ???", "") {{
        }});
        add( new AnnotationBtn( "???", "") {{
        }});
        add( new AnnotationBtn( "??? - ???", "") {{
        }});
    }


    /**
     *
     */
    public static class AnnotationBtn
            extends Button {

        public AnnotationBtn( String l1, String l2 ) {
            cssClasses.add( "ImageBtn" );
            layoutConstraints.set( RowConstraints.width( (max( l1.length(), l2.length() ) * 11) + (2 * RagtimeApp.SPACE) ) );

            format.set( Format.HTML );
            label.set( String.format( "<span style=\"font-weight:bold;\">%s</span><br/>", l1 ) +
                    String.format( "<span style=color:#e7763f;\">%s</span>", l2 ) );

            if (l2.length() == 3) {
                addDecorator( new Tag() {{
                    icons.add( "help" );
                }});
            }
        }

    }
}
