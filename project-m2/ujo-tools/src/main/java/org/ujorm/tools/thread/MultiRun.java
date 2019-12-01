/*
 * Copyright 2019-2019 Pavel Ponec
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

package org.ujorm.tools.thread;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A multi task runner
 * @author Pavel Ponec
 */
public class MultiRun<P> {

    /** Job arguments */
    private final Stream<P> params;

    public MultiRun(@Nonnull final Stream<P> params) {
        this.params = params;
    }

    /** Get result as a Stream
     * @param timeout The maximum time to wait
     * @param job Job with a stream result
     * @return The result stream
     * */
    public <R> Stream<R> getStream(@Nonnull final Duration timeout, @Nonnull final Function<P, Stream<R>> job)
            throws MultiRunException {
        return params.map(params -> CompletableFuture.supplyAsync(() -> job.apply(params)))
                .collect(Collectors.toList()).stream() // For a parallel processing!
                .map(createGrabber(timeout))
                .flatMap(Function.identity()); // Join all streams
    }

    /** Get a single value result where a null values are excluded
     * @param timeout The maximum time to wait
     * @param job Job with a simple value result
     * @return The result stream
     */
    public <R> Stream<R> getSingle(@Nonnull final Duration timeout, @Nonnull final Function<P, R> job)
            throws MultiRunException {
        return params.map(params -> CompletableFuture.supplyAsync(() -> job.apply(params)))
                .collect(Collectors.toList()).stream() // For a parallel processing!
                .map(createGrabber(timeout))
                .filter(Objects::nonNull);
    }

    /**
     * @param timeout The maximum time to wait
     */
    protected <R> Function<CompletableFuture<R>, R> createGrabber(@Nonnull final Duration timeout) {
        return new Function<CompletableFuture<R>, R>() {

            @Override
            public R apply(@Nonnull final CompletableFuture<R> t) {
                try {
                    return convert(t);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw new MultiRunException(e);
                }
            }

            protected R convert(@Nonnull final CompletableFuture<R> t)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return t.get(timeout.toMillis(), MILLISECONDS);
            }
        };
    }

    // --- Static methods ---

    public static <P> MultiRun<P> params(@Nonnull final Stream<P> params) {
        return new MultiRun<P>(params);
    }

    public static <P> MultiRun<P> params(@Nonnull final List<P> params) {
        return params(params.stream());
    }

    public static <P> MultiRun<P> params(@Nonnull final P... params) {
        return params(Arrays.asList(params));
    }

    public static <P> MultiRun<P> params(@Nonnull final Iterable<P> params) {
        return params(StreamSupport.stream(params.spliterator(), false));
    }

    // --- Class or Interfaces ---

    /** Internal exception */
    public static class MultiRunException extends IllegalStateException {

        public MultiRunException(@Nonnull final Throwable cause) {
            super(cause);
        }

    }

}
