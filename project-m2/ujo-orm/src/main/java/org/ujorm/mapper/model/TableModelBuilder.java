/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.mapper.model;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.ujorm.DomainHandler;
import org.ujorm.Key;
import org.ujorm.core.generator.TableIdentifier;
import org.ujorm.mapper.Config;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.utils.JdbcTypeProvider;
import org.ujorm.tools.common.StreamUtils;
import org.ujorm.tools.common.StringUtils;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Table Model Builder */
@RequiredArgsConstructor
public class TableModelBuilder<D> {
    private static final Logger LOGGER = Logger.getLogger(TableModelBuilder.class.getName());
    private final DomainHandler<D> handler;
    private final Context ctx;
    private final JdbcTypeProvider jdbcTypeProvider = new JdbcTypeProvider();

    /** Map a database columns where the key is lower-case */
    private Map<String, String> dbColumMapLowerCase;

    public TableModel<D> build(Connection initConnection) {
        var softTableModel = TableIdentifier.of(handler.getDomainClass());
        var realTableModel = createTableIdentifier(softTableModel, initConnection);
        dbColumMapLowerCase = findDatabaseColumnMap(realTableModel, initConnection);
        var columns = handler.getKeyList().stream()
                .map(this::column)
                .toList();
        var pk = findPk(columns);
        var insertedColumns = columns.stream()
                .filter(c -> c != pk)
                .toList();
        var isOracleDb = isOracle(initConnection);
        var quoteChar = getIdentifierQuoteChar(initConnection, ctx.config());
        var jdbc = new Jdbc(isOracleDb, quoteChar);
        var tableName = softTableModel.merge(realTableModel).getQualifiedName();
        return new TableModel(handler, pk, columns, tableName, insertedColumns, jdbc);
    }

    /**
     * Finds the real table identifier from database metadata.
     *
     * @param table domainClass
     * @param initConnection The database connection.
     * @return result - The real table identifier.
     */
    protected TableIdentifier createTableIdentifier(TableIdentifier table, Connection initConnection) {
        var catalog = StringUtils.isFilled(table.catalog()) ? table.catalog() : null;
        var schema = StringUtils.isFilled(table.schema()) ? table.schema() : null;
        var tableName = table.table();

        try {
            var metaData = initConnection.getMetaData();
            try (var resultSet = metaData.getTables(catalog, schema, null, new String[]{"TABLE", "VIEW"})) {
                while (resultSet.next()) {
                    var realTableName = resultSet.getString("TABLE_NAME");
                    if (tableName.equalsIgnoreCase(realTableName)) {
                        var realCatalog = resultSet.getString("TABLE_CAT");
                        var realSchema = resultSet.getString("TABLE_SCHEM");
                        return new TableIdentifier(realTableName, realSchema, realCatalog);
                    }
                }
            }
        } catch (SQLException e) {
            var msg = "Cannot retrieve table metadata for: " + table;
            throw SQLExceptionBuilder.build(msg, e);
        }

        var msg = "No table was found in database for: " + table;
        throw new IllegalStateException(msg);
    }

    /**
     * Finds all database columns for a given table identifier and create map according the lower-case name.
     *
     * @param table The identifier of the table.
     * @param initConnection The connection to the database.
     * @return result - List of column names.
     */
    @NotNull
    private Map<String, String> findDatabaseColumnMap(TableIdentifier table, Connection initConnection) {
        var columns = findDatabaseColumnList(table, initConnection);
        return StreamUtils.map(name -> name.toLowerCase(Locale.ENGLISH), columns);
    }

    /**
     * Finds all database columns for a given table identifier.
     *
     * @param table The identifier of the table.
     * @param initConnection The connection to the database.
     * @return result - List of column names.
     */
    @NotNull
    private static List<String> findDatabaseColumnList(TableIdentifier table, Connection initConnection) {
        var result = new ArrayList<String>();
        try {
            var metaData = initConnection.getMetaData();
            var catalog = StringUtils.isFilled(table.catalog()) ? table.catalog() : null;
            var schema = StringUtils.isFilled(table.schema()) ? table.schema() : null;

            try (var resultSet = metaData.getColumns(catalog, schema, table.table(), null)) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException e) {
            var msg = "Cannot retrieve columns for table: %s"
                    .formatted(table.getQualifiedName());
            throw SQLExceptionBuilder.build(msg, e);
        }

        if (result.isEmpty()) {
            var msg = "Entity has no column in the table %s"
                    .formatted(table);
            throw new IllegalStateException(msg);
        }
        return result;
    }

    /**
     * Determines if the provided connection is directed to an Oracle database.
     *
     * @param connection The database connection to check.
     * @return true if the database product name contains "oracle", false otherwise.
     */
    protected boolean isOracle(Connection connection) {
        try {
            var metaData = connection.getMetaData();
            var productName = metaData.getDatabaseProductName();
            if (productName != null && productName.toLowerCase().contains("oracle")) {
                return true;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Oracle test failed", ex);
        }
        return false;
    }

    /**
     * Retrieves the identifier quote character used by the database connection.
     *
     * @param connection The database connection to check.
     * @param config The current configuration context.
     * @return The identifier quote char.
     */
    protected char getIdentifierQuoteChar(Connection connection, Config config) {
        if (!config.isEnableSqlQuoting()) {
            return ' ';
        }
        try {
            var quoteString = connection.getMetaData().getIdentifierQuoteString();
            return (quoteString != null && !quoteString.isBlank()) ? quoteString.charAt(0) : '"';
        } catch (SQLException ex) {
            throw SQLExceptionBuilder.build("Failed to retrieve identifier quote string from metadata", ex);
        }
    }

    /** Find real column name from database. */
    protected <V> ColumnModel<D,V> column(Key<D,V> key) {
        var jdbcType = (JDBCType) null;
        var foreignKey = (Key<V,?>) null;
        if (key.foreignKey()) {
            var foreignHandler = ctx.domainService().getHandler(key.type());
            foreignKey = ctx.commonService().findPrimaryKey(foreignHandler.getDomainClass(), ctx);
            jdbcType = jdbcTypeProvider.findJdbcType(foreignKey);
        } else {
            jdbcType = jdbcTypeProvider.findJdbcType(key);
        }
        var columnName = dbColumMapLowerCase.get(key.columnLabel().toLowerCase(Locale.ENGLISH));
        if (StringUtils.isEmpty(columnName)) {
            var msg = "Property %s mapped to column '%s' not found in database."
                    .formatted(key.fullName(), key.columnLabel());
            throw new IllegalStateException(msg);
        }
        return new ColumnModel<>(key, columnName.intern(), jdbcType, foreignKey);
    }

    protected ColumnModel<D,?> findPk(List<? extends ColumnModel<D,?>> columns) {
        for (var col : columns) {
            if (col.pk()) return col;
        }
        var firstColumn = columns.get(0);
        if (ctx.config().isFirstPropertyIsIdentifier()) {
            return firstColumn;
        } else {
            var msg = "No primary key was found by to annotation in " + firstColumn.key().domainClass();
            throw new IllegalStateException(msg);
        }
    }

    /** Static builder */
    public static <D> TableModel<D> build(DomainHandler<D> handler, Context ctx, Connection initConnection) {
        return new TableModelBuilder<>(handler, ctx).build(initConnection);
    }
}