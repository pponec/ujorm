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
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.generator.TableIdentifier;
import org.ujorm.mapper.impl.Config;
import org.ujorm.mapper.impl.Context;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Table Model Builder */
@RequiredArgsConstructor
public class TableModelBuilder<D> {
    private static final Logger LOGGER = Logger.getLogger(TableModelBuilder.class.getName());
    private final DomainHandler<D> handler;
    private final Context ctx;

    public TableModel<D> build(Connection initConnection) {
        var dbModel = TableIdentifier.of(handler.getDomainClass());
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

    /** Map a lower case column name to the original column name. */
    protected static Map<String, String> jdbcColumnMap(TableIdentifier dbModel, Connection initConnection) {
        throw new UnsupportedOperationException("TODO");
    }

    protected <V> ColumnModel<D,V> column(Key<D,V> key) {
        var jdbcType = ctx.commonService().findJdbcType(key.type());
        var foreignKey = (Key<V,?>) null;
        if (key.foreignKey()) {
            var foreignHandler = ctx.domainService().getHandler(key.type());
            foreignKey = ctx.commonService().findPrimaryKey(foreignHandler.getDomainClass(), ctx);
            jdbcType = ctx.commonService().findJdbcType(foreignKey.type());
        }
        return new ColumnModel<>(key, jdbcType, foreignKey);
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
        return new TableModelBuilder<D>(handler, ctx).build(initConnection);
    }
}
