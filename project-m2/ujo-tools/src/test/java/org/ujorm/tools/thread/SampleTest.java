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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @see https://www.baeldung.com/java-8-parallel-streams-custom-threadpool
 * @author Pavel Ponec
 */
public class SampleTest {

    private final Logger logger = Logger.getLogger(getClass().getName());

    @Test
    public void test_01() {

        int threadCount = 120;
        List<Duration> resources = Collections.nCopies(threadCount, Duration.ofSeconds(1));
        LocalDateTime start = LocalDateTime.now();
        int timeMs = resources.stream().parallel().map(d -> sleep(d)).mapToInt(i -> i).sum();
        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start, end);

        assertEquals(Duration.ofSeconds(threadCount), Duration.ofMillis(timeMs));
        assertTrue(duration.getSeconds() > 15);
    }

    @Test
    public void test_02() throws InterruptedException, ExecutionException {

        int threadCount = 120;
        List<Duration> resources = Collections.nCopies(threadCount, Duration.ofSeconds(1));
        LocalDateTime start = LocalDateTime.now();

        ForkJoinPool customThreadPool = new ForkJoinPool(threadCount);
        int timeMs = customThreadPool.submit(() -> {
            return resources.stream().parallel().map(d -> sleep(d)).mapToInt(i -> i).sum();
        }).get();

        LocalDateTime end = LocalDateTime.now();
        Duration duration = Duration.between(start, end);

        assertEquals(Duration.ofSeconds(threadCount), Duration.ofMillis(timeMs));
        assertTrue(duration.getSeconds() > 3);
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
