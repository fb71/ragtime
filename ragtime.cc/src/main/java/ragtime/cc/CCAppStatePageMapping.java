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
package ragtime.cc;

import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import areca.ui.pageflow.Page;
import areca.ui.pageflow.Pageflow;
import areca.ui.statenaction.State;
import areca.ui.statenaction.StateChangeEvent;
import areca.ui.statenaction.StateChangeEvent.EventType;
import areca.ui.statenaction.StateChangeListener;

/**
 * The connection between application {@link State} and the UI. This class creates
 * the corresponding UI {@link Page} when a new State is activated.
 *
 * @author Falko Bräutigam
 */
public class CCAppStatePageMapping
        implements StateChangeListener {

    private static final Log LOG = LogFactory.getLog( CCAppStatePageMapping.class );

    private Pageflow pageflow;

    public CCAppStatePageMapping( Pageflow pageflow ) {
        this.pageflow = pageflow;
    }


    @Override
    public void handle( StateChangeEvent ev ) {
        if (ev.type == EventType.INITIALIZED) {
            // FrontPage
            if (ev.getSource() instanceof FrontPage) {
                pageflow.create( FrontPage.class )
                        .putContext( ev.getSource(), Page.Context.DEFAULT_SCOPE )
                        .open();
            }
            else {
                throw new RuntimeException( "Unhandled State type: " + ev.getSource() );
            }
        }
    }

}
