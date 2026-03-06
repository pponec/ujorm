package org.ujorm.mapper.impl;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.ujorm.core.AbstractSnapshotable;
import org.ujorm.core.csv.CsvConfig;
import org.ujorm.mapper.UjormServiceProvider;
import org.ujorm.tools.common.Primitive;

@Getter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigImpl extends AbstractSnapshotable<ConfigImpl> implements Config {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(ConfigImpl.class.getName());

    /** Property Prefix */
    private static final String PREFIX = "org.ujorm.";

    /** Configuration file */
    private static final String CONFIG_FILE = "ujorm-config.properties";

    /** The first key in the sequence represents the primary key. */
    @Builder.Default
    private boolean firstPropertyIsIdentifier = true;

    /** Maximum size of the cache in the ResultSet Mapper */
    @Builder.Default
    private int maxCacheSize = 512;

    /** Batch size for the INSERT, UPDATE, SELECT */
    @Builder.Default
    private int batchSize = 512;

    /** Prints all SQL templates to the log. */
    @Builder.Default
    private boolean printSql = true;

    /** Enable quoting the SQL columns */
    @Builder.Default
    private boolean enableSqlQuoting = true;

    /** Write a warning if the column is not a relation and has no JDBC mapping. */
    @Builder.Default
    private boolean columnMappingWarning = true;

    /** Print warnings, if Connection autocommit is true in batch operations. */
    @Builder.Default
    private boolean autoCommitWarned = true;

    /** Enable or disable the service of the {@link UjormServiceProvider} object. */
    @Builder.Default
    private boolean enabledUjormServiceProvider = true;

    /** Only for testing */
    @Builder.Default
    private String testOnly = "";

    /** Map  */
    private static final Map<Class<?>, Function<String, ?>> funMap = Map.copyOf(CsvConfig.initConverterMap());

    public ConfigImpl() {
        var properties = properties();
        this.firstPropertyIsIdentifier = value(true, "firstPropertyIsIdentifier", properties);
        this.maxCacheSize = value(512, "maxCacheSize", properties);
        this.batchSize = value(512, "batchSize", properties);
        this.printSql = value(true, "printSql", properties);
        this.enableSqlQuoting = value(true, "enableSqlQuoting", properties);
        this.columnMappingWarning = value(true, "columnMappingWarning", properties);
        this.autoCommitWarned = value(true, "autoCommitWarned", properties);
        this.enabledUjormServiceProvider = value(true, "enabledUjormServiceProvider", properties);
        this.testOnly = value("", "testOnly", properties);
    }

    /**
     * Vytvoří Builder naplněný výchozími hodnotami a hodnotami z konfiguračního souboru.
     * Ideální pro operativní změnu konfigurace v testech.
     */
    public static ConfigImplBuilder builderWithDefaults() {
        return new ConfigImpl().toBuilder();
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
        var fun = funMap.get(type);
        if (fun == null) {
            var msg ="Can't convert value '%s' to %s".formatted(value, type.getSimpleName());
            throw new IllegalStateException(msg);
        }
        return (T) fun.apply(value);
    }

    /** Loads a value from system properties, properties file or returns the default value. */
    @SuppressWarnings("unchecked")
    public <T> T value(@NotNull T defaultValue, @NotNull String key, @NotNull Properties fileProps) {
        Objects.requireNonNull(defaultValue, "Parameter 'defaultValue' is required.");
        var fullKey = PREFIX + key;
        var result = System.getProperty(fullKey);

        if (result == null) {
            result = fileProps.getProperty(fullKey);
        }

        if (result != null) {
            return convertValue(result, (Class<T>) defaultValue.getClass());
        }
        return defaultValue;
    }
}