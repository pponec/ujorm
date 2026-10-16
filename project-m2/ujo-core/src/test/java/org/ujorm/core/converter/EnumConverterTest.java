package org.ujorm.core.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tests of the built-in Enum converters */
class EnumConverterTest {

    enum Color { RED, GREEN }

    @Test
    void nameConverter() {
        var converter = EnumNameConverter.of(Color.class);
        assertEquals(String.class, converter.dbType());
        assertEquals("GREEN", converter.toDb(Color.GREEN));
        assertEquals(Color.GREEN, converter.fromDb("GREEN"));

        var e = assertThrows(IllegalArgumentException.class, () -> converter.fromDb("BLUE"));
        assertEquals("No enum item of the Color class for [BLUE]", e.getMessage());
    }

    @Test
    void ordinalConverter() {
        var converter = EnumOrdinalConverter.of(Color.class);
        assertEquals(Integer.class, converter.dbType());
        assertEquals(1, converter.toDb(Color.GREEN));
        assertEquals(Color.GREEN, converter.fromDb(1));

        var e = assertThrows(IllegalArgumentException.class, () -> converter.fromDb(2));
        assertEquals("No enum item of the Color class for [2]", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> converter.fromDb(-1));
    }
}
