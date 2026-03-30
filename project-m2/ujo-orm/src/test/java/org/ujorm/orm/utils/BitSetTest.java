package org.ujorm.orm.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BitSetTest {

    /** Tests basic setting and getting of values within the first 64 bits. */
    @Test
    public void testSetAndGetValueSingleWord() {
        var bitSet = BitSet.of(64);
        assertTrue(bitSet.isEmpty());
        bitSet.setValue(0, true);
        bitSet.setValue(32, true);
        bitSet.setValue(63, true);

        assertFalse(bitSet.isEmpty());
        assertTrue(bitSet.getValue(0));
        assertTrue(bitSet.getValue(32));
        assertTrue(bitSet.getValue(63));
        assertFalse(bitSet.getValue(1));

        bitSet.setValue(32, false);
        assertFalse(bitSet.getValue(32));
    }

    /** Tests boundaries crossing multiple 64-bit words. */
    @Test
    public void testSetAndGetValueMultipleWords() {
        var bitSet = BitSet.of(200);
        bitSet.setValue(63, true);  // End of word 0
        bitSet.setValue(64, true);  // Start of word 1
        bitSet.setValue(127, true); // End of word 1
        bitSet.setValue(128, true); // Start of word 2

        assertTrue(bitSet.getValue(63));
        assertTrue(bitSet.getValue(64));
        assertTrue(bitSet.getValue(127));
        assertTrue(bitSet.getValue(128));
        assertFalse(bitSet.getValue(65));
    }

    /** Tests extraction of active indexes using the getActive method. */
    @Test
    public void testGetActive() {
        var bitSet = BitSet.of(150);
        bitSet.setValue(2, true);
        bitSet.setValue(65, true);
        bitSet.setValue(130, true);

        var expected = new int[]{2, 65, 130};
        var result = bitSet.getActive();

        assertArrayEquals(expected, result);
    }

    /** Tests empty active indexes extraction. */
    @Test
    public void testGetActiveEmpty() {
        var bitSet = BitSet.of(50);
        var expected = new int[0];
        var result = bitSet.getActive();

        assertArrayEquals(expected, result);
        assertTrue(bitSet.isEmpty());
    }

    /** Tests equality and hash code contracts for use in HashMaps. */
    @Test
    public void testEqualsAndHashCode() {
        var bitSet1 = BitSet.of(100);
        var bitSet2 = BitSet.of(100);
        var bitSet3 = BitSet.of(100);

        bitSet1.setValue(50, true);
        bitSet1.setValue(99, true);

        bitSet2.setValue(50, true);
        bitSet2.setValue(99, true);

        bitSet3.setValue(50, true);

        assertEquals(bitSet1, bitSet2);
        assertNotEquals(bitSet1, bitSet3);
        assertEquals(bitSet1.hashCode(), bitSet2.hashCode());
    }

    /** Tests equality and hash code contracts for use in HashMaps. */
    @Test
    void testToString() {
        var bitSet = BitSet.of(10);

        bitSet.setValue(2, true);
        bitSet.setValue(4, true);
        assertEquals("2,4", bitSet.toString());

        bitSet.setValue(2, true);
        bitSet.setValue(4, false);
        bitSet.setValue(9, true);
        assertEquals("2,9", bitSet.toString());
    }
}