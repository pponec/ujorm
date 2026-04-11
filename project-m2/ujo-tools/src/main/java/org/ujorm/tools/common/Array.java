/*
 * Copyright 2024-2026 Pavel Ponec
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
package org.ujorm.tools.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * Methods to manage object arrays.
 *
 * <h4>Usage</h4>
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
public final class Array<T> implements Serializable, Iterable<T> {

    @NotNull
    private final T[] array;

    /** Internal constructor */
    protected Array(@NotNull final T[] array) {
        this.array = array;
    }

    /** Copy constructor */
    public Array(@NotNull final Array<T> source) {
        this(source.toArray());
    }

    /** Create a shallow copy of the object */
    @NotNull
    public final Array<T> copy() {
        return new Array<>(this);
    }

    /** Create a new array and copy all items */
    @SuppressWarnings("unchecked")
    @NotNull
    public T[] toArray() {
        var type = (Class<T>) array.getClass().getComponentType();
        var result = (T[]) java.lang.reflect.Array.newInstance(type, array.length);
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    @NotNull
    public List<T> toList() {
        return Arrays.asList(array);
    }

    @NotNull
    public Stream<T> stream() {
        return Stream.of(array);
    }

    /** Returns an iterator over elements of type T */
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    /** Negative index value is supported, the index out of the range returns the {@code null} value. */
    @NotNull
    public Optional<T> getOptional(final int i) {
        var j = i >= 0 ? i : array.length + i;
        return Optional.ofNullable(j >= 0 && j < array.length ? array[j] : null);
    }

    /** No validations for the best performance */
    @Nullable
    public T get(final int i) {
        return array[i];
    }

    /** Negative index is supported */
    public T getValue(final int i) {
        return array[i >= 0 ? i : array.length + i];
    }

    @NotNull
    public Optional<T> getFirst() {
        return getOptional(0);
    }

    /** No validations for the best performance */
    @Nullable
    public T getFirstValue() {
        return array[0];
    }

    /** No validations for the best performance */
    @Nullable
    public T getFirstValue(@Nullable T defaultValue) {
        return array.length > 0 ? array[0] : defaultValue;
    }

    @NotNull
    public Optional<T> getLast() {
        return getOptional(-1);
    }

    /** No validations for the best performance */
    @Nullable
    public T getLastValue(@Nullable T defaultValue) {
        return array.length > 0 ? array[array.length - 1] : defaultValue;
    }


    /** No validations for the best performance */
    @Nullable
    public T getLastValue() {
        return array[array.length - 1];
    }

    public Array<T> removeFirst() {
        var result = array.length > 0 ? Arrays.copyOfRange(array, 1, array.length) : array;
        return new Array<>(result);
    }

    /** @param from Negative value is supported */
    @NotNull
    public Array<T> subArray(final int from) {
        var from2 = from < 0 ? array.length + from : from;
        var startIndex = Math.max(0, Math.min(from2, array.length));
        var result = Arrays.copyOfRange(array, startIndex, array.length);
        return new Array<>(result);
    }

    /** Add new items to the new Array */
    @SafeVarargs
    @NotNull
    public final Array<T> add(@NotNull final T... toAdd) {
        var result = Arrays.copyOf(array, array.length + toAdd.length);
        System.arraycopy(toAdd, 0, result, array.length, toAdd.length);
        return new Array<>(result);
    }

    /** Add new items to the new Array */
    @NotNull
    public Array<T> add(@NotNull final Array<T> toAdd) {
        return add(toAdd.array);
    }

    public boolean isEmpty() {
        return array.length == 0;
    }

    public int size() {
        return array.length;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(array);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        return (obj instanceof Array) && Arrays.equals(array, ((Array<?>) obj).array);
    }

    @NotNull
    @Override
    public String toString() {
        var result = new StringBuilder(64).append('[');
        for (var i = 0; i < array.length; i++) {
            result.append(i == 0 ? "" : ", ");
            result.append(array[i]);
        }
        return result.append(']').toString();
    }

    /** An optimized iterator */
    private class Itr implements Iterator<T> {
        /** Index of element to be returned by subsequent call to next */
        private int index = 0;

        @Override
        public boolean hasNext() {
            return index != array.length;
        }

        @Override
        public T next() {
            if (index >= array.length) {
                throw new NoSuchElementException();
            }
            return array[index++];
        }
    }

    /** Factory method */
    @SafeVarargs
    @NotNull
    public static <T> Array<T> of(@NotNull final T... items) {
        return new Array<>(items);
    }

    /** Converts the value safely to an Object Array to prevent varargs method resolution issues. */
    @SuppressWarnings("unchecked")
    public static Array<Object> ofObject(Object value) {
        return value instanceof Array<?> ar ? (Array<Object>) ar : Array.of(value);
    }
}