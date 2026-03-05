package org.ujorm.mapper.impl;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.ujorm.core.AbstractSnapshotable;
import org.ujorm.core.csv.CsvConfig;
import org.ujorm.tools.common.Primitive;

@Setter @Getter @ToString
public class ConfigImpl extends AbstractSnapshotable<ConfigImpl> implements Config {
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(ConfigImpl.class.getName());

    /** Property Prefix */
    private static final String PREFIX = "org.ujorm.";

    /** Configuration file */
    private static final String CONFIG_FILE = "ujorm-config.properties";

    /** The first key in the sequence represents the primary key. */
    private boolean firstPropertyIsIdentifier = true;

    /** Maximum size of the cache in the ResultSet Mapper */
    private int maxCacheSize = 512;

    /** Batch size for the INSERT, UPDATE, SELECT */
    private int batchSize = 512;

    /** Prints all SQL templates to the log. */
    private boolean printSql = true;

    /** Enable quoting the SQL columns */
    private boolean enableSqlQuoting = true;

    /** Write a warning if the column is not a relation and has no JDBC mapping. */
    private boolean columnMappingWarning = true;

    /** Print warnings, if Connection autocommit is true in batch operations. */
    private boolean autoCommitWarned = true;

    /** Enable or disable the service of the EntityManagerProvider object. */
    private boolean enabledEntityManagerProvider = true;

    /** Only for testing */
    private String testOnly = "";

    /** Helper attributes */
    private Map<Class<?>, Function<String, ?>> __funMap = null;

    public ConfigImpl() {
        __funMap = CsvConfig.initConverterMap();
        var properties = properties();
        firstPropertyIsIdentifier = value(firstPropertyIsIdentifier, "firstPropertyIsIdentifier", properties);
        maxCacheSize = value(maxCacheSize, "maxCacheSize", properties);
        batchSize = value(batchSize, "batchSize", properties);
        printSql = value(printSql, "printSql", properties);
        enableSqlQuoting = value(enableSqlQuoting, "enableSqlQuoting", properties);
        columnMappingWarning = value(columnMappingWarning, "columnMappingWarning", properties);
        autoCommitWarned = value(autoCommitWarned, "autoCommitWarned", properties);
        enabledEntityManagerProvider = value(enabledEntityManagerProvider, "enabledEntityManagerProvider", properties);
        testOnly = value(testOnly, "testOnly", properties);
        __funMap = null;
    }

    /** Load properties from the {@link #CONFIG_FILE}. */
    @NotNull Properties properties() {
        var result = new Properties();
        try (var stream = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
            if (stream != null) {
                result.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            } else {
                LOGGER.log(Level.INFO, "Configuration file {0} not found, using another source.", CONFIG_FILE);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load configuration file: " + CONFIG_FILE, e);
        }
        return result;
    }

    /** Converts a string value to the required data type. */
    public <T> T convertValue(@NotNull String value, @NotNull Class<T> type) {
        var clazz = Primitive.wrapPrimitive(type);
        var fun = __funMap.get(type);
        if (fun == null) {
            var msg ="Can't convert value '%s' to %s".formatted(value, type.getSimpleName());
            throw new IllegalStateException(msg);
        }
        return (T) fun.apply(value);
    }

    /** Loads a value from system properties, properties file or returns the default value. */
    @SuppressWarnings("unchecked")
    public <T> T value(@NotNull T defaultValue, @NotNull String key, @NotNull Properties fileProps) {
        var fullKey = PREFIX + key;
        var result = System.getProperty(fullKey);

        if (result == null) {
            result = fileProps.getProperty(fullKey);
        }

        if (result != null) {
            var type = (Class<T>) defaultValue.getClass();
            return convertValue(result, type);
        }
        return defaultValue;
    }


    /** Clone the configuration in case the argument is type of {@link ConfigImpl}. */
    @NotNull
    public static Config copy(@NotNull Config config) {
        try {
            return (config instanceof ConfigImpl impl)
                    ? impl.saveSnapshot().readSnapshot()
                    : config;
        } catch (IllegalStateException ex) {
            LOGGER.warning("Can`t clone the configuration: " + config.getClass().getName());
            return config;
        }
    }
}