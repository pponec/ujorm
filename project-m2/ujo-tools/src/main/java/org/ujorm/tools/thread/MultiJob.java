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
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * A multithreading task runner
 * @author Pavel Ponec
 *
 * @see https://dzone.com/articles/think-twice-using-java-8
 * @see https://www.baeldung.com/java-completablefuture
 * @since 1.94
 */
public class MultiJob<P> {

    /** Template message for an invalid input */
    protected static final String REQUIRED_INPUT_TEMPLATE_MSG = "The {} is required";

    /** Job arguments */
    @Nonnull
    final protected Stream<P> params;

    /** A timeout where a default duration is the one hour */
    @Nonnull
    protected Duration timeout = Duration.ofHours(1);

    /** Executor contains a thread pool */
    @Nullable
    protected Executor executor;

    protected MultiJob(@Nonnull final Stream<P> params) {
        this.params = Assert.notNull(params, REQUIRED_INPUT_TEMPLATE_MSG, "params");
    }

    /**
     * Set a timeout where a default duration is the one hour
     * @param timeout The maximum time to wait.
     * @return The same object
     */
    public MultiJob<P> setTimeout(@Nonnull final Duration timeout) {
        this.timeout = Assert.notNull(timeout, REQUIRED_INPUT_TEMPLATE_MSG, "timeout");
        return this;
    }

    /** Assign an excecutor where the {@code null} value activates a default executor.
     * @param executor For examle: {@code Executors.newFixedThreadPool(10)}
     */
    public MultiJob<P> setExecutor(@Nullable final Executor executor) {
        this.executor = executor;
        return this;
    }

    /**
     * Assign a new Fixed Thread Pool
     * @param nThreads the number of threads in the pool
     * @return The same object
     */
    public MultiJob<P> setNewFixedThreadPool(final int nThreads) {
        return setExecutor(Executors.newFixedThreadPool(nThreads));
    }

    /** Get of single values where all nulls are excluded

     * @param job Job with a simple value result
     * @return The result stream
     */
    public <R> Stream<R> run(@Nonnull final UserFunction<P, R> job)
            throws MultiJobException {
        return params.map(getAsync(job))
                .collect(Collectors.toList()).stream() // join all threads
                .map(createGrabber(timeout))
                .filter(Objects::nonNull);
    }

    /** Get result of a Streams
     * @param job Job with a stream result
     * @return The result stream
     * */
    public <R> Stream<R> runOfStream(@Nonnull final UserFunction<P, Stream<R>> job)
            throws MultiJobException {
        return params.map(getAsync(job))
                .collect(Collectors.toList()).stream() // join all threads
                .map(createGrabber(timeout))
                .flatMap(Function.identity()); // Join all streams
    }

    /** Get a sum of job results type of {@code long}
     *
     * @param job Job with a simple value result
     * @return The sum of job results
     */
    public <R> long runOfSum(@Nonnull final UserFunction<P, Integer> job)
            throws MultiJobException {
        return run(job)
                .mapToLong(n -> n)
                .sum();
    }

    /** Create an async function */
    protected <R> Function<P, CompletableFuture<R>> getAsync(@Nonnull final UserFunction<P, R> job) {
        return executor != null
                ? p -> CompletableFuture.supplyAsync(() -> job.apply(p), executor)
                : p -> CompletableFuture.supplyAsync(() -> job.apply(p));
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

    // --- Static methods ---

    public static <P> MultiJob<P> forEach(@Nonnull final Stream<P> params) {
        return new MultiJob<>(params);
    }

    public static <P> MultiJob<P> forEach(@Nonnull final Collection<P> params) {
        return forEach(params.stream());
    }

    public static <P> MultiJob<P> forEach(@Nonnull final P... params) {
        return new MultiJob<>(Stream.of(params));
    }

    public static <P> MultiJob<P> forEach(@Nonnull final Iterable<P> params) {
        return forEach(StreamSupport.stream(params.spliterator(), false));
    }

    /**
     * @param params All aguments
     * @param multiThread Multithreading can be disabled
     * @return
     */
    public static <P> MultiJob<P> forEach(@Nonnull final Collection<P> params, final boolean multiThread) {
        return forEach(params.stream(), multiThread);
    }

    /**
     * @param params All aguments
     * @param multiThread Multithreading can be disabled
     * @return
     */
    public static <P> MultiJob<P> forEach(@Nonnull final Stream<P> params, final boolean multiThread) {
        if (multiThread) {
            return forEach(params, false);
        } else {
            return new MultiJob<P>(params) {
                @Override
                public <R> Stream<R> run(@Nonnull final UserFunction<P, R> job)
                        throws MultiJobException {
                    return params.map(job).filter(Objects::nonNull);
                }

                @Override
                public <R> Stream<R> runOfStream(@Nonnull final UserFunction<P, Stream<R>> job)
                        throws MultiJobException {
                    return params.map(job).flatMap(Function.identity());
                }
            };
        }
    }

    // --- Class or Interfaces ---

    /** An envelope for checked exceptions */
    public static final class MultiJobException extends IllegalStateException {

        public MultiJobException(@Nonnull final Throwable cause) {
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
                    throw new MultiJobException(e);
                }
            }
        }

        /** Applies this function to the given argument */
        R run(T t) throws Exception;


    }
}
