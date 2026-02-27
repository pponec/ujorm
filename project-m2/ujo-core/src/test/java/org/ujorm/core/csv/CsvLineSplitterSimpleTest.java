package org.ujorm.core.csv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvLineSplitterSimpleTest {

    /** Initial capacity is 8, but the string contains 4 elements */
    @Test
    void testStandardSplitNoResize() {
        var splitter = CsvLineSplitter.ofSimple('.');
        var result = splitter.split("one.two.three.four", 8);

        assertArrayEquals(new String[]{"one", "two", "three", "four"}, result);
    }


    /** Initial capacity is 2, but the string contains 4 elements */
    @Test
    void testStandardSplitAndResize() {
        var splitter = CsvLineSplitter.ofSimple('.');
        var result = splitter.split("one.two.three.four", 2);
        assertArrayEquals(new String[]{"one", "two", "three", "four"}, result);
    }

    @Test
    void testEmptyAndNull() {
        var splitter = CsvLineSplitter.ofSimple('.');

        var emptyResult = splitter.split("", 5);
        assertEquals(0, emptyResult.length);

        var nullResult = splitter.split(null, 5);
        assertEquals(0, nullResult.length);
    }

    @Test
    void testNoDelimiter() {
        var splitter = CsvLineSplitter.ofSimple('.');
        var result = splitter.split("single_value", 3);

        assertArrayEquals(new String[]{"single_value"}, result);
    }

    @Test
    void testConsecutiveDelimiters() {
        var splitter = CsvLineSplitter.ofSimple('.');
        var result = splitter.split("a..c", 5);

        assertArrayEquals(new String[]{"a", "", "c"}, result);
    }

    @Test
    void testTrailingDelimiter() {
        var splitter = CsvLineSplitter.ofSimple('.');
        var result = splitter.split("a.b.", 5);

        assertArrayEquals(new String[]{"a", "b", ""}, result);
    }

    @Test
    void testZeroInitialCapacity() {
        var splitter = CsvLineSplitter.ofSimple('.');
        var result = splitter.split("a.b", 0);

        assertArrayEquals(new String[]{"a", "b"}, result);
    }
}