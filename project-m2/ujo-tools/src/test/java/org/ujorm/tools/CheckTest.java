/*
 * Copyright 2017-2026 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of the Check class
 * @author Pavel Ponec
 */
class CheckTest {

    /** Test of basic methods and demonstration. */
    @Test
    public void testDemo() {
        assertTrue(Check.hasLength("ABC"));
        assertTrue(Check.hasLength(new char[]{'A', 'B', 'C'}));
        assertTrue(Check.hasLength(new StringBuilder().append("ABC")));
        assertTrue(Check.hasLength(Arrays.asList("A", "B", "C")));

        assertTrue(Check.isEmpty(""));
        assertTrue(Check.isEmpty(new char[0]));
        assertTrue(Check.isEmpty(new StringBuilder()));
        assertTrue(Check.isEmpty((List<?>) null));
    }

    /** Test of hasLength method for byte array. */
    @Test
    public void testHasLength_byteArr() {
        assertFalse(Check.hasLength((byte[]) null));
        assertFalse(Check.hasLength(new byte[0]));
        assertTrue(Check.hasLength(new byte[1]));
    }

    /** Test of hasLength method for char array. */
    @Test
    public void testHasLength_charArr() {
        assertFalse(Check.hasLength((char[]) null));
        assertFalse(Check.hasLength(new char[0]));
        assertTrue(Check.hasLength(new char[1]));
    }

    /** Test of hasLength method for Object array. */
    @Test
    public void testHasLength_ObjectArr() {
        assertFalse(Check.hasLength((Object[]) null));
        assertFalse(Check.hasLength());
        assertTrue(Check.hasLength(new Object[1]));

        var emptyArray = new String[]{};
        assertFalse(Check.hasLength(emptyArray));

        var filledArray = new String[]{"A", "B"};
        assertTrue(Check.hasLength(filledArray));
    }

    /** Test of hasLength method for Collection. */
    @Test
    public void testHasLength_Collection() {
        assertFalse(Check.hasLength((Collection<?>) null));
        assertFalse(Check.hasLength(Collections.emptyList()));
        assertTrue(Check.hasLength(Arrays.asList("A", "B", "C")));
    }

    /** Test of hasLength method for CharSequence. */
    @Test
    public void testHasLength_CharSequence() {
        assertFalse(Check.hasLength((CharSequence) null));
        assertFalse(Check.hasLength(""));
        assertTrue(Check.hasLength("ABC"));
    }

    /** Test of isEmpty method for byte array. */
    @Test
    public void testIsEmpty_byteArr() {
        assertTrue(Check.isEmpty((byte[]) null));
        assertTrue(Check.isEmpty(new byte[0]));
        assertFalse(Check.isEmpty(new byte[1]));
    }

    /** Test of isEmpty method for char array. */
    @Test
    public void testIsEmpty_charArr() {
        assertTrue(Check.isEmpty((char[]) null));
        assertTrue(Check.isEmpty(new char[0]));
        assertFalse(Check.isEmpty(new char[1]));
    }

    /** Test of isEmpty method for Object array. */
    @Test
    public void testIsEmpty_ObjectArr() {
        assertTrue(Check.isEmpty((Object[]) null));
        assertTrue(Check.isEmpty());
        assertFalse(Check.isEmpty(new Object[1]));
    }

    /** Test of isEmpty method for Collection. */
    @Test
    public void testIsEmpty_Collection() {
        assertTrue(Check.isEmpty((Collection<?>) null));
        assertTrue(Check.isEmpty(Collections.emptyList()));
        assertFalse(Check.isEmpty(Arrays.asList("A", "B", "C")));
    }

    /** Test of isEmpty method for CharSequence. */
    @Test
    public void testIsEmpty_CharSequence() {
        assertTrue(Check.isEmpty((CharSequence) null));
        assertTrue(Check.isEmpty(""));
        assertFalse(Check.isEmpty("ABC"));
    }

    /** Test of Map methods. */
    @Test
    public void testMap() {
        assertTrue(Check.isEmpty((Map<?, ?>) null));
        assertTrue(Check.isEmpty(Collections.emptyMap()));
        assertFalse(Check.isEmpty(Collections.singletonMap("K", "V")));

        assertFalse(Check.hasLength((Map<?, ?>) null));
        assertFalse(Check.hasLength(Collections.emptyMap()));
        assertTrue(Check.hasLength(Collections.singletonMap("K", "V")));
    }

    /** Test of firstItem method. */
    @Test
    public void testFirstItem() {
        assertTrue(Check.firstItem("A", "A", "B", "C"));
        assertFalse(Check.firstItem("B", "A", "B", "C"));
        assertFalse(Check.firstItem("A", (String[]) null));
        assertFalse(Check.firstItem("A")); // empty varargs
        assertTrue(Check.firstItem(null, null, "B")); // first item is null, value is null
    }

}