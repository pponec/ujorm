package org.ujorm.orm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.utils.Lines;

import java.util.logging.Level;

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
    @SuppressWarnings("deprecation")
    void testDefaultAndFileValues() {
        var config = new Config();

        // Verifies fallback to defaults or file properties
        assertNotNull(config._testOnly());
        assertTrue(config.acceptDefaultPk());
    }

    @Test
    void testSystemPropertyOverrides() {
        // Simulates external configuration via System properties (replaces the need for Mock)
        System.setProperty("org.ujorm.maxCacheSize", "1024");
        System.setProperty("org.ujorm.logSqlLevel", "OFF");
        System.setProperty("org.ujorm.batchSize", "128");

        var config = new Config();

        assertEquals(1024, config.getMaxCacheSize());
        assertEquals(Level.OFF, config.getLogSqlLevel());
        assertEquals(128, config.getBatchSize());

        // This remains default as it was not overridden
        assertTrue(config.acceptDefaultPk());
    }

    @Test
    void testInvalidSystemPropertyConversion() {
        System.setProperty("org.ujorm.maxCacheSize", "not_a_number");

        var exception = assertThrows(
                IllegalStateException.class,
                Config::new,
                "Should throw an exception when property cannot be parsed into an Integer."
        );
        assertNotNull(exception);
    }

    /** Sample, how to set a value for the hight priority  */
    @Test
    @SuppressWarnings("deprecation")
    void testKeySetters() {
        // Verifies the Typed Key Pattern setters
        var config = new Config();

        config.setValue(Config.maxCacheSize, 9999);
        config.setValue(Config.logSqlLevel, Level.OFF);
        config.setValue(Config.testOnly, "BUILDER_TEST");

        assertEquals(9999, config.getMaxCacheSize());
        assertEquals(Level.OFF, config.getLogSqlLevel());
        assertEquals("BUILDER_TEST", config._testOnly());
        assertTrue(config.acceptDefaultPk());
    }

    @Test
    void testSetValueWithNullThrowsException() {
        var config = new Config();

        var exception = assertThrows(
                RuntimeException.class,
                () -> config.setValue(Config.maxCacheSize, null));
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

    @Test
    void testToString() {
        var config = Config.ofDefault();
        var lines = Lines.of(config.toString());
        System.out.println(lines);

        assertEquals("org.ujorm.orm.Config (locked: true)", lines.get(0));
        assertEquals("org.ujorm.firstPropertyIsIdentifier: true", lines.get(1).trim());
    }
}