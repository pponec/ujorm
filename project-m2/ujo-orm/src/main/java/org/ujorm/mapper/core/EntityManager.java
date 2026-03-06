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
import org.ujorm.mapper.Crud;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.jdbc.ResultSetMapper;
import org.ujorm.mapper.model.ColumnModel;
import org.ujorm.mapper.model.TableModel;
import org.ujorm.mapper.model.TableModelBuilder;
import org.ujorm.mapper.utils.StatementCache;
import org.ujorm.mapper.utils.Tools;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;
import org.ujorm.tools.jdbc.SqlParamBuilder;

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

    /** Map a ResultSet to the Domain object. */
    public D map(@NotNull ResultSet rs, @Nullable CharSequence... columnLabels) {
        return resultSetMapper.map(rs, columnLabels);
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
                columns.add(model.getColumn(key.index()));
            }
            return buildUpdateSql(columns);
        }

        /** Logs and executes the SQL statement using the provided connection. */
        public <R> R run(boolean batch, @NotNull Connection connection, final CharSequence sql, final boolean returnGeneratedKeys, final SqlFunction<PreparedStatement, R> fun) {
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

    /** The CRUD operations' implementation. */
    public final class CrudImpl implements Crud<D, V> {
        private final Connection dbconnection;

        public CrudImpl(@NotNull Connection dbconnection) {
            this.dbconnection = dbconnection;
        }

        public final long insertBatch(@NotNull Stream<D> domains, @Nullable Consumer<D> onInserted) {
            var result = 0L;
            if (domains == null) return result;
            var safeStream = domains.isParallel() ? domains.sequential() : domains;
            try (var inserter = new BatchInserter(dbconnection, onInserted)) {
                utilities.checkAutoCommit(dbconnection);

                var iterator = safeStream.iterator();
                while (iterator.hasNext()) {
                    var domain = iterator.next();
                    if (domain == null) continue;
                    result += inserter.add(domain);
                }
                result += inserter.flush();
            } catch (SQLException e) {
                throw SQLExceptionBuilder.build("Batch insert failed", e);
            }
            return result;
        }

        @Override
        @SafeVarargs
        public final D[] insertBatch(@NotNull D... domains) {
            if (domains == null || domains.length == 0) {
                return domains;
            }
            var index = new AtomicInteger(0);
            insertBatch(Arrays.stream(domains), newDomain -> {
                while (index.get() < domains.length && domains[index.get()] == null) {
                    index.incrementAndGet();
                }
                if (index.get() < domains.length) {
                    domains[index.getAndIncrement()] = newDomain;
                }
            });
            return domains;
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
                    return resultSetMapper.convert(rs, labels).findFirst();
                }
            });
        }

        /**
         * Creates an instance of SqlParamBuilder to bind parameters and execute SELECT.
         * The builder shares the database connection with this object.
         *
         * @param whereCondition Undefined or empty value returns all records.
         * @return Query builder
         */
        @Override
        @NotNull
        public SqlParamBuilder select(@Nullable String whereCondition) {
            var sql = new StringBuilder(128);
            buildSelectSql(true, sql);
            sql.append(" WHERE ");
            sql.append(whereCondition == null || whereCondition.isEmpty() ? "1=1" : whereCondition);
            return new SqlParamBuilder(dbconnection).sql(sql.toString()).fetchSize(utilities.getBatchLimit());
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
        public long updateBatch(@NotNull Stream<D> domains, CharSequence... properties) {
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
        public int deleteBatch(@NotNull Stream<D> domains) {
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
    }

    /** Stateful helper to manage JDBC batch inserts and encapsulate execution parameters. */
    @lombok.RequiredArgsConstructor
    private final class BatchInserter implements AutoCloseable {
        private final Connection dbconnection;
        private final Consumer<D> onInserted;

        private final int limit = utilities.getBatchLimit();
        private final Key<D, V> pk = pk();
        private final List<D> batchItems = new java.util.ArrayList<>(limit);

        private PreparedStatement ps;
        private Boolean generateKeys;
        private List<ColumnModel<D, Object>> columns;
        private int batchCount = 0;

        /** Adds a domain object to the batch, executing flush if necessary. */
        public long add(D domain) throws SQLException {
            var pkOriginalValue = utilities.getPrimaryKeyValue(domain);
            var currentDomainGenerateKeys = utilities.isPkEmpty(pkOriginalValue);
            var result = 0L;

            if (generateKeys != null && generateKeys != currentDomainGenerateKeys) {
                result += flush();
                closePreparedStatement();
            }

            if (ps == null) {
                generateKeys = currentDomainGenerateKeys;
                columns = tableModel().createInsertedColumns(pkOriginalValue);
                var sql = utilities.buildInsertSql(columns);

                if (context.config().isPrintSql()) {
                    LOGGER.info(sql);
                }

                ps = !generateKeys
                        ? dbconnection.prepareStatement(sql)
                        : tableModel().jdbc().isOracleDb()
                        ? dbconnection.prepareStatement(sql, new String[]{pkColumn().name()})
                        : dbconnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }

            utilities.setValuesToStatement(domain, columns, ps);
            ps.addBatch();
            batchItems.add(domain);
            batchCount++;

            if (batchCount >= limit) {
                result += flush();
            }
            return result;
        }

        /** Executes the current batch and triggers the consumer callback. */
        public long flush() throws SQLException {
            if (batchCount == 0 || ps == null) {
                return 0L;
            }

            var batchResults = ps.executeBatch();
            var result = utilities.sumBatchRows(batchResults);

            if (Boolean.TRUE.equals(generateKeys) && onInserted != null) {
                try (var rs = ps.getGeneratedKeys()) {
                    for (var domain : batchItems) {
                        if (rs.next()) {
                            onInserted.accept(utilities.assignGeneratedKey(domain, rs, pk));
                        } else {
                            throw new IllegalStateException("Missing generated key");
                        }
                    }
                }
            } else if (onInserted != null) {
                for (var domain : batchItems) {
                    onInserted.accept(domain);
                }
            }

            batchCount = 0;
            batchItems.clear();
            return result;
        }

        /** Closes the PreparedStatement safely. */
        private void closePreparedStatement() throws SQLException {
            if (ps != null) {
                ps.close();
                ps = null;
            }
        }

        @Override
        public void close() throws SQLException {
            closePreparedStatement();
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

    /** Factory method with provided connection */
    public static <D, V> EntityManager<D,V> of(@NotNull Class<D> domainClass, @NotNull Connection connection, @NotNull Context context) {
        var manager = new EntityManager<D, V>(domainClass, context, ResultSetMapper.of(domainClass));
        manager.initModel(connection);
        return manager;
    }
}