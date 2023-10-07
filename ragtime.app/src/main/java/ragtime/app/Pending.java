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

import areca.common.Platform;
import areca.common.base.Consumer.RConsumer;
import areca.common.base.Supplier.RSupplier;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;

/**
 *
 * @param <T>
 */
public class Pending<T> {

    static final Log LOG = LogFactory.getLog( Pending.class );

    private RSupplier<T>        supplier;

    private volatile T          cached;

    public Pending( RSupplier<T> supplier ) {
        this.supplier = supplier;
    }

    public void whenAvailable( RConsumer<T> consumer ) {
        if (cached != null) {
            consumer.accept( cached );
        }
        else {
            LOG.debug( "WAITING: ..." );
            Platform.schedule( 100, () -> {
                cached = supplier.get();
                whenAvailable( consumer );
            });
        }
    }

}