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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.ujorm.tools.thread.MultiJob.MultiJobException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pavel Ponec
 */
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
    @Test
    public void testGetStream() throws IOException {
        System.out.println("run");
        int maxThreadCount = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadCount);

        Stream<Integer> result = MultiJob.forEach(new Integer[]{1, 2, 3}, threadPool).run(p -> p * 10);
        List<Integer> sortedList = result.sorted().collect(Collectors.toList());
        threadPool.shutdown();
        assertEquals(3, sortedList.size());
        assertEquals(10, sortedList.get(0).intValue());
    }

    /**
     * Test of getSingle method, of class MultiRun.
     */
    @Test
    public void testRunToStream() {
        System.out.println("runToStream");
        int maxThreadCount = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadCount);

        Stream<Integer> result = MultiJob.forEach(new Integer[]{1, 2, 3}, threadPool)
                .runOfStream(p -> Stream.of(p * 10));

        List<Integer> sortedList = result.sorted().collect(Collectors.toList());
        assertEquals(3, sortedList.size());
        assertEquals(10, sortedList.get(0).intValue());
    }

    /**
     * Check a timeout
     */
    @Test
    public void testCheckTimeout() {
        System.out.println("getTimeout");
        int maxThreadCount = 10;
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadCount);
        Duration timeout = Duration.ofMillis(100);
        MultiJobException result = null;
        Stream<Long> stream = null;

        try {
            MultiJob.forEach(new Integer[]{100, 200, 500}, threadPool)
                    .setTimeout(timeout)
                    .run(p -> sleep(Duration.ofMillis(p)))
                    .collect(Collectors.toList());
        } catch (MultiJobException e) {
            result = e;
        }

        assertTrue(stream == null);
        assertTrue(result != null);
        assertTrue(result.getCause() instanceof TimeoutException);
    }

    /**
     * Check Time of parallel work.
     */
    @Test
    public void testTimeOfParalellWork() {
        System.out.println("timeOfParalellWork");

        Duration jobDuration = Duration.ofSeconds(1);
        int jobCount = 120;
        List<Duration> params = Collections.nCopies(jobCount, jobDuration);
        LocalDateTime start = LocalDateTime.now();
        ExecutorService threadPool = Executors.newFixedThreadPool(jobCount);

        List<Integer> list = MultiJob.forEach(params, threadPool)
                .run(duration -> sleep(duration)) // 1 sec
                .collect(Collectors.toList());

        Duration duration = Duration.between(start, LocalDateTime.now());
        assertTrue("Real time took millis: " + duration.toMillis(), duration.toMillis() > jobDuration.toMillis());
        assertTrue("Real time took millis: " + duration.toMillis(), duration.toMillis() < jobDuration.toMillis() + 200);
        assertEquals(jobCount, list.size());
    }

    /**
     * Check Time of paralel work..
     */
    @Test
    public void testTimeOfSequentialWork() {
        System.out.println("timeOfSequentialWork");

        Duration jobDuration = Duration.ofSeconds(1);
        int jobCount = 10;
        List<Duration> params = Collections.nCopies(jobCount, jobDuration);

        LocalDateTime start = LocalDateTime.now();
        List<Integer> list = MultiJob.forEach(params, null)
                .run(duration -> sleep(duration)) // 1 sec
                .collect(Collectors.toList());
        LocalDateTime stop = LocalDateTime.now();

        Duration duration = Duration.between(start, LocalDateTime.now());
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() >= jobCount);
        assertTrue("Real time took sec: " + duration.getSeconds(), duration.getSeconds() < jobCount + 1);
        assertEquals(jobCount, list.size());
    }

    private Integer sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
            return (int) duration.toMillis();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "An interrupting of the test", e);
            return null;
        }
    }
}
