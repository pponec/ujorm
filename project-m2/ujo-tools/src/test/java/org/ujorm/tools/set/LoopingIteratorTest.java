/*
 * Copyright 2019 Pavel Ponec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.set;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pavel Ponec
 */
public class LoopingIteratorTest {

    /**
     * Test of toStream method, of class LoopingIterator.
     */
    @Test
    public void testCloseStream() {
        System.out.println("close");
        LoopingIteratorImpl instance = new LoopingIteratorImpl();
        assertFalse(instance.isClosed());

        Stream<Integer> intStream = instance.toStream();
        intStream.close();
        assertTrue(instance.isClosed());
    }

    /**
     * Test of toStream method, of class LoopingIterator.
     */
    @Test
    public void testAutoCloseStream() {
        System.out.println("close");
        LoopingIteratorImpl instance = new LoopingIteratorImpl();
        assertFalse(instance.isClosed());

        try (Stream<Integer> intStream = instance.toStream()) {
            assertEquals((Integer) 0, intStream.findFirst().orElse(-1));
        }
        assertTrue(instance.isClosed());
    }

    /**
     * Test of toStream method, of class LoopingIterator.
     */
    @Test
    public void testSizeStream() {
        System.out.println("count");
        LoopingIteratorImpl instance = new LoopingIteratorImpl();

        Stream<Integer> intStream = instance.toStream();
        assertEquals(3, intStream.count());
    }

    public class LoopingIteratorImpl implements LoopingIterator<Integer> {

        final int max = 3;
        int value = 0;
        boolean closed = false;

        @Override
        public boolean hasNext() {
            return value < 3;
        }

        @Override
        public Integer next() {
            return value++;
        }

        @Override
        public Iterator iterator() {
            return this;
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }

}
