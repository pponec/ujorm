/*
 * Copyright 2024-2024 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.common;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Optional;

/**
 * Methods to manage object arrays.
 *
 * <h3>Usage</h3>
 * <pre class="pre">
 *   ArrayUtils tools = new ArrayUtils();
 *   tools.getFirst(array).orElse(undef);
 *   tools.getLast(array).orElse(undef);
 *   tools.getItem(9, array).orElse(undef);
 *   tools.removeFirst(array);
 *   tools.join(array, 'P', 'C')
 * </pre>
 *
 * @author Pavel Ponec
 */
public class ArrayUtils {

    @NotNull
    public <T> T[] clone(@NotNull T... array) {
        final Class<T> type = (Class<T>) array.getClass().getComponentType();
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) Array.newInstance(type, array.length);
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    @NotNull
    public <T> Optional<T> getItem(final int i, @NotNull final T[] array) {
        return Optional.ofNullable(i >= 0 && i < array.length ? array[i] : null);
    }

    @NotNull
    public <T> Optional<T> getFirst(@NotNull final T[] array) {
        return getItem(0, array);
    }

    @NotNull
    public <T> Optional<T> getLast(@NotNull final T[] array) {
        return getItem(array.length - 1, array);
    }

    @NotNull
    public <T> T[] removeFirst(@NotNull final T[] array) {
        return array.length > 0
                ? Arrays.copyOfRange(array, 1, array.length)
                : array;
    }

    @NotNull
    public <T> T[] subArray(final int from, @NotNull final T[] array) {
        final int frm = Math.min(from, array.length);
        return Arrays.copyOfRange(array, frm, array.length);
    }

    /** Add new items */
    @NotNull
    public static <T> T[] join(@NotNull final T[] baseArray, @NotNull  final T... toAdd) {
        final T[] result = Arrays.copyOf(baseArray, baseArray.length + toAdd.length);
        System.arraycopy(toAdd, 0, result, baseArray.length, toAdd.length);
        return result;
    }

}
