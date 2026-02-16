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
 *
 * @author Pavel Ponec
 */
public interface Ujo<UJO extends Object> {


    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     *
     * to external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors.
     * <br>NOTE: A reaction on an incorrect key depends on the implementation.
     *
     * @param key The Key must be a direct type only!
     * @return Property value
     * @see Key#getValue(UJO)
     */
    Object readValue(@NotNull Key<UJO,?> key);


    /** The Unified Java Object.
     *
     * @param key Property must be a direct type only!
     * @param value Value
     */
    void writeValue(@NotNull Key<UJO,?> key, @Nullable Object value);

    /** Returns all direct keys.
     * There is recommended to be a "name" of each key is unique (but it is NOT a necessary condition).
     *
     * <br>An index key in the array UJO must be unique a continuous, an order of key array depends on an implementation of UJO object.
     */
    <U extends Ujo> KeyList<U> readKeys();

}
