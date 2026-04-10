package org.ujorm.tools.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.NoSuchElementException;
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

    /** Test of the copy constructor */
    @Test
    void testCopyConstructor() {
        var clone = new Array<>(array);
        Assertions.assertNotSame(array, clone);
        Assertions.assertEquals(array, clone);
        Assertions.assertEquals('A', clone.getItem(0));
    }

    /** Test of the copy method */
    @Test
    void testCopy() {
        var clone = array.copy();
        Assertions.assertNotSame(array, clone);
        Assertions.assertEquals(array.getItem(0), clone.getItem(0));
        Assertions.assertEquals(array.getItem(4), clone.getItem(4));
        Assertions.assertArrayEquals(array.stream().toArray(), clone.stream().toArray());
        Assertions.assertEquals(array, clone);
    }

    /** Test of the toArray method */
    @Test
    void testToArray() {
        var plainArray = array.toArray();
        Assertions.assertEquals(5, plainArray.length);
        Assertions.assertEquals('A', plainArray[0]);
        Assertions.assertEquals('E', plainArray[4]);

        var emptyArray = empty.toArray();
        Assertions.assertEquals(0, emptyArray.length);
    }

    /** Test of the get method with various indices */
    @Test
    void get() {
        Assertions.assertEquals('A', array.get(0).orElse(undef));
        Assertions.assertEquals('B', array.get(1).orElse(undef));
        Assertions.assertEquals('E', array.get(4).orElse(undef));
        Assertions.assertEquals('X', array.get(5).orElse(undef));
        Assertions.assertEquals('E', array.get(-1).orElse(undef));
        Assertions.assertEquals('D', array.get(-2).orElse(undef));
        Assertions.assertEquals(undef, empty.get(0).orElse(undef));
        Assertions.assertEquals(undef, array.get(9).orElse(undef));
        Assertions.assertEquals(undef, array.get(-9).orElse(undef));
    }

    /** Test of the getValue method */
    @Test
    void getValue() {
        Assertions.assertEquals('A', array.getValue(0));
        Assertions.assertEquals('C', array.getValue(2));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.getValue(5));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> empty.getValue(0));
    }

    /** Test of the getItem method with positive and negative indices */
    @Test
    void getItem() {
        Assertions.assertEquals('A', array.getItem(0));
        Assertions.assertEquals('E', array.getItem(-1));
        Assertions.assertEquals('D', array.getItem(-2));

        // Out of bounds checks
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.getItem(5));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.getItem(-6));
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> empty.getItem(0));
    }

    /** Test of the getFirst method */
    @Test
    void getFirst() {
        Assertions.assertEquals('A', array.getFirst().orElse(undef));
        Assertions.assertEquals(undef, empty.getFirst().orElse(undef));
    }

    /** Test of the getFirstValue method without arguments */
    @Test
    void getFirstValue() {
        Assertions.assertEquals('A', array.getFirstValue());
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> empty.getFirstValue());
    }

    /** Test of the getFirstValue method with default value */
    @Test
    void getFirstValueDefault() {
        Assertions.assertEquals('A', array.getFirstValue(undef));
        Assertions.assertEquals(undef, empty.getFirstValue(undef));
        Assertions.assertNull(empty.getFirstValue(null));
    }

    /** Test of the getLast method */
    @Test
    void getLast() {
        Assertions.assertEquals('E', array.getLast().orElse(undef));
        Assertions.assertEquals(undef, empty.getLast().orElse(undef));
    }

    /** Test of the getLastValue method with default value */
    @Test
    void getLastValueDefault() {
        Assertions.assertEquals('E', array.getLastValue(undef));
        Assertions.assertEquals(undef, empty.getLastValue(undef));
        Assertions.assertNull(empty.getLastValue(null));
    }

    /** Test of the getLastValue method without arguments */
    @Test
    void getLastValue() {
        Assertions.assertEquals('E', array.getLastValue());
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> empty.getLastValue());
    }

    /** Test of the removeFirst method */
    @Test
    void removeFirst() {
        var trim = array.removeFirst();
        Assertions.assertEquals(5, array.size()); // original is unmodified
        Assertions.assertEquals(4, trim.size());
        Assertions.assertEquals('B', trim.getItem(0));

        trim = empty.removeFirst();
        Assertions.assertEquals(0, trim.size());
    }

    /** Test of the subArray method with positive and negative indices */
    @Test
    void subArray() {
        var trim1 = array.subArray(3);
        Assertions.assertEquals(2, trim1.size());
        Assertions.assertEquals('D', trim1.getItem(0));
        Assertions.assertEquals('E', trim1.getItem(1));

        var trim2 = array.subArray(-2);
        Assertions.assertEquals(2, trim2.size());
        Assertions.assertEquals('D', trim2.getItem(0));

        var trimEmpty = empty.subArray(0);
        Assertions.assertEquals(0, trimEmpty.size());

        var trimOutOfBounds = array.subArray(10);
        Assertions.assertEquals(0, trimOutOfBounds.size());
    }

    /** Test of the add method with varargs */
    @Test
    void addVarargs() {
        var extended = array.add('P', 'C');
        Assertions.assertEquals(5, array.size()); // original is unmodified
        Assertions.assertEquals(7, extended.size());
        Assertions.assertEquals('P', extended.getItem(5));
        Assertions.assertEquals('C', extended.getItem(6));

        var fromEmpty = empty.add('A');
        Assertions.assertEquals(1, fromEmpty.size());
        Assertions.assertEquals('A', fromEmpty.getItem(0));
    }

    /** Test of the add method with Array argument */
    @Test
    void addArray() {
        var extra = Array.of('X', 'Y');
        var extended = array.add(extra);

        Assertions.assertEquals(7, extended.size());
        Assertions.assertEquals('X', extended.getItem(5));
        Assertions.assertEquals('Y', extended.getItem(-1));

        var extendedEmpty = array.add(empty);
        Assertions.assertEquals(5, extendedEmpty.size());
        Assertions.assertEquals(array, extendedEmpty);
    }

    /** Test of conversion to List */
    @Test
    void toList() {
        var list = array.toList();

        Assertions.assertEquals(array.getItem(0), list.get(0));
        Assertions.assertEquals(array.getItem(1), list.get(1));
        Assertions.assertEquals(array.getItem(4), list.get(4));
        Assertions.assertEquals(array.size(), list.size());

        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.add('X'));
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

        var emptyList = empty.stream().collect(Collectors.toList());
        Assertions.assertTrue(emptyList.isEmpty());
    }

    /** Test of the iterator method */
    @Test
    void testIterator() {
        var result = new ArrayList<Character>();
        for (var item : array) {
            result.add(item);
        }
        Assertions.assertEquals(array.size(), result.size());
        Assertions.assertEquals('A', result.get(0));
        Assertions.assertEquals('E', result.get(4));

        var it = empty.iterator();
        Assertions.assertFalse(it.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, it::next);
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
        Assertions.assertNotEquals(array, null);
        Assertions.assertNotEquals(array, "String");
    }

    /** Test of the toString method */
    @Test
    void testToString() {
        Assertions.assertEquals("[A, B, C, D, E]", array.toString());
        Assertions.assertEquals("[]", empty.toString());
    }

    /** Test of the static factory method ofObject */
    @Test
    void ofObject() {
        var obj = "Hello";
        var result = Array.ofObject(obj);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(obj, result.getItem(0));

        var wrapped = Array.ofObject(array);
        Assertions.assertSame(array, wrapped);

        var nullResult = Array.ofObject(null);
        Assertions.assertEquals(1, nullResult.size());
        Assertions.assertNull(nullResult.getItem(0));
    }

    /** Factory method for a testing array */
    Array<Character> createArray() {
        return Array.of('A', 'B', 'C', 'D', 'E');
    }
}