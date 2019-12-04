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
 * A multithreading task runner
 * @author Pavel Ponec
 *
 * @see https://stackoverflow.com/questions/53435098/completablefuture-supplyasync-with-stream-map
 * @see https://www.baeldung.com/java-completablefuture
 */
public class MultiJob<P> {

    /** Job arguments */
    private final Stream<P> params;

    protected MultiJob(@Nonnull final Stream<P> params) {
        this.params = params;
    }

    /** Get of single values where a nulls are excluded. A default timeout one hour.
     * @param job Job with a simple value result
     * @return The result stream
     */
    final public <R> Stream<R> run(@Nonnull final Function<P, R> job)
            throws MultiJobException {
        return run(job, defaultDuration());
    }

    /** Get of single values where a nulls are excluded
     * @param timeout The maximum time to wait
     * @param job Job with a simple value result
     * @return The result stream
     */
    public <R> Stream<R> run(@Nonnull final Function<P, R> job, @Nonnull final Duration timeout)
            throws MultiJobException {
        return params.map(params -> CompletableFuture.supplyAsync(() -> job.apply(params)))
                .collect(Collectors.toList()).stream() // For a parallel processing!
                .map(createGrabber(timeout))
                .filter(Objects::nonNull);
    }

    /** Get result of a Stream. A default timeout one hour.
     * @param job Job with a stream result
     * @return The result stream
     * */
    final public <R> Stream<R> runToStream(@Nonnull final Function<P, Stream<R>> job)
            throws MultiJobException {
        return runToStream(job, defaultDuration());
    }

    /** Get result of a Stream
     * @param timeout The maximum time to wait
     * @param job Job with a stream result
     * @return The result stream
     * */
    public <R> Stream<R> runToStream(@Nonnull final Function<P, Stream<R>> job, @Nonnull final Duration timeout)
            throws MultiJobException {
        return params.map(params -> CompletableFuture.supplyAsync(() -> job.apply(params)))
                .collect(Collectors.toList()).stream() // For a parallel processing!
                .map(createGrabber(timeout))
                .flatMap(Function.identity()); // Join all streams
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
                    throw new MultiJobException(e);
                }
            }

            protected R convert(@Nonnull final CompletableFuture<R> t)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return t.get(timeout.toMillis(), MILLISECONDS);
            }
        };
    }

    /** A default duration is the one hour */
    protected Duration defaultDuration() {
        return Duration.ofHours(1);
    }

    // --- Static methods ---

    public static <P> MultiJob<P> forParams(@Nonnull final Stream<P> params) {
        return new MultiJob<P>(params);
    }

    public static <P> MultiJob<P> forParams(@Nonnull final List<P> params) {
        return forParams(params.stream());
    }

    public static <P> MultiJob<P> forParams(@Nonnull final P... params) {
        return new MultiJob<P>(Arrays.stream(params));
    }

    public static <P> MultiJob<P> forParams(@Nonnull final Iterable<P> params) {
        return forParams(StreamSupport.stream(params.spliterator(), false));
    }

    // --- Class or Interfaces ---

    /** Internal exception */
    public static class MultiJobException extends IllegalStateException {

        public MultiJobException(@Nonnull final Throwable cause) {
            super(cause);
        }
    }

}
