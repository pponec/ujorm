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
package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.model.ColumnModel;
import org.ujorm.mapper.model.TableModel;
import org.ujorm.mapper.model.TableModelBuilder;
import org.ujorm.mapper.utils.Tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The Entity Manager for the JDBC API.
 * Each method may throw an unchecked {@link org.ujorm.tools.jdbc.SQLException}.
 *
 * @param <D> Domain class
 * @param <V> Primary key class
 */
public class EntityManager<D, V> {
    private static final Logger LOGGER = Logger.getLogger(EntityManager.class.getName());

    private final Connection connection;
    private final DomainHandler<D> domainHandler;
    /** TODO: Get from a local thread */
    private final Context context;
    private final TableModel<D> tableModel;
    private final ColumnModel<D, V> pkColumn;
    private final Key<D, V> pk;
    private final String quote;

    /** Size of batch for multi-insert and delete.
     * Note: This attribute is not fully implemented yet. */
    @Deprecated
    private final int batchSize;

    public EntityManager(@NotNull Class<D> domainClass, @NotNull Connection connection) {
        this(domainClass, connection, Context.ofDefault());
    }

    public EntityManager(@NotNull Class<D> domainClass, @NotNull Connection connection, @NotNull Context context) {
        this(domainClass, connection, context, 500);
    }

    @SuppressWarnings("unchecked")
    public EntityManager(@NotNull Class<D> domainClass, @NotNull Connection connection, @NotNull Context context, int batchSize) {
        this.connection = connection;
        this.domainHandler = context.domainService().getHandler(domainClass);
        this.context = context;
        this.tableModel = TableModelBuilder.build(domainHandler, context, connection);
        this.pkColumn = (ColumnModel<D, V>) tableModel.pk();
        this.pk = pkColumn.key();
        this.batchSize = batchSize;
        this.quote = Tools.getQuoteIdentifier(connection);
    }

    /** Inserts multiple domain objects using a loop.
     * TODO: Implement a true multi-insert with batching support. */
    @SafeVarargs
    public final void insert(int batchSize, @NotNull D... domains) {
        for (var domain : domains) {
            insert(domain);
        }
    }

    /**
     * Inserts a domain object into the database.
     *
     * @param domain The domain object to be inserted (either a bean or a record)
     * @return The object with an assigned identifier.
     * Whenever possible, it returns the same instance provided as a parameter.
     */
    public D insert(@NotNull D domain) {
        var pkOriginalValue = getPrimaryKeyValue(domain);
        var columns = tableModel.createInsertedColumns(pkOriginalValue);
        var sql = new StringBuilder(256)
                .append("INSERT INTO ")
                .append(domainHandler.getDatabaseTable())
                .append(" (");
        write(sql, columns, ", ");
        sql.append(") VALUES (?");
        for (int j = columns.size() - 1; j > 0; j--) {
            sql.append(",?");
        }
        sql.append(")");

        var returnGeneratedKeys = pkOriginalValue == null;
        return run(sql, returnGeneratedKeys, ps -> {
            setValuesToStatement(domain, columns, ps);
            if (!returnGeneratedKeys) {
                ps.executeUpdate();
                return domain;
            } else {
                ps.executeUpdate();
                V id = null;
                try (var rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getObject(1, pk.type());
                    }
                }
                if (id == null) {
                    throw new IllegalArgumentException("No ID value was generated.");
                }
                var ujo = AbstractUjo.of(domain, domainHandler);
                ujo.setValue(pk, id);
                return ujo.toDomainObject();
            }
        });
    }

    /** Set values to the Prepared Statement */
    protected void setValuesToStatement(D domain, List<ColumnModel<D,Object>> columns, PreparedStatement ps) throws SQLException {
        for (int i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            var value = column.valueOf(domain);
            if (column.relation()) {
                if (value != null) {
                    value = column.foreignKey().getValue(value);
                }
            }
            ps.setObject(i + 1, value, column.jdbcType());
        }
    }

    /** Set values to the Prepared Staement */
    protected final void setPkToStatement(final D domain, final int index, final PreparedStatement ps) throws SQLException {
        ps.setObject(index, getPrimaryKeyValue(domain), pkColumn.jdbcType());
    }

        /** Reads a domain object by its identifier. */
    public D read(@NotNull V id) {
        var sql = new StringBuilder(128)
                .append("SELECT \n");  // "*"
        for (var column : this.tableModel.columns()) {
            sql.append(column.index() > 0 ? ", ": "  ");
            sql.append(column.name()).append(" AS ").append(column.property()).append("\n");
        }
        sql.append(" FROM ")
                .append(domainHandler.getDatabaseTable())
                .append(" WHERE ")
                .append(pk.columnName())
                .append(" = ?");

        return run(sql, false, ps -> {
            ps.setObject(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    var ujo = AbstractUjo.of(domainHandler);
                    for (var column : tableModel.columns()) {
                        var value = rs.getObject(column.name(), column.key().type());
                        ujo.setValue(column.keyObject(), value);
                    }
                    return ujo.toDomainObject();
                }
                return null;
            }
        });
    }

    /**
     * Updates a domain object.
     * @param domain Domain object to update.
     * @param properties Optional list of property names to update. If empty, all properties are updated (excluding id).
     * @return The number of affected rows.
     */
    public long update(@NotNull D domain, String... properties) {
        var columns = properties.length > 0
                ? new ArrayList<ColumnModel<D,Object>>(properties.length)
                : tableModel.insertedColumns();
        for (var prop : properties) {
            columns.add(tableModel.getColumn(prop));
        }
        return update(domain, columns);
    }

    /**
     * Updates a domain object.
     * @param domain Domain object to update.
     * @param columns Optional list of property names to update. If empty, all properties are updated (excluding id).
     * @return The number of affected rows.
     */
    protected long update(@NotNull D domain, List<ColumnModel<D, Object>> columns) {
        var sql = new StringBuilder(256)
                .append("UPDATE ")
                .append(domainHandler.getDatabaseTable());
        for (int i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            sql.append(i == 0 ? " SET ": ", ");
            sql.append(column.name()).append(" = ?");
        }
        sql.append(" WHERE ").append(pkColumn.name()).append(" = ").append("?");
        return run(sql, false, ps -> {
            setValuesToStatement(domain, columns, ps);
            setPkToStatement(domain, columns.size() + 1, ps);
            return (long) ps.executeUpdate();
        });
    }

    /** Deletes a domain object. */
    public int delete(@NotNull D domain) {
        return deleteById(getPrimaryKeyValue(domain));
    }

    /** Deletes a domain object by its identifier. */
    public int deleteById(@NotNull V id) {
        var sql = "DELETE FROM " + domainHandler.getDatabaseTable() + " WHERE " + pk.columnName() + " = ?";
        return run(sql, false, ps -> {
            ps.setObject(1, id);
            return ps.executeUpdate();
        });
    }

    /** Returns the value of the primary key. */
    private V getPrimaryKeyValue(@NotNull final D domain) {
        return this.pk.getValue(domain);
    }

    /** Logs and executes the SQL statement. */
    protected <R> R run(final CharSequence sql, final boolean returnGeneratedKeys, final SqlFunction<PreparedStatement, R> fun) {
        try (var ps = !returnGeneratedKeys
            ? connection.prepareStatement(sql.toString())
            : tableModel.isOracleDb()
            ? connection.prepareStatement(sql.toString(), new String[]{pkColumn.name()})
            : connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)
        ) {
            LOGGER.info(sql::toString);
            return fun.applyValue(ps);
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : new IllegalStateException(ex);
        }
    }

    /** Write column name and quote it. */
    protected void write(final StringBuilder writer, final List<ColumnModel<D,Object>> columns, final String separator) {
        for (int i = 0, max = columns.size(); i < max; i++) {
            if (i > 0) {
                writer.append(separator);
            }
            final var column = columns.get(i);
            final var name = column.name();
            writer.append(name.charAt(0) == '`' ? name.replace("`", quote) : name);
        }
    }

    @FunctionalInterface
    protected interface SqlFunction<T, R> {
        R applyValue(T ps) throws Exception;
    }
}