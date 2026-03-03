/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.generator.TableIdentifier;
import org.ujorm.mapper.impl.Config;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.utils.JdbcTypeProvider;
import org.ujorm.tools.common.StreamUtils;
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
        var dbModel = createTableIdentifier(handler.getDomainClass(), initConnection);
        dbColumMapLowerCase = findDatabaseColumnMap(dbModel, initConnection);
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
        return new TableModel(handler, pk, columns, dbModel, insertedColumns, jdbc);
    }

    /**
     * Finds the real table identifier from database metadata.
     *
     * @param domainClass domainClass
     * @param initConnection The database connection.
     * @return result - The real table identifier.
     */
    protected TableIdentifier createTableIdentifier(Class<D> domainClass, Connection initConnection) {
        var table = TableIdentifier.of(domainClass);
        var catalog = (table.catalog() != null && !table.catalog().isEmpty()) ? table.catalog() : null;
        var schema = (table.schema() != null && !table.schema().isEmpty()) ? table.schema() : null;
        var tableName = table.table();
        var tableNames = new String[] {
                tableName,
                tableName.toUpperCase(Locale.ENGLISH),
                tableName.toLowerCase(Locale.ENGLISH)
        };
        try {
            var metaData = initConnection.getMetaData();
            for (var name : tableNames) {
                try (var resultSet = metaData.getTables(catalog, schema, name, new String[]{"TABLE", "VIEW"})) {
                    if (resultSet.next()) {
                        var realCatalog = resultSet.getString("TABLE_CAT");
                        var realSchema = resultSet.getString("TABLE_SCHEM");
                        var realTableName = resultSet.getString("TABLE_NAME");
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
    private List<String> findDatabaseColumnList(TableIdentifier table, Connection initConnection) {
        var result = new ArrayList<String>();
        try {
            var metaData = initConnection.getMetaData();
            var catalog = (table.catalog() != null && !table.catalog().isEmpty()) ? table.catalog() : null;
            var schema = (table.schema() != null && !table.schema().isEmpty()) ? table.schema() : null;

            try (var resultSet = metaData.getColumns(catalog, schema, table.table(), null)) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException e) {
            var msg = "Cannot retrieve columns for table: " + table.getQualifiedName();
            throw SQLExceptionBuilder.build(msg, e);
        }

        if (result.isEmpty()) {
            var msg = "Entity %s has no column in the table %s.: "
                    .formatted(handler.getDomainClass().getSimpleName(), table);
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
        var result = false;
        try {
            var metaData = connection.getMetaData();
            var productName = metaData.getDatabaseProductName();
            if (productName != null && productName.toLowerCase().contains("oracle")) {
                result = true;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Oracle test faild", ex);
        }
        return result;
    }

    /**
     * Determines if the provided connection is directed to an Oracle database.
     *
     * @param connection The database connection to check.
     * @return true if the database product name contains "oracle", false otherwise.
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
        if (columnName == null || columnName.isEmpty()) {
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

    /** Map a lower case column name to the original column name. */
    protected static Map<String, String> jdbcColumnMap(TableIdentifier dbModel, Connection initConnection) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Static builder */
    public static <D> TableModel<D> build(DomainHandler<D> handler, Context ctx, Connection initConnection) {
        return new TableModelBuilder<D>(handler, ctx).build(initConnection);
    }
}