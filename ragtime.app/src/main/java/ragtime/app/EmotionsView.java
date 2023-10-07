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
                layout.set( RasterLayout.withComponentSize( 170, 50 ).spacing( 15 ) );
                add( new Button() {{
                    label.set( "Ziele" );
                    events.on( EventType.SELECT, ev -> {
                        psite.createPage( new ImageLabPage() ).open();
                    });
                }});
                add( new Button() {{
                    label.set( "Bedürfnisse" );
                }});
                add( new Button() {{
                    label.set( "Werte" );
                }});

                add( new Button() {{
                    label.set( "Gefühle" );
                }});
                add( new Button() {{
                    label.set( "Stärken" );
                }});
                add( new Button() {{
                    label.set( "Schwächen" );
                }});
            }});
        }};
    }

}
