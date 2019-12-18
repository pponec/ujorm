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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pavel Ponec
 */
public class MultiJobThreadPoolTest {

    private final Logger logger = Logger.getLogger(getClass().getName());

    /**
     * Check Time of parallel work.
     */
    @Test
    public void testTimeOfParalellWork() {
        System.out.println("timeOfParalellWork");

        Duration jobDuration = Duration.ofSeconds(1);
        int jobCount = 100;
        List<Duration> params = Collections.nCopies(jobCount, jobDuration);
        LocalDateTime start = LocalDateTime.now();

        int sum = MultiJob.forEach(params, jobCount)
                .run(duration -> runManyTheads(duration)) // 1 sec
                .mapToInt(i -> i)
                .sum();


        Duration duration = Duration.between(start, LocalDateTime.now());
        assertTrue("Real time took millis: " + duration.toMillis(), duration.toMillis() > jobDuration.toMillis());
        assertTrue("Real time took millis: " + duration.toMillis(), duration.toMillis() < jobDuration.toMillis() + 200);
        assertEquals(jobCount * 10, sum);
    }


    private Integer runManyTheads(final Duration jobDuration) {
        int jobCount = 10;
        List<Duration> params = Collections.nCopies(jobCount, jobDuration);
        int sum = MultiJob.forEach(params, jobCount)
                .run(d -> sleep(d)) // 1 sec
                .mapToInt(i -> i)
                .sum();
        return sum;
    }

    private Integer sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
            return 1;
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "An interrupting of the test", e);
            return null;
        }
    }
}
