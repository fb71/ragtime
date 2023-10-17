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

import static java.lang.String.format;
import static ragtime.app.EmotionsView.FONT_SIZE_HEAD;

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
import areca.ui.layout.RowConstraints;
import areca.ui.layout.RowLayout;
import areca.ui.pageflow.Page;
import ragtime.app.RagtimeApp.PendingUnitOfWork;

/**
 *
 * @author Falko Bräutigam
 */
@RuntimeInfo
public class NeedsView {

    private static final Log LOG = LogFactory.getLog( NeedsView.class );

    public static final ClassInfo<NeedsView> INFO = NeedsViewClassInfo.instance();

    @Page.Context
    protected Page.PageSite     psite;

    @Page.Context
    protected PendingUnitOfWork puow;

    @Page.Part
    protected SelfAwarenessPage page;


    public UIComponent create() {
        return new ScrollableComposite() {{
            layout.set( RowLayout.filled().vertical().margins( Size.of( 15, 15 ) ) );

            // head / switch
            add( new UIComposite() {{
                layoutConstraints.set( RowConstraints.height( 40 ) );
                layout.set( RowLayout.filled() );
                styles.add( CssStyle.of( "font-size", EmotionsView.FONT_SIZE_HEAD ) );
                add( new Link() {{
                    content.set( "Gefühle" );
                    events.on( EventType.SELECT, ev -> page.flip() );
                }});
                add( new Text() {{
                    content.set( "<u>Bedürfnisse</u>" );
                    format.set( Format.HTML );
                    styles.add( CssStyle.of( "text-align", "right" ) );
                }});
            }});

            //
            add( new UIComposite() {{
                layout.set( RowLayout.filled().vertical().spacing( 15 ) );

                add( new NeedsBtn( "Selbstverwirklichung", "Persönlichkeit", "+Fähigkeiten", "Potentiale nutzen" ) {{
                    //cssClasses.add( "NeedsBtnGood" );
                }});
                add( new NeedsBtn( "Anerkennung", "Gesehen werden, Würdigung, Bla-Bla" ) {{
                }});
                add( new NeedsBtn( "Gemeinschaft",
                        "Familie", "Freundschaft", "-Zugehörigkeit", "+Kommunikation", "sozialer Austausch", "gegenseitige Unterstützung" ) {{
                    //cssClasses.add( "NeedsBtnBad" );
                }});
                add( new NeedsBtn( "Sicherheit", "Schutz", "Stabilität", "Ordnung" ) {{
                }});
                add( new NeedsBtn( "Existenz", "Nahrung", "Schlaf", "Wohnung" ) {{
                }});
            }});
        }};
    }

    /**
     *
     */
    protected class NeedsBtn
            extends Button {

        public NeedsBtn( String l1, String... l2 ) {
            cssClasses.add( "ImageBtn" );
            format.set( Format.HTML );

            var sb = new StringBuilder( 1024 ).append(
                    format( "<span style=\"font-weight:bold; font-size:%s; line-height:2em;\">%s</span><br/>", FONT_SIZE_HEAD, l1 ) );

            for (int i = 0; i < l2.length; i++) {
                var color = "#919191";
                if (l2[i].startsWith( "-" )) {
                    color = "#51a386"; //"#d34242";
                }
                if (l2[i].startsWith( "+" )) {
                    color = "#e7763f"; //"#38a331";
                }
                sb.append( format( "<span style=\"color:%s;\">%s</span>", color, l2[i] ) );
                sb.append( i < l2.length-1 ? ", " : "" );
            }
            label.set( sb.toString() );
        }
    }

}
