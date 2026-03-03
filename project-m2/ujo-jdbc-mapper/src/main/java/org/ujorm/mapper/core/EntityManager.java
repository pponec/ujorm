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
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Entity Manager for the JDBC API.
 * Each method may throw an unchecked {@link org.ujorm.tools.jdbc.SQLException}.
 * <p>
 * It is highly recommended to call the {@link #crud(Connection)} method as part
 * of the class initialization to pre-build the internal table model safely.
 *
 * @param <D> Domain class
 * @param <V> Primary key class
 */
public final class EntityManager<D, V> {
    private static final Logger LOGGER = Logger.getLogger(EntityManager.class.getName());

    private final DomainHandler<D> domainHandler;
    private final ResultSetMapper<D> resultSetMapper;
    private final Context context;
    private final Utilities utilities;

    /** Lazy initialized TableModel. Use {@link #tableModel()} method to access it safely. */
    private volatile TableModel<D> _tableModel;

    public EntityManager(
            @NotNull Class<D> domainClass,
            @NotNull Context context,
            @NotNull ResultSetMapper<D> resultSetMapper) {
        this.domainHandler = context.domainService().getHandler(domainClass);
        this.context = context;
        this.resultSetMapper = resultSetMapper;
        this.utilities = new Utilities();
    }

    /** Initializes TableModel if not already done. */
    private void initModel(@NotNull Connection connection) {
        if (this._tableModel == null) {
            synchronized (this) {
                if (this._tableModel == null) {
                    this._tableModel = TableModelBuilder.build(domainHandler, context, connection);
                    LOGGER.log(Level.INFO, () ->
                            "Lazy initialization of %s was triggered for the %s entity.".formatted(
                                    TableModel.class.getSimpleName(),
                                    EntityManager.this.domainHandler.getDomainClass().getSimpleName()
                            ));
                }
            }
        }
    }

    /** Creates a new Crud instance to perform database operations. */
    public Crud crud(@NotNull Connection connection) {
        initModel(connection);
        return new Crud(connection);
    }

    /** Thread-safe access to the TableModel. */
    @NotNull
    private TableModel<D> tableModel() {
        var result = this._tableModel;
        if (result == null) {
            throw new IllegalStateException("%s is not initialized.".formatted(getClass().getSimpleName()));
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

    /** Utilities for EntityManager */
    final class Utilities {

        /** Returns a safe limit for batch operations (insert and update). */
        public int getBatchLimit() {
            var limit = context.config().getInsertBatchSize();
            return limit > 0 ? limit : 500;
        }

        /** Returns the value of the primary key. */
        public V getPrimaryKeyValue(@NotNull final D domain) {
            return pk().getValue(domain);
        }

        /** Checks if the primary key is empty (null or zero). */
        public boolean isPkEmpty(@Nullable final V id) {
            return id == null || (id instanceof Number n && n.longValue() == 0L);
        }

        /** Reads the generated key from ResultSet and assigns it to the domain object. */
        public D assignGeneratedKey(D domain, java.sql.ResultSet rs, Key<D, V> pk) throws SQLException {
            V id = rs.getObject(1, pk.type());
            if (id == null) {
                throw new IllegalArgumentException("No ID value was generated.");
            }
            var ujo = AbstractUjo.of(domain, domainHandler);
            ujo.setValue(pk, id);
            return ujo.buildDomain();
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

        /** Set both column values and PK to the Prepared Statement for UPDATE queries */
        public void setValuesAndPkToStatement(D domain, List<ColumnModel<D, Object>> columns, PreparedStatement ps) throws SQLException {
            setValuesToStatement(domain, columns, ps);
            setPkToStatement(domain, columns.size() + 1, ps);
        }

        /** Sums the affected rows from a batch execution, handling SUCCESS_NO_INFO. */
        public long sumBatchRows(int[] batchResults) {
            var result = 0L;
            for (var rowCount : batchResults) {
                if (rowCount >= 0) {
                    result += rowCount;
                } else if (rowCount == Statement.SUCCESS_NO_INFO) {
                    result++; // Fallback for databases like Oracle
                }
            }
            return result;
        }

        /** Builds an SQL INSERT statement for the specified columns. */
        public String buildInsertSql(@NotNull List<ColumnModel<D, Object>> columns) {
            var q = getQuote();
            var tableName = tableModel().tableName();
            var sql = new StringBuilder(256)
                    .append("INSERT INTO ")
                    .append(q).append(tableName).append(q)
                    .append(" (");
            write(sql, columns, ", ", q);
            sql.append(") VALUES (?");
            for (var j = columns.size() - 1; j > 0; j--) {
                sql.append(",?");
            }
            sql.append(")");
            return sql.toString();
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

        /** Logs and executes the SQL statement using the provided connection. */
        public <R> R run(@NotNull Connection connection, final CharSequence sql, final boolean returnGeneratedKeys, final SqlFunction<PreparedStatement, R> fun) {
            try (var ps = !returnGeneratedKeys
                    ? connection.prepareStatement(sql.toString())
                    : tableModel().jdbc().isOracleDb()
                    ? connection.prepareStatement(sql.toString(), new String[]{pkColumn().name()})
                    : connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)
            ) {
                if (context.config().isPrintSql()) {
                    LOGGER.info(sql::toString);
                }
                return fun.applyValue(ps);
            } catch (SQLException ex) {
                throw SQLExceptionBuilder.build(ex);
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

    /** The CRUD operations wrapper. */
    public final class Crud {
        private final Connection dbconnection;

        public Crud(@NotNull Connection dbconnection) {
            this.dbconnection = dbconnection;
        }

        /**
         * Inserts multiple domain objects using batching support.
         */
        @SafeVarargs
        public final void insertBatch(@NotNull D... domains) {
            if (domains == null || domains.length == 0) {
                return;
            }

            var withPk = new ArrayList<D>();
            var withoutPk = new ArrayList<D>();

            for (var domain : domains) {
                if (domain == null) {
                    continue;
                }
                var pkOriginalValue = utilities.getPrimaryKeyValue(domain);
                if (utilities.isPkEmpty(pkOriginalValue)) {
                    withoutPk.add(domain);
                } else {
                    withPk.add(domain);
                }
            }

            if (!withPk.isEmpty()) {
                insertBatch(withPk, false);
            }
            if (!withoutPk.isEmpty()) {
                insertBatch(withoutPk, true);
            }
        }

        /**
         * Executes a batch insert for a given list of domain objects.
         *
         * @param domains The list of domain objects to insert
         * @param returnGeneratedKeys True if primary keys should be generated and assigned
         */
        protected void insertBatch(@NotNull List<D> domains, boolean returnGeneratedKeys) {
            if (domains.isEmpty()) {
                return;
            }

            var pkOriginalValue = returnGeneratedKeys ? null : utilities.getPrimaryKeyValue(domains.get(0));
            var columns = tableModel().createInsertedColumns(pkOriginalValue);
            var sql = utilities.buildInsertSql(columns);
            var limit = utilities.getBatchLimit();

            utilities.run(dbconnection, sql, returnGeneratedKeys, ps -> {
                var pk = returnGeneratedKeys ? pk() : null;
                var batchCount = 0;

                for (var i = 0; i < domains.size(); i++) {
                    utilities.setValuesToStatement(domains.get(i), columns, ps);
                    ps.addBatch();
                    batchCount++;

                    // Execute batch when the limit is reached or it's the last element
                    if (batchCount == limit || i == domains.size() - 1) {
                        ps.executeBatch();

                        if (returnGeneratedKeys) {
                            try (var rs = ps.getGeneratedKeys()) {
                                var startIndex = i - batchCount + 1;
                                for (var j = startIndex; j <= i; j++) {
                                    if (rs.next()) {
                                        domains.set(j, utilities.assignGeneratedKey(domains.get(j), rs, pk));
                                    } else {
                                        throw new IllegalStateException("Not enough generated keys returned for the batch.");
                                    }
                                }
                            }
                        }
                        batchCount = 0; // Reset counter for the next chunk
                    }
                }
                return null;
            });
        }

        /**
         * Inserts a domain object into the database.
         *
         * @param domain The domain object to be inserted (either a bean or a record)
         * @return The object with an assigned identifier.
         * Whenever possible, it returns the same instance provided as a parameter.
         */
        public D insert(@NotNull D domain) {
            var pkOriginalValue = utilities.getPrimaryKeyValue(domain);
            var columns = tableModel().createInsertedColumns(pkOriginalValue);
            var sql = utilities.buildInsertSql(columns);
            var returnGeneratedKeys = utilities.isPkEmpty(pkOriginalValue);

            return utilities.run(dbconnection, sql, returnGeneratedKeys, ps -> {
                utilities.setValuesToStatement(domain, columns, ps);
                if (!returnGeneratedKeys) {
                    ps.executeUpdate();
                    return domain;
                } else {
                    ps.executeUpdate();
                    var pk = pk();
                    try (var rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            return utilities.assignGeneratedKey(domain, rs, pk);
                        } else {
                            throw new IllegalArgumentException("No ID value was generated.");
                        }
                    }
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

            return utilities.run(dbconnection, sql, false, ps -> {
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
            return updateBatch(domain, columns);
        }

        /**
         * Updates a domain object.
         * @param domains Domain objects to update. If the list is empty, update all columns excluding PK.
         * @param properties Optional list of property names to update. If empty, all properties are updated (excluding id).
         * @return The number of affected rows.
         */
        public long updateBatch(@NotNull List<D> domains, CharSequence... properties) {
            var columns = tableModel().getColumns(properties);
            return updateList(domains, columns);
        }

        /**
         * Updates multiple domain objects using batching and collision detection.
         * Note: Requires connection.setAutoCommit(false) for transactional safety.
         */
        @SafeVarargs
        public final <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull D2... domains) {
            if (domains == null || domains.length == 0) {
                return 0L;
            }
            var result = 0L;
            var limit = utilities.getBatchLimit();
            var batchCount = 0;

            try (var cache = new StatementCache<V>()) {
                for (var j = 0; j < domains.length; j++) {
                    var domain_ = domains[j];
                    if (!domainHandler.getDomainClass().isInstance(domain_)) {
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
                        statement = dbconnection.prepareStatement(sql);
                        result += cache.put(changes, statement);
                    }
                    result += updateInternal(statement, domain, modifiedKeys);
                    cache.addId(id);
                    batchCount++;

                    // Execute batch when the limit is reached across the cache
                    if (batchCount >= limit) {
                        result += cache.flush();
                        batchCount = 0;
                    }
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
            utilities.setValuesAndPkToStatement(entity, columns, statement);
            statement.addBatch();
            return 0L;
        }

        /**
         * Updates a domain object.
         * @param domain Domain object to update.
         * @param columns Optional list of property names to update. If empty, all properties are updated (excluding id).
         * @return The number of affected rows.
         */
        protected long updateBatch(@NotNull D domain, List<ColumnModel<D, Object>> columns) {
            var sql = utilities.buildUpdateSql(columns);
            return utilities.run(dbconnection, sql, false, ps -> {
                utilities.setValuesAndPkToStatement(domain, columns, ps);
                return (long) ps.executeUpdate();
            });
        }

        /**
         * Executes a batch update for a given list of domain objects.
         * Handles drivers returning SUCCESS_NO_INFO (-2).
         * Note: Requires connection.setAutoCommit(false) for transactional safety.
         *
         * @param domains A list of domain entities to be updated in the database.
         * @param columns A list of column models defining which specific attributes should be updated.
         * @return The total number of rows affected by the batch execution.
         */
        protected long updateList(@NotNull List<D> domains, @NotNull List<ColumnModel<D, Object>> columns) {
            if (domains.isEmpty()) {
                return 0L;
            }
            var sql = utilities.buildUpdateSql(columns);
            var limit = utilities.getBatchLimit();

            return utilities.run(dbconnection, sql, false, ps -> {
                var result = 0L;
                var batchCount = 0;

                for (var i = 0; i < domains.size(); i++) {
                    utilities.setValuesAndPkToStatement(domains.get(i), columns, ps);
                    ps.addBatch();
                    batchCount++;

                    // Execute batch when the limit is reached or it's the last element
                    if (batchCount == limit || i == domains.size() - 1) {
                        result += utilities.sumBatchRows(ps.executeBatch());
                        batchCount = 0; // Reset counter for the next chunk
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
            var tableName = tableModel().tableName();
            var sql = new StringBuilder(64)
                    .append("DELETE FROM ").append(q).append(tableName).append(q)
                    .append(" WHERE ").append(q).append(pkColumn().name()).append(q).append(" = ?");
            return utilities.run(dbconnection, sql, false, ps -> {
                ps.setObject(1, id);
                return ps.executeUpdate();
            });
        }

        /** Deletes multiple domain objects using batching support. */
        @SafeVarargs
        public final int deleteBatch(@NotNull D... domains) {
            if (domains == null || domains.length == 0) {
                return 0;
            }
            var q = getQuote();
            var tableName = tableModel().tableName();
            var sql = new StringBuilder(64)
                    .append("DELETE FROM ").append(q).append(tableName).append(q)
                    .append(" WHERE ").append(q).append(pkColumn().name()).append(q).append(" = ?");

            var limit = utilities.getBatchLimit();

            return utilities.run(dbconnection, sql, false, ps -> {
                var result = 0L;
                var batchCount = 0;

                for (var i = 0; i < domains.length; i++) {
                    var domain = domains[i];
                    if (domain == null) continue;

                    ps.setObject(1, utilities.getPrimaryKeyValue(domain));
                    ps.addBatch();
                    batchCount++;

                    if (batchCount == limit || i == domains.length - 1) {
                        result += utilities.sumBatchRows(ps.executeBatch());
                        batchCount = 0;
                    }
                }
                return (int) result;
            });
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
        manager.initModel(connection);
        return manager;
    }
}