package org.ujorm.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Test for EnumMapper */
class EnumMapperTest {

    private final EnumMapper mapper = new EnumMapper();

    /** Test getting enum item by valid index */
    @Test
    void testGetByIndex() {
        var result = mapper.getByIndex(Sample.class, 1);
        assertEquals(Sample.GREEN, result);
    }

    /** Test getting enum item by invalid index with exception */
    @Test
    void testGetByIndexFailure() {
        assertThrows(IllegalArgumentException.class, () -> mapper.getByIndex(Sample.class, 5));
    }

    /** Test getting enum item by invalid index with default value */
    @Test
    void testGetByIndexWithDefault() {
        var result = mapper.getByIndex(Sample.class, 10, Sample.RED);
        assertEquals(Sample.RED, result);
    }

    /** Test getting enum item by valid name */
    @Test
    void testGetByName() {
        var result = mapper.getByName(Sample.class, "BLUE");
        assertEquals(Sample.BLUE, result);
    }

    /** Test getting enum item by invalid name with exception */
    @Test
    void testGetByNameFailure() {
        assertThrows(IllegalArgumentException.class, () -> mapper.getByName(Sample.class, "NON_EXISTING"));
    }

    /** Test getting enum item by invalid name with default value */
    @Test
    void testGetByNameWithDefault() {
        var result = mapper.getByName(Sample.class, "UNKNOWN", Sample.GREEN);
        assertEquals(Sample.GREEN, result);
    }

    /** Sample enum for testing purposes */
    private enum Sample {
        RED, GREEN, BLUE
    }
}