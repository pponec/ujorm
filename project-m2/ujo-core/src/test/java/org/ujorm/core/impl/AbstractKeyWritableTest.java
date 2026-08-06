package org.ujorm.core.impl;

import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.generated.City_;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A hand-written key inherits the default of the {@link AbstractKey#writable()} method,
 * so the default must be safe for a record, where no component has a setter.
 */
class AbstractKeyWritableTest {

    @Test
    @SuppressWarnings("unchecked")
    void handWrittenRecordKeyIsNotWritable() {
        var handler = new City_();

        for (var rawKey : handler.getKeyList()) {
            var key = (Key<City, Object>) rawKey;
            assertFalse(key.info().writable(), "A record component is not writable: " + key.name());
            assertThrows(UnsupportedOperationException.class,
                    () -> key.setValue(City.of(1L, "Prague"), null),
                    "The setter of the key is unsupported: " + key.name());
        }
        assertFalse(handler.getKeyList().isEmpty(), "The handler has keys");
    }
}
