/*
 * Copyright 2020-2020 Pavel Ponec
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
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class AsyncStreamBuilderTest {

    /**
     * Test of addParams method, of class AsyncStreamBuilder.
     */
    @Test
    public void testAddParams_1() {
        System.out.println("addParams");

        int limit = 5;
        int firstNumber = 10;
        List<Integer> result = new ArrayList<>();

        AsyncStreamBuilder<Integer> instance = new AsyncStreamBuilder<>(limit);

        instance.add(firstNumber);
        instance.stream().forEach(i -> {
            result.add(i);
            if (result.size() < limit) {
                instance.add(i + 1);
            }
        });

        assertEquals(limit, result.size());
        assertEquals(firstNumber, result.get(0).intValue());
        assertEquals(firstNumber + limit - 1, result.get(limit - 1).intValue());
    }

    /**
     * Test of addParams method, of class AsyncStreamBuilder.
     */
    @Test
    public void testAddParams_2() {
        System.out.println("addParams");

        int limit = 5;
        int firstNumber = 10;
        List<Integer> result = new ArrayList<>();

        AsyncStreamBuilder<Integer> instance = new AsyncStreamBuilder<>(limit);

        instance.add(firstNumber);
        instance.stream().forEach(i -> {
            result.add(i);
            if (result.size() < limit) {
                instance.add(i + 1);
            }
        });

        assertEquals(limit, result.size());
        assertEquals(firstNumber, result.get(0).intValue());
        assertEquals(firstNumber + limit - 1, result.get(limit - 1).intValue());
    }

    /**
     * Test of addParams method, of class AsyncStreamBuilder.
     */

    public void testAddParams_b() {
        System.out.println("addParams");

        int limit = 5;
        int firstNumber = 10;
        List<Integer> result = new ArrayList<>();

        AsyncStreamBuilder<Integer> instance = new AsyncStreamBuilder<>(limit);

        instance.add(firstNumber);
        instance.stream().forEach(i -> {
            result.add(i);
            instance.add(i + 1);
        });
    }


    /**
     * Test of addParams method, of class AsyncStreamBuilder.
     */
    @Test(expected = IllegalStateException.class)
    public void testAddParams_3() {
        System.out.println("addParams");

        int limit = 1;
        AsyncStreamBuilder<Integer> instance = new AsyncStreamBuilder<>(limit);
        instance.addAll(10, 11);
    }

    /**
     * Test of addParams method, of class AsyncStreamBuilder.
     */
    @Test
    public void testAddParams_4() {
        System.out.println("addParams");

        int limit = 3;
        AsyncStreamBuilder<Integer> instance = new AsyncStreamBuilder<>(limit);
        instance.addAll(10, null, null);

        assertEquals(1, instance.stream().count());
    }

    /**
     * Test of addParams method, of class AsyncStreamBuilder.
     */
    @Test(expected = JobException.class)
    public void testAddParams_5() {
        System.out.println("addParams");

        int limit = 3;
        AsyncStreamBuilder<Integer> instance = new AsyncStreamBuilder<>(limit, Duration.ofSeconds(1));
        // No parameter is incomming ....

        instance.stream().forEach(i -> {
            System.out.println("i :" + i);
        });
    }


}
