/*
 *  Copyright 2009-2022 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.orm;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.Assert;
import static org.ujorm.tools.Check.hasLength;

/**
 * The default sequence provider.
 * A result value is received from a special database table.
 * @author Pavel Ponec
 */
public class UjoSequencer {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(UjoSequencer.class);

    /** The default schema symbol */
    public static final String DEFAULT_SCHEMA_SYMBOL = "~";
    /** DB field: seqLimit */
    public static final int SEQ_LIMIT = 1;
    /** DB field: step */
    public static final int SEQ_STEP = 1 + SEQ_LIMIT;
    /** DB field: maxValue */
    public static final int SEQ_MAX_VALUE = 1 + SEQ_STEP;

    /** Basic table. */
    final protected MetaTable table;
    /** Basic database */
    transient private MetaDatabase db = null;
    /** Current sequence value */
    protected long sequence = 0;
    /** Buffer limit */
    protected long seqLimit = 0;
    /** Total limit, zero means no restriction */
    protected long maxValue = 0;

    public UjoSequencer(@NotNull MetaTable table) {
        this.table = table;
    }

    /** Returns the <strong>next sequence value</strong> by a synchronized method. */
    public synchronized long nextValue(final Session session) {

        if (sequence<seqLimit) {
            return ++sequence;
        } else {
            final MetaDatabase db = getDatabase();
            Connection connection = null;
            String sql = null;
            StringBuilder out = new StringBuilder(64);
            try {
                connection = session.getSeqConnection(db);
                String tableName = getTableName();

                // UPDATE the next sequence:
                out.setLength(0);
                sql = db.getDialect().printSequenceNextValue(this, out).toString();

                if (LOGGER.isLoggable(UjoLogger.TRACE)) {
                    LOGGER.log(UjoLogger.TRACE, "{}; [{}]", sql, tableName);
                }
                final int i = executeSql(connection, sql, tableName);
                if (i==0) {
                    // INSERT the new sequence:
                    out.setLength(0);
                    sql = db.getDialect().printSequenceInit(this, out).toString();
                    if (LOGGER.isLoggable(UjoLogger.TRACE)) {
                        LOGGER.log(UjoLogger.TRACE, "{}; [{}]", sql, tableName);
                    }
                    executeSql(connection, sql, tableName);
                }

                // SELECT UPDATE:
                long[] seqMap = getCurrentDBSequence(connection, out);
                seqLimit = seqMap[SEQ_LIMIT];
                int step = (int) seqMap[SEQ_STEP];
                maxValue = seqMap[SEQ_MAX_VALUE];
                sequence = seqLimit - step + 1; // Get the last assigned number + 1;

                if (LOGGER.isLoggable(UjoLogger.INFO)) {
                    final String msg = getClass().getSimpleName()
                            + ": tableName=" + tableName
                            + ", seqLimit=" + seqLimit
                            + ", step=" + step
                            + ", maxValue=" + maxValue
                            + ", sequence=" + sequence;
                    LOGGER.log(UjoLogger.INFO, msg);
                }

                if (maxValue!=0L) {
                    if (seqLimit>maxValue) {
                        seqLimit=maxValue;

                        Assert.isTrue(sequence <= maxValue
                                , "The sequence '{}' needs to raise the maximum value: {}"
                                , tableName
                                , maxValue);

                        out.setLength(0);
                        sql = db.getDialect().printSequenceNextValue(this, out).toString();
                        if (LOGGER.isLoggable(UjoLogger.INFO)) {
                            LOGGER.log(UjoLogger.INFO, "{}; [{}]", sql, tableName);
                        }
                        executeSql(connection, sql, tableName);
                    }
                    if (maxValue > Long.MAX_VALUE - step) {
                        String msg = "The sequence attribute '"
                            + tableName
                            + ".maxValue' is too hight,"
                            + " the recommended maximal value is: "
                            +   (Long.MAX_VALUE-step)
                            + " (Long.MAX_VALUE-step)"
                            ;
                        LOGGER.log(UjoLogger.WARN, msg);
                    }
                }
                connection.commit();

            } catch (IOException | SQLException | RuntimeException | OutOfMemoryError e) {
                if (connection!=null) try {
                    connection.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(UjoLogger.WARN, "Rollback fails");
                }
                IllegalStateException exception = e instanceof IllegalStateException
                    ? (IllegalStateException) e
                    : new IllegalUjormException("ILLEGAL SQL: " + sql, e);
                throw exception;
            }
            return sequence;
        }
    }

    /** Returns the database schema */
    public String getDatabaseSchema() {
        return MetaDatabase.SCHEMA.of(getDatabase());
    }

    /** The UJO cache is the number of pre-allocated numbers inside the OrmUjo framework. */
    public int getIncrement() {
        final int result = MetaParams.SEQUENCE_CACHE.of(getDatabase().getParams());
        return result;
    }

    /** The cache of a database sequence is zero by default. */
    public int getInitDbCache() {
        return 1;
    }

    /** Returns model of the database */
    public MetaDatabase getDatabase() {
        if (db == null) {
            db = MetaTable.DATABASE.of(table);
        }
        return db;
    }

    /** Returns a related table or null if sequence is general for the all MetaDatabase space */
    @Nullable
    public MetaTable getTable() {
        return table;
    }

    /** Returns related table name with no quoting */
    protected String getTableName() {
        final StringBuilder result = new StringBuilder(32);
        final String tableSchema = MetaTable.SCHEMA.of(table);
        final String tableName = MetaTable.NAME.of(table);

        if (hasLength(tableSchema)) {
            result.append(table.isDefaultSchema() ? DEFAULT_SCHEMA_SYMBOL : tableSchema).append('.');
        }
        result.append(tableName);
        return result.toString();
    }

    /** Method returns true because the internal table 'ujorm_pk_support' is required to get a next sequence value.
     * In case you have a different implementation, there is possible overwrite this method and return an another value. */
    public boolean isSequenceTableRequired() {
        return true;
    }

    /** Forces to reload sequence from db on next call for nextValue. */
    public synchronized void reset() {
        sequence = 0;
        seqLimit = 0;
        maxValue = 0;

        LOGGER.log(UjoLogger.INFO
              , "{}: reset the sequencer for the table {}"
              , getClass().getSimpleName()
              , getTableName());
    }

    /** Returns current db sequence for an actual table with a performance optimizations.
     * @param connection Connection
     * @param sql Temporary buffer for a better performance. The value can be {@code null} a not null will be cleaned always.
     * @return Returns current db sequence for an actual table with a value order:
     * <br>[SEQ_LIMIT, SEQ_STEP, SEQ_MAX_VALUE].
     * <br>If no sequence is found then the method returns the value {@code null}.
     * @throws java.io.IOException
     */
    @Nullable
    public long[] getCurrentDBSequence(final Connection connection, StringBuilder sql) throws SQLException, IOException  {
        if (sql != null) {
            sql.setLength(0);
        } else {
            sql = new StringBuilder(64);
        }
        final String tableName = getTableName();

        sql.setLength(0);
        getDatabase().getDialect().printSequenceCurrentValue(this, sql);

        PreparedStatement statement = null;
        ResultSet res = null;
        try {
            statement = connection.prepareStatement(sql.toString());
            statement.setString(1, tableName);
            res = statement.executeQuery();
            if (res.next()) {
                long[] result = new long[1 + SEQ_MAX_VALUE];
                result[SEQ_LIMIT] = res.getLong(SEQ_LIMIT);
                result[SEQ_STEP] = res.getLong(SEQ_STEP);
                result[SEQ_MAX_VALUE] = res.getLong(SEQ_MAX_VALUE);
                return result;
            } else {
                return null;
            }
        } finally {
            MetaDatabase.close(null, statement, res, true);
        }
    }

    /**
     * Executes UPDATE for required parameters
     * @param connection JDBC connection
     * @param sql The SQL statement
     * @param tableName full table name (including schema, if any)
     * @return The count of modified database rows.
     * @throws SQLException
     */
    protected int executeSql
            ( @NotNull final Connection connection
            , @NotNull final String sql
            , @NotNull final String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            return statement.executeUpdate();
        }
    }
}
