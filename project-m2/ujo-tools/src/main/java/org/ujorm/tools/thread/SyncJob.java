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
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

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
public class SyncJob<P> extends Jobs<P> {

    public SyncJob(
            @Nonnull final Collection<P> params,
            @Nonnull final Duration timeout
    ) {
        super(params, timeout);
    }

    /** Get of single values where all nulls are excluded
     * @param job Job with a simple value result
     * @return The result stream
     */
    @Override
    public <R> Stream<R> run(@Nonnull final UserFunction<P, R> job) {
        return params.stream().map(job).filter(Objects::nonNull);
    }

    /** Get result of a Streams
     * @param job Job with a stream result
     * @return The result stream
     * */
    @Override
    public <R> Stream<R> runOfStream(@Nonnull final UserFunction<P, Stream<R>> job) {
         return params.stream().map(job).flatMap(Function.identity());
    }

    @Override
    protected Stream createStream(UserFunction job) {
        throw new UnsupportedOperationException("Unsupported method");
    }
}
