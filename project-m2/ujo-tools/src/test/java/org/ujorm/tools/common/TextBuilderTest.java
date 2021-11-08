/*
 * Copyright 2021-2021 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.common;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pavel
 */
public class TextBuilderTest {

    @Test
    public void sample() {
        String result = new TextBuilder()
            .line("A", "B", "C").add("D")
            .line("X", "Y").add("Z")
            .toString();
        String expected = "ABCD\nXYZ";
        assertEquals(expected, result);
    }

    @Test
    public void testEmptyLine() {
        String result = new TextBuilder()
                .line("A", "B", "C").add("D")
                .emptyLine()
                .line("X", "Y\n")
                .line("ZZ")
                .toString();
        String expected = "ABCD\n\nXY\nZZ";
        assertEquals(expected, result);
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new TextBuilder().isEmpty());
    }

}
