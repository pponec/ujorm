package org.ujorm.tools.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArrayUtilsTest {

    private final Character[] empty = new Character[0];

    private final Character undef = 'X';

    private final ArrayUtils instance = new ArrayUtils();

    @Test
    void testClone() {
        Character[] array = createArray();
        Character[] clone = instance.clone(array);

        Assertions.assertNotSame(array, clone);
        Assertions.assertEquals(array[0], clone[0]);
        Assertions.assertEquals(array[4], clone[4]);
        Assertions.assertArrayEquals(array, clone);
    }

    @Test
    void getItem() {
        Character[] array = createArray();
        Assertions.assertEquals('A', instance.getItem(0, array).orElse(undef));
        Assertions.assertEquals('B', instance.getItem(1, array).orElse(undef));
        Assertions.assertEquals('E', instance.getItem(4, array).orElse(undef));
        Assertions.assertEquals('X', instance.getItem(5, array).orElse(undef));
        Assertions.assertEquals(undef, instance.getItem(0, empty).orElse(undef));
        Assertions.assertEquals(undef, instance.getItem(9, array).orElse(undef));
        Assertions.assertEquals(undef, instance.getItem(-1, array).orElse(undef));
    }

    @Test
    void getFirst() {
        Character[] array = createArray();
        Assertions.assertEquals('A', instance.getFirst(array).orElse(undef));
        Assertions.assertEquals(undef, instance.getFirst(empty).orElse(undef));
    }

    @Test
    void getLast() {
        Character[] array = createArray();
        Assertions.assertEquals('E', instance.getLast(array).orElse(undef));
        Assertions.assertEquals(undef, instance.getLast(empty).orElse(undef));
    }

    @Test
    void removeFirst() {
        Character[] array = createArray();
        Character[] trim = instance.removeFirst(array);
        Assertions.assertEquals(5, array.length);
        Assertions.assertEquals(4, trim.length);
        Assertions.assertEquals('B', trim[0]);

        trim = instance.removeFirst(empty);
        Assertions.assertEquals(0, trim.length);
    }

    @Test
    void subArray() {
        Character[] array = createArray();
        Character[] trim = instance.subArray(3, array);
        Assertions.assertEquals(2, trim.length);
        Assertions.assertEquals('D', trim[0]);
    }

    @Test
    void join() {
        Character[] array = createArray();
        Character[] extended = instance.join(array, 'P', 'C');

        Assertions.assertEquals(array.length + 2, extended.length);
        Assertions.assertEquals('P', extended[5]);
        Assertions.assertEquals('C', extended[6]);
    }

    // - - - - - - - - - - -

    Character[] createArray() {
        return new Character[]{'A', 'B', 'C', 'D', 'E'};
    }
}