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

import areca.common.base.Consumer;
import areca.common.base.Supplier.RSupplier;

/**
 *
 * @param <T>
 */
public class Pending<T> {

    private RSupplier<T>        supplier;

    private T                   cached;

    public Pending( RSupplier<T> supplier ) {
        this.supplier = supplier;
    }

    public <E extends Exception> void whenAvailable( Consumer<T,E> consumer ) throws E {

    }
}