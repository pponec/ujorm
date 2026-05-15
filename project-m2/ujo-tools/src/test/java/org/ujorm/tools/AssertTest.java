package org.ujorm.tools;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the method Assert
 * @author Pavel Ponec
 */
class AssertTest {

    private static final Supplier<String> TEST_MESSAGE = () -> "MESSAGE:%s%s%s".formatted("A", "B", "C");

    /** Demo test of the Assert class. All the claims are true. */
    @Test
    public void testDemo() {
        Assert.isTrue(true, () -> "TEST:%s%s".formatted("A", "B"));
        Assert.isTrue(10, (x) -> x < 20, () -> "Wrong No");
        Assert.notNull("ABC", (String) null);
        Assert.hasLength("ABC", null);

        assertTrue(Assert.isPresented("A", TEST_MESSAGE).isPresent());
        assertFalse(Assert.isPresented(null, TEST_MESSAGE).isPresent());
    }

    /** Test of message building. All the claims are false. */
    @Test
    public void testDemoMessage() {
        var value = 20;

        var e1 = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrue(value, (x) -> x < 10, () -> "Wrong number %s".formatted(value)));
        assertEquals("Wrong number 20", e1.getMessage());

        var e2 = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrue(value, (x) -> x < 10, () -> "Wrong".formatted(value)));
        assertEquals("Wrong", e2.getMessage());

        var e3 = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrue(value, (x) -> x < 10, () -> String.valueOf(value)));
        assertEquals("20", e3.getMessage());
    }

    /** Test of a failed message generation (e.g. invalid format placeholders) */
    @Test
    public void testInvalidMessageFormat() {
        var e = assertThrows(IllegalArgumentException.class, () ->
                Assert.isTrue(false, () -> "Error %s %s".formatted("only one")));

        assertNotNull(e.getMessage());
        assertTrue(e.getMessage().startsWith("Message evaluation failed:"));
    }

    /** Test of required method and its clean stacktrace (no cause). */
    @Test
    public void testRequired_CleanStacktrace() {
        var e = assertThrows(IllegalArgumentException.class, () -> Assert.notNull(null, TEST_MESSAGE));
        assertEquals("MESSAGE:ABC", e.getMessage());
        assertNull(e.getCause());
    }

    /** Test of requiredState method and its clean stacktrace (no cause). */
    @Test
    public void testRequiredState_CleanStacktrace() {
        var e = assertThrows(IllegalStateException.class, () -> Assert.notNullState(null, TEST_MESSAGE));
        assertEquals("MESSAGE:ABC", e.getMessage());
        assertNull(e.getCause());
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
    public void testHasLength_Collection_ok() {
        var values = Arrays.asList("A", "B", "C");
        Assert.hasLength(values, TEST_MESSAGE);
        assertTrue(true);
    }

    @Test
    public void testIsNull_Object_ok() {
        Assert.isNull(null, TEST_MESSAGE);
        assertTrue(true);
    }

    @Test
    public void testIsEmpty_CharSequence_ok() {
        Assert.isEmpty((CharSequence) null, null);
        Assert.isEmpty("", null);
        assertTrue(true);
    }

    @Test
    public void testHasLength_CharSequence_nok() {
        assertThrows(IllegalArgumentException.class, () -> Assert.hasLength((CharSequence) null, TEST_MESSAGE));
        assertThrows(IllegalArgumentException.class, () -> Assert.hasLength("", TEST_MESSAGE));
    }
}