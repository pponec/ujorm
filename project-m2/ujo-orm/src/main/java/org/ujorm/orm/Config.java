package org.ujorm.orm;

import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.ujorm.core.csv.CsvConfig;
import org.ujorm.tools.common.Primitive;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ORM Configuration.
 * <p>
 * Each {@link Key} attribute in this class represents a single configuration parameter,
 * where the data type of the parameter is defined by the generic type {@code <V>}.
 * </p>
 *
 * <h3>Parameter Assembly and Priority</h3>
 *
 * The configuration values are assembled from multiple sources. When a value is requested,
 * the mechanism follows this priority (from highest to lowest):
 *
 * <ol>
 *   <li><b>Manual Settings:</b> Values explicitly set using the {@link #setValue(Key, Object)} method.</li>
 *   <li><b>System Properties:</b> JVM system properties prefixed with {@code org.ujorm.} (e.g., {@code -Dorg.ujorm.batchSize=1000}).</li>
 *   <li><b>Configuration File:</b> Values loaded from the {@code ujorm-config.properties} file located on the classpath.</li>
 *   <li><b>Default Values:</b> The initial values defined directly in the code during {@link Key} creation.</li>
 * </ol>
 *
 * <h3>Lifecycle and Locking</h3>
 *
 * It is highly recommended to <b>lock</b> the configuration instance using the {@link #lock()} method
 * before passing it to the ORM engine or using it in a multi-threaded environment.
 * Once locked, the configuration becomes immutable, ensuring consistency and thread safety.
 *
 * <h3>SQL statement logging</h3>
 *
 * <p>
 * Generated SQL is optionally written through {@code java.util.logging} (JUL).
 * Use {@link #logSqlLevel} to choose the severity of each log record (for example
 * {@link java.util.logging.Level#INFO} for console-friendly output, or {@link java.util.logging.Level#FINE}
 * for quieter diagnostics) and {@link #logSqlParams} to include bound parameter values in the message
 * instead of placeholder marks only.
 * Set {@link #logSqlLevel} to {@link java.util.logging.Level#OFF} to disable SQL logging from the ORM side.
 * These keys follow the same priority order as the rest of this class: explicit {@link #setValue(Key, Object)},
 * JVM system properties ({@code -Dorg.ujorm.logSqlLevel=INFO}, {@code -Dorg.ujorm.logSqlParams=true}),
 * {@code ujorm-config.properties}, then built-in defaults.
 * </p>
 * <p>
 * For a fixed {@code INFO} preset, you can use {@link #ofSqlInfoWithParams(boolean)} or
 * {@link org.ujorm.orm.utils.EntityContext#ofSqlInfoWithParams(boolean)} when building the context.
 * </p>
 * <p>
 * Even when the ORM emits a log record, it appears only if JUL accepts that severity: statements routed
 * through {@link org.ujorm.orm.core.EntityManager} or {@link org.ujorm.tools.jdbc.AbstractSqlQuery} require
 * matching logger and handler levels at or below {@link #logSqlLevel}.
 * </p>
 * <p>
 * <b>Zero-configuration logging for tests:</b> add JUL properties to {@value #CONFIG_FILE}
 * in {@code src/test/resources/} and this class will apply them automatically on first use —
 * no JVM argument or Maven Surefire setup required.
 * To route SQL output to {@code System.out} (so that {@code ./mvnw test | tee log.txt} captures it),
 * use {@link org.ujorm.tools.logging.UjormConsoleHandler} as the handler.
 * </p>
 */
@Log
public class Config {

    private static final String PREFIX = "org.ujorm.";
    private static final String CONFIG_FILE = "ujorm-config.properties";
    private static final KeyProvider meta = new KeyProvider();

    static {
        JulConfig.init();
    }

    // --- Start the public list of the configuration parameters ---

    /**
     * Determines the default strategy for identifying the primary key of an entity.
     * <p>
     * If this parameter is enabled ({@code true}), the framework automatically assumes
     * that the first declared field in the JavaBean or Record is the identifier.
     * Otherwise, or if you need to specify a different field as the primary key,
     * you must explicitly annotate the field using the JPA {@code @Id} annotation.
     * The default value is {@code true}.
     */
    public static final Key<Boolean> acceptDefaultPk = meta.key("firstPropertyIsIdentifier", true);

    /**
     * Maximum size of the cache in the {@link org.ujorm.orm.jdbc.ResultSetMapper}.
     * The default value is 512.
     */
    public static final Key<Integer> maxCacheSize = meta.key("maxCacheSize", 512);

    /** Batch size for the INSERT. The default value is 512. */
    public static final Key<Integer> batchSize = meta.key("batchSize", 512);

    /**
     * Log level for SQL statement logging.
     * The default value is {@code Level.FINE}.
     * To disable SQL logs, use the {@code Level.OFF} value.
     * <p>
     * Note: For the logs to be visible, the logging framework configuration
     * (e.g., {@code logging.properties}) must also be set to a level
     * equal to or lower than this parameter.
     */
    public static final Key<Level> logSqlLevel = meta.key("logSqlLevel", Level.FINE);

    /** Log parameters of the SQL statement. The default value is {@code false}. */
    public static final Key<Boolean> logSqlParams = meta.key("logSqlParams", false);

    /** Print warnings, if Connection autocommit is true in batch operations. The default value is {@code true}. */
    public static final Key<Boolean> autoCommitWarned = meta.key("autoCommitWarned", true);

    /** Enable quoting the SQL columns. The default value is {@code true}.
     * @see #quotePair
     */
    public static final Key<Boolean> enableSqlQuoting = meta.key("enableSqlQuoting", true);

    /**
     * Quotes for SQL column names. An empty string attempts to fetch them via JDBC.
     * Otherwise, the first and last characters serve as opening and closing delimiters.
     * The default value is an empty string.
     * @see #enableSqlQuoting
     * @see org.ujorm.orm.model.QuotePair
     */
    public static final Key<String> quotePair = meta.key("quotePair", "");

    /**
     * Log all configuration values in the {@link org.ujorm.orm.utils.EntityContext} class.
     * The default value is {@code true}.
     */
    public static final Key<Boolean> logConfigValues = meta.key("logConfigValues", true);


    /**
     * Controls whether the {@link org.ujorm.orm.core.EntityManager} validates and adapts to the
     * database context (catalog, schema, database product) on every
     * {@link org.ujorm.orm.core.EntityManager#crud(java.sql.Connection)} call.
     *
     * <h4>When to enable ({@code true})</h4>
     * <p>
     * <strong>Enable this parameter whenever more than one database or schema may be involved:</strong>
     * </p>
     * <ul>
     *   <li><b>Multi-tenant applications</b> — each tenant has its own schema or database.
     *       The ORM detects the change from connection metadata and refreshes the cached
     *       {@link org.ujorm.orm.model.TableModel} automatically.
     *       Typical variants: <em>tenant per schema</em> (connections differ by {@code schema})
     *       or <em>tenant per database/catalog</em> (connections differ by catalog or JDBC URL).
     *       Mixed deployments are supported; isolation is driven by the key that
     *       {@link org.ujorm.orm.core.TableModelService} builds from the connection metadata.</li>
     *   <li><b>Heterogeneous connection pools</b> — connections in the pool may resolve to
     *       different schemas (e.g. during integration tests or dynamic routing).</li>
     *   <li><b>Development and test environments</b> — validation catches programming mistakes
     *       (wrong connection passed to {@code crud()}) early, with a clear
     *       {@link IllegalStateException} instead of an obscure SQL failure.</li>
     * </ul>
     *
     * <h4>Default: {@code false} — no validation, maximum performance</h4>
     * <p>
     * After the first {@code crud()} call initializes the internal
     * {@link org.ujorm.orm.model.TableModel}, all subsequent calls skip the connection metadata
     * inspection entirely. This eliminates per-call overhead (JDBC metadata reads, string
     * allocation, cache lookup), which is measurable on fast in-memory databases such as H2.
     * The trade-off is that passing a connection to a wrong database or schema produces a plain
     * SQL error instead of a descriptive {@link IllegalStateException}.
     * </p>
     *
     * <h4>Performance note</h4>
     * <p>
     * When {@code true}, each {@code crud()} call inspects JDBC connection metadata
     * ({@code getCatalog()}, {@code getSchema()}, {@code getDatabaseProductName()},
     * {@code getIdentifierQuoteString()}). These values are typically cached by the driver,
     * so the overhead is small but non-zero. For single-context applications on fast databases
     * the default {@code false} is the better choice.
     * </p>
     *
     * @see #tenantPerDatabaseSchema()
     * @see org.ujorm.orm.core.EntityManager
     * @see org.ujorm.orm.core.TableModelService
     */
    public static final Key<Boolean> tenantPerDatabaseSchema = meta.key("tenantPerDatabaseSchema", false);

    // --- End of the list ---

    /** A technical parameter for the jUnit test only. The default value is an empty string. */
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
     * @param <V> The value type
     */
    public <V> Config setValue(@NotNull Key<V> key, @NotNull V value) {
        if (locked) {
            throw new IllegalStateException("The configuration is locked.");
        }
        Objects.requireNonNull(value, "The value is required.");
        key.setValue(value, values);
        return this;
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
    public boolean logConfigValues() { return logConfigValues.getValue(values); }

    /** @see #tenantPerDatabaseSchema */
    public boolean tenantPerDatabaseSchema() { return tenantPerDatabaseSchema.getValue(values); }
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
            log.log(Level.WARNING, "Failed to load " + CONFIG_FILE, e);
        }
        return result;
    }

    /** Get all values */
    @Override
    public String toString() {
        var separator = "\n  ";
        var result = new StringBuilder(256)
                .append(getClass().getName())
                .append(" (locked: ").append(locked).append(')');
        for (var key : meta.keys) {
            if (testOnly == key) continue;
            var value = key.getValue(values);
            result.append(separator).append(key).append(": ").append(value);
        }
        return result.toString();
    }


    /**
     * Internal Key definition
     *
     * @param <V> The value type
     */
    @SuppressWarnings("unchecked")
    public record Key<V>(
            /**
             * Returns the name of the key
             * @return The name
             */
            String name,

            /**
             * Returns the index of the key
             * @return The index
             */
            int index,

            /**
             * Returns the default value
             * @return The default value
             */
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

    /**
     * Reads {@value #CONFIG_FILE} from the classpath and applies any JUL logging properties it contains.
     * Runs once at class load via the outer {@code static} block.
     * Supported properties alongside the standard {@code org.ujorm.*} keys:
     * <ul>
     *   <li>{@code handlers} — comma-separated handler class names added to the root logger</li>
     *   <li>{@code <logger-name>.level} — level for a specific logger</li>
     *   <li>{@code java.util.logging.SimpleFormatter.format} — log line format</li>
     * </ul>
     * Handler classes are loaded via {@code Config.class.getClassLoader()}, which avoids
     * the {@link java.util.logging.LogManager} lazy-loading path that uses the system
     * classloader and fails in forked Maven Surefire JVMs.
     *
     * @see org.ujorm.tools.logging.UjormConsoleHandler
     */
    private static class JulConfig {

        static void init() {
            try (var stream = Config.class.getResourceAsStream("/" + CONFIG_FILE)) {
                if (stream == null) return;
                var props = new Properties();
                props.load(stream);
                apply(props);
            } catch (Exception e) {
                // Logging config is best-effort — never block startup
            }
        }

        @SuppressWarnings("unchecked")
        private static void apply(Properties props) throws Exception {
            // SimpleFormatter reads this system property at construction time
            var format = props.getProperty("java.util.logging.SimpleFormatter.format");
            if (format != null) {
                System.setProperty("java.util.logging.SimpleFormatter.format", format);
            }

            // Install handlers on root logger using Config's own classloader (not the system CL)
            var handlersValue = props.getProperty("handlers", "").trim();
            if (!handlersValue.isEmpty()) {
                var root = Logger.getLogger("");
                for (var h : root.getHandlers()) root.removeHandler(h);
                var cl = Config.class.getClassLoader();
                for (var name : handlersValue.split("[,\\s]+")) {
                    name = name.trim();
                    if (!name.isEmpty()) {
                        var handlerClass = (Class<? extends Handler>) Class.forName(name, true, cl);
                        root.addHandler(handlerClass.getDeclaredConstructor().newInstance());
                    }
                }
            }

            // Apply individual logger levels (e.g. org.ujorm.tools.jdbc.AbstractSqlQuery.level = INFO)
            for (var entry : props.entrySet()) {
                var key = ((String) entry.getKey()).trim();
                if (key.endsWith(".level")) {
                    var loggerName = key.substring(0, key.length() - ".level".length());
                    Logger.getLogger(loggerName).setLevel(Level.parse(((String) entry.getValue()).trim()));
                }
            }
        }
    }

    /** Build an immutable object with default arguments */
    public static Config ofDefault() {
        return new Config().lock();
    }

    /** Factory method for logging SQL with INFO level including configuration. */
    public static Config ofSqlInfoWithParams(boolean logSqlParams) {
        var result = new Config()
                .setValue(Config.logSqlLevel, Level.INFO)
                .setValue(Config.logSqlParams, logSqlParams)
                .setValue(Config.logConfigValues, true);
        return result.lock();
    }

}