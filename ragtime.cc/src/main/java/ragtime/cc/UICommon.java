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
import areca.ui.Size;
import areca.ui.layout.RowLayout;

/**
 * Common UI settings and functions depending on the current window size.
 *
 * @author Falko Bräutigam
 */
public class UICommon {

    private static final Log LOG = LogFactory.getLog( UICommon.class );

    public static final String ICON_SAVE = "save";

    /** Default space in the UI */
    public int space = 15;

    /** Slightly bigger {@link #space} */
    public int spaceL = space + (space / 2);

    /** Default margins used in the UI */
    public Size margins = Size.of( space, space );

    public Size marginsL = Size.of( spaceL, spaceL );

    /** */
    public RowLayout vertical() {
        return RowLayout.filled().vertical().margins( margins ).spacing( space );
    }

    /** */
    public RowLayout verticalL() {
        return RowLayout.filled().vertical().margins( marginsL ).spacing( spaceL );
    }
}
