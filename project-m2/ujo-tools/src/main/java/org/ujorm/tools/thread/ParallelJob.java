/*
 * Copyright 2019-2019 Pavel Ponec
 * Original source of Ujorm framework: https://bit.ly/340mx4T
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.set.LoopingIterator;

/**
 * A multithreading task runner
 * @author Pavel Ponec
 *
 * @see https://www.baeldung.com/java-fork-join
 * @see https://stackoverflow.com/questions/21163108/custom-thread-pool-in-java-8-parallel-stream
 * @since 1.95
 * @deprecated The class is deprecated due a failed test: {@code ParallelJob#testTimeOfParalellWork()}.
 *    The bug was fixed on version openjdk8u222: https://bugs.openjdk.java.net/browse/JDK-8224620
 * @see https://bugs.openjdk.java.net/browse/JDK-8190974
 */
@Deprecated
public class ParallelJob<P> {

    /** Template message for an invalid input */
    protected static final String REQUIRED_INPUT_TEMPLATE_MSG = "The {} is required";

    /** Job arguments */
    @Nonnull
    protected final Stream<P> params;

    /** Thread pool */
    @Nonnull
    protected final ForkJoinPool threadPool;

    /** A timeout where a default duration is the one hour */
    @Nonnull
    protected Duration timeout = Duration.ofHours(1);

    protected ParallelJob(@Nonnull final Stream<P> params, @Nonnull final ForkJoinPool threadPool) {
        this.params = Assert.notNull(params, REQUIRED_INPUT_TEMPLATE_MSG, "params");
        this.threadPool = Assert.notNull(threadPool, REQUIRED_INPUT_TEMPLATE_MSG, "threadPool");
    }

    /**
     * Set a timeout where a default duration is the one hour
     * @param timeout The maximum time to wait.
     * @return The same object
     */
    public ParallelJob<P> setTimeout(@Nonnull final Duration timeout) {
        this.timeout = Assert.notNull(timeout, REQUIRED_INPUT_TEMPLATE_MSG, "timeout");
        return this;
    }

    /** Get of single values where all nulls are excluded

     * @param job Job with a simple value result
     * @return The result stream
     */
    public <R> Stream<R> run(@Nonnull final UserFunction<P, R> job)
            throws ParallelJobException {

        try  {
            return threadPool.submit(() -> params
                    .parallel()
                    .map(job)
                    .collect(Collectors.toList()).stream()
            ).get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .filter(Objects::nonNull);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ParallelJobException(e);
        }
    }

    /** Get result of a Streams
     * @param job Job with a stream result
     * @return The result stream
     * */
    public <R> Stream<R> runOfStream(@Nonnull final UserFunction<P, Stream<R>> job)
            throws ParallelJobException {

        try {
            return threadPool.submit(() -> params
                    .parallel()
                    .map(job)
                    .collect(Collectors.toList()).stream()
            ).get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .flatMap(Function.identity()); // Join all streams
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ParallelJobException(e);
        }
    }

    /** Get a sum of job results type of {@code long}
     *
     * @param job Job with a simple value result
     * @return The sum of job results
     */
    public <R> long runOfSum(@Nonnull final UserFunction<P, Integer> job)
            throws ParallelJobException {
        return run(job)
                .mapToLong(n -> n)
                .sum();
    }

    // --- Static methods ---

    /**
     * A factory method for a multithreading instance
     * @param params All aguments
     * @param threadPool A target {@code threadPoll} or {@code null} to run the job on the current single thread.
     *    For example: {@code new ForkJoinPool(maxThreadCount)}.
     * @return An instance of MultiJob
     */
    public static <P> ParallelJob<P> forEach(@Nonnull final P[] params, @Nullable final ForkJoinPool threadPool) {
        return forEach(Stream.of(params), threadPool);
    }

    /**
     * A factory method
     * @param params All aguments
     * @param threadPool A target {@code threadPoll} or {@code null} to run the job on the current single thread.
     *    For example: {@code new ForkJoinPool(maxThreadCount)}.
     * @return An instance of multiJob
     */
    public static <P> ParallelJob<P> forEach(@Nonnull final Iterable<P> params, @Nullable final ForkJoinPool threadPool) {
        return forEach(StreamSupport.stream(params.spliterator(), false), threadPool);
    }

    /**
     * A factory method
     * @param params All aguments
     * @param threadPool A target {@code threadPoll} or {@code null} to run the job on the current single thread.
     *    For example: {@code new ForkJoinPool(maxThreadCount)}.
     * @return An instance of multiJob
     */
    public static <P> ParallelJob<P> forEach(@Nonnull final LoopingIterator<P> params, @Nullable final ForkJoinPool threadPool) {
        return forEach(params.toStream(), threadPool);
    }

    /**
     * A factory method
     * @param params All aguments
     * @param threadPool A target {@code threadPoll} or {@code null} to run the job on the current single thread.
     *    For example: {@code new ForkJoinPool(maxThreadCount)}.
     * @return An instance of multiJob
     */
    public static <P> ParallelJob<P> forEach(@Nonnull final Stream<P> params, @Nullable final ForkJoinPool threadPool) {
        return threadPool != null
             ? new ParallelJob<>(params, threadPool)
             : new ParallelJob<P>(params, ForkJoinPool.commonPool()) {
                @Override
                public <R> Stream<R> run(@Nonnull final UserFunction<P, R> job)
                        throws ParallelJobException {
                    return params.map(job).filter(Objects::nonNull);
                }

                @Override
                public <R> Stream<R> runOfStream(@Nonnull final UserFunction<P, Stream<R>> job)
                        throws ParallelJobException {
                    return params.map(job).flatMap(Function.identity());
                }
            };
    }

    // --- Class or Interfaces ---

    /** An envelope for checked exceptions */
    public static final class ParallelJobException extends IllegalStateException {

        public ParallelJobException(@Nonnull final Throwable cause) {
            super(Assert.notNull(cause, REQUIRED_INPUT_TEMPLATE_MSG, "cause"));
        }

        @Nonnull @Override
        public Throwable getCause() {
            return super.getCause(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public interface UserFunction<T, R> extends Function<T, R> {

        @Override
        default public R apply(final T t) {
            try {
                return run(t);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new ParallelJobException(e);
                }
            }
        }

        /** Applies this function to the given argument */
        R run(T t) throws Exception;
    }
}
