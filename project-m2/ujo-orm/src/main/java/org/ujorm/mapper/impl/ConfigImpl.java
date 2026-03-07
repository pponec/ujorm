package org.ujorm.mapper.impl;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.AbstractSnapshotable;
import org.ujorm.core.csv.CsvConfig;
import org.ujorm.mapper.Config;
import org.ujorm.tools.common.Primitive;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigImpl extends AbstractSnapshotable<ConfigImpl> implements Config {

    private static final Logger LOGGER = Logger.getLogger(ConfigImpl.class.getName());
    private static final String PREFIX = "org.ujorm.";
    private static final String CONFIG_FILE = "ujorm-config.properties";
    private static final Map<Class<?>, Function<String, ?>> funMap = Map.copyOf(CsvConfig.initConverterMap());

    // --- Key Constants ---

    private static KeyProvider p = new KeyProvider();
    public static final Key<Boolean> firstPropertyIsIdentifier = p.key("firstPropertyIsIdentifier", true);
    public static final Key<Integer> maxCacheSize = p.key("maxCacheSize", 512);
    public static final Key<Integer> batchSize = p.key("batchSize", 512);
    public static final Key<Boolean> printSql = p.key("printSql", true);
    public static final Key<Boolean> enableSqlQuoting = p.key("enableSqlQuoting", true);
    public static final Key<Boolean> columnMappingWarning = p.key("columnMappingWarning", true);
    public static final Key<Boolean> autoCommitWarned = p.key("autoCommitWarned", true);
    public static final Key<Boolean> enabledUjormServiceProvider = p.key("enabledUjormServiceProvider", true);
    public static final Key<String> testOnly = p.key("testOnly", "");

    /** Object state stored in an array */
    private final Object[] values = new Object[p.keys.size()];

    /** Write lock */
    private boolean writeLock = false;

    public ConfigImpl() {
        Properties properties = loadProperties();
        for(var key : p.keys) {
            loadKey(key, properties);
        }
    }

    // --- Core Accessors ---

    /** General getter returning value by key index or default value */
    @SuppressWarnings("unchecked")
    public <V> V getValue(@NotNull Key<V> key) {
        V value = (V) values[key.index()];
        return (value != null) ? value : key.defaultValue();
    }

    /** General setter with lock check */
    /** General setter with lock check */
    public <V> void setValue(@NotNull Key<V> key, V value) {
        if (writeLock) {
            throw new IllegalStateException("The configuration is locked.");
        }
        key.setValue(value, values);
    }

    /** Lock the configuration for further writes */
    public void makeReadOnly() {
        this.writeLock = true;
    }

    // --- Interface implementation ---

    @Override public boolean isFirstPropertyIsIdentifier() { return firstPropertyIsIdentifier.getValue(values); }
    @Override public int getMaxCacheSize() { return getValue(maxCacheSize); }
    @Override public int getBatchSize() { return getValue(batchSize); }
    @Override public boolean isPrintSql() { return getValue(printSql); }
    @Override public boolean isEnableSqlQuoting() { return getValue(enableSqlQuoting); }
    @Override public boolean isColumnMappingWarning() { return getValue(columnMappingWarning); }
    @Override public boolean isAutoCommitWarned() { return getValue(autoCommitWarned); }
    @Override public boolean isEnabledUjormServiceProvider() { return getValue(enabledUjormServiceProvider); }

    // --- Loading and conversion logic ---

    /** Loads value from System properties or file properties */
    private <V> void loadKey(Key<V> key, Properties props) {
        String fullKey = PREFIX + key.name();
        String val = System.getProperty(fullKey);
        if (val == null) {
            val = props.getProperty(fullKey);
        }
        if (val != null) {
            setValue(key, convertValue(val, key.defaultValue()));
        }
    }

    /** Converts string to the type of the default value */
    @SuppressWarnings("unchecked")
    private <T> T convertValue(String value, T defaultValue) {
        Class<?> type = Primitive.wrapPrimitive(defaultValue.getClass());
        Function<String, ?> converter = funMap.get(type);
        if (converter == null) {
            throw new IllegalStateException("No converter found for type: " + type);
        }
        return (T) converter.apply(value);
    }

    /** Loads properties from the classpath */
    private Properties loadProperties() {
        Properties result = new Properties();
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
        return "ConfigImpl{count=" + p.keys.size() + ", locked=" + writeLock + "}";
    }

    /** Internal Key definition */
    public record Key<V>(String name, int index, V defaultValue) {
        public Class<V> getType() {
            return (Class<V>) defaultValue.getClass();
        }
        public V getValue(Object[] objects) {
            var result = objects[index];
            return result != null ? (V) result : defaultValue;
        }
        public void setValue(V value, Object[] objects) {
            if (value == null) throw new IllegalArgumentException("Value is required");
            objects[index] = value;
        }
    }

    private static class KeyProvider {
        List<Key<?>> keys = new ArrayList();
        <V> Key key(String name, V defaultValue) {
            var result = new Key<V>(name, keys.size(), defaultValue);
            keys.add(result);
            return result;
        }
    }
}