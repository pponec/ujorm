package org.ujorm.mapper.jdbc;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.Key;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps a database ResultSet to a hierarchical Bean structure using a pre-compiled mapping tree.
 *
 * @param <D> the root domain type
 */
public class TreeResultSetMapper<D> {

    @NonNull
    private final DomainHandlerService service;
    @NonNull
    private final DomainHandler<D> rootHandler;
    @NotNull
    private final MappingNode<D> rootNode;
    @NotNull
    private final String[] columnAliases;

    /**
     * Constructs the mapper and initializes the mapping tree.
     *
     * @param domainClass the class of the root domain object
     * @param service the domain handler service for instance creation
     * @param columnAliases the array of database column aliases
     */
    public TreeResultSetMapper(
            @NonNull Class<D> domainClass,
            @NonNull DomainHandlerService service,
            @NonNull String... columnAliases
    ) {
        this.service = service;
        this.rootHandler = service.getHandler(domainClass);
        this.columnAliases = columnAliases;
        this.rootNode = buildMappingTree();
    }

    /**
     * Converts the current row of the given ResultSet into a domain object.
     *
     * @param rs the ResultSet pointing to the current row
     * @return the populated domain object
     * @throws SQLException if a database error occurs
     */
    public D convert(ResultSet rs) throws SQLException {
        var result = rootHandler.newDomain();
        populateNode(rootNode, result, rs);
        return result;
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
        // Map direct properties (leaf nodes)
        for (var mapping : node.getDirectMappings()) {
            var value = extractValue(rs, mapping);
            mapping.getKey().setValue(target, value);
        }

        // Process child relations
        for (var relation : node.getRelations()) {
            var childKey = relation.getChildKey();
            var childNode = relation.getChildNode();

            // Check if the relation object already exists
            var childInstance = childKey.getValue(target);
            if (childInstance == null) {
                childInstance = createInstance(childKey.getType());
                childKey.setValue(target, childInstance);
            }

            // Recursively populate the child
            populateNode(childNode, childInstance, rs);
        }
    }

    /**
     * Extracts a value from the ResultSet based on the mapping definition.
     *
     * @param rs the result set
     * @param mapping the direct mapping containing the column alias and key
     * @param <V> the type of the value
     * @return the extracted value
     * @throws SQLException if a database error occurs
     */
    private <V> V extractValue(ResultSet rs, DirectMapping<?, V> mapping) throws SQLException {
        return (V) rs.getObject(mapping.getColumnAlias(), mapping.getKey().getType());
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
     * @return the root mapping node
     */
    private MappingNode<D> buildMappingTree() {
        throw new UnsupportedOperationException("TODO: Implement mapping tree initialization from columnAliases.");
    }

    /**
     * Extracts column aliases from the ResultSet metadata.
     *
     * @param rs the database result set
     * @return an array of column aliases
     * @throws SQLException if a database error occurs
     */
    public static String[] extractColumnAliases(ResultSet rs) throws SQLException {
        var metaData = rs.getMetaData();
        var columnCount = metaData.getColumnCount();
        var result = new String[columnCount];

        for (var i = 1; i <= columnCount; i++) {
            // JDBC indexes are 1-based
            result[i - 1] = metaData.getColumnLabel(i);
        }

        return result;
    }

    // --- Internal structures to represent the tree ---

    /**
     * Represents a node in the mapping tree structure.
     */
    private static class MappingNode<T> {
        private final List<DirectMapping<T, Object>> directMappings = new ArrayList<>();
        private final List<RelationMapping<T, Object>> relations = new ArrayList<>();

        public List<DirectMapping<T, Object>> getDirectMappings() {
            return directMappings;
        }

        public List<RelationMapping<T, Object>> getRelations() {
            return relations;
        }
    }

    /**
     * Represents a direct mapping from a ResultSet column to a Bean property.
     */
    private static class DirectMapping<T, V> {
        private final Key<T, V> key;
        private final String columnAlias;

        public DirectMapping(Key<T, V> key, String columnAlias) {
            this.key = key;
            this.columnAlias = columnAlias;
        }

        public Key<T, V> getKey() {
            return key;
        }

        public String getColumnAlias() {
            return columnAlias;
        }
    }

    /**
     * Represents a relation mapping to a child Bean.
     */
    private static class RelationMapping<PARENT, CHILD> {
        private final Key<PARENT, CHILD> childKey;
        private final MappingNode<CHILD> childNode;

        public RelationMapping(Key<PARENT, CHILD> childKey, MappingNode<CHILD> childNode) {
            this.childKey = childKey;
            this.childNode = childNode;
        }

        public Key<PARENT, CHILD> getChildKey() {
            return childKey;
        }

        public MappingNode<CHILD> getChildNode() {
            return childNode;
        }
    }

    public static <D> TreeResultSetMapper of(
            @NonNull Class<D> domainClass,
            @NonNull DomainHandlerService service,
            @NonNull ResultSet rs) throws SQLException {
        return new TreeResultSetMapper(domainClass, service, getAliasColumns(rs));
    }

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
            throw new org.ujorm.tools.jdbc.SQLException(ex);
        }
    }
}