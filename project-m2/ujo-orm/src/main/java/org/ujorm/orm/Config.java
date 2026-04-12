package org.ujorm.orm;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.csv.CsvConfig;
import org.ujorm.tools.common.Primitive;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/** ORM Configuration */
public class Config {
    /** The class {@link org.ujorm.orm.dsl.DslQuery} enables only SQL SELECT statements. */
    public static final boolean DSL_SELECT_ONLY = true;

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());
    private static final String PREFIX = "org.ujorm.";
    private static final String CONFIG_FILE = "ujorm-config.properties";
    private static final KeyProvider meta = new KeyProvider();

    // --- Start the public list of the configuration parameters ---

    /**
     * Determines the default strategy for identifying the primary key of an entity.
     * <p>
     * If this parameter is enabled ({@code true}), the framework automatically assumes
     * that the first declared field in the JavaBean or Record is the identifier.
     * Otherwise, or if you need to specify a different field as the primary key,
     * you must explicitly annotate the field using the JPA {@code @Id} annotation.
     */
    public static final Key<Boolean> acceptDefaultPk = meta.key("firstPropertyIsIdentifier", true);

    /** Maximum size of the cache in the {@link org.ujorm.orm.jdbc.ResultSetMapper} */
    public static final Key<Integer> maxCacheSize = meta.key("maxCacheSize", 512);

    /** Batch size for the INSERT */
    public static final Key<Integer> batchSize = meta.key("batchSize", 512);

    /** Log level for the SQL STATEMENTS logs. */
    public static final Key<Level> logSqlLevel = meta.key("logSqlLevel", Level.INFO);

    /** Log parameters of the SQL statement. */
    public static final Key<Boolean> logSqlParams = meta.key("logSqlParams", false);

    /** Print warnings, if Connection autocommit is true in batch operations. */
    public static final Key<Boolean> autoCommitWarned = meta.key("autoCommitWarned", true);

    /** Enable quoting the SQL columns.
     * @see #quotePair
     */
    public static final Key<Boolean> enableSqlQuoting = meta.key("enableSqlQuoting", true);

    /**
     * Quotes for SQL column names. An empty string attempts to fetch them via JDBC.
     * Otherwise, the first and last characters serve as opening and closing delimiters.
     * @see #enableSqlQuoting
     * @see org.ujorm.orm.model.QuotePair
     */
    public static final Key<String> quotePair = meta.key("quotePair", "");


    // --- End of the list ---

    /** A technical parameter for the jUnit test only */
    public static final Key<String> testOnly = meta.key("testOnly", "");

    /** Object state stored in an array */
    private final Object[] values = new Object[meta.keys.size()];

    /** The object is locked and immutable. */
    private boolean locked = false;

    public Config() {
        var properties = loadProperties();
        var converters = CsvConfig.initConverterMap();
        for (var key : meta.keys) {
            loadKey(key, properties, converters);
        }
    }

    // --- Core Accessors ---

    /** General setter with lock check.
     * Values set here have the highest priority over other sources.
     * @param key Key instance
     * @param value Required value
     * @param <V> The value type (annotation breaks IntelliJ tests
     */
    public <V> void setValue(@NotNull Key<V> key, @NotNull V value) {
        if (locked) {
            throw new IllegalStateException("The configuration is locked.");
        }
        Objects.requireNonNull(value, "The value is required.");
        key.setValue(value, values);
    }

    /** Lock the configuration for further writes */
    public Config lock() {
        this.locked = true;
        return this;
    }

    // --- Getters ---

    public boolean acceptDefaultPk() { return acceptDefaultPk.getValue(values); }
    public int getMaxCacheSize() { return maxCacheSize.getValue(values); }
    public int getBatchSize() { return batchSize.getValue(values); }
    public Level getLogSqlLevel() { return logSqlLevel.getValue(values); }
    public boolean isLogSqlParams() { return logSqlParams.getValue(values); }
    public boolean isAutoCommitWarned() { return autoCommitWarned.getValue(values); }
    public boolean isEnableSqlQuoting() { return enableSqlQuoting.getValue(values); }
    public String getQuotePair() { return quotePair.getValue(values); }
    /** @deprecated For jUnit test only */
    @Deprecated
    String _testOnly() { return testOnly.getValue(values); }


    // --- Loading and conversion logic ---

    /** Loads value from System properties or file properties */
    private <V> void loadKey(
            @NotNull Key<V> key,
            @NotNull Properties props,
            @NotNull Map<Class<?>, Function<String, ?>> converters
    ) {
        var fullKey = PREFIX + key.name();
        var value = System.getProperty(fullKey);

        if (value == null) {
            value = props.getProperty(fullKey);
        }
        if (value != null) {
            setValue(key, convertValue(key, value, converters));
        }
    }

    /** Converts string to the type of the default value */
    @SuppressWarnings("unchecked")
    private <V> V convertValue(
            @NotNull Key<V> key,
            @NotNull String value,
            @NotNull Map<Class<?>, Function<String, ?>> converters
    ) {
        var type = Primitive.wrapPrimitive(key.type());
        var converter = (Function<String, ?>) converters.get(type);
        if (converter == null) {
            var msg = "Parameter %s has no converter for type %s".formatted(key.name, key.type());
            throw new IllegalStateException(msg);
        }
        try {
            return (V) converter.apply(value);
        } catch (RuntimeException e) {
            var msg = "Cannot convert value '%s' to type %s of the parameter %s"
                    .formatted(value, key.type().getSimpleName(), key.name());
            throw new IllegalStateException(msg, e);
        }
    }

    /** Loads properties from the classpath */
    private Properties loadProperties() {
        var result = new Properties();
        try (var stream = getClass().getResourceAsStream("/" + CONFIG_FILE)) {
            if (stream != null) {
                result.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load " + CONFIG_FILE, e);
        }
        return result;
    }

    @Override
    public String toString() {
        return "ConfigImpl{count=" + meta.keys.size() + ", locked=" + locked + "}";
    }

    /** Internal Key definition */
    @SuppressWarnings("unchecked")
    public record Key<V>(
            /** Name of the key */
            String name,

            /** Index of the key */
            int index,

            /** Default value */
            V defaultValue
    ) {
        public Class<V> type() {
            return (Class<V>) defaultValue.getClass();
        }

        @NotNull
        private V getValue(final @NotNull Object[] objects) {
            var result = objects[index];
            return result != null ? (V) result : defaultValue;
        }

        private void setValue(@NotNull final V value, @NotNull final Object[] objects) {
            objects[index] = value;
        }

        /** Returns full name of the parameters */
        @Override
        public String toString() {
            return PREFIX + name;
        }
    }

    /** Provider of configuration keys */
    private static class KeyProvider {
        private final List<Key<?>> keys = new ArrayList<>(10);

        <V> Key<V> key(String name, V defaultValue) {
            var result = new Key<V>(name, keys.size(), defaultValue);
            keys.add(result);
            return result;
        }
    }

    /** Build an immutable object with default arguments */
    public static Config ofDefault() {
        return new Config().lock();
    }
}