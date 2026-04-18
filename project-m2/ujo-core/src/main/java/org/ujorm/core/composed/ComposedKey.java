/*
 * Copyright 2007-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.core.composed;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.Key;

import java.util.NoSuchElementException;

public interface ComposedKey<DOMAIN, VALUE> {

    /** Returns a current instance of the Key for the default Criterion implementations */
    @NotNull
    Key<DOMAIN, VALUE> self();

    /** Joins the current key with another one */
    @NotNull
    default <V2> Key<DOMAIN, V2> join(@NotNull Key<VALUE, V2> key) {
        return ComposedKeyImpl.ofDirtyKeys(self(), key);
    }

    /** Joins the current key with another one */
    @NotNull
    default <V2, V3> Key<DOMAIN, V2> join(@NotNull Key<VALUE, V2> key2, @NotNull Key<V2, V3> key3) {
        return ComposedKeyImpl.ofDirtyKeys(self(), key2, key3);
    }


    /** Joins the current key with another one */
    @NotNull
    default <V2, V3, V4> Key<DOMAIN, V2> join(@NotNull Key<VALUE, V2> key2, @NotNull Key<V2, V3> key3, @NotNull Key<V3, V4> key4) {
        return ComposedKeyImpl.ofDirtyKeys(self(), key2, key3, key4);
    }

    /** Total count of the path items */
    default int pathSize() {
        return 1;
    }

    /** Is the key a composite (contains more than one item) */
    default boolean isComposite() {
        return pathSize() > 1;
    }

    /** Returns a key item at the specific index. A negative index counts from the end. */
    default Key<?, ?> pathItem(int index) {
        return switch (index) {
            case -1, 0 -> self();
            default -> throw new NoSuchElementException("Out of range: %s[%]".formatted(toString(), index));
        };
    }

}