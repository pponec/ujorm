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
package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.SnapshotProvider;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.jdbc.ResultSetMapper;
import org.ujorm.mapper.model.ColumnModel;
import org.ujorm.mapper.model.TableModel;
import org.ujorm.mapper.model.TableModelBuilder;
import org.ujorm.mapper.utils.StatementCache;
import org.ujorm.mapper.utils.Tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    private final ThreadLocal<Connection> connection = new ThreadLocal<>();
    private final DomainHandler<D> domainHandler;
    private final ResultSetMapper<D> resultSetMapper;
    /** TODO: Get from a local thread */
    private final Context context;
    private final Utilities utilities;

    /** Lazy initialized TableModel.
     * Use {@link #tableModel()} method to access it safely. */
    private volatile TableModel<D> _tableModel;

    /** Size of batch for multi-insert and delete.
     * Note: This attribute is not fully implemented yet. */
    @Deprecated
    private final int insertBatchSize;

    public EntityManager(
            @NotNull Class<D> domainClass,
            @NotNull Context context,
            @NotNull ResultSetMapper<D> resultSetMapper) {
        this.domainHandler = context.domainService().getHandler(domainClass);
        this.context = context;
        this.insertBatchSize = context.config().getInsertBatchSize();
        this.resultSetMapper = resultSetMapper;
        this.utilities = new Utilities();
    }

    /** Sets a connection for the current thread and initializes the model if necessary. */
    public EntityManager<D, V> setConnection(@NotNull Connection connection) {
        this.connection.set(connection);
        if (this._tableModel == null) {
            synchronized (this) {
                if (this._tableModel == null) {
                    this._tableModel = TableModelBuilder.build(domainHandler, context, connection);
                }
            }
        }
        return this;
    }

    /** Removes the connection from the current thread to prevent memory leaks. */
    public void removeConnection() {
        this.connection.remove();
    }

    /** Gets the connection for the current thread. */
    private Connection connection() {
        var conn = this.connection.get();
        if (conn == null) {
            throw new IllegalStateException("DB Connection is not available for the current thread.");
        }
        return conn;
    }

    /** Thread-safe access to the TableModel. */
    @NotNull
    private TableModel<D> tableModel() {
        var result = this._tableModel;
        if (result == null) {
            throw new IllegalStateException("%s is not initialized. Call setConnection() first.".formatted(
                    getClass().getSimpleName()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private ColumnModel<D, V> pkColumn() {
        return (ColumnModel<D, V>) tableModel().pk();
    }

    private Key<D, V> pk() {
        return pkColumn().key();
    }

    private char getQuote() {
        return tableModel().jdbc().quoteChar();
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
        var q = getQuote();
        var tableName = tableModel().tableName();
        var pkOriginalValue = utilities.getPrimaryKeyValue(domain);
        var columns = tableModel().createInsertedColumns(pkOriginalValue);
        var sql = new StringBuilder(256)
                .append("INSERT INTO ")
                .append(q).append(tableName).append(q)
                .append(" (");
        utilities.write(sql, columns, ", ", q);
        sql.append(") VALUES (?");
        for (var j = columns.size() - 1; j > 0; j--) {
            sql.append(",?");
        }
        sql.append(")");

        var returnGeneratedKeys = pkOriginalValue == null;
        return utilities.run(sql, returnGeneratedKeys, ps -> {
            utilities.setValuesToStatement(domain, columns, ps);
            if (!returnGeneratedKeys) {
                ps.executeUpdate();
                return domain;
            } else {
                ps.executeUpdate();
                V id = null;
                var pk = pk();
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
                return ujo.buildDomain();
            }
        });
    }

    /** Reads a domain object by its identifier. */
    @Nullable
    public D readNullable(@NotNull V id) {
        return read(id).orElse(null);
    }

    /** Reads a domain object by its identifier. */
    @NotNull
    public Optional<D> read(@NotNull V id) {
        var q = getQuote();
        var tableName = tableModel().tableName();
        var columns = tableModel().columns();
        var labels = new Key[columns.size()];
        var sql = new StringBuilder(128).append("SELECT \n"); // "*"
        for (var i = 0; i < columns.size(); i++) {
            var column = columns.get(i);
            labels[i] = column.key();
            sql.append(column.index() > 0 ? ", ": "  ").append(q).append(column.name()).append(q);
        }
        sql.append(" FROM ").append(q).append(tableName).append(q);
        sql.append(" WHERE ").append(q).append(pkColumn().name()).append(q).append(" = ?");

        return utilities.run(sql, false, ps -> {
            ps.setObject(1, id);
            try (var rs = ps.executeQuery()) {
                return resultSetMapper.convert(rs, labels).findFirst();
            }
        });
    }

    /**
     * Updates a domain object.
     * @param domain Domain object to update.
     * @param properties Optional list of property names to update. If empty, all properties are updated (excluding id).
     * @return The number of affected rows.
     */
    public long update(@NotNull D domain, CharSequence... properties) {
        var columns = tableModel().getColumns(properties);
        return update(domain, columns);
    }

    /**
     * Updates a domain object.
     * @param domains Domain objects to update. If the list is empty, update all columns excluding PK.
     * @param properties Optional list of property names to update. If empty, all properties are updated (excluding id).
     * @return The number of affected rows.
     */
    public long update(@NotNull List<D> domains, CharSequence... properties) {
        var columns = tableModel().getColumns(properties);
        return updateList(domains, columns);
    }

    /** Updates multiple domain objects using batching and collision detection. */
    @SafeVarargs
    public final <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull D2... domains) {
        if (domains == null || domains.length == 0) {
            return 0L;
        }
        var result = 0L;
        try (var cache = new StatementCache<V>()) {
            for (var j = 0; j < domains.length; j++) {
                var domain_ = domains[j];
                if (!this.domainHandler.getDomainClass().isInstance(domain_)) {
                    var msg = domain_ == null
                            ? "The entity at index %s must not be null.".formatted(j)
                            : "The entity at index %s must be of type %s.".formatted(j,
                            domainHandler.getDomainClass().getSimpleName());
                    throw new IllegalArgumentException(msg);
                }
                var domain = (D) domain_;
                var snapshot = (D) domain_.readSnapshot();
                if (snapshot == null) {
                    throw new IllegalStateException(("Missing snapshot for entity at index %s. " +
                            "Call saveSnapshot() before update.").formatted(j));
                }
                var changes = Tools.findChanges(domain, snapshot, domainHandler);
                var modifiedIdx = changes.getActive();

                if (modifiedIdx.length == 0) {
                    continue;
                }
                var id = utilities.getPrimaryKeyValue(domain);

                // Flush on ID collision to prevent DB deadlocks and preserve update order
                result += cache.flushOnCollision(id);

                var modifiedKeys = new Key[modifiedIdx.length];
                for (var i = 0; i < modifiedIdx.length; i++) {
                    modifiedKeys[i] = domainHandler.getKey(modifiedIdx[i]);
                }
                var statement = cache.get(changes);
                if (statement == null) {
                    var sql = utilities.buildUpdateSql(modifiedKeys);
                    if (context.config().isPrintSql()) {
                        LOGGER.info(sql);
                    }
                    statement = connection().prepareStatement(sql);
                    result += cache.put(changes, statement);
                }
                result += updateInternal(statement, domain, modifiedKeys);
                cache.addId(id);
            }
            result += cache.flush(); // Flush any remaining statements before the AutoCloseable block finishes
        } catch (SQLException e) {
            throw new IllegalStateException("Batch update failed", e);
        }
        return result;
    }

    /** Binds values to the PreparedStatement and adds it to the current batch. */
    @SafeVarargs
    protected final long updateInternal(PreparedStatement statement, D entity, Key<D,?>... keys) throws SQLException {
        var model = tableModel();
        var columns = new ArrayList<ColumnModel<D, Object>>(keys.length);
        for (var key : keys) {
            columns.add(model.getColumn(key.index()));
        }
        utilities.setValuesToStatement(entity, columns, statement);
        utilities.setPkToStatement(entity, columns.size() + 1, statement);
        statement.addBatch();
        return 0L;
    }

    /**
     * Updates a domain object.
     * @param domain Domain object to update.
     * @param columns Optional list of property names to update. If empty, all properties are updated (excluding id).
     * @return The number of affected rows.
     */
    protected long update(@NotNull D domain, List<ColumnModel<D, Object>> columns) {
        var sql = utilities.buildUpdateSql(columns);
        return utilities.run(sql, false, ps -> {
            utilities.setValuesToStatement(domain, columns, ps);
            utilities.setPkToStatement(domain, columns.size() + 1, ps);
            return (long) ps.executeUpdate();
        });
    }

    /**
     * Executes a batch update for a given list of domain objects and a specific set of columns.
     * The method prepares a single SQL statement and utilizes JDBC batching to optimize the update process.
     *
     * @param domains A list of domain entities to be updated in the database.
     * @param columns A list of column models defining which specific attributes should be updated.
     * @return The total number of rows affected by the batch execution.
     */
    protected long updateList(@NotNull List<D> domains, @NotNull List<ColumnModel<D, Object>> columns) {
        var sql = utilities.buildUpdateSql(columns);
        return utilities.run(sql, false, ps -> {
            for (var domain : domains) {
                utilities.setValuesToStatement(domain, columns, ps);
                utilities.setPkToStatement(domain, columns.size() + 1, ps);
                ps.addBatch();
            }

            var result = 0L;
            var batchResults = ps.executeBatch();
            for (var rowCount : batchResults) {
                if (rowCount > 0) {
                    result += rowCount;
                }
            }
            return result;
        });
    }

    /** Deletes a domain object. */
    public int delete(@NotNull D domain) {
        return deleteById(utilities.getPrimaryKeyValue(domain));
    }

    /** Deletes a domain object by its identifier. */
    public int deleteById(@NotNull V id) {
        var q = getQuote();
        var sql = new StringBuilder(64)
                .append("DELETE FROM ").append(q).append(domainHandler.getDatabaseTable()).append(q)
                .append(" WHERE ").append(q).append(pkColumn().name()).append(q).append(" = ?");
        return utilities.run(sql, false, ps -> {
            ps.setObject(1, id);
            return ps.executeUpdate();
        });
    }

    /** Utilities for EntityManager */
    class Utilities {

        /** Returns the value of the primary key. */
        public V getPrimaryKeyValue(@NotNull final D domain) {
            return pk().getValue(domain);
        }

        /** Set values to the Prepared Statement */
        public void setValuesToStatement(D domain, List<ColumnModel<D, Object>> columns, PreparedStatement ps) throws SQLException {
            for (var i = 0; i < columns.size(); i++) {
                var column = columns.get(i);
                var value = column.valueOf(domain);
                if (column.relation() && value != null) {
                    value = column.foreignKey().getValue(value);
                }
                ps.setObject(i + 1, value, column.jdbcType());
            }
        }

        /** Set PK to the Prepared Statement */
        public void setPkToStatement(final D domain, final int index, final PreparedStatement ps) throws SQLException {
            ps.setObject(index, getPrimaryKeyValue(domain), pkColumn().jdbcType());
        }

        /** Builds an SQL UPDATE statement for the specified columns. */
        public String buildUpdateSql(@NotNull List<ColumnModel<D, Object>> columns) {
            var q = getQuote();
            var tableName = tableModel().tableName();
            var sql = new StringBuilder(256)
                    .append("UPDATE ")
                    .append(q).append(tableName).append(q);
            for (var i = 0; i < columns.size(); i++) {
                var column = columns.get(i);
                sql.append(i == 0 ? " SET " : ", ");
                sql.append(q).append(column.name()).append(q).append(" = ?");
            }
            sql.append(" WHERE ").append(q).append(pkColumn().name()).append(q).append(" = ?");
            return sql.toString();
        }

        /** Builds an SQL UPDATE statement for the specified keys. */
        public String buildUpdateSql(Key<D, ?>[] keys) {
            var model = tableModel();
            var columns = new java.util.ArrayList<ColumnModel<D, Object>>(keys.length);
            for (var key : keys) {
                columns.add((ColumnModel<D, Object>) model.getColumn(key.index()));
            }
            return buildUpdateSql(columns);
        }

        /** Logs and executes the SQL statement. */
        public <R> R run(final CharSequence sql, final boolean returnGeneratedKeys, final SqlFunction<PreparedStatement, R> fun) {
            try (var ps = !returnGeneratedKeys
                    ? connection().prepareStatement(sql.toString())
                    : tableModel().jdbc().isOracleDb()
                    ? connection().prepareStatement(sql.toString(), new String[]{pkColumn().name()})
                    : connection().prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)
            ) {
                if (context.config().isPrintSql()) {
                    LOGGER.info(sql::toString);
                }
                return fun.applyValue(ps);
            } catch (Exception ex) {
                throw (ex instanceof RuntimeException re) ? re : new IllegalStateException(ex);
            }
        }

        /** Write column name. */
        public void write(final StringBuilder writer, final List<ColumnModel<D,Object>> columns, final String separator, final char q) {
            for (var i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    writer.append(separator);
                }
                writer.append(q).append(columns.get(i).name()).append(q);
            }
        }

        @FunctionalInterface
        public interface SqlFunction<T, R> {
            R applyValue(T ps) throws Exception;
        }
    }

    // --- STATIC METHOD(s) ---

    /** Factory method */
    public static <D, V> EntityManager<D,V> of(@NotNull Class<D> domainClass, @Nullable Class<V> type) {
        return new EntityManager<>(domainClass, Context.ofDefault(), ResultSetMapper.of(domainClass));
    }

    /** Factory method */
    public static <D, V> EntityManager<D,V> of(@NotNull Class<D> domainClass, @NotNull Context context, @NotNull ResultSetMapper<D> resultSetMapper) {
        return new EntityManager<>(domainClass, context, resultSetMapper);
    }

    /** Factory method with provided connection */
    public static <D, V> EntityManager<D,V> of(@NotNull Class<D> domainClass, @NotNull Connection connection, @NotNull Context context, @NotNull ResultSetMapper<D> resultSetMapper) {
        var manager = new EntityManager<D, V>(domainClass, context, resultSetMapper);
        manager.setConnection(connection);
        return manager;
    }
}