package org.ujorm.orm.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.Config;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the ConfigImpl class. */
class ConfigTest {

    @BeforeEach
    @AfterEach
    void cleanUpSystemProperties() {
        // Ensures a clean environment before and after every test
        System.clearProperty("org.ujorm.maxCacheSize");
        System.clearProperty("org.ujorm.printSql");
        System.clearProperty("org.ujorm.batchSize");
        System.clearProperty("org.ujorm.testOnly");
    }

    @Test
    void testDefaultAndFileValues() {
        var config = new Config();

        // Verifies fallback to defaults or file properties
        assertNotNull(config.getValue(Config.testOnly));
        assertTrue(config.isFirstPropertyIsIdentifier());
    }

    @Test
    void testSystemPropertyOverrides() {
        // Simulates external configuration via System properties (replaces the need for Mock)
        System.setProperty("org.ujorm.maxCacheSize", "1024");
        System.setProperty("org.ujorm.printSql", "false");
        System.setProperty("org.ujorm.batchSize", "128");

        var config = new Config();

        assertEquals(1024, config.getMaxCacheSize());
        assertFalse(config.isPrintSql());
        assertEquals(128, config.getBatchSize());

        // This remains default as it was not overridden
        assertTrue(config.isFirstPropertyIsIdentifier());
    }

    @Test
    void testInvalidSystemPropertyConversion() {
        System.setProperty("org.ujorm.maxCacheSize", "not_a_number");

        var exception = assertThrows(
                NumberFormatException.class,
                Config::new,
                "Should throw an exception when property cannot be parsed into an Integer."
        );
        assertNotNull(exception);
    }

    @Test
    void testKeySetters() {
        // Verifies the Typed Key Pattern setters
        var config = new Config();

        config.setValue(Config.maxCacheSize, 9999);
        config.setValue(Config.printSql, false);
        config.setValue(Config.testOnly, "BUILDER_TEST");

        assertEquals(9999, config.getMaxCacheSize());
        assertFalse(config.isPrintSql());
        assertEquals("BUILDER_TEST", config.getValue(Config.testOnly));
        assertTrue(config.isFirstPropertyIsIdentifier());
    }

    @Test
    void testSetValueWithNullThrowsException() {
        var config = new Config();

        var exception = assertThrows(
                IllegalArgumentException.class,
                () -> config.setValue(Config.maxCacheSize, null),
                "The setValue() method must throw an exception if value is null."
        );

        assertTrue(exception.getMessage().contains("Value is required"));
    }

    @Test
    void testLockThrowsExceptionOnModification() {
        var config = new Config().lock();

        // Verifies that locked configuration prevents further modifications
        var exception = assertThrows(
                IllegalStateException.class,
                () -> config.setValue(Config.maxCacheSize, 2048),
                "Locked configuration cannot be modified."
        );

        assertTrue(exception.getMessage().contains("locked"));
    }
}