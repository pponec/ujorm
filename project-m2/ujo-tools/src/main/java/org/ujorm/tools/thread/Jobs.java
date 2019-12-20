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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;

/**
 * API for a multithreading tasks.
 *
 * Usage:
 * <pre class="pre">
 *   JobContext jobContext = JobContext.forMultiJob(10);
 *   Stream&lt;Integer&gt; result = jobContext.forEach(1, 2, 3)
 *         .run(p -> p * 10);
 * </pre>
 *
 * For more samples see a {@code MultiJobTest} class.
 *
 * @author Pavel Ponec
 *
 * @see https://dzone.com/articles/think-twice-using-java-8
 * @see https://www.baeldung.com/java-completablefuture
 * @since 1.94
 */
public class Jobs<P> {

    /** Template message for an invalid input */
    protected static final String REQUIRED_INPUT_TEMPLATE_MSG = "The {} is required";

    /** Job arguments */
    @Nonnull
    private final Stream<P> params;

    /** A timeout of a job where a default duration is the one hour */
    @Nonnull
    protected Duration timeout;

    protected Jobs(
            @Nonnull final Stream<P> params,
            @Nonnull final Duration timeout
    ) {
        Assert.notNull(params, REQUIRED_INPUT_TEMPLATE_MSG, "params");
        Assert.notNull(timeout, REQUIRED_INPUT_TEMPLATE_MSG, "timeout");

        this.params = params;
        this.timeout = timeout;
    }

    /** Get a parameter Stream like a parallel type */
    @Nonnull
    protected final Stream<P> getParallel() {
        final Stream<P> result = params.parallel();
        return result.isParallel()
                ? result
                : params.collect(Collectors.toList()).parallelStream();
    }

    /**
     * Set a timeout where a default duration is the one hour
     * @param timeout The maximum time to wait.
     * @return The same object
     */
    public final Jobs<P> setTimeout(@Nonnull final Duration timeout) {
        this.timeout = Assert.notNull(timeout, REQUIRED_INPUT_TEMPLATE_MSG, "timeout");
        return this;
    }

    /** Get of single values where all nulls are excluded
     * @param job Job with a simple value result
     * @return The result stream
     */
    public <R> Stream<R> run(@Nonnull final UserFunction<P, R> job) {
        return params.map(job).filter(Objects::nonNull);
    }

    /** Get result of a Streams
     * @param job Job with a stream result
     * @return The result stream
     * */
    public <R> Stream<R> runOfStream(@Nonnull final UserFunction<P, Stream<R>> job) {
         return params.map(job).flatMap(Function.identity());
    }

    /** Get a sum of job results type of {@code long}
     *
     * @param job Job with a simple value result
     * @return The sum of job results
     */
    public <R> long runOfSum(@Nonnull final UserFunction<P, Integer> job)
            throws JobException {
        return run(job)
                .mapToLong(n -> n)
                .sum();
    }

    // --- Class or Interfaces ---

    /** An envelope for checked exceptions */
    public static final class JobException extends IllegalStateException {

        public JobException(@Nonnull final Throwable cause) {
            super(Assert.notNull(cause, REQUIRED_INPUT_TEMPLATE_MSG, "cause"));
        }

        @Nonnull @Override
        public Throwable getCause() {
            return super.getCause(); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public interface UserFunction<T, R> extends Function<T, R> {

        @Nullable
        @Override
        default public R apply(final T t) {
            try {
                return run(t);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new JobException(e);
                }
            }
        }

        /** Applies this function to the given argument */
        @Nullable
        R run(T t) throws Exception;
    }
}
