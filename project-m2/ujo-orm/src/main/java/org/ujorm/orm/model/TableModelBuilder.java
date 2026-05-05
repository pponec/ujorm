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
package org.ujorm.orm.model;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.core.generator.TableIdentifier;
import org.ujorm.orm.Config;
import org.ujorm.orm.utils.JdbcUtils;
import org.ujorm.tools.Check;
import org.ujorm.tools.common.StreamUtils;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Table Model Builder */
@RequiredArgsConstructor
public class TableModelBuilder<D> {
    private static final Logger LOGGER = Logger.getLogger(TableModelBuilder.class.getName());
    private final DomainHandler<D> handler;
    private final Config config;

    /** Map a database columns where the key is lower-case */
    private Map<String, String> dbColumMapLowerCase;

    public TableModel<D> build(Connection initConnection) {
        var softTableModel = TableIdentifier.of(handler.getDomainClass());
        var realTableModel = createTableIdentifier(softTableModel, initConnection);
        dbColumMapLowerCase = findDatabaseColumnMap(realTableModel, initConnection);
        List<ColumnModel<D, ?>> columns = handler.getKeyList().stream()
                .<ColumnModel<D, ?>>map(this::column)
                .collect(Collectors.toList());
        var pk = findPk(columns);
        List<ColumnModel<D, ?>> insertedColumns = columns.stream()
                .filter(c -> c != pk)
                .collect(Collectors.toList());
        var isOracleDb = getDbVendor(initConnection);
        var sqlQuote = getSqlQuote(initConnection, config);
        var jdbc = new Jdbc(isOracleDb, sqlQuote);
        var tableName = softTableModel.merge(realTableModel);
        return new TableModel<>(handler, pk, columns, tableName, insertedColumns, jdbc);
    }

    /**
     * Finds the real table identifier from database metadata.
     *
     * @param table domainClass
     * @param initConnection The database connection.
     * @return result - The real table identifier.
     */
    protected TableIdentifier createTableIdentifier(TableIdentifier table, Connection initConnection) {
        var catalog = Check.hasLength(table.catalog()) ? table.catalog() : null;
        var schema = Check.hasLength(table.schema()) ? table.schema() : null;
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
        final var columns = findDatabaseColumnList(table, initConnection);
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
            var catalog = Check.hasLength(table.catalog()) ? table.catalog() : null;
            var schema = Check.hasLength(table.schema()) ? table.schema() : null;

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
     * Determines the database vendor from the provided connection metadata.
     *
     * @param connection The database connection to check.
     * @return The identified database vendor or DEFAULT if unknown or an error occurs.
     */
    protected DatabaseVendor getDbVendor(Connection connection) {
        try {
            var productName = connection.getMetaData().getDatabaseProductName();
            if (productName != null) {
                var nameLower = productName.toLowerCase();
                if (nameLower.contains("oracle")) {
                    return DatabaseVendor.ORACLE;
                }
                if (nameLower.contains("sql server")) {
                    return DatabaseVendor.MS_SQL_SERVER;
                }
                if (nameLower.contains("mariadb")) {
                    return DatabaseVendor.MARIA_DB;
                }
                if (nameLower.contains("mysql")) {
                    return DatabaseVendor.MY_SQL;
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to determine database vendor", ex);
        }
        return DatabaseVendor.DEFAULT;
    }

    /**
     * Retrieves the identifier quotes character configuration for the database connection.
     * <p>
     * The method resolves the quotes in the following priority:
     * <ol>
     *   <li>If quoting is disabled in the configuration, returns no quotes.</li>
     *   <li>If a custom quotes string is defined in the configuration, uses that string.</li>
     *   <li>Otherwise, falls back to the database metadata provided by JDBC.</li>
     * </ol>
     *
     * @param connection The database connection to check.
     * @param config The current configuration context.
     * @return The identifier quotes configuration.
     */
    protected QuotePair getSqlQuote(@NotNull Connection connection, @NotNull Config config) {
        if (!config.isEnableSqlQuoting()) {
            return QuotePair.ofNone();
        }

        if (!config.getQuotePair().isEmpty()) {
            return QuotePair.ofString(config.getQuotePair());
        }

        try {
            var metaData = connection.getMetaData();
            var dbName = metaData.getDatabaseProductName();
            if (Check.hasLength(dbName)) {
                if (dbName.contains("Microsoft SQL Server")) {
                    return QuotePair.ofMsSqlServer();
                }
                if (dbName.contains("MySQL") || dbName.contains("MariaDB")) {
                    return QuotePair.ofMySql();
                }
            }

            var jdbcQuote = metaData.getIdentifierQuoteString();
            if (" ".equals(jdbcQuote)) {
                return QuotePair.ofNone();
            }
            if (jdbcQuote == null || jdbcQuote.isEmpty()) {
                return QuotePair.ofDefault();
            }

            var doubleQuote = '"';
            var quoteChar = jdbcQuote.charAt(0);
            return quoteChar == doubleQuote
                    ? QuotePair.ofDefault()
                    : new QuotePair(quoteChar, quoteChar);
        } catch (SQLException ex) {
            throw SQLExceptionBuilder.build("Cannot read DB metadata", ex);
        }
    }

    /** Find real column name from database. */
    protected <V> ColumnModel<D,V> column(Key<D,V> key) {
        var jdbcType = (JDBCType) null;
        var foreignKey = (Key<V,?>) null;
        if (key.info().foreignKey()) {
            var foreignHandler = DomainHandlerProvider.getHandler(key.type());
            var acceptDefaultPk = config.acceptDefaultPk();
            foreignKey = foreignHandler.findPrimaryKey(acceptDefaultPk);
            jdbcType = JdbcUtils.findJdbcType(foreignKey);
        } else {
            jdbcType = JdbcUtils.findJdbcType(key);
        }
        Objects.requireNonNull(jdbcType, () -> "No jdbcType found for " + key.fullName());
        var columnName = dbColumMapLowerCase.get(key.info().columnLabel().toLowerCase(Locale.ENGLISH));
        if (Check.isEmpty(columnName)) {
            var msg = "Property %s mapped to column '%s' not found in database."
                    .formatted(key.fullName(), key.info().columnLabel());
            throw new IllegalStateException(msg);
        }
        return new ColumnModel<>(key, columnName.intern(), jdbcType, foreignKey);
    }

    protected ColumnModel<D,?> findPk(List<? extends ColumnModel<D,?>> columns) {
        for (var col : columns) {
            if (col.pk()) return col;
        }
        var firstColumn = columns.get(0);
        if (config.acceptDefaultPk()) {
            return firstColumn;
        } else {
            var msg = "No primary key was found by to annotation in " + firstColumn.key().domainClass();
            throw new IllegalStateException(msg);
        }
    }

    /** Static builder */
    public static <D> TableModel<D> build(DomainHandler<D> handler, Config config, Connection initConnection) {
        return new TableModelBuilder<>(handler, config).build(initConnection);
    }
}