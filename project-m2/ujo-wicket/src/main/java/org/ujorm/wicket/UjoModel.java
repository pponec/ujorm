/*
 *  Copyright 2018 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.wicket;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.tools.Assert;
import org.ujorm.wicket.function.UjoSupplier;

/**
 * Extended Wicket Model implements an interface UjoSupplier;
 * @author Pavel Ponec
 */
public final class UjoModel<V extends Serializable> implements IModel<V>, UjoSupplier<V> {
    private static final long serialVersionUID = 1L;

    /** ProxyValue */
    @Nonnull
    private final UjoSupplier<V> proxyValue;

    public UjoModel(@Nonnull final UjoSupplier<V> proxyValue) {
        this.proxyValue = Assert.notNull(proxyValue, "proxyValue");
    }

    @Override @Nullable
    public V getObject() {
        return proxyValue.get();
    }

    /** An unsupported method, use an {@link Model} class rather */
    @Override @Deprecated
    public void setObject(V object) {
        throw new UnsupportedOperationException("Unsupported method");
    }

    @Override
    public void detach() {
    }

    @Override @Nullable
    public V get() {
        return proxyValue.get();
    }

    // --- Static method ---

    /** A factory method */
    public static <V extends Serializable> UjoModel<V> of(@Nonnull final UjoSupplier<V> proxyValue) {
        return new UjoModel(proxyValue);
    }

}
