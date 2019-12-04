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
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.ujorm.tools.thread.MultiJob.MultiRunException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pavel Ponec
 */
public class MultiRunTest {

    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Test of getStream method, of class MultiRun.
     */
    @Test
    public void testGetStream() {
        System.out.println("run");
        Duration timeout = Duration.ofSeconds(3);

        Stream<Long> result = MultiJob.forParams(1, 2, 3).run(p -> p * 10L, timeout);

        List<Long> sortedList = result.sorted().collect(Collectors.toList());
        assertEquals(3, sortedList.size());
        assertEquals(10L, sortedList.get(0).longValue());
    }

    /**
     * Test of getSingle method, of class MultiRun.
     */
    @Test
    public void testRunToStream() {
        System.out.println("runToStream");
        Duration timeout = Duration.ofSeconds(3);

        Stream<Long> result = MultiJob.forParams(1, 2, 3).runToStream(p -> Stream.of(p * 10L, p * 100L), timeout);

        List<Long> sortedList = result.sorted().collect(Collectors.toList());
        assertEquals(6, sortedList.size());
        assertEquals(10L, sortedList.get(0).longValue());
    }

    /**
     * Test of getSingle method, of class MultiRun.
     */
    @Test
    public void testGetTimeout() {
        System.out.println("getSingle");
        Duration timeout = Duration.ofMillis(100);
        MultiRunException result = null;
        Stream<Long> stream = null;

        try {
            stream = MultiJob.forParams(100, 200, 400).run(p -> sleep(p), timeout);
        } catch (MultiRunException e) {
            result = e;
        }

        assertTrue(stream == null);
        assertTrue(result != null);
        assertTrue(result.getCause() instanceof TimeoutException);
    }

    private long sleep(int millis) {
        try {
            Thread.sleep(millis);
            return millis;
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "An interrupting of the test", e);
            return -1L;
        }
    }

}
