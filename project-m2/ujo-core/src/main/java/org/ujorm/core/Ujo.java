/*
 * Copyright 2007-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The Unified Java Object.
 * @param <D> Domain object type
 * @author Pavel Ponec
 */
public interface Ujo<D extends Object> {

    /** Common method to reading object value.
     *
     * to external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors.
     * <br>NOTE: A reaction on an incorrect key depends on the implementation.
     *
     * @return Property value
     */
    <V> V getValue(@NotNull Key<D,V> key);

    /** Common method to assign object value.
     *
     * @param key Property must be a direct type only!
     * @param value Value
     */
    <V> void setValue(@NotNull Key<D,V> key, @Nullable V value);

    /** Common method to get object value by the keyName.
     *
     * @param keyName Property must be a direct type only!
     */
    default <V> V getValue(@NotNull String keyName) {
        return (V) getValue(domainHandler().getKey(keyName));
    }

    /** Common method to assign object value by the keyName.
     *
     * @param keyName Property must be a direct type only!
     * @param value Value
     */
    default <V> void setValue(@NotNull String keyName, @Nullable V value) {
        setValue(domainHandler().getKey(keyName), value);
    }

    /** Provides an original domain object. */
    default D buildDomain() {
        return domainHandler().newDomain();
    }

    /** Returns a domain handler */
    DomainHandler<D> domainHandler();

}
