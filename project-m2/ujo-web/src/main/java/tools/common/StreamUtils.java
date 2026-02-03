/*
 * Copyright 2012-2012 Pavel Ponec
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

package tools.common;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Static methods
 * @author Pavel Ponec
 */
public abstract class StreamUtils {

        static final Set<Collector.Characteristics> CH_ID
            = Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH));

    private StreamUtils() {
    }

        /** Read a String.
     * A line separator can be modifed in the result
     * @return The result must be closed.
     */
    @NotNull
    public Stream<String> rows(@NotNull final String text) {
        return new BufferedReader(new CharArrayReader(text.toCharArray())).lines();
    }

    /** Returns a stream of lines form URL resource
     *
     * @param url An URL link to a resource
     * @return The customer is responsible for closing the stream.
     *         During closing, an IllegalStateException may occur due to an IOException.
     */
    public static Stream<String> rowsOfUrl(@NotNull final URL url) throws IOException {
        return StringUtils.readLines(url);
    }

    /**
     * Convert an interator to a Stream
     * @param <T> An item type
     * @param iterator Source iterator
     * @return
     */
    public static <T> Stream<T> toStream(@NotNull final Iterator<T> iterator) {
        return toStream(iterator, false);
    }

    /**
     * Convert an interator to a Stream
     * @param <T> An item type
     * @param iterator Source iterator
     * @param parallel Parrallell processing is enabled
     * @return
     */
    public static <T> Stream<T> toStream(@NotNull final Iterator<T> iterator, final boolean parallel) {
        final Iterable<T> iterable = () -> iterator;
        return StreamSupport.stream(iterable.spliterator(), parallel);

        // # For Java 9:
        //
        //Stream.generate(() -> null)
        //    .takeWhile(x -> iterator.hasNext())
        //    .map(n -> iterator.next());
    }

    /** A stream collecetor to a ArrayDeque type */
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

}
