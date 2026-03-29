package org.ujorm.orm.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.*;
import org.ujorm.core.csv.CsvLineSplitter;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.orm.Config;
import org.ujorm.tools.common.Primitive;
import org.ujorm.tools.jdbc.JdbcUtils;
import org.ujorm.tools.jdbc.AbstractSqlQuery.SqlFunction;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
 *
 * @param <D> the root domain type
 */
public final class ResultSetMapper<D> {

    private static final Logger LOGGER = Logger.getLogger(ResultSetMapper.class.getName());

    /** The very fast dot splitter */
    private static final CsvLineSplitter SPLITTER = CsvLineSplitter.ofFast('.');
    private static final int SPLITTER_INIT_CAPACITY = 8;

    @NotNull
    private final Class<D> domainClass;
    @NotNull
    private final DomainHandlerService service;
    @NotNull
    private final DomainHandler<D> rootHandler;
    @NotNull
    private final MappingCache<D> cache;

    /** Constructs the mapper. */
    private ResultSetMapper(
            @NotNull Class<D> domainClass,
            @NotNull DomainHandlerService service,
            int maxCacheSize
    ) {
        this.domainClass = domainClass;
        this.service = service;
        this.rootHandler = service.getHandler(domainClass);
        this.cache = new MappingCache<>(maxCacheSize);
    }

    /**
     * Creates a stateful mapping function for efficient stream processing.
     * Returns SqlFunction to be compatible with SqlQuery.
     */
    public @NotNull SqlFunction<ResultSet, D> mapper(@Nullable CharSequence... columnLabels) {
        return new RowContext(columnLabels)::map;
    }

    /** Maps a single row of a ResultSet. */
    public @NotNull D map(@NotNull ResultSet resultSet, @Nullable CharSequence... columnLabels) {
        return this.mapper(columnLabels).apply(resultSet);
    }

    /** Converts the given Stream of ResultSets into a stream of domain objects. */
    public @NotNull Stream<D> convert(@NotNull Stream<ResultSet> rs, @Nullable CharSequence... columnLabels) {
        var mapper = this.mapper(columnLabels);
        return rs.map(mapper::apply);
    }

    /** Converts the given ResultSet into a stream of domain objects. */
    public @NotNull Stream<D> convert(@NotNull ResultSet rs, @Nullable CharSequence... columnLabels) {
        return convert(JdbcUtils.stream(rs), columnLabels);
    }

    /** Type-safe mapping using Keys for a selection without relations. */
    @SafeVarargs
    public final @NotNull Stream<D> convertFlat(@NotNull Stream<ResultSet> rs, @NotNull Key<D, ?>... columnLabels) {
        return convert(rs, columnLabels);
    }

    /** Get the last timestamp of the cache clearing */
    public Instant getCacheCleared() {
        return cache.getLastCleared();
    }

    /**
     * Recursively populates the target Ujo wrapper and its relations.
     * @return true if at least one non-null value was set in this node or its children
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private <D2> boolean populateNode(
            MappingNode<D2> node,
            Ujo<D2> target,
            ResultSet rs,
            Map<MappingNode<?>, AbstractUjo<?>> relationCache
    ) throws SQLException {
        var hasData = false;
        for (var mapping : node.directMappings()) {
            var targetType = mapping.key().type();
            var value = targetType.isEnum()
                    ? getEnumValue(rs, mapping, (Class) targetType)
                    : rs.getObject(mapping.columnIndex(), Primitive.wrapPrimitive(targetType));
            if (value != null) {
                target.setValue(mapping.key(), value);
                hasData = true;
            }
        }

        for (var relation : node.relations()) {
            var childKey = relation.childKey();
            var childNode = relation.childNode();

            var childTarget = (AbstractUjo<Object>) relationCache.get(childNode);
            if (childTarget == null) {
                var childHandler = service.getHandler(childKey.type());
                childTarget = AbstractUjo.of(childHandler);
                relationCache.put(childNode, childTarget);
            } else {
                childTarget.reset();
            }

            if (populateNode(childNode, childTarget, rs, relationCache)) {
                target.setValue(childKey, childTarget.buildDomain());
                hasData = true;
            }
        }
        return hasData;
    }

    private <D2> @Nullable Object getEnumValue(
            @NotNull ResultSet rs,
            @NotNull DirectMapping<D2, Object> mapping,
            @NotNull Class targetType) throws SQLException {
        var rawValue = rs.getObject(mapping.columnIndex());
        if (rawValue == null) {
            return null;
        } else if (rawValue instanceof String str) {
            return service.getEnumMapper().getByName(targetType, str);
        } else if (rawValue instanceof Integer
                || rawValue instanceof Long
                || rawValue instanceof Short
                || rawValue instanceof Byte) {
            return service.getEnumMapper().getByIndex(targetType, ((Number) rawValue).intValue());
        } else {
            throw new IllegalArgumentException(String.format(
                    "Unsupported DB type '%s' for Enum mapping of column index %d.",
                    rawValue.getClass().getSimpleName(),
                    mapping.columnIndex()
            ));
        }
    }

    /** Builds the internal tree structure from the flat column definitions. */
    private MappingNode<D> buildMappingTree(@NotNull List<ColumnMetadata> columns) {
        var result = new MappingNode<D>();
        for (var i = 0; i < columns.size(); i++) {
            var col = columns.get(i);
            var parts = SPLITTER.split(col.label(), SPLITTER_INIT_CAPACITY);
            buildPath(result, this.domainClass, parts, i + 1, col.isDbColumn());
        }
        return result;
    }

    /** Internal method to build a path for a single column. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void buildPath(MappingNode node, Class<?> currentClass, String[] parts, int colIdx, boolean byColumn) {
        var currentNode = node;
        var clazz = currentClass;

        for (var i = 0; i < parts.length; i++) {
            var isLast = (i == parts.length - 1);
            var key = findKey(clazz, parts[i], byColumn);

            if (isLast && !key.foreignKey()) {
                currentNode.addMapping(key, colIdx);
            } else {
                currentNode = currentNode.getOrCreateRelation(key);
                if (isLast) {
                    currentNode.addMapping(findPrimaryKey(key.type()), colIdx);
                } else {
                    clazz = key.type();
                }
            }
        }
    }

    /** Finds a property Key by its name within the given domain class. */
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

    /** Finds the primary key for the given domain class. */
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

    private static final class MappingCache<D> {
        private final int maxCacheSize;
        private final ConcurrentMap<CacheKey, MappingNode<D>> data = new ConcurrentHashMap<>();
        private volatile Instant lastCleared = Instant.now();

        private MappingCache(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
        }

        public MappingNode<D> getOrCreate(CacheKey key, java.util.function.Function<CacheKey, MappingNode<D>> builder) {
            if (data.size() >= maxCacheSize) {
                checkAndClear();
            }
            return data.computeIfAbsent(key, builder);
        }

        private synchronized void checkAndClear() {
            if (data.size() >= maxCacheSize) {
                LOGGER.log(Level.WARNING, "Mapping cache exceeded limit, clearing.");
                data.clear();
                lastCleared = Instant.now();
            }
        }

        public Instant getLastCleared() {
            return lastCleared;
        }
    }

    private record MappingNode<D2>(
            /** Gets the direct mappings. */
            List<DirectMapping<D2, Object>> directMappings,
            /** Gets the relations. */
            List<RelationMapping<D2, Object>> relations
    ) {
        public MappingNode() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        public void addMapping(Key<D2, Object> key, int columnIndex) {
            directMappings.add(new DirectMapping<>(key, columnIndex));
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <C> MappingNode<C> getOrCreateRelation(Key<D2, C> key) {
            for (var rel : relations) {
                if (rel.childKey().name().equals(key.name())) {
                    return (MappingNode<C>) rel.childNode();
                }
            }
            var childNode = new MappingNode<C>();
            ((List) relations).add(new RelationMapping<>(key, childNode));
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

    private final class RowContext {
        private final CharSequence[] columnLabels;
        private MappingNode<D> rootNode;
        private AbstractUjo<D> rootUjo;
        private final Map<MappingNode<?>, AbstractUjo<?>> relationCache = new IdentityHashMap<>();

        RowContext(CharSequence[] columnLabels) {
            this.columnLabels = columnLabels;
        }

        public D map(ResultSet resultSet) throws SQLException {
            if (rootNode == null) {
                var extracted = getLabelColumns(resultSet, columnLabels);
                var key = new CacheKey(extracted);
                rootNode = cache.getOrCreate(key, k -> buildMappingTree(k.columns()));
                rootUjo = AbstractUjo.of(rootHandler);
            } else {
                rootUjo.reset();
            }
            populateNode(rootNode, rootUjo, resultSet, relationCache);
            return rootUjo.buildDomain();
        }
    }

    // --- Static methods ---

    private static List<ColumnMetadata> getLabelColumns(ResultSet rs, CharSequence... explicitLabels) throws SQLException {
        if (explicitLabels != null && explicitLabels.length > 0) {
            var result = new ArrayList<ColumnMetadata>(explicitLabels.length);
            for (var label : explicitLabels) {
                result.add(new ColumnMetadata(label.toString(), false));
            }
            return result;
        }
        var metaData = rs.getMetaData();
        var columnCount = metaData.getColumnCount();
        var result = new ArrayList<ColumnMetadata>(columnCount);
        for (var i = 1; i <= columnCount; i++) {
            var label = metaData.getColumnLabel(i);
            var name = metaData.getColumnName(i);
            var isDb = label != null && label.equalsIgnoreCase(name);
            result.add(new ColumnMetadata(label, isDb));
        }
        return result;
    }

    public static <D> ResultSetMapper<D> of(@NotNull Class<D> domainClass, @NotNull DomainHandlerService service, int maxCacheSize) {
        return new ResultSetMapper<>(domainClass, service, maxCacheSize);
    }

    public static <D> ResultSetMapper<D> of(@NotNull Class<D> domainClass, @NotNull DomainHandlerService service) {
        return of(domainClass, service, Config.ofDefault().getMaxCacheSize());
    }

    public static <D> ResultSetMapper<D> of(@NotNull Class<D> domainClass, Config config) {
        return of(domainClass, DomainHandlerProvider.provider(), config.getMaxCacheSize());
    }

    public static <D> ResultSetMapper<D> of(@NotNull Class<D> domainClass) {
        return of(domainClass, Config.ofDefault().lock());
    }
}