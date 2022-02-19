/*
 *  Copyright 2010-2022 Pavel Ponec
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
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.jdbc.JdbcBuilder;

/**
 * Optional correction the internal Ujorm sequences
 * @see org.ujorm.orm.metaModel.MetaParams#FIXING_TABLE_SEQUENCES
 * @author Pavel Ponec
 */
public class FixingTableSequences implements Runnable {

    /** DB column order */
    protected static final int COLUMN_ID = 1;
    /** DB column order */
    protected static final int COLUMN_VALUE = COLUMN_ID + 1;

    /** Full table name of sequence */
    protected final Connection connection;
    /** Database model */
    protected final MetaDatabase db;
    /** Dialect */
    protected final SqlDialect dialect;
    /** Table model of seqences */
    protected final SeqTableModel seqModel;
    /** Full table name of sequence */
    protected final String sequenceTableName;

    /** Constructor */
    public FixingTableSequences(@Nullable final MetaDatabase db, @NotNull final Connection conn) throws Exception {
        final boolean noDb = db == null;

        this.db = db;
        this.connection = noDb ? null : conn;
        this.dialect    = noDb ? null : db.getDialect();
        this.seqModel   = noDb ? null : dialect.getSeqTableModel();
        this.sequenceTableName = noDb ? null
            : dialect.printSequenceTableName(MetaDatabase.SCHEMA.of(db), new StringBuilder()).toString();
    }

    @Override
    public void run() throws IllegalStateException {
        Assert.notNull(db, "Database model is required");
        try {
            onBefore(connection);
            runInternal();
            onAfter(connection);
        } catch (SQLException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Method called before internal processing is empty by default */
    protected void onBefore(@NotNull final Connection connection) throws SQLException, IOException {
    }

    /** Method called after internal processing is empty by default */
    protected void onAfter(@NotNull final Connection connection) throws SQLException, IOException {
    }

    /** A correction code */
    protected void runInternal() throws SQLException, IOException {
        final Set<String> wrongSet = new HashSet<>();
        final String id = "%" + dialect.getQuoteChar(true) + "%" + dialect.getQuoteChar(false);

        try (PreparedStatement ps = selectFromSequence(id, COLUMN_ID, true); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                wrongSet.add(rs.getString(COLUMN_ID));
            }
        }
        if (wrongSet.isEmpty()) {
            return;
        }
        for (MetaTable table : MetaDatabase.TABLES.getList(db)) {
            if (table.isTable()) {
                String tableIdOk = table.getSequencer().getTableName();
                String tableIdWrong = dialect.printFullTableName(table, new StringBuilder(32)).toString();

                switch (db.getParams().get(MetaParams.QUOTATION_POLICY)) {
                    default:
                        continue;
                    case QUOTE_SQL_NAMES:
                    case QUOTE_ONLY_SQL_KEYWORDS:
                        // Do conversion ...
                }
                if (db.getParams().get(MetaParams.SEQUENCE_SCHEMA_SYMBOL)) {
                    int i = tableIdWrong.indexOf('.');
                    if (i > 0) {
                        tableIdWrong = UjoSequencer.DEFAULT_SCHEMA_SYMBOL + tableIdWrong.substring(i);
                    }
                }
                if (tableIdOk.equals(tableIdWrong)) {
                    continue;
                }

                if (wrongSet.contains(tableIdWrong)) {
                    final Long v1 = selectValueFromSequence(tableIdOk, COLUMN_VALUE);
                    final Long v2 = selectValueFromSequence(tableIdWrong, COLUMN_VALUE);
                    final Long value = max(v1, v2);

                    if (v1 == null) {
                        insertSequence(tableIdOk, table);
                        updateSequence(tableIdOk, value);
                    } else if (v1 < value) {
                        updateSequence(tableIdOk, value);
                    }
                    if (v2 != null) {
                        deleteSequence(tableIdWrong);
                    }
                }
            }
        }
    }

    /** Insert new sequence record to table by a dialect */
    public void insertSequence(String id, MetaTable table)
        throws IOException, SQLException {
        final String sql = dialect.printSequenceInit(table.getSequencer(), new StringBuilder()).toString();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    /** Returns the great value */
    protected long max(@Nullable Long v1, @Nullable Long v2) {
        if (v1 == null) {
            v1 = 0L;
        }
        if (v2 == null) {
            v2 = 0L;
        }
        return v1 > v2 ? v1 : v2;
    }


    /** Return the required column from sequences table<br>
     * SELECT {} FROM {} WHERE {} LIKE '?' */
    @Nullable
    protected <T> T selectValueFromSequence(@NotNull final String id, final int dbColumn) throws SQLException, IOException {
        try (PreparedStatement ps = selectFromSequence(id, dbColumn, false); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                switch (dbColumn) {
                    case COLUMN_ID:
                        return (T) rs.getObject(dbColumn, String.class);
                    case COLUMN_VALUE:
                        return (T) rs.getObject(dbColumn, Long.class);
                    default:
                        throw new IllegalArgumentException("Unsupporrted column: " + dbColumn);
                }
            }
            return null;
        }
    }

    /** Return the first column from sequences table<br>
     * SELECT {}, {} FROM {} WHERE {} LIKE '?' */
    protected PreparedStatement selectFromSequence(@NotNull final String id, final int dbColumn, boolean likeOp) throws SQLException, IOException {
        final JdbcBuilder sql = new JdbcBuilder()
           .write("SELECT")
           .column(dialect.getQuotedName(seqModel.getId())) // 1
           .column(dialect.getQuotedName(seqModel.getSequence())) // 2
           .write("FROM")
           .write(sequenceTableName)
           .write("WHERE")
           .andCondition(dialect.getQuotedName(seqModel.getId()), likeOp ? "LIKE" : "=", id);

        return sql.prepareStatement(connection);
    }

    /** UPDATE {} SET {} = ? WHERE {} = '?' */
    protected int updateSequence(@NotNull final String id, final long value) throws SQLException, IOException {
        final JdbcBuilder sql = new JdbcBuilder()
           .write("UPDATE")
           .write(sequenceTableName)
           .write("SET")
           .columnUpdate(dialect.getQuotedName(seqModel.getSequence()), value)
           .write("WHERE")
           .andCondition(dialect.getQuotedName(seqModel.getId()), "=", id);

        return sql.executeUpdate(connection);
    }

    /** DELETE FROM {} WHERE {} = '?' */
    protected int deleteSequence(@NotNull final String id) throws SQLException, IOException {
        final JdbcBuilder sql = new JdbcBuilder()
           .write("DELETE FROM")
           .write(sequenceTableName)
           .write("WHERE")
           .andCondition(dialect.getQuotedName(seqModel.getId()), "=", id);

        return sql.executeUpdate(connection);
    }

}
