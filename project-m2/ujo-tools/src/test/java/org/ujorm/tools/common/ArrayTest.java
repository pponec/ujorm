package org.ujorm.tools.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

class ArrayTest {

    private final Array<Character> array = createArray();
    private final Array<Character> empty = Array.of();

    private final Character undef = 'X';

    @Test
    void testClone() {
        Array<Character> clone = array.clone();
        Assertions.assertNotSame(array, clone);
        Assertions.assertEquals(array.getItem(0), clone.getItem(0));
        Assertions.assertEquals(array.getItem(4), clone.getItem(4));
        Assertions.assertArrayEquals(array.stream().toArray(), clone.stream().toArray());
    }

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

    @Test
    void getFirst() {
        Assertions.assertEquals('A', array.getFirst().orElse(undef));
        Assertions.assertEquals(undef, empty.getFirst().orElse(undef));
    }

    @Test
    void getLast() {
        Assertions.assertEquals('E', array.getLast().orElse(undef));
        Assertions.assertEquals(undef, empty.getLast().orElse(undef));
    }

    @Test
    void removeFirst() {
        Array<Character> trim = array.removeFirst();
        Assertions.assertEquals(5, array.size());
        Assertions.assertEquals(4, trim.size());
        Assertions.assertEquals('B', trim.getItem(0));

        trim = empty.removeFirst();
        Assertions.assertEquals(0, trim.size());
    }

    @Test
    void subArray() {
        Array<Character> trim = array.subArray(3);
        Assertions.assertEquals(2, trim.size());
        Assertions.assertEquals('D', trim.getItem(0));
    }

    @Test
    void join() {
        Array<Character> extended = array.join('P', 'C');
        Assertions.assertEquals(array.size() + 2, extended.size());
        Assertions.assertEquals('P', extended.getItem(5));
        Assertions.assertEquals('C', extended.getItem(6));
    }

    @Test
    void toList() {
        List<Character> list = array.toList();

        Assertions.assertEquals(array.getItem(0), list.get(0));
        Assertions.assertEquals(array.getItem(1), list.get(1));
        Assertions.assertEquals(array.getItem(4), list.get(4));
        Assertions.assertEquals(array.size(), list.size());
    }

    @Test
    void isEmpty() {
        Assertions.assertFalse(array.isEmpty());
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    void size() {
        Assertions.assertEquals(5, array.size());
        Assertions.assertEquals(0, empty.size());
    }

    @Test
    void stream() {
        List<Character> list = array.stream().collect(Collectors.toList());

        Assertions.assertEquals(array.getItem(0), list.get(0));
        Assertions.assertEquals(array.getItem(1), list.get(1));
        Assertions.assertEquals(array.getItem(4), list.get(4));
        Assertions.assertEquals(array.size(), list.size());
    }

    @Test
    void testHashCode() {
        Array<Character> other = createArray();
        Assertions.assertEquals(array.hashCode(), other.hashCode());
        Assertions.assertNotEquals(array.hashCode(), empty.hashCode());
    }

    @Test
    void testEquals() {
        Array<Character> other = createArray();
        Assertions.assertEquals(array, other);
        Assertions.assertNotEquals(array, empty);
    }

    // - - - - - - - - - - -

    Array<Character> createArray() {
        return Array.of('A', 'B', 'C', 'D', 'E');
    }
}