/*
 * Copyright 2020-2020 Pavel Ponec
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

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.ujorm.tools.Assert;
import static org.junit.Assert.assertEquals;

/**
 * Java RX implementation test
 *
 * @author Pavel Ponec
 */
public class RxJobTest {

    /**
     * Test of getSingle method, of class MultiRun.
     */
    @Test
    public void testRunToStream() {
        System.out.println("runToStream");
        int maxThreadCount = 10;
        JobContext jobContext = new RxContext(Schedulers.io(), maxThreadCount);
        IntStream params = IntStream.rangeClosed(1, 10);

        List<Integer> result = jobContext.forEach(params)
                .runOfStream(i -> Stream.of(i * 10))
                .sorted()
                .collect(Collectors.toList());

        assertEquals(maxThreadCount, result.size());
        assertEquals(10, result.get(0).intValue());
    }

    // === RxJava implementation context ===

    private static class RxContext extends JobContext {
        private final Scheduler schdlr;
        private final int prefetch;

        public RxContext(@NonNull Scheduler schdlr, int prefetch) {
            super(null, Duration.ofHours(1));
            this.schdlr = Assert.notNull(schdlr, "schdlr");
            this.prefetch = prefetch;
        }

        /**
         * A factory method
         * @param params All aguments
         * @return An instance of multiJob
         */
        @Override
        public <P> Jobs<P> forEach(@Nonnull final Collection<P> params) {
            return new Jobs<P>(params, getTimeout()) {
                @Override
                protected <R> Stream<R> createStream(Function<P, R> job) {
                    AsyncStreamBuilder<R> result = new AsyncStreamBuilder<>(params.size());
                    Flowable.fromIterable(params)
                            .parallel()
                            .runOn(RxContext.this.schdlr, prefetch)
                            .map(v -> job.apply(v))
                            .sequential()
                            .blockingSubscribe(t -> result.add(t));
                    return result.stream();
                }
            };
        }
    }
}
