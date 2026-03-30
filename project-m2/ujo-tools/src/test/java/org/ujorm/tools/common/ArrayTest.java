package org.ujorm.tools.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.stream.Collectors;

/**
 * Tests for the {@link Array} class.
 * @author Pavel Ponec
 */
class ArrayTest {

    /** Sample data */
    private final Array<Character> array = createArray();
    /** Empty sample data */
    private final Array<Character> empty = Array.of();
    /** Undefined value constant */
    private final Character undef = 'X';

    /** Test of the copy method */
    @Test
    void testCopy() {
        var clone = array.copy();
        Assertions.assertNotSame(array, clone);
        Assertions.assertEquals(array.getItem(0), clone.getItem(0));
        Assertions.assertEquals(array.getItem(4), clone.getItem(4));
        Assertions.assertArrayEquals(array.stream().toArray(), clone.stream().toArray());
    }

    /** Test of the get method with various indices */
    @Test
    void getItem() {
        Assertions.assertEquals('A', array.get(0).orElse(undef));
        Assertions.assertEquals('B', array.get(1).orElse(undef));
        Assertions.assertEquals('E', array.get(4).orElse(undef));
        Assertions.assertEquals('X', array.get(5).orElse(undef));
        Assertions.assertEquals('E', array.get(-1).orElse(undef));
        Assertions.assertEquals('D', array.get(-2).orElse(undef));
        Assertions.assertEquals(undef, empty.get(0).orElse(undef));
        Assertions.assertEquals(undef, array.get(9).orElse(undef));
    }

    /** Test of the getFirst method */
    @Test
    void getFirst() {
        Assertions.assertEquals('A', array.getFirst().orElse(undef));
        Assertions.assertEquals(undef, empty.getFirst().orElse(undef));
    }

    /** Test of the getLast method */
    @Test
    void getLast() {
        Assertions.assertEquals('E', array.getLast().orElse(undef));
        Assertions.assertEquals(undef, empty.getLast().orElse(undef));
    }

    /** Test of the removeFirst method */
    @Test
    void removeFirst() {
        var trim = array.removeFirst();
        Assertions.assertEquals(5, array.size());
        Assertions.assertEquals(4, trim.size());
        Assertions.assertEquals('B', trim.getItem(0));

        trim = empty.removeFirst();
        Assertions.assertEquals(0, trim.size());
    }

    /** Test of the subArray method */
    @Test
    void subArray() {
        var trim = array.subArray(3);
        Assertions.assertEquals(2, trim.size());
        Assertions.assertEquals('D', trim.getItem(0));
    }

    /** Test of the add method */
    @Test
    void add() {
        var extended = array.add('P', 'C');
        Assertions.assertEquals(array.size() + 2, extended.size());
        Assertions.assertEquals('P', extended.getItem(5));
        Assertions.assertEquals('C', extended.getItem(6));
    }

    /** Test of conversion to List */
    @Test
    void toList() {
        var list = array.toList();

        Assertions.assertEquals(array.getItem(0), list.get(0));
        Assertions.assertEquals(array.getItem(1), list.get(1));
        Assertions.assertEquals(array.getItem(4), list.get(4));
        Assertions.assertEquals(array.size(), list.size());
    }

    /** Test of the isEmpty method */
    @Test
    void isEmpty() {
        Assertions.assertFalse(array.isEmpty());
        Assertions.assertTrue(empty.isEmpty());
    }

    /** Test of the size method */
    @Test
    void size() {
        Assertions.assertEquals(5, array.size());
        Assertions.assertEquals(0, empty.size());
    }

    /** Test of the stream method */
    @Test
    void stream() {
        var list = array.stream().collect(Collectors.toList());

        Assertions.assertEquals(array.getItem(0), list.get(0));
        Assertions.assertEquals(array.getItem(1), list.get(1));
        Assertions.assertEquals(array.getItem(4), list.get(4));
        Assertions.assertEquals(array.size(), list.size());
    }

    /** Test of the hashCode method */
    @Test
    void testHashCode() {
        var other = createArray();
        Assertions.assertEquals(array.hashCode(), other.hashCode());
        Assertions.assertNotEquals(array.hashCode(), empty.hashCode());
    }

    /** Test of the equals method */
    @Test
    void testEquals() {
        var other = createArray();
        Assertions.assertEquals(array, other);
        Assertions.assertNotEquals(array, empty);
    }

    /** Factory method for a testing array */
    Array<Character> createArray() {
        return Array.of('A', 'B', 'C', 'D', 'E');
    }
}