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

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * All tested pass on JRE 8 (build 1.8.0_191-b12).
 * First three tests failed on JRE 11 ((build 11.0.4+11-post-Ubuntu-1ubuntu218.04.3).
 * @see https://www.baeldung.com/java-8-parallel-streams-custom-threadpool
 * @author Pavel Ponec
 */
public class SampleTest {

    private final Logger logger = Logger.getLogger(getClass().getName());

    /** A time limit of the one task */
    private final Duration TASK_LIMIT = Duration.ofSeconds(2);

    @Test
    public void test_01() {

        final Duration jobDuration = Duration.ofSeconds(1);
        final int threadCount = 120;
        final List<Duration> resources = Collections.nCopies(threadCount, jobDuration);
        final LocalDateTime start = LocalDateTime.now();
        final int timeMs = resources.stream()
                .parallel()
                .map(d -> sleep(d))
                .mapToInt(i -> i)
                .sum();
        final Duration duration = Duration.between(start, LocalDateTime.now());

        assertEquals(Duration.ofSeconds(threadCount), Duration.ofMillis(timeMs));
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() > 14);
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() < 20);
    }

    @Test
    public void test_02a() {

        final Duration jobDuration = Duration.ofSeconds(1);
        final int threadCount = 120;
        final List<Duration> resources = Collections.nCopies(threadCount, jobDuration);
        final ForkJoinPool customThreadPool = new ForkJoinPool(threadCount);
        final Stream <Integer> result;
        final LocalDateTime start = LocalDateTime.now();

        try (Closeable c = () -> customThreadPool.shutdown()) {
            result = customThreadPool.submit(() -> {
                return resources
                        .parallelStream()
                        .map(p -> sleep(p));
            }).get(TASK_LIMIT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | IOException | TimeoutException e) {
            throw new IllegalStateException(e);
        }

        final int timeMs = result.mapToInt(i -> i).sum();
        final Duration duration = Duration.between(start, LocalDateTime.now());

        assertEquals(Duration.ofSeconds(threadCount), Duration.ofMillis(timeMs));
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() > 14);
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() < 20);
    }

    @Test
    public void test_02b() throws InterruptedException, ExecutionException {

        final Duration jobDuration = Duration.ofSeconds(1);
        final int threadCount = 120;
        final List<Duration> resources = Collections.nCopies(threadCount, jobDuration);
        final LocalDateTime start = LocalDateTime.now();

        final Stream<Integer> result;
        final ForkJoinPool customThreadPool = new ForkJoinPool(threadCount);

        try (Closeable c = () -> customThreadPool.shutdown()) {
            result = customThreadPool.submit(() -> {
                return resources
                        .parallelStream()
                        .map(p -> Stream.of(sleep(p)));
            }).get(TASK_LIMIT.toMillis(), TimeUnit.MILLISECONDS)
                    .flatMap(s -> s);
        } catch (InterruptedException | ExecutionException | IOException | TimeoutException e) {
            throw new IllegalStateException(e);
        }

        final int timeMs = result.mapToInt(i -> i).sum();
        final Duration duration = Duration.between(start, LocalDateTime.now());

        assertEquals(Duration.ofSeconds(threadCount), Duration.ofMillis(timeMs));
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() > 14);
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() < 20);
    }

    @Test(expected = IllegalStateException.class)
    public void test_03_timeout() {

        final int timeLimitSec = 1;
        final int threadCount = 2;
        final List<Duration> resources = Collections.nCopies(threadCount, Duration.ofSeconds(timeLimitSec + 2));
        final ForkJoinPool customThreadPool = new ForkJoinPool(threadCount);
        final Stream <Integer> result;
        final LocalDateTime start = LocalDateTime.now();

        try (Closeable c = () -> customThreadPool.shutdown()) {
            result = customThreadPool.submit(() -> {
                return resources
                        .parallelStream()
                        .map(p -> sleep(p))
                        .collect(Collectors.toList()) // Required to check a job timeout
                        .stream();
            }).get(timeLimitSec, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | IOException | TimeoutException e) {
            throw new IllegalStateException(e);
        }

        final int timeMs = result.mapToInt(i -> i).sum();
        final Duration duration = Duration.between(start, LocalDateTime.now());

        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() > 14);
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() < 20);
        assertTrue("Incorrect statement", timeMs < 0);
    }

    private Integer sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
            logger.log(Level.INFO, "Sleeping {0} millis on {1}",
                     new Object[]{ duration.toMillis(), LocalDateTime.now()} );
            return (int) duration.toMillis();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "An interrupting of the test", e);
            return null;
        }
    }
}
