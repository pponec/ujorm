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

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;

/**
 * A multithreading task runner
 *
 * Before using this class, make sure the target JRE contains a JDK-8224620 fix :
 * https://bugs.openjdk.java.net/browse/JDK-8224620 .
 *
 * @see https://www.baeldung.com/java-fork-join
 * @see https://stackoverflow.com/questions/21163108/custom-thread-pool-in-java-8-parallel-stream
 * @since 1.95
 * @author Pavel Ponec
 */
public class ParallelJob<P> extends Jobs<P> {

    /** Template message for an invalid input */
    protected static final String REQUIRED_INPUT_TEMPLATE_MSG = "The {} is required";

    /** Thread pool */
    @Nonnull
    protected final ForkJoinPool threadPool;

    protected ParallelJob(
            @Nonnull final Collection<P> params,
            @Nonnull final JobContext jobContext
    ) {
        super(params, jobContext.getTimeout());
        this.threadPool = Assert.notNull(jobContext.getThreadPool(), REQUIRED_INPUT_TEMPLATE_MSG, "threadPool");
    }

    /** Get of single values where all nulls are excluded

     * @param job Job with a simple value result
     * @return The result stream
     */
    @Override
    public <R> Stream<R> run(@Nonnull final UserFunction<P, R> job)
            throws JobException {

        final AsyncStreamBuilder<R> result = new AsyncStreamBuilder<>(params.size(), timeout);
        try  {
            threadPool.submit(() -> getParallel().map(job))
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .forEach(t -> result.add(t));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new JobException(e);
        }
        return result.stream();
    }

    /** Get result of a Streams
     * @param job Job with a stream result
     * @return The result stream
     * */
    @Override
    public <R> Stream<R> runOfStream(@Nonnull final UserFunction<P, Stream<R>> job)
            throws JobException {
        final AsyncStreamBuilder<R> result = new AsyncStreamBuilder<>(params.size(), timeout);
        try {
            threadPool.submit(() -> getParallel().map(job))
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS)
                    .flatMap(Function.identity())
                    .forEach(t -> result.add(t));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new JobException(e);
        }
        return result.stream();
    }
}
