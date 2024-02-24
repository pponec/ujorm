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

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * Methods to manage object arrays.
 *
 * <h3>Usage</h3>
 * <pre class="pre">
 *   Array<Character> array = Array.of('A', 'B', 'C', 'D', 'E');
 *   array.getFirst().orElse(undef);
 *   array.getLast().orElse(undef);
 *   array.getItem(9).orElse(undef);
 *   array.getItem(-2).orElse(undef);
 *   array.removeFirst();
 *   array.join('P', 'C')
 * </pre>
 *
 * @author Pavel Ponec
 */
public class Array<T> implements Serializable {

    protected final T[] array;

    protected Array(@NotNull final T[] array) {
        this.array = array;
    }

    @NotNull
    public Array<T> clone() {
        final Class<T> type = (Class<T>) array.getClass().getComponentType();
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) java.lang.reflect.Array.newInstance(type, array.length);
        System.arraycopy(array, 0, result, 0, array.length);
        return new Array<>(result);
    }

    /** Negative index value is supported, the index out of the range returns the {@code null} value. */
    @NotNull
    public Optional<T> get(final int i) {
        final int j = i >= 0 ? i : array.length + i;
        return Optional.ofNullable(j >= 0 && j < array.length ? array[j] : null);
    }

    /** Negative index is supported */
    public T getItem(final int i) {
        return array[i >= 0 ? i : array.length + i];
    }

    @NotNull
    public Optional<T> getFirst() {
        return get(0);
    }

    @NotNull
    public Optional<T> getLast() {
        return get(-1);
    }

    public Array<T> removeFirst() {
        final T[] result = array.length > 0 ? Arrays.copyOfRange(array, 1, array.length) : array;
        return new Array<>(result);
    }

    @NotNull
    public Array<T> subArray(final int from) {
        final T[] result = Arrays.copyOfRange(array, from, array.length);
        return new Array<>(result);
    }

    /** Add new items */
    @NotNull
    public Array<T> join(@NotNull  final T... toAdd) {
        final T[] result = Arrays.copyOf(array, array.length + toAdd.length);
        System.arraycopy(toAdd, 0, result, array.length, toAdd.length);
        return new Array<>(result);
    }

    public List<T> toList() {
        return Arrays.asList(array);
    }

    public boolean isEmpty() {
        return array.length == 0;
    }

    public int size() {
        return array.length;
    }

    public Stream<T> stream() {
        return Stream.of(array);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Array)
                ?  Arrays.equals(array, ((Array) obj).array)
                : false;
    }

    /** Factory method */
    public static <T> Array<T> of(T... chars) {
        return new Array<T>(chars);
    }

}
