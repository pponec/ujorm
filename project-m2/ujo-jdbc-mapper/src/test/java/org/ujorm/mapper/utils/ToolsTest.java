package org.ujorm.mapper.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolsTest {

    @Test
    void testSplitWithSmallerArrayReturnsOriginalInstance() {
        String[] input = {"A", "B"};
        var batchSize = 5;

        var result = Tools.splitIntoBatches(batchSize, input);

        assertEquals(1, result.length);
        // We verify that it is the exact same array instance in memory
        assertSame(input, result[0], "The original array instance should be returned unchanged.");
    }

    @Test
    void testSplitWithExactMultiple() {
        Integer[] input = {1, 2, 3, 4};
        var batchSize = 2;

        var result = Tools.splitIntoBatches(batchSize, input);

        assertEquals(2, result.length);
        // Explicit casting is required because the outer array is Object[][]
        assertArrayEquals(new Integer[]{1, 2}, (Integer[]) result[0]);
        assertArrayEquals(new Integer[]{3, 4}, (Integer[]) result[1]);
    }

    @Test
    void testSplitWithRemainderBatch() {
        Integer[] input = {1, 2, 3, 4, 5};
        var batchSize = 2;

        var result = Tools.splitIntoBatches(batchSize, input);

        assertEquals(3, result.length);
        assertArrayEquals(new Integer[]{1, 2}, (Integer[]) result[0]);
        assertArrayEquals(new Integer[]{3, 4}, (Integer[]) result[1]);
        assertArrayEquals(new Integer[]{5}, (Integer[]) result[2]);
    }
}