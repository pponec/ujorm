/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.orm;

import java.sql.ResultSet;
import org.ujoframework.orm.metaModel.MetaDatabase;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaTable;

/**
 * Sequence provider
 * @author Pavel Ponec
 */
public class UjoSequencer {

    //** Logger */
    //private static final java.util.logging.Logger.Logger LOGGER = java.util.logging.Logger.Logger.getLogger(UjoSequencer.class.toString());

    final private MetaDatabase database;
    final private MetaTable table;
    final private int increment;
    private long sequence = 0;
    private long seqLimit = 0;

    public UjoSequencer(MetaDatabase database) {
        this.database = database;
        this.increment = MetaParams.SEQUENCE_INCREMENT.of(database.getParams());
        this.table = null;
    }

    protected void loadBuffer() {

    }

    /** Returns the <strong>next sequence value</strong> by a synchronized method. */
    public synchronized long nextValue(final Session session) {

        if (sequence<seqLimit) {
            return ++sequence;
        } else {
            JdbcStatement statement = null;
            ResultSet res = null;
            String sql = "";
            StringBuilder out = new StringBuilder(64);
            try {
                sql = database.getDialect().printSeqNextValue(this, out).toString();
                statement = session.getStatement(database, sql);
                res = statement.executeQuery();
                res.next();
                seqLimit = res.getLong(1);
                sequence = (seqLimit - increment) + 1; // Get the last assigned number + 1;

                // update sequence:
                out.setLength(0);
                sql = database.getDialect().printSeqNextValueUpdate(this, out).toString();
                if (sql.length()>0) {
                    // TODO: sequence must be updated by a different DB connection !!!
                    String tableKey = table!=null ? MetaTable.NAME.of(table) : SqlDialect.COMMON_SEQ_TABLE_KEY ;
                    statement = session.getStatement(database, sql);
                    statement.getPreparedStatement().setString(1, tableKey);
                    statement.executeUpdate();
                }

            } catch (Throwable e) {
                throw new IllegalStateException("ILLEGAL SQL: " + sql, e);
            } finally {
                MetaDatabase.close(null, statement, res, true);
            }
            return sequence;
        }
    }

    /** Returns the sequence name */
    public String getSequenceName() {
        return "OrmUjoSequence";
    }

    /** Returns a database schema */
    public String getDatabasSchema() {
        return MetaDatabase.SCHEMA.of(database);
    }

    /** The UJO cache is the number of pre-allocated numbers inside the OrmUjo framework. */
    public int getIncrement() {
        return increment;
    }

    /** The cache of a database sequence is zero by default. */
    public int getInitDbCache() {
        return 1;
    }

    /** Returns model of the database */
    public MetaDatabase getDatabase() {
        return database;
    }

    /** Returns a related table or null if sequence is general for the all MetaDatabase space */
    public MetaTable getTable() {
        return table;
    }

}
