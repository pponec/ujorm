/*
 * Copyright 2012-2026 Pavel Ponec
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

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jetbrains.annotations.NotNull;

/**
 * Static methods for Stream processing
 * @author Pavel Ponec
 */
public final class StreamUtils {

    /** Default characteristics for identity collectors */
    static final Set<Collector.Characteristics> CH_ID = Set.of(Collector.Characteristics.IDENTITY_FINISH);

    /**  Prevent instantiation */
    private StreamUtils() {
    }

    /** Read a text by line.
     * @return The result must be closed.
     */
    @NotNull
    public static Stream<String> rows(@NotNull final String text) {
        return text.lines();
    }

    /** Returns a stream of lines from URL resource
     *
     * @param url An URL link to a resource
     * @return The customer is responsible for closing the stream.
     * During closing, an IllegalStateException may occur due to an IOException.
     */
    public static Stream<String> rowsOfUrl(@NotNull final URL url) throws IOException {
        return StringUtils.readLines(url);
    }

    /**
     * Convert an iterator to a Stream
     * @param <T> An item type
     * @param iterator Source iterator
     * @return A sequential stream
     */
    public static <T> Stream<T> toStream(@NotNull final Iterator<T> iterator) {
        return toStream(iterator, false);
    }

    /**
     * Converts an iterator to a Stream using Spliterators for better performance.
     *
     * @param <T>      An item type
     * @param iterator Source iterator
     * @param parallel Parallel processing is enabled
     * @return A stream wrapping the iterator
     */
    public static <T> Stream<T> toStream(@NotNull final Iterator<T> iterator, final boolean parallel) {
        var spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, parallel);
    }

    /** A stream collector to a ArrayDeque type */
    @NotNull
    public static <T> Collector<T, ?, ArrayDeque<T>> collectToDequeue() {
        return Collectors.toCollection(ArrayDeque::new);
    }

    /** Create a joinable function
     *
     * <h4>Usage</h4>Assert.java:54
     * <pre>
     *  Function&lt;Person, String&gt; nameProvider = Joinable
     *     .of (Person::getBoss)
     *     .add(Person::getBoss)
     *     .add(Person::getName);
     *  String superBossName = nameProvider.apply(getPerson());
     * </pre>
     *
     * @param <D> Domain value
     * @param <R> Result value
     * @param fce An original function
     * @return The new object type of Function
     */
    @NotNull
    public static <D, R> Joinable<D, R> toJoinable(@NotNull final Function<D, R> fce) {
        return Joinable.of(fce);
    }

    /**
     * Builds an unmodifiable Map from a given collection.
     *
     * @param key  The function to extract the key from each value.
     * @param values The collection of items to be mapped.
     * @param <D>  The type of the items in the collection (values in the map).
     * @param <V>  The type of the extracted keys.
     * @return An unmodifiable Map containing the mapped items.
     * @throws IllegalStateException If the key mapping function resolves to duplicate keys.
     */
    public static <D, V> Map<V, D> map(@NotNull final Function<D, V> key, @NotNull final Collection<D> values) {
        return map(key, values.stream());
    }

    /**
     * Builds an unmodifiable Map from a given array.
     *
     * @param key  The function to extract the key from each value.
     * @param values The collection of items to be mapped.
     * @param <D>  The type of the items in the collection (values in the map).
     * @param <V>  The type of the extracted keys.
     * @return An unmodifiable Map containing the mapped items.
     * @throws IllegalStateException If the key mapping function resolves to duplicate keys.
     */
    @SafeVarargs
    public static <D, V> Map<V, D> map(@NotNull final Function<D, V> key, @NotNull final D... values) {
        return map(key, Stream.of(values));
    }

    /**
     * Builds an unmodifiable Map from a given stream.
     *
     * @param key  The function to extract the key from each value.
     * @param values The collection of items to be mapped.
     * @param <D>  The type of the items in the collection (values in the map).
     * @param <V>  The type of the extracted keys.
     * @return An unmodifiable Map containing the mapped items.
     * @throws IllegalStateException If the key mapping function resolves to duplicate keys.
     */
    public static <D, V> Map<V, D> map(@NotNull final Function<D, V> key, @NotNull final Stream<D> values) {
        return values.collect(Collectors.toUnmodifiableMap(key, Function.identity()));
    }
}