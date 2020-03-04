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
import java.util.function.Function;
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
 */
public abstract class Jobs<P> {

    /** Template message for an invalid input */
    protected static final String REQUIRED_INPUT_TEMPLATE_MSG = "The {} is required";

    /** Job arguments */
    @Nonnull
    protected final Collection<P> params;

    /** A timeout of a job where a default duration is the one hour */
    @Nonnull
    protected Duration timeout;

    protected Jobs(
            @Nonnull final Collection<P> params,
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
        return params.parallelStream();
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
     *
     * @param job Job with a simple value result
     * @return The result stream
     */
    public <R> Stream<R> run(@Nonnull final JobFunction<P, R> job)
            throws JobException {

        final AsyncStreamBuilder<R> result = new AsyncStreamBuilder<>(params.size(), timeout);
        try {
            createStream(innerJob(job, result))
                    .forEach(t -> result.add(t));
        } catch (Exception e) {
            result.interrupt(e);
        }
        return result.stream();
    }

    /** Get result of a Streams
     *
     * @param job Job with a stream result
     * @return The result stream
     */
    public <R> Stream<R> runOfStream(@Nonnull final JobFunction<P, Stream<R>> job)
            throws JobException {
        final AsyncStreamBuilder<R> result = new AsyncStreamBuilder<>(params.size(), timeout);
        try {
            createStream(innerJob(job, result))
                    .flatMap(Function.identity())
                    .forEach(t -> result.add(t));
        } catch (Exception e) {
            result.interrupt(e);
        }
        return result.stream();
    }

    /** An envelope of working job */
    protected <P2, R2> Function<P2, R2> innerJob(
            @Nonnull final JobFunction<P2, R2> job,
            @Nullable final AsyncStreamBuilder result
    ) {
        return (final P2 p) -> {
            try {
                return job.run(p);
            } catch (Exception e) {
//                if (result != null) {
//                    result.interrupt(e.getCause());
//                }
                throw JobException.of(e);
            }
        };
    }

    /** Create a stream with a job processing */
    protected abstract <R> Stream<R> createStream(
            @Nonnull final Function<P, R> job);

    // --- Class or Interfaces ---

    public interface JobFunction<T, R> extends Function<T, R> {

        default R apply(final T t) {
            try {
                return run(t);
            } catch (Exception e) {
                throw JobException.of(e);
            }
        }

        /** Applies this function to the given argument */
        @Nullable
        R run(T t) throws Exception;
    }
}
