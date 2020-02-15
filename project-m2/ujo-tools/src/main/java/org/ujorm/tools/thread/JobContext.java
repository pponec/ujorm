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
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;

/**
 * A context for a {@link Jobs} class.
 *
 * The instance affects to a job performace, three implementations are available.
 * <ul>
 *   <li>A solution based on a parallel Stream object has less memory footprint but worse performance of remote call operations.</li>
 *   <li>A solution based on CompletableFuture objects has great performance of remote call operations, but higher memory footprint.</li>
 *   <li>The last implementation call required job on the current single thread.</li>
 * </ul>
 *
 * Save an instance of the JobContent to your application context per a group of tasks.
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
public class JobContext {

    /** Thread pool */
    @Nullable
    private final ExecutorService threadPool;

    /** Default timeout */
    @Nonnull
    private final Duration timeout;

    protected JobContext(@Nullable final ExecutorService threadPool, @Nonnull final Duration timeout) {
        this.threadPool = threadPool;
        this.timeout = Assert.notNull(timeout, "timeout");
    }

    @Nonnull
    public Duration getTimeout() {
        return timeout;
    }

    protected final boolean noThreadPool() {
        return threadPool == null;
    }

    protected final boolean hasForkJoinPool() {
        return threadPool instanceof ForkJoinPool;
    }

    @Nullable
    public <T extends ExecutorService> T getThreadPool() {
        return (T) threadPool;
    }

    public void shutdown() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }

    public void shutdownNow() {
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
    }

    // --- Runner methods ---

    /**
     * A factory method for a multithreading instance
     * @param params All aguments
     * @return An instance of MultiJob
     */
    public Jobs<Integer> forEach(@Nonnull final IntStream params) {
        return forEach(params.boxed());
    }

    /**
     * A factory method for a multithreading instance
     * @param params All aguments
     * @return An instance of MultiJob
     */
    public <P> Jobs<P> forEach(@Nonnull final Stream<P> params) {
        return forEach(params.collect(Collectors.toList()));
    }

    /**
     * A factory method for a multithreading instance
     * @param params All aguments
     * @return An instance of MultiJob
     */
    public <P> Jobs<P> forEach(@Nonnull final P... params) {
        return forEach(Arrays.asList(params));
    }

    /**
     * A factory method
     * @param params All aguments
     * @return An instance of multiJob
     */
    public <P> Jobs<P> forEach(@Nonnull final Collection<P> params) {
        return noThreadPool()
                ? new SyncJob(params, getTimeout()) // A single thread solution
                : hasForkJoinPool()
                ? new ParallelJob<>(params, this) // ThreadPool based on ForkJoinPool
                : new MultiJob<>(params, this); // ThreadPool based on ExecutorService
    }

    // --- Static methods ----

    public static JobContext forSingleThread() {
        return forAny(null, Duration.ofHours(1));
    }

    public static JobContext forMultiJob(int nThreads) {
        return forMultiJob(nThreads, Duration.ofHours(1));
    }

    public static JobContext forMultiJob(int nThreads, Duration timeout) {
        return new JobContext(Executors.newFixedThreadPool(nThreads), timeout);
    }

    public static JobContext forParallelJob(int parallelism) {
        return forParallelJob(parallelism, Duration.ofHours(1));
    }

    public static JobContext forParallelJob(Duration timeout) {
        return forAny(ForkJoinPool.commonPool(), timeout);
    }

    public static JobContext forParallelJob(int parallelism, Duration timeout) {
        return forAny(new ForkJoinPool(parallelism), timeout);
    }

    public static JobContext forAny(@Nullable final ExecutorService threadPool, @Nonnull final Duration timeout) {
        return new JobContext(threadPool, timeout);
    }

}
