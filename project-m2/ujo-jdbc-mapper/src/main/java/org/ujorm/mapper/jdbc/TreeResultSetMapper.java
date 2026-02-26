package org.ujorm.mapper.jdbc;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.Key;
import org.ujorm.tools.jdbc.JdbcUtils;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Maps a database ResultSet to a hierarchical Bean structure using a pre-compiled mapping tree.
 *
 * @param <D> the root domain type
 */
public class TreeResultSetMapper<D> {

    @NonNull
    private final Class<D> domainClass;
    @NonNull
    private final DomainHandlerService service;
    @NonNull
    private final DomainHandler<D> rootHandler;
    @Nullable
    private MappingNode<D> rootNode;

    /**
     * Constructs the mapper. The mapping tree is initialized lazily.
     *
     * @param domainClass the class of the root domain object
     * @param service the domain handler service for instance creation
     */
    protected TreeResultSetMapper(
            @NonNull Class<D> domainClass,
            @NonNull DomainHandlerService service
    ) {
        this.domainClass = domainClass;
        this.service = service;
        this.rootHandler = service.getHandler(domainClass);
    }

    /**
     * Converts the given Stream of ResultSets into a stream of domain objects.
     *
     * @param rs the Stream of ResultSets to process
     * @param columns optional explicitly defined column aliases
     * @return a stream of populated domain objects
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if explicit columns don't match the ResultSet metadata
     */
    @NotNull
    public Stream<D> convert(@NotNull Stream<ResultSet> rs, @Nullable String... columns) throws SQLException {
        return rs.map(row -> {
            try {
                if (this.rootNode == null) {
                    var actualColumns = columns;
                    if (actualColumns == null || actualColumns.length == 0) {
                        actualColumns = getAliasColumns(row);
                    } else {
                        var metaCount = row.getMetaData().getColumnCount();
                        if (actualColumns.length != metaCount) {
                            throw new IllegalArgumentException("Column count mismatch between aliases and ResultSet.");
                        }
                    }
                    this.rootNode = buildMappingTree(this.domainClass, actualColumns);
                }

                var result = rootHandler.newDomain();
                populateNode(this.rootNode, result, row);
                return result;
            } catch (SQLException ex) {
                throw SQLExceptionBuilder.build("Failed to map ResultSet row to domain object", ex);
            }
        });
    }

    /**
     * Converts the given ResultSet into a stream of domain objects.
     *
     * @param rs the ResultSet to process
     * @param columns optional explicitly defined column aliases
     * @return a stream of populated domain objects
     * @throws SQLException if a database error occurs
     * @throws IllegalArgumentException if explicit columns don't match the ResultSet metadata
     */
    @NotNull
    public Stream<D> convert(@NotNull ResultSet rs, @Nullable String... columns) throws SQLException {
        return convert(JdbcUtils.stream(rs), columns);
    }

    /**
     * Recursively populates the target bean and its relations.
     *
     * @param node the current mapping node
     * @param target the target bean to populate
     * @param rs the database result set
     * @param <T> the type of the target bean
     * @throws SQLException if a database error occurs
     */
    private <T> void populateNode(MappingNode<T> node, T target, ResultSet rs) throws SQLException {
        for (var mapping : node.directMappings()) {
            var value = extractValue(rs, mapping);
            mapping.key().setValue(target, value);
        }

        for (var relation : node.relations()) {
            var childKey = relation.childKey();
            var childNode = relation.childNode();

            var childInstance = childKey.getValue(target);
            if (childInstance == null) {
                childInstance = createInstance(childKey.getType());
                childKey.setValue(target, childInstance);
            }

            populateNode(childNode, childInstance, rs);
        }
    }

    /**
     * Extracts a value from the ResultSet based on the mapping definition using column index.
     *
     * @param rs the result set
     * @param mapping the direct mapping containing the column index and key
     * @param <V> the type of the value
     * @return the extracted value
     * @throws SQLException if a database error occurs
     */
    private <V> V extractValue(ResultSet rs, DirectMapping<?, V> mapping) throws SQLException {
        return rs.getObject(mapping.columnIndex(), mapping.key().getType());
    }

    /**
     * Creates a new instance for a related bean.
     *
     * @param type the class of the related bean
     * @param <T> the type of the related bean
     * @return a new instance of the related bean
     */
    private <T> T createInstance(Class<T> type) {
        return service.createInstance(type);
    }

    /**
     * Builds the internal tree structure from the flat column definitions.
     *
     * @param rootClass the root domain class
     * @param columnAliases the column aliases
     * @return the root mapping node
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private MappingNode<D> buildMappingTree(@NonNull Class<D> rootClass, @NotNull String... columnAliases) {
        var result = new MappingNode<D>();

        for (var colIndex = 0; colIndex < columnAliases.length; colIndex++) {
            var alias = columnAliases[colIndex];
            var parts = alias.split("\\.");
            var currentNode = (MappingNode) result;
            var currentClass = (Class<?>) rootClass;

            for (var i = 0; i < parts.length; i++) {
                var part = parts[i];
                var isLast = (i == parts.length - 1);
                var key = findKey(currentClass, part);

                if (isLast) {
                    currentNode.directMappings().add(new DirectMapping<>(key, colIndex + 1));
                } else {
                    var existingRelation = (RelationMapping) null;
                    for (var relObj : currentNode.relations()) {
                        var rel = (RelationMapping) relObj;
                        if (rel.childKey().getName().equals(key.getName())) {
                            existingRelation = rel;
                            break;
                        }
                    }

                    if (existingRelation != null) {
                        currentNode = existingRelation.childNode();
                    } else {
                        var childNode = new MappingNode<>();
                        currentNode.relations().add(new RelationMapping<>(key, childNode));
                        currentNode = childNode;
                    }
                    currentClass = key.getType();
                }
            }
        }

        return result;
    }

    /**
     * Finds a property Key by its name within the given domain class.
     *
     * @param domainType the domain class to inspect
     * @param keyName the name of the property
     * @param <T> the type of the domain class
     * @return the property Key
     * @throws IllegalArgumentException if the key is not found
     */
    private <T> Key<T, Object> findKey(Class<T> domainType, String keyName) {
        return service.getHandler(domainType).getKey(keyName);
    }

    // --- Internal structures to represent the tree ---

    /** Represents a node in the mapping tree structure. */
    private record MappingNode<T>(
            List<DirectMapping<T, Object>> directMappings,
            List<RelationMapping<T, Object>> relations
    ) {
        public MappingNode() {
            this(new ArrayList<>(), new ArrayList<>());
        }
    }

    /** Represents a direct mapping from a ResultSet column to a Bean property. */
    private record DirectMapping<T, V>(Key<T, V> key, int columnIndex) {}

    /** Represents a relation mapping to a child Bean. */
    private record RelationMapping<PARENT, CHILD>(Key<PARENT, CHILD> childKey, MappingNode<CHILD> childNode) {}

    // --- Factory Method(s) & Statics ---

    /**
     * Factory method to create a new instance.
     *
     * @param domainClass the root domain class
     * @param service the domain handler service
     * @param <D> the root domain type
     * @return a new instance of TreeResultSetMapper
     */
    public static <D> TreeResultSetMapper<D> of(
            @NonNull Class<D> domainClass,
            @NonNull DomainHandlerService service) {
        return new TreeResultSetMapper<>(domainClass, service);
    }

    /**
     * Factory method to create a new instance.
     *
     * @param domainClass the root domain class
     * @param <D> the root domain type
     * @return a new instance of TreeResultSetMapper
     */
    public static <D> TreeResultSetMapper<D> of(@NonNull Class<D> domainClass) {
        return new TreeResultSetMapper<>(domainClass, DomainHandlerProvider.provider());
    }

    /**
     * Extracts column aliases from the ResultSet metadata.
     *
     * @param rs the database result set
     * @return an array of column aliases
     */
    private static String[] getAliasColumns(ResultSet rs) {
        try {
            var metaData = rs.getMetaData();
            var columnCount = metaData.getColumnCount();
            var result = new String[columnCount];
            for (var i = 1; i <= columnCount; i++) {
                result[i - 1] = metaData.getColumnLabel(i);
            }
            return result;
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to extract column metadata", ex);
        }
    }
}