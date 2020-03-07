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

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pavel Ponec
 */
@Ignore // TODO
public class MultiJobTest {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Before
    public void tearUp() {
        final String msg = String.format(">>> Free memory: %.1f/%.1f MB.",
                Runtime.getRuntime().freeMemory() / 1_000_000f,
                Runtime.getRuntime().maxMemory() / 1_000_000f);
        System.out.println(msg);
    }

    /**
     * Test of getStream method, of class MultiRun.
     */
    @Test(timeout = 100)
    public void testGetStream() throws IOException {
        System.out.println("run");
        int maxThreadCount = 10;
        JobContext jobContext = JobContext.forMultiJob(maxThreadCount);
        IntStream params = IntStream.of(1, 2, 3);

        List<Integer> result = jobContext.forEach(params)
                .run(p -> p * 10)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(3, result.size());
        assertEquals(10, result.get(0).intValue());
    }

    /**
     * Test of getSingle method, of class MultiRun.
     */
    @Test(timeout = 100)
    public void testRunToStream() {
        System.out.println("runToStream");
        int maxThreadCount = 10;
        JobContext jobContext = JobContext.forMultiJob(maxThreadCount);
        IntStream params = IntStream.of(1, 2, 3);

        List<Integer> result = jobContext.forEach(params)
                .runOfStream(i -> Stream.of(i * 10))
                .sorted()
                .collect(Collectors.toList());

        assertEquals(3, result.size());
        assertEquals(10, result.get(0).intValue());
    }

    /**
     * Check a timeout
     */
    @Test(timeout = 2_500_000)
    public void testCheckTimeout() {
        System.out.println("checkTimeout");

        int jobCount = 10;
        IntStream params = IntStream.of(100, 200, 300);
        Duration timeout = Duration.ofMillis(150);
        JobContext jobContext = JobContext.forMultiJob(jobCount, timeout);
        JobException exeption = null;
        int result = 0;

        try {
            result = jobContext.forEach(params)
                    .setTimeout(timeout)
                    .run(i -> sleep(i, Duration.ofMillis(i)))
                    .mapToInt(i -> i)
                    .sum();
        } catch (JobException e) {
            exeption = e;
        }

        assertEquals(0, result);
        assertTrue(exeption != null);
        assertEquals(exeption.getCause().getClass(), TimeoutException.class);
    }

    /**
     * Check a timeout
     */
    @Ignore
    @Test(timeout = 2_500)
    public void testCheckTimeoutOfStream() {
        System.out.println("checkTimeoutOfStream");
        int maxThreadCount = 10;
        Duration timeout = Duration.ofMillis(150);
        IntStream params = IntStream.of(100, 200, 300);
        JobContext jobContext = JobContext.forMultiJob(maxThreadCount, timeout);
        JobException exeption = null;
        int result = 0;

        try {
            result = jobContext.forEach(params)
                    .runOfStream(i -> Stream.of(sleep(i, Duration.ofMillis(i))))
                    .mapToInt(i -> i)
                    .sum();
        } catch (JobException e) {
            exeption = e;
        }

        assertEquals(0, result);
        assertTrue(exeption != null);
        assertEquals(exeption.getCause().getClass(), TimeoutException.class);
    }

    /**
     * Check Time of parallel work.
     */
    @Test(timeout = 2_000)
    public void testParalellWorkTime() {
        System.out.println("paralellWorkTime");

        int jobCount = 120;
        Duration duration = Duration.ofSeconds(1);
        IntStream params = IntStream.rangeClosed(1, jobCount);
        JobContext jobContext = JobContext.forMultiJob(jobCount);
        LocalDateTime start = LocalDateTime.now();

        List<Integer> list = jobContext.forEach(params)
                .run(i -> sleep(i, duration))
                .collect(Collectors.toList());

        Duration total = Duration.between(start, LocalDateTime.now());
        assertTrue("Real time took millis: " + total.toMillis(), total.toMillis() > duration.toMillis());
        assertTrue("Real time took millis: " + total.toMillis(), total.toMillis() < duration.toMillis() + 200);
        assertEquals(jobCount, list.size());
    }

    /**
     * Check Time of paralel work..
     */
    @Test(timeout = 1_200)
    public void testTimeOfSequentialWork() {
        System.out.println("timeOfSequentialWork");

        Duration duration = Duration.ofMillis(100);
        int jobCount = 10;
        IntStream params = IntStream.rangeClosed(1, jobCount);
        JobContext jobContext = JobContext.forSingleThread();
        LocalDateTime start = LocalDateTime.now();

        List<Integer> result = jobContext.forEach(params)
                .run(id -> sleep(id, duration))
                .sorted()
                .collect(Collectors.toList());

        Duration total = Duration.between(start, LocalDateTime.now());
        assertTrue("Real time took millis: " + total.toMillis(), total.toMillis() >= jobCount * duration.toMillis());
        assertTrue("Real time took millis: " + total.toMillis(), total.toMillis() < jobCount * duration.toMillis() + 100);
        assertEquals(jobCount, result.size());
    }

    /** Wait a required duration and returns the same numerical value */
    private Integer sleep(Integer i, Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
            return i;
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "An interrupting of the test", e);
            return null;
        }
    }
}
