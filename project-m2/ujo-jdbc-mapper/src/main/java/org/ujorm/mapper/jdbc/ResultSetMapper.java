package org.ujorm.mapper.jdbc;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.Key;
import org.ujorm.core.csv.CsvLineSplitter;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.tools.common.Primitive;
import org.ujorm.tools.jdbc.JdbcUtils;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Maps a database {@link java.sql.ResultSet} to a hierarchical Bean structure using a pre-compiled mapping tree.
 * <h3>How Mapping Works</h3>
 * <p>
 * The mapper converts each row of a {@code ResultSet} into a domain object of type {@code D}.
 * It uses column labels (which may contain dot notation, e.g., "address.city") to navigate,
 * instantiate, and populate the hierarchical relation tree. To ensure high performance, it translates
 * the flat column definitions into an internal {@code MappingNode} tree structure. This tree is
 * cached and reused for subsequent result sets that share the exact same column layout.
 * </p>
 * <h3>Relation Instantiation</h3>
 * <p>
 * When mapping hierarchical relations, the mapper evaluates data availability from the bottom up.
 * A child domain object is instantiated and assigned to its parent if and only if the {@code ResultSet}
 * provides at least one non-null value for any of the child's mapped properties. If all columns mapped
 * to a specific relation return {@code null}, the property in the parent object will simply remain {@code null}.
 * </p>
 * * <h3>Default Behavior</h3>
 * <p>
 * By default, the mapper dynamically reads {@link java.sql.ResultSetMetaData} to extract column labels
 * and detect database column markers. The constructed mapping trees are stored in an internal
 * concurrent cache with a default maximum capacity of 512 entries.
 * </p>
 * <h3>Customizing Behavior & Performance Impact</h3>
 * <ul>
 * <li><b>Explicit Column Labels:</b> You can provide explicit column labels or {@link org.ujorm.core.Key}s
 * via the {@code convert} methods.
 * <i>Speed:</i> This completely bypasses the JDBC metadata query. Since extracting metadata can
 * be a heavy network or processing operation depending on the JDBC driver, providing explicit labels
 * significantly speeds up the initialization phase.
 * <i>Memory:</i> Reduces temporary object allocations by avoiding metadata instantiation.</li>
 * <li><b>Cache Size:</b> The cache size can be adjusted using the {@code ujorm.mapper.cache.size}
 * system property or the specific factory method {@code of(Class, DomainHandlerService, int)}.
 * <i>Speed:</i> A properly sized cache prevents the costly repetitive parsing of dot-notation paths
 * and rebuilding of mapping trees. If the application executes more unique queries than the cache
 * capacity, the entire cache is cleared, causing a temporary performance degradation.
 * <i>Memory:</i> Larger caches hold more mapping structures in the heap. Tuning this value allows
 * you to balance between fast mapping execution and memory consumption.</li>
 * </ul>
 *
 * @param <D> the root domain type
 */
public class ResultSetMapper<D> {
    private static final Logger LOGGER = Logger.getLogger(ResultSetMapper.class.getName());

    /** The very fast dot splitter */
    private static final CsvLineSplitter SPLITTER = CsvLineSplitter.ofFast('.');
    private static final int SPLITTER_INIT_CAPACITY = 8;
    private static final String MAPPER_CACHE_SIZE = "ujorm.mapper.cache.size";
    private static final int DEFAULT_CACHE_SIZE = Integer.getInteger(MAPPER_CACHE_SIZE, 512);

    @NonNull
    private final Class<D> domainClass;
    @NonNull
    private final DomainHandlerService service;
    @NonNull
    private final DomainHandler<D> rootHandler;
    @NonNull
    private final MappingCache<D> cache;

    /**
     * Constructs the mapper.
     *
     * @param domainClass the class of the root domain object
     * @param service the domain handler service for instance creation
     * @param maxCacheSize the maximum number of cached mapping trees
     */
    protected ResultSetMapper(
            @NonNull Class<D> domainClass,
            @NonNull DomainHandlerService service,
            int maxCacheSize
    ) {
        this.domainClass = domainClass;
        this.service = service;
        this.rootHandler = service.getHandler(domainClass);
        this.cache = new MappingCache<>(maxCacheSize);
    }

    /**
     * Converts the given Stream of ResultSets into a stream of domain objects.
     *
     * @param rs the Stream of ResultSets to process
     * @param columnLabels optional explicitly defined column labels
     * @return a stream of populated domain objects
     * @throws NoSuchElementException if explicit columns don't match the ResultSet metadata
     */
    @NotNull
    public Stream<D> convert(@NotNull Stream<ResultSet> rs, @Nullable CharSequence... columnLabels) {
        return rs.map(resultSet -> {
            try {
                var extracted = getLabelColumns(resultSet, columnLabels);
                var key = new CacheKey(extracted);
                var node = cache.getOrCreate(key, k ->
                        buildMappingTree(k.columns()));
                var result = rootHandler.newUjoDomain();
                populateNode(node, result, resultSet);
                return result.buildDomain();
            } catch (SQLException ex) {
                throw SQLExceptionBuilder.build("Failed to map ResultSet row to domain object", ex);
            }
        });
    }

    /**
     * Converts the given ResultSet into a stream of domain objects.
     *
     * @param rs the ResultSet to process
     * @param columnLabels optional explicitly defined column labels
     * @return a stream of populated domain objects
     */
    @NotNull
    public Stream<D> convert(@NotNull ResultSet rs, @Nullable CharSequence... columnLabels) {
        return convert(JdbcUtils.stream(rs), columnLabels);
    }

    /**
     * Type-safe mapping using Keys for a selection without relations.
     *
     * @param rs A stream of ResultSets to process
     * @param columnLabels Explicitly defined column keys (labels)
     * @return A stream of populated domain objects
     */
    @SafeVarargs
    @NotNull
    public final Stream<D> convertFlat(@NotNull Stream<ResultSet> rs, @NotNull Key<D, ?>... columnLabels) {
        return convert(rs, columnLabels);
    }

    /** Get the last timestamp of the cache clearing */
    public Instant getCacheCleared() {
        return cache.getLastCleared();
    }

    /**
     * Recursively populates the target AbstractUjo wrapper and its relations.
     * @return true if at least one non-null value was set in this node or its children
     */
    private <D2> boolean populateNode(MappingNode<D2> node, AbstractUjo<D2> target, ResultSet rs) throws SQLException {
        var hasData = false;
        for (var mapping : node.directMappings()) {
            var objectType = Primitive.wrapPrimitive(mapping.key().type());
            var value = rs.getObject(mapping.columnIndex(), objectType);
            if (value != null) {
                target.setValue(mapping.key(), value);
                hasData = true;
            }
        }
        for (var relation : node.relations()) {
            var childKey = relation.childKey();
            var childHandler = service.getHandler(childKey.type());
            var childTarget = childHandler.newUjoDomain();

            if (populateNode(relation.childNode(), childTarget, rs)) {
                target.setValue(childKey, childTarget.buildDomain());
                hasData = true;
            }
        }
        return hasData;
    }

    /**
     * Builds the internal tree structure from the flat column definitions.
     */
    private MappingNode<D> buildMappingTree(@NotNull List<ColumnMetadata> columns) {
        var result = new MappingNode<D>();
        for (var i = 0; i < columns.size(); i++) {
            var col = columns.get(i);
            var parts = SPLITTER.split(col.label(), SPLITTER_INIT_CAPACITY);
            buildPath(result, this.domainClass, parts, i + 1, col.isDbColumn());
        }
        return result;
    }

    /**
     * Internal method to build a path for a single column.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void buildPath(MappingNode node, Class<?> currentClass, String[] parts, int colIdx, boolean byColumn) {
        var currentNode = node;
        var clazz = currentClass;

        for (var i = 0; i < parts.length; i++) {
            var isLast = (i == parts.length - 1);
            var key = findKey(clazz, parts[i], byColumn);

            if (isLast) {
                if (key.foreignKey()) {
                    var relationNode = currentNode.getOrCreateRelation(key);
                    var targetClass = key.type();
                    var pkKey = findPrimaryKey(targetClass);
                    relationNode.addMapping(pkKey, colIdx);
                } else {
                    currentNode.addMapping(key, colIdx);
                }
            } else {
                currentNode = currentNode.getOrCreateRelation(key);
                clazz = key.type();
            }
        }
    }

    /**
     * Finds a property Key by its name within the given domain class.
     */
    private <D2> Key<D2, Object> findKey(Class<D2> domainType, String keyName, boolean byColumn) {
        var handler = service.getHandler(domainType);
        if (byColumn) {
            var columnKey = handler.getKeyByColumn(keyName, false, null);
            if (columnKey != null) {
                return columnKey;
            }
        }
        return handler.getKey(keyName);
    }

    /**
     * Finds the primary key for the given domain class.
     *
     * @param domainType The domain class to find the primary key for
     * @param <D2> The domain type
     * @return The primary key of the domain object
     * @throws IllegalStateException If the primary key is not found
     */
    @SuppressWarnings("unchecked")
    private <D2> Key<D2, Object> findPrimaryKey(Class<D2> domainType) {
        var handler = service.getHandler(domainType);
        for (var key : handler.getKeyList()) {
            if (key.primaryKey()) {
                return (Key<D2, Object>) key;
            }
        }
        throw new IllegalStateException("Primary key not found for class: " + domainType.getName());
    }

    // --- Inner classes ---

    /** Cache manager for mapping trees. */
    private static final class MappingCache<D> {
        private final int maxCacheSize;
        private final ConcurrentMap<CacheKey, MappingNode<D>> data = new ConcurrentHashMap<>();
        private volatile Instant lastCleared = Instant.now();

        private MappingCache(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }

        /** Returns a cached node or creates a new one. */
        public MappingNode<D> getOrCreate(CacheKey key, Function<CacheKey, MappingNode<D>> builder) {
            if (data.size() >= maxCacheSize) {
                checkAndClear();
            }
            return data.computeIfAbsent(key, builder);
        }

        /** Clears the cache if the limit is exceeded. */
        private synchronized void checkAndClear() {
            if (data.size() >= maxCacheSize) {
                var msg = String.join(" ",
                        "Mapping cache exceeded the limit of %d, clearing.",
                        "Consider increasing '%s' to avoid performance degradation."
                ).formatted(maxCacheSize, MAPPER_CACHE_SIZE);
                LOGGER.log(Level.WARNING, msg);
                data.clear();
                lastCleared = Instant.now();
            }
        }

        /** Returns the last clear timestamp. */
        public Instant getLastCleared() {
            return lastCleared;
        }
    }

    /**
     * Represents a node in the mapping tree structure.
     * @param <D2> Domain type
     */
    private record MappingNode<D2>(
            /** Gets the list of direct column mappings for the current node. */
            List<DirectMapping<D2, Object>> directMappings,

            /** Gets the list of relation mappings to child nodes. */
            List<RelationMapping<D2, Object>> relations
    ) {
        public MappingNode() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        /** Adds a direct mapping to this node. */
        public void addMapping(Key<D2, Object> key, int columnIndex) {
            directMappings.add(new DirectMapping<>(key, columnIndex));
        }

        /** Gets an existing relation or creates a new one. */
        @SuppressWarnings("unchecked")
        public <C> MappingNode<C> getOrCreateRelation(Key<D2, C> key) {
            for (var rel : relations) {
                if (rel.childKey().name().equals(key.name())) {
                    return (MappingNode<C>) rel.childNode();
                }
            }
            var childNode = new MappingNode<C>();
            relations.add(new RelationMapping(key, childNode));
            return childNode;
        }
    }

    /**
     * Represents a direct mapping from a ResultSet column to a Bean property.
     *
     * @param <D2> Domain type
     * @param <V> Value type
     */
    private record DirectMapping<D2, V>(
            /** Gets the property key. */
            Key<D2, V> key,
            /** Gets the column index. */
            int columnIndex
    ) {}

    /**
     * Represents a relation mapping to a child Bean.
     *
     * @param <D2> Domain type
     * @param <CHILD> Child type
     */
    private record RelationMapping<D2, CHILD>(
            /** Gets the key for the child relation. */
            Key<D2, CHILD> childKey,

            /** Gets the mapping node for the child. */
            MappingNode<CHILD> childNode
    ) {}

    /** Metadata for a single column. */
    private record ColumnMetadata(
            /** Gets the column label. */
            String label,

            /** Gets whether it is a database column name. */
            boolean isDbColumn
    ) {}

    /** Cache key based on column metadata. */
    private record CacheKey(
            /** Gets the list of column metadata. */
            List<ColumnMetadata> columns
    ) {}

    // --- Static methods ---

    /** Extracts column labels and markers from the ResultSet metadata or uses explicit ones. */
    private static List<ColumnMetadata> getLabelColumns(ResultSet rs, CharSequence... explicitLabels) throws SQLException {
        var result = new ArrayList<ColumnMetadata>();
        if (explicitLabels != null && explicitLabels.length > 0) {
            var metaCount = rs.getMetaData().getColumnCount();
            if (explicitLabels.length != metaCount) {
                throw new IllegalArgumentException("Column count mismatch between labels and ResultSet.");
            }
            for (var label : explicitLabels) {
                result.add(new ColumnMetadata(label.toString(), false));
            }
            return result;
        }

        var metaData = rs.getMetaData();
        var columnCount = metaData.getColumnCount();
        for (var i = 1; i <= columnCount; i++) {
            var label = metaData.getColumnLabel(i);
            var name = metaData.getColumnName(i);
            var isDb = label != null && label.equalsIgnoreCase(name);
            result.add(new ColumnMetadata(label, isDb));
        }
        return result;
    }

    /** Factory method to create a new instance with a custom cache size. */
    public static <D> ResultSetMapper<D> of(@NonNull Class<D> domainClass, @NonNull DomainHandlerService service, int maxCacheSize) {
        return new ResultSetMapper<>(domainClass, service, maxCacheSize);
    }

    /** Factory method to create a new instance with default cache size. */
    public static <D> ResultSetMapper<D> of(@NonNull Class<D> domainClass, @NonNull DomainHandlerService service) {
        return of(domainClass, service, DEFAULT_CACHE_SIZE);
    }

    /** Factory method to create a new instance with default service and cache size. */
    public static <D> ResultSetMapper<D> of(@NonNull Class<D> domainClass) {
        return of(domainClass, DomainHandlerProvider.provider(), DEFAULT_CACHE_SIZE);
    }
}