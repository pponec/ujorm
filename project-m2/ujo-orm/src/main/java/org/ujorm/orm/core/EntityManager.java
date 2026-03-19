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
package org.ujorm.orm.core;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.DomainHandler;
import org.ujorm.Key;
import org.ujorm.core.SnapshotProvider;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.orm.Crud;
import org.ujorm.orm.impl.Context;
import org.ujorm.orm.jdbc.ResultSetMapper;
import org.ujorm.orm.model.ColumnModel;
import org.ujorm.orm.model.TableModel;
import org.ujorm.orm.model.TableModelBuilder;
import org.ujorm.orm.utils.StatementCache;
import org.ujorm.orm.utils.Tools;
import org.ujorm.tools.common.StringUtils;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;
import org.ujorm.tools.jdbc.SqlQuery;
import org.ujorm.tools.jdbc.AbstractSqlQuery.SqlFunction;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

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

    /**
     * Maps a single {@link ResultSet} row to the Domain object.
     * <p>
     * <strong>Usage:</strong> Best suited for isolated, single-row mappings where you only
     * need to process a specific, individual record without iterating through a large dataset.
     * <p>
     * <strong>Relationship:</strong> This is a convenience wrapper that internally creates
     * a mapping function (similar to calling {@link #mapper(CharSequence...)}) and applies it immediately.
     * Because it resolves the mapping function and column metadata on every single call, it is less
     * efficient for processing large result sets in a loop compared to reusing the function
     * provided directly by {@link #mapper(CharSequence...)}.
     *
     * @param rs The ResultSet positioned at the current row.
     * @param columnLabels Optional custom column labels to map.
     * @return A newly instantiated Domain object mapped from the ResultSet.
     */
    public D map(@NotNull ResultSet rs, @Nullable CharSequence... columnLabels) {
        try {
            return resultSetMapper.map(columnLabels).applyFunction(rs);
        } catch (SQLException e) {
            throw SQLExceptionBuilder.build(e);
        }
    }

    /**
     * Returns a stateful mapper (mapping function) for efficient processing of multiple rows.
     * <p>
     * <strong>Usage:</strong> Highly recommended for stream processing (e.g., {@code .streamMap(entityManager.mapper())})
     * or manual {@code while(rs.next())} loops. By instantiating the mapper exactly once outside
     * the loop, column metadata is resolved upfront. This makes it significantly more efficient
     * for bulk operations.
     * <p>
     * <strong>Relationship:</strong> Acts as the core mapping mechanism. While {@link #map(ResultSet, CharSequence...)}
     * creates and consumes this function on the fly for a single use, this method exposes the reusable
     * function for high-performance, repeated iteration.
     *
     * @param columnLabels Optional custom column labels to map.
     * @return A reusable mapping function.
     */
    public @NotNull SqlFunction<ResultSet, D> mapper(@Nullable CharSequence... columnLabels) {
        return resultSetMapper.map(columnLabels);
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
    public Crud<D,V> crud(@NotNull Connection connection) {
        initModel(connection);
        return new CrudImpl(connection);
    }

    /** Default batch size */
    public int defaultBatchSize() {
        return this.context.config().getBatchSize();
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
        private boolean autoCommitLogged = false;

        /** Checks autoCommit state and logs a warning once per instance if enabled. */
        public void checkAutoCommit(@NotNull Connection connection) throws SQLException {
            if (!autoCommitLogged && context.config().isAutoCommitWarned() && connection.getAutoCommit()) {
                var msg = ("Connection has autoCommit=true in the entity '%s'. " +
                        "Batch operations will be significantly slower and lack transactional safety.")
                        .formatted(domainHandler.getDomainClass().getName());
                LOGGER.warning(msg);
                autoCommitLogged = true;
            }
        }

        /** Returns a safe limit for batch operations (insert, update, select). */
        public int getBatchLimit() {
            var limit = context.config().getBatchSize();
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
            var id = rs.getObject(1, pk.type());
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
            sql.append(",?".repeat(columns.size() - 1));
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
                columns.add(model.getColumn(key.index()));
            }
            return buildUpdateSql(columns);
        }

        /** Logs and executes the SQL statement using the provided connection. */
        public <R> R run(boolean batch, @NotNull Connection connection, final CharSequence sql, final boolean returnGeneratedKeys, final SqlQuery.SqlFunction<PreparedStatement, R> fun) {
            try (var ps = !returnGeneratedKeys
                    ? connection.prepareStatement(sql.toString())
                    : tableModel().jdbc().isOracleDb()
                    ? connection.prepareStatement(sql.toString(), new String[]{pkColumn().name()})
                    : connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)
            ) {
                if (batch) {
                    checkAutoCommit(connection);
                }
                if (context.config().isPrintSql()) {
                    LOGGER.info(sql::toString);
                }
                return fun.applyFunction(ps);
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
    }

    /** The CRUD operations' implementation. */
    public final class CrudImpl implements Crud<D, V> {
        private final Connection dbconnection;

        public CrudImpl(@NotNull Connection dbconnection) {
            this.dbconnection = dbconnection;
        }

        @Override
        @SafeVarargs
        public final D[] insert(@NotNull D... domains) {
            if (domains == null || domains.length == 0) {
                return domains;
            }
            var index = new AtomicInteger(0);
            insert(Arrays.stream(domains), newDomain -> {
                while (index.get() < domains.length && domains[index.get()] == null) {
                    index.incrementAndGet();
                }

                // Assign the returned domain object (e.g. new Record instance) back
                if (index.get() < domains.length) {
                    domains[index.getAndIncrement()] = newDomain;
                }
            });
            return domains;
        }

        /**
         * Inserts a stream of domain objects into the database using JDBC batching.
         * Evaluates sequentially and eagerly to guarantee proper database execution.
         * Memory efficient.
         *
         * @param domains Stream of entities to be inserted.
         * @param onInserted Optional callback invoked for each successfully inserted entity.
         * Useful for retrieving assigned generated primary keys.
         * @return The total number of rows inserted.
         */
        @Override
        public long insert(@NotNull Stream<D> domains, @Nullable Consumer<D> onInserted) {
            var result = 0L;
            if (domains == null) return result;
            var safeStream = domains.isParallel() ? domains.sequential() : domains;
            try (var inserter = new BatchInserter(onInserted)) {
                utilities.checkAutoCommit(dbconnection);
                var iterator = safeStream.iterator();
                while (iterator.hasNext()) {
                    var domain = iterator.next();
                    if (domain != null) result += inserter.add(domain);
                }
                return result + inserter.flush();
            } catch (SQLException e) {
                throw SQLExceptionBuilder.build("Batch insert failed", e);
            }
        }

        /**
         * Processes a stream of domain objects, deciding whether to insert or update each entity
         * based on the state of its primary key. If the primary key is empty (null or 0),
         * an INSERT is performed; otherwise, an UPDATE is performed.
         * <p>
         * Evaluates sequentially. Memory efficient: processes records in small batches.
         *
         * @param domains Stream of entities to be processed.
         * @param onInserted Optional callback invoked specifically for entities that were INSERTED.
         * Useful for retrieving assigned generated primary keys.
         * @param properties Optional list of property names to update for the UPDATE operations.
         * If empty, all properties are updated.
         * @return The total number of rows affected (inserted + updated).
         */
        public long insertOrUpdate(@NotNull Stream<D> domains, @Nullable Consumer<D> onInserted, CharSequence... properties) {
            var result = 0L;
            if (domains == null) return result;
            var safeStream = domains.isParallel() ? domains.sequential() : domains;
            var limit = utilities.getBatchLimit();

            try (var inserter = new BatchInserter(onInserted)) {
                utilities.checkAutoCommit(dbconnection);
                var iterator = safeStream.iterator();

                var updateList = new ArrayList<D>(limit);
                var updateColumns = tableModel().getColumns(properties);

                while (iterator.hasNext()) {
                    var domain = iterator.next();
                    if (domain == null) continue;

                    if (utilities.isPkEmpty(utilities.getPrimaryKeyValue(domain))) {
                        result += inserter.add(domain);
                    } else {
                        updateList.add(domain);
                        if (updateList.size() >= limit) {
                            result += updateStreamInternal(updateList.stream(), updateColumns);
                            updateList.clear();
                        }
                    }
                }

                result += inserter.flush();
                if (!updateList.isEmpty()) {
                    result += updateStreamInternal(updateList.stream(), updateColumns);
                }

            } catch (SQLException e) {
                throw SQLExceptionBuilder.build("Batch insertOrUpdate failed", e);
            }
            return result;
        }

        @Override
        public D insert(@NotNull D domain) {
            Objects.requireNonNull(domain, "Domain object must not be null");
            var pkOriginalValue = utilities.getPrimaryKeyValue(domain);
            var columns = tableModel().createInsertedColumns(pkOriginalValue);
            var sql = utilities.buildInsertSql(columns);
            var returnGeneratedKeys = utilities.isPkEmpty(pkOriginalValue);

            return utilities.run(false, dbconnection, sql, returnGeneratedKeys, ps -> {
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

        /** Finds a domain object by its identifier or returns null. */
        @Override
        @Nullable
        public D findByIdNullable(@NotNull V id) {
            return findById(id).orElse(null);
        }

        /** Finds a domain object by its identifier. */
        @Override
        @NotNull
        public Optional<D> findById(@NotNull V id) {
            Objects.requireNonNull(id, "Identifier must not be null");
            var sql = new StringBuilder(128);
            var labels = buildSelectSql(false, sql);
            var q = getQuote();
            sql.append(" WHERE ").append(q).append(pkColumn().name()).append(q).append(" = ?");

            return utilities.run(false, dbconnection, sql, false, ps -> {
                ps.setObject(1, id);
                try (var rs = ps.executeQuery()) {
                    var mapFunction = resultSetMapper.map(labels);
                    if (rs.next()) {
                        return Optional.of(mapFunction.applyFunction(rs));
                    }
                    return Optional.empty();
                }
            });
        }

        /**
         * Creates an instance of SqlQuery to bind parameters and execute SELECT.
         * The builder shares the database connection with this object.
         *
         * @param whereCondition Undefined or empty value returns all records.
         * @return Query builder
         */
        @Override
        @NotNull
        public <R> R selectWhere(
                @Nullable String whereCondition,
                @NotNull SqlQuery.SqlFunction<SqlQuery, R> fun
        ) {
            var sql = new StringBuilder(256);
            buildSelectSql(true, sql);
            sql.append(" WHERE ");
            sql.append(StringUtils.isFilled(whereCondition) ? whereCondition : "1=1");
            try (var query = new SqlQuery(dbconnection)) {
                query.sql(sql.toString());
                query.fetchSize(context.config().getBatchSize());
                query.log(context.config().isPrintSql() ? Level.INFO : null, false);
                return fun.applyFunction(query);
            } catch (Exception ex) {
                throw (ex instanceof RuntimeException re) ? re : SQLExceptionBuilder.build(ex);
            }
        }

        /**
         * Builds the common SELECT clause for read operations.
         *
         * @param includeAliases If true, column labels are appended using AS alias.
         * @param sql The StringBuilder to append the SQL to.
         * @return Array of column keys if includeAliases is false, otherwise an empty array.
         */
        @Nullable
        private Key[] buildSelectSql(boolean includeAliases, @NotNull StringBuilder sql) {
            var q = getQuote();
            var tableName = tableModel().tableName();
            var columns = tableModel().columns();
            var labels = includeAliases ? null : new Key[columns.size()];

            sql.append("SELECT ");
            for (var i = 0; i < columns.size(); i++) {
                var column = columns.get(i);
                if (i > 0) sql.append(", ");
                sql.append(q).append(column.name()).append(q);

                if (includeAliases) {
                    sql.append(" AS ").append(q).append(column.key()).append(q);
                } else {
                    labels[i] = column.key();
                }
            }
            sql.append(" FROM ").append(q).append(tableName).append(q);
            return labels;
        }

        @Override
        public long update(@NotNull D domain, CharSequence... properties) {
            Objects.requireNonNull(domain, "Domain object must not be null");
            var columns = tableModel().getColumns(properties);
            return updateInternal(domain, columns);
        }

        @Override
        public long update(@NotNull Stream<D> domains, CharSequence... properties) {
            Objects.requireNonNull(domains, "Stream of domains must not be null");
            var columns = tableModel().getColumns(properties);
            return updateStreamInternal(domains, columns);
        }

        @Override
        public <D2 extends SnapshotProvider<D2>> long updateChanged(@NotNull Stream<D2> domains) {
            Objects.requireNonNull(domains, "Stream of domains must not be null");
            var result = 0L;
            var limit = utilities.getBatchLimit();
            var batchCount = 0;

            try (var cache = new StatementCache<V>()) {
                utilities.checkAutoCommit(dbconnection);
                var iterator = domains.iterator();
                var index = -1;
                while (iterator.hasNext()) {
                    index++;
                    var domain_ = iterator.next();
                    if (domain_ == null || !domainHandler.getDomainClass().isInstance(domain_)) {
                        var msg = domain_ == null
                                ? "The entity at index %s must not be null.".formatted(index)
                                : "The entity at index %s must be of type %s.".formatted(index,
                                domainHandler.getDomainClass().getSimpleName());
                        throw new IllegalArgumentException(msg);
                    }
                    var domain = (D) domain_;
                    var snapshot = (D) domain_.readSnapshot();
                    if (snapshot == null) {
                        throw new IllegalStateException(("Missing snapshot for entity at index %s. " +
                                "Call saveSnapshot() before update.").formatted(index));
                    }
                    var changes = Tools.findChanges(domain, snapshot, domainHandler);
                    var modifiedIdx = changes.getActive();

                    if (modifiedIdx.length == 0) {
                        continue;
                    }
                    var id = utilities.getPrimaryKeyValue(domain);

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

                    updateInternalBinding(statement, domain, modifiedKeys);
                    cache.addId(id);
                    batchCount++;

                    // Execute batch when the limit is reached across the cache
                    if (batchCount >= limit) {
                        result += cache.flush();
                        batchCount = 0;
                    }
                }
                result += cache.flush();
            } catch (SQLException e) {
                throw SQLExceptionBuilder.build("Batch update failed", e);
            }
            return result;
        }

        /** Binds values to the PreparedStatement and adds it to the current batch. */
        @SafeVarargs
        private final void updateInternalBinding(PreparedStatement statement, D entity, Key<D, ?>... keys) throws SQLException {
            var model = tableModel();
            var columns = new ArrayList<ColumnModel<D, Object>>(keys.length);
            for (var key : keys) {
                columns.add(model.getColumn(key.index()));
            }
            utilities.setValuesAndPkToStatement(entity, columns, statement);
            statement.addBatch();
        }

        /**
         * Updates a single domain object.
         * @param domain Domain object to update.
         * @param columns Optional list of property names to update. If empty, all properties are updated (excluding id).
         * @return The number of affected rows.
         */
        private long updateInternal(@NotNull D domain, List<ColumnModel<D, Object>> columns) {
            var sql = utilities.buildUpdateSql(columns);
            return utilities.run(false, dbconnection, sql, false, ps -> {
                utilities.setValuesAndPkToStatement(domain, columns, ps);
                return (long) ps.executeUpdate();
            });
        }

        /**
         * Executes a batch update for a given stream of domain objects.
         * Handles drivers returning SUCCESS_NO_INFO (-2).
         * Note: Requires connection.setAutoCommit(false) for transactional safety.
         *
         * @param domains A stream of domain entities to be updated in the database.
         * @param columns A list of column models defining which specific attributes should be updated.
         * @return The total number of rows affected by the batch execution.
         */
        private long updateStreamInternal(@NotNull Stream<D> domains, @NotNull List<ColumnModel<D, Object>> columns) {
            var sql = utilities.buildUpdateSql(columns);
            var limit = utilities.getBatchLimit();

            return utilities.run(true, dbconnection, sql, false, ps -> {
                var result = 0L;
                var batchCount = 0;
                var iterator = domains.iterator();

                while (iterator.hasNext()) {
                    var domain = iterator.next();
                    if (domain == null) continue;

                    utilities.setValuesAndPkToStatement(domain, columns, ps);
                    ps.addBatch();
                    batchCount++;

                    if (batchCount >= limit) {
                        result += utilities.sumBatchRows(ps.executeBatch());
                        batchCount = 0;
                    }
                }

                if (batchCount > 0) {
                    result += utilities.sumBatchRows(ps.executeBatch());
                }
                return result;
            });
        }

        @Override
        public int delete(@NotNull D domain) {
            Objects.requireNonNull(domain, "Domain object must not be null");
            return deleteById(utilities.getPrimaryKeyValue(domain));
        }

        @Override
        public int deleteById(@NotNull V id) {
            Objects.requireNonNull(id, "Identifier must not be null");
            var q = getQuote();
            var tableName = tableModel().tableName();
            var sql = new StringBuilder(64)
                    .append("DELETE FROM ").append(q).append(tableName).append(q)
                    .append(" WHERE ").append(q).append(pkColumn().name()).append(q).append(" = ?");
            return utilities.run(false, dbconnection, sql, false, ps -> {
                ps.setObject(1, id);
                return ps.executeUpdate();
            });
        }

        @Override
        public int delete(@NotNull Stream<D> domains) {
            Objects.requireNonNull(domains, "Stream of domains must not be null");
            var q = getQuote();
            var tableName = tableModel().tableName();
            var sql = new StringBuilder(64)
                    .append("DELETE FROM ").append(q).append(tableName).append(q)
                    .append(" WHERE ").append(q).append(pkColumn().name()).append(q).append(" = ?");

            var limit = utilities.getBatchLimit();

            return utilities.run(true, dbconnection, sql, false, ps -> {
                var result = 0L;
                var batchCount = 0;
                var iterator = domains.iterator();

                while (iterator.hasNext()) {
                    var domain = iterator.next();
                    if (domain == null) continue;

                    ps.setObject(1, utilities.getPrimaryKeyValue(domain));
                    ps.addBatch();
                    batchCount++;

                    if (batchCount >= limit) {
                        result += utilities.sumBatchRows(ps.executeBatch());
                        batchCount = 0;
                    }
                }

                if (batchCount > 0) {
                    result += utilities.sumBatchRows(ps.executeBatch());
                }
                return (int) result;
            });
        }

        /**
         * A stateful helper class to manage JDBC batch INSERT operations.
         * Encapsulates the PreparedStatement lifecycle, handles switching between auto-generated
         * primary keys and explicitly provided keys, and buffers entities to minimize memory footprint.
         */
        @RequiredArgsConstructor
        private final class BatchInserter implements AutoCloseable {
            @Nullable
            private final Consumer<D> onInserted;
            private final List<D> domains = new ArrayList<>(utilities.getBatchLimit());
            private final Key<D,V> pk = pk();
            private PreparedStatement ps = null;
            private Boolean genKeys = null;
            private List<ColumnModel<D, Object>> cols = null;

            /**
             * Adds a domain entity to the current batch.
             * If the batch limit is reached, or if the primary key generation strategy changes
             * for the incoming entity, the current batch is automatically flushed to the database.
             *
             * @param domain The entity to be inserted.
             * @return The number of rows affected if a flush occurred, 0 otherwise.
             * @throws SQLException If a database access error occurs.
             */
            public long add(D domain) throws SQLException {
                var pkVal = utilities.getPrimaryKeyValue(domain);
                var emptyPk = utilities.isPkEmpty(pkVal);
                var result = 0L;

                // Flush and recreate statement if the PK generation strategy changes
                if (genKeys != null && genKeys != emptyPk) {
                    result += flush();
                    close(); // Closes the current PreparedStatement
                }

                if (ps == null) {
                    genKeys = emptyPk;
                    cols = tableModel().createInsertedColumns(pkVal);
                    var sql = utilities.buildInsertSql(cols);
                    if (context.config().isPrintSql()) LOGGER.info(sql);
                    ps = !emptyPk
                            ? dbconnection.prepareStatement(sql)
                            : tableModel().jdbc().isOracleDb()
                            ? dbconnection.prepareStatement(sql, new String[]{pkColumn().name()})
                            : dbconnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                }

                utilities.setValuesToStatement(domain, cols, ps);
                ps.addBatch();
                domains.add(domain);
                return result + (domains.size() >= utilities.getBatchLimit() ? flush() : 0L);
            }

            /**
             * Executes the currently queued batch of INSERT statements, retrieves generated keys
             * (if applicable), and invokes the {@code onInserted} Consumer callback.
             *
             * @return The number of rows affected by the batch execution.
             * @throws SQLException If a database access error occurs.
             */
            public long flush() throws SQLException {
                if (domains.isEmpty() || ps == null) return 0L;
                var result = utilities.sumBatchRows(ps.executeBatch());
                if (onInserted != null) {
                    if (Boolean.TRUE.equals(genKeys)) {
                        try (var rs = ps.getGeneratedKeys()) {
                            for (var domain : domains) {
                                if (!rs.next()) throw new IllegalStateException("Missing key");
                                onInserted.accept(utilities.assignGeneratedKey(domain, rs, pk));
                            }
                        }
                    } else {
                        domains.forEach(onInserted);
                    }
                }
                domains.clear();
                return result;
            }

            /**
             * Safely closes the underlying {@link PreparedStatement} if it is open.
             * @throws SQLException If a database access error occurs.
             */
            @Override
            public void close() throws SQLException {
                try (var psOrig = ps) {
                } finally { ps = null; }
            }
        }
    }

    // --- STATIC METHOD(s) ---

    /** Factory method */
    public static <D, V> EntityManager<D,V> of(@NotNull Class<D> domainClass) {
        return of(domainClass, (Class<V>) null);
    }

    /** Factory method */
    public static <D, V> EntityManager<D, V> of(@NotNull Class<D> domainClass, @Nullable Class<V> type) {
        var context = Context.ofDefault();
        return new EntityManager<>(domainClass, context, ResultSetMapper.of(domainClass, context.config()));
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

    /** Factory method with provided connection */
    public static <D, V> EntityManager<D,V> of(@NotNull Class<D> domainClass, @NotNull Connection connection, @NotNull Context context) {
        var manager = new EntityManager<D, V>(domainClass, context, ResultSetMapper.of(domainClass, context.config()));
        manager.initModel(connection);
        return manager;
    }
}