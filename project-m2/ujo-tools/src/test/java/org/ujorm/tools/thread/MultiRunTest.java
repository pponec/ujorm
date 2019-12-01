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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.ujorm.tools.thread.MultiRun.MultiRunException;
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
        System.out.println("getStream");
        Duration duration = Duration.ofSeconds(3);

        MultiRun<Integer> instance = MultiRun.params(1, 2, 3);
        Stream<Long> stream = instance.getSingle(duration, p -> p * 10L);

        List<Long> sortedList = stream.sorted().collect(Collectors.toList());
        assertEquals(3, sortedList.size());
        assertEquals(10L, sortedList.get(0).longValue());
    }

    /**
     * Test of getSingle method, of class MultiRun.
     */
    @Test
    public void testGetSingle() {
        System.out.println("getSingle");
        Duration duration = Duration.ofSeconds(3);

        MultiRun<Integer> instance = MultiRun.params(1, 2, 3);
        Stream<Long> stream = instance.getStream(duration, (Integer p) -> Arrays.asList(p * 10L, p * 100L).stream());

        List<Long> sortedList = stream.sorted().collect(Collectors.toList());
        assertEquals(6, sortedList.size());
        assertEquals(10L, sortedList.get(0).longValue());
    }

        /**
     * Test of getSingle method, of class MultiRun.
     */
    @Test
    public void testGetTimeout() {
        System.out.println("getSingle");
        Duration duration = Duration.ofMillis(100);
        Exception result;
        List<Long> sortedList = null;

        try {
           MultiRun<Integer> instance = MultiRun.params(100, 200, 400);
           Stream<Long> stream = instance.getSingle(duration, (Integer p) -> sleep(p));
           sortedList = stream.sorted().collect(Collectors.toList());
           result = null;
        } catch (Exception e) {
            result = e;
        }

        assertTrue(result != null);
        assertTrue(result.getClass().equals(MultiRunException.class));
        assertTrue(result.getCause().getClass().equals(TimeoutException.class));
        assertTrue(sortedList == null);
    }

    private Long sleep(Integer millis) {
        try {
            Thread.sleep(millis);
            return (long) millis;
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "An interrupting of the test", e);
            return -1L;
        }
    }


}
