package org.ujorm.core.csv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvLineSplitterTest {

    @Test
    void ofQuoted_4() {
        var splitter = CsvLineSplitter.ofQuoted('|');
        var line = """
                4|false|28|4|52000|Lee|Jones(""2"")|0.0|M|"Michael"|"Lee|Jones(""2"")"|1100.10|2026-01-05T09:00:00
                """.trim();
        var count = 12;
        var cells = splitter.split(line, count);
        assertEquals(count, cells.length);
        assertEquals("4", cells[0]);
        assertEquals("Michael", cells[9]);
        assertEquals("Lee|Jones(\"2\")", cells[10]);
    }

    @Test
    void ofQuoted_7() {
        var splitter = CsvLineSplitter.ofQuoted('|');
        var line = "7";
        var count = 12;
        var cells = splitter.split(line, count);
        assertEquals(1, cells.length);
        assertEquals("7", cells[0]);
    }

    @Test
    void ofQuoted_8() {
        var splitter = CsvLineSplitter.ofQuoted('|');
        var line = """
                8||||||||||||||||||||||||||||||
                """.trim();
        var count = 12;
        var cells = splitter.split(line, count);
        assertEquals(count, cells.length);
        assertEquals("8", cells[0]);
        assertEquals("", cells[9]);
        assertEquals("", cells[10]);
    }
}