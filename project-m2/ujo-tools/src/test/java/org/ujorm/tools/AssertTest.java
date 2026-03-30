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
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the method Assert
 * @author Pavel Ponec
 */
class AssertTest {

    /** Demo message {@code MESSAGE:ABC} */
    private static final Object[] TEST_MESSAGE = {"MESSAGE:{}{}{}", "A", "B", "C"};

    private static final Object[] NO_MESSAGE = {};

    /** Demo test of the Assert class. All the claims are true. */
    @Test
    public void testDemo() {
        Assert.isTrue(true, "TEST:{}{}", "A", "B");
        Assert.isTrue(10, (x) -> x < 20, "Wrong No");
        Assert.required("ABC");
        Assert.hasLength("ABC");
        Assert.hasLength(new char[]{'A', 'B', 'C'});
        Assert.hasLength(new StringBuilder().append("ABC"));
        Assert.hasLength(Arrays.asList("A", "B", "C"));

        Assert.isFalse(false);
        Assert.isFalse(30, (x) -> x < 20);
        Assert.isNull (null);
        Assert.isEmpty("");
        Assert.isEmpty(new char[0]);
        Assert.isEmpty(new StringBuilder());
        Assert.isEmpty((List<?>) null);

        assertTrue(Assert.isPresented("A").isPresent());
        assertFalse(Assert.isPresented(null).isPresent());
    }

    /** Test of message building. All the claims are false. */
    @Test
    public void testDemoMessage() {
        var value = 20;

        var e1 = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrue(value, (x) -> x < 10, "Wrong number {}", value));
        assertEquals("Wrong number 20", e1.getMessage());

        var e2 = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrue(value, (x) -> x < 10, "Wrong", value));
        assertEquals("Wrong 20", e2.getMessage());

        var e3 = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrue(value, (x) -> x < 10, value));
        assertEquals("20", e3.getMessage());

        var e4 = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrue(value, (x) -> x < 10));
        assertNull(e4.getMessage());

        Integer nullValue = null;
        var e5 = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrueRequired(nullValue, (x) -> x < 10, "Wrong number {}", nullValue));
        assertEquals("Wrong number null", e5.getMessage());
    }

    /** Test of required method and its clean stacktrace (no cause). */
    @Test
    public void testRequired_CleanStacktrace() {
        var e = assertThrows(IllegalArgumentException.class, () -> Assert.required(null, TEST_MESSAGE));
        assertEquals("MESSAGE:ABC", e.getMessage());
        assertNull(e.getCause()); // Verification: No nested NullPointerException
    }

    /** Test of requiredState method and its clean stacktrace (no cause). */
    @Test
    public void testRequiredState_CleanStacktrace() {
        var e = assertThrows(IllegalStateException.class, () -> Assert.requiredState(null, TEST_MESSAGE));
        assertEquals("MESSAGE:ABC", e.getMessage());
        assertNull(e.getCause()); // Verification: No nested NullPointerException
    }

    /** Test of requiredValue with a failing supplier. */
    @Test
    public void testRequiredValue_WithCause() {
        var e = assertThrows(IllegalArgumentException.class, () -> Assert.requiredValue(() -> {
            throw new RuntimeException("Inner error");
        }, TEST_MESSAGE));
        assertEquals("MESSAGE:ABC", e.getMessage());
        assertNotNull(e.getCause());
        assertEquals("Inner error", e.getCause().getMessage());
    }

    /** Test of isPresented method. */
    @Test
    public void testIsPresented() {
        var val = "A";
        var result = Assert.isPresented(val, TEST_MESSAGE);
        assertTrue(result.isPresent());
        assertEquals(val, result.get());

        var emptyResult = Assert.isPresented(null, TEST_MESSAGE);
        assertFalse(emptyResult.isPresent());
    }

    @Test
    public void testIsTrue_boolean_ok() {
        Assert.isTrue(true);
    }

    @Test
    public void testIsTrue_Predicate_ok() {
        Assert.isTrue(10, (x) -> x < 20, TEST_MESSAGE);
    }

    @Test
    public void testHasLength_byteArr_ok() {
        var array = new byte[1];
        Assert.hasLength(array, NO_MESSAGE);
    }

    @Test
    public void testHasLength_Collection_ok() {
        var values = Arrays.asList("A", "B", "C");
        Assert.hasLength(values, TEST_MESSAGE);
    }

    @Test
    public void testIsNull_Object_ok() {
        Assert.isNull(null, TEST_MESSAGE);
    }

    @Test
    public void testIsEmpty_CharSequence_ok() {
        Assert.isEmpty((CharSequence) null, NO_MESSAGE);
        Assert.isEmpty("", NO_MESSAGE);
    }

    // ------------- EXCEPTION TESTS -------------

    @Test
    public void testIsTrue_boolean_nok() {
        assertThrows(IllegalArgumentException.class, () -> Assert.isTrue(false));
    }

    @Test
    public void testHasLength_CharSequence_nok() {
        assertThrows(IllegalArgumentException.class, () -> Assert.hasLength((CharSequence) null, TEST_MESSAGE));
        assertThrows(IllegalArgumentException.class, () -> Assert.hasLength("", TEST_MESSAGE));
    }

    @Test
    public void testIsFalse_boolean_nok() {
        assertThrows(IllegalArgumentException.class, () -> Assert.isFalse(true, TEST_MESSAGE));
    }

    @Test
    public void testIsEmpty_Collection_nok() {
        var values = Arrays.asList("A", "B");
        assertThrows(IllegalArgumentException.class, () -> Assert.isEmpty(values, TEST_MESSAGE));
    }
}