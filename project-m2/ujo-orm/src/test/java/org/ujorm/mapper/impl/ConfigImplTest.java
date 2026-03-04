package org.ujorm.mapper.impl;

import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Tests for the ConfigImpl class. */
class ConfigImplTest {

    @AfterEach
    void tearDown() {
        System.clearProperty("org.ujorm.insertBatchSize");
    }

    @Test
    void testLoadFromFile() {
        var config = new ConfigImpl();
        assertEquals("fileValue", config.getTestOnly());
    }

    @Test
    void testMockedProperties() {
        var config = new ConfigImplMock();

        assertEquals(1024, config.getMaxCacheSize());
        assertFalse(config.isPrintSql());
        assertTrue(config.isFirstPropertyIsIdentifier());
    }

    @Test
    void testSystemPropertyPriority() {
        System.setProperty("org.ujorm.insertBatchSize", "2048");
        var config = new ConfigImplMock();

        assertEquals(2048, config.getInsertBatchSize());
    }

    /** Inner class for mocking properties */
    static class ConfigImplMock extends ConfigImpl {

        @Override
        protected Properties properties() {
            var props = new Properties();
            props.setProperty("org.ujorm.maxCacheSize", "1024");
            props.setProperty("org.ujorm.printSql", "false");
            props.setProperty("org.ujorm.insertBatchSize", "128");
            return props;
        }
    }
}