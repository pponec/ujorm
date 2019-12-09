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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;
import org.ujorm.tools.thread.ParallelJob.ParallelJobException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pavel Ponec
 */
public class ParallelJobTest {

    private final Logger logger = Logger.getLogger(getClass().getName());

    /** A time of the one task */
    private final Duration TASK_TIME = Duration.ofSeconds(1);

    /**
     * Test of getStream method, of class MultiRun.
     */
    @Test
    public void testGetStream() {
        System.out.println("run");

        Stream<Integer> result = ParallelJob.forEach(1, 2, 3).run(p -> p * 10);

        List<Integer> sortedList = result.sorted().collect(Collectors.toList());
        assertEquals(3, sortedList.size());
        assertEquals(10, sortedList.get(0).intValue());
    }

    /**
     * Test of getSingle method, of class MultiRun.
     */
    @Test
    public void testRunToStream() {
        System.out.println("runToStream");

        Stream<Integer> result = ParallelJob.forEach(1, 2, 3).runOfStream(p -> Stream.of(p * 10));

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
        Duration timeout = Duration.ofMillis(100);
        ParallelJobException result = null;
        Stream<Long> stream = null;

        try {
            ParallelJob.forEach(100, 200, 500)
                    .setTimeout(timeout)
                    .run(p -> sleep(Duration.ofMillis(p)))
                    .collect(Collectors.toList());
        } catch (ParallelJobException e) {
            result = e;
        }

        assertTrue(stream == null);
        assertTrue(result != null);
        assertTrue(result.getCause() instanceof TimeoutException);
    }

    /**
     * Check Time of parallel work.
     * @deprecated The test fails
     */
    @Ignore
    @Test
    public void testTimeOfParalellWork() {
        System.out.println("timeOfParalellWork");

        int jobCount = 100;
        Duration[] params = new Duration[jobCount];
        Arrays.fill(params, TASK_TIME);

        LocalDateTime start = LocalDateTime.now();
        List<Integer> list = ParallelJob.forEach(params)
                .setExecutor(new ForkJoinPool(jobCount))
                .run(duration -> sleep(duration)) // 1 sec
                   .collect(Collectors.toList());
        LocalDateTime stop = LocalDateTime.now();

        Duration duration = Duration.between(start, stop);
        assertTrue(String.format("Real working time was %s millis",
                duration.toMillis()),
                duration.toMillis() <= 1.15 * TASK_TIME.toMillis());
        assertTrue(String.format("Real working time was %s millis",
                duration.toMillis()),
                duration.toMillis() > 1.00 * TASK_TIME.toMillis());
        assertEquals(jobCount, list.size());
        logger.log(Level.INFO, "Real working time was {0} seconds.", duration.getSeconds());
    }

    /**
     * Check Time of paralel work..
     */
    @Test
    public void testTimeOfSequentialWork() {
        System.out.println("timeOfSequentialWork");

        int jobCount = 10;
        Duration[] params = new Duration[jobCount];
        Arrays.fill(params, TASK_TIME);

        LocalDateTime start = LocalDateTime.now();
        List<Integer> list = ParallelJob.forEach(Stream.of(params), false)
                .setNewFixedThreadPool(jobCount)
                .run(duration -> sleep(duration)) // 1 sec
                   .collect(Collectors.toList());
        LocalDateTime stop = LocalDateTime.now();

        Duration duration = Duration.between(start, stop);
        assertTrue(String.format("Real working time was %s millis",
                duration.toMillis()),
                duration.toMillis() > jobCount * TASK_TIME.toMillis());
        assertEquals(jobCount, list.size());
        logger.log(Level.INFO, "Real working time was {0} seconds.", duration.getSeconds());
    }

    /**
     * Check Time of ParallelStream.
     */
    @Test
    public void testTimeOfParallelStream() {
        System.out.println("timeOfParallelStream");

        int jobCount = 100;
        Duration[] params = new Duration[jobCount];
        Arrays.fill(params, TASK_TIME);

        LocalDateTime start = LocalDateTime.now();
        List<Integer> list = Arrays.stream(params)
                .parallel()
                .map(duration -> sleep(duration)) // 1 sec
                .collect(Collectors.toList());
        LocalDateTime stop = LocalDateTime.now();

        Duration duration = Duration.between(start, stop);
        assertTrue(String.format("Real working time was %s millis",
                duration.toMillis()),
                duration.toMillis() <= 15.5 * TASK_TIME.toMillis());
        assertEquals(jobCount, list.size());
        logger.log(Level.INFO, "Real working time was {0} seconds.", duration.getSeconds());
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
