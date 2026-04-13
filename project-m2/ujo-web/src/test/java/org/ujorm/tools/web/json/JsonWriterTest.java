package org.ujorm.tools.web.json;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the JsonWriter class
 */
class JsonWriterTest {

    @Test
    /** Test of appending standard characters without escaping */
    void testAppendStandardChars() throws IOException {
        var out = new StringBuilder();
        var writer = new JsonWriter(out);

        writer.append('a');
        writer.append("bcd");

        assertEquals("abcd", out.toString());
    }

    @Test
    /** Test of appending special JSON characters that require a backslash */
    void testAppendSpecialChars() throws IOException {
        var out = new StringBuilder();
        var writer = new JsonWriter(out);

        writer.append('"');
        writer.append('\\');

        assertEquals("\\\"\\\\", out.toString());
    }

    @Test
    /** Test of appending common control characters */
    void testAppendControlChars() throws IOException {
        var out = new StringBuilder();
        var writer = new JsonWriter(out);

        writer.append('\b');
        writer.append('\f');
        writer.append('\n');
        writer.append('\r');
        writer.append('\t');

        assertEquals("\\b\\f\\n\\r\\t", out.toString());
    }

    @Test
    /** Test of appending characters that must be escaped using Unicode sequences */
    void testAppendUnicodeEscaping() throws IOException {
        var out = new StringBuilder();
        var writer = new JsonWriter(out);

        writer.append((char) 0x01);
        writer.append('\u2028');
        writer.append('\u2029');

        assertEquals("\\u0001\\u2028\\u2029", out.toString());
    }

    @Test
    /** Test of appending a sub-sequence of a string */
    void testAppendSubSequence() throws IOException {
        var out = new StringBuilder();
        var writer = new JsonWriter(out);

        writer.append("---abc---", 3, 6);

        assertEquals("abc", out.toString());
    }

    @Test
    /** Test for accessing the original writer */
    void testOriginalWriter() {
        var out = new StringBuilder();
        var writer = new JsonWriter(out);

        assertSame(out, writer.original());
    }
}