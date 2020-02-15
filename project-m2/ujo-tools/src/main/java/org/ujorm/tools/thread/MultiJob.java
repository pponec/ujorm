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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
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
public class MultiJob<P> extends Jobs<P> {

    @Nonnull
    protected final Executor threadPool;

    protected MultiJob(
            @Nonnull final Collection<P> params,
            @Nonnull final JobContext jobContext
    ) {
        super(params, jobContext.getTimeout());
        this.threadPool = Assert.notNull(jobContext.getThreadPool(), REQUIRED_INPUT_TEMPLATE_MSG, "threadPool");
    }

    /** Get of single values where all nulls are excluded
     *
     * @param job Job with a simple value result
     * @return The result stream
     */
    @Override
    public <R> Stream<R> run(@Nonnull final UserFunction<P, R> job)
            throws JobException {
        AsyncStreamBuilder<R> result = new AsyncStreamBuilder<>(params.size(), timeout);
        getParallel().map(p -> CompletableFuture.supplyAsync(() -> job.apply(p), threadPool))
                .map(createGrabber())
                .forEach(t ->  result.add(t));
        return result.stream();
    }

    /** Get result of a Streams
     *
     * @param job Job with a stream result
     * @return The result stream
     * */
    @Override
    public <R> Stream<R> runOfStream(@Nonnull final UserFunction<P, Stream<R>> job)
            throws JobException {
        AsyncStreamBuilder<R> result = new AsyncStreamBuilder<>(params.size(), timeout);
        getParallel().map(p -> CompletableFuture.supplyAsync(() -> job.apply(p), threadPool))
                .map(createGrabber())
                .flatMap(Function.identity())
                .forEach(t -> result.add(t));
        return result.stream();
    }

    protected <R> Function<CompletableFuture<R>, R> createGrabber() {
        return t -> {
            try {
                return t.get(timeout.toMillis(), MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new JobException(e);
            }
        };
    }
}
