package org.ujorm.core.criterion;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for ValueFormatter
 */
public class ValueFormatterTest {

    /** Test formatting of a null value */
    @Test
    public void testFormatNull() {
        var result = ValueFormatter.format("{}", (Object) null);
        assertEquals("null", result);
    }

    /** Test formatting using a Supplier */
    @Test
    public void testFormatSupplier() {
        Supplier<String> supplier = () -> "Delayed Value";
        var result = ValueFormatter.format("{}", supplier);
        assertEquals("Delayed Value", result);
    }

    /** Test formatting of a CharSequence (String) */
    @Test
    public void testFormatCharSequence() {
        var result = ValueFormatter.format("{}", "Simple Text");
        assertEquals("Simple Text", result);
    }

    /** Test formatting of Numbers */
    @Test
    public void testFormatNumber() {
        var resultInt = ValueFormatter.format("{}", 42);
        assertEquals("42", resultInt);

        var resultDouble = ValueFormatter.format("{}", 3.14);
        assertEquals("3.14", resultDouble);
    }

    /** Test formatting of java.util.Date */
    @Test
    public void testFormatUtilDate() {
        var date = new Date(0); // 1970-01-01
        var expectedFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).format(date);
        var result = ValueFormatter.format("{}", date);
        assertEquals(expectedFormat, result);
    }

    /** Test formatting of java.sql.Date */
    @Test
    public void testFormatSqlDate() {
        var date = java.sql.Date.valueOf("2026-04-06");
        var result = ValueFormatter.format("{}", date);
        assertEquals("2026-04-06", result);
    }

    /** Test formatting of byte array to hexadecimal string */
    @Test
    public void testFormatByteArray() {
        var bytes = new byte[] { 10, 15, 0, (byte) 255 }; // 0A, 0F, 00, FF
        var result = ValueFormatter.format("{}", bytes);
        assertEquals("0A0F00FF", result);
    }

    /** Test formatting of a Character */
    @Test
    public void testFormatCharacter() {
        var result = ValueFormatter.format("{}", 'X');
        assertEquals("X", result);
    }

    /** Test formatting of an Enum */
    @Test
    public void testFormatEnum() {
        var result = ValueFormatter.format("{}", TestEnum.READY);
        assertEquals("READY", result);
    }

    /** Test formatting of a Throwable */
    @Test
    public void testFormatThrowable() {
        var exception = new IllegalArgumentException("Invalid argument passed");
        var result = ValueFormatter.format("{}", exception);
        assertEquals("IllegalArgumentException:Invalid argument passed", result);
    }

    /** Test formatting of a List */
    @Test
    public void testFormatList() {
        var list = List.of(1, 2, 3);
        var result = ValueFormatter.format("{}", list);
        assertEquals("[1, 2, 3]", result);
    }

    /** Test formatting of an unexpected Object fallback */
    @Test
    public void testFormatFallbackObject() {
        var customObj = new Object() {
            @Override
            public String toString() {
                return "CustomObject";
            }
        };
        var result = ValueFormatter.format("{}", customObj);
        assertEquals("CustomObject", result);
    }

    /** Test formatting of a List with custom text borders (SQL format) */
    @Test
    public void testFormatSqlCustomBorders() {
        var result = ValueFormatter.formatSql("?", "Text");
        assertEquals("'Text'", result);

        var date = java.sql.Date.valueOf("2026-04-06");
        var resultDate = ValueFormatter.formatSql("?", date);
        assertEquals("'2026-04-06'", resultDate);
    }


    /** Test enum for formatting purposes */
    enum TestEnum {
        READY, SET, GO
    }

}