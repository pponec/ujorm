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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.orm.metaModel.MetaDatabase;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaTable;

/**
 * Sequence provider
 * @author Pavel Ponec
 */
public class UjoSequencer {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(UjoSequencer.class.toString());

    final private MetaTable table;
    private long sequence = 0;
    private long seqLimit = 0;

    public UjoSequencer(MetaTable table) {
        this.table = table;
    }

    /** Returns the <strong>next sequence value</strong> by a synchronized method. */
    public synchronized long nextValue() {

        if (sequence<seqLimit) {
            return ++sequence;
        } else {

            final MetaDatabase db = MetaTable.DATABASE.of(table);
            Connection connection;
            ResultSet res = null;
            String sql = null;
            PreparedStatement statement = null;
            StringBuilder out = new StringBuilder(64);
            try {
                connection = db.createConnection();
                String tableName = db.getDialect().printFullTableName(getTable(), out).toString();
                out.setLength(0);

                // UPDATE the next sequence:
                out.setLength(0);
                sql = db.getDialect().printSequenceNextValue(this, out).toString();
                
                if (LOGGER.isLoggable(Level.INFO)) { LOGGER.log(Level.INFO, sql + "; ["+tableName+']'); }
                statement = connection.prepareStatement(sql);
                statement.setString(1, tableName);
                int i = statement.executeUpdate();

                if (i==0) {
                    // INSERT the new sequence:
                    out.setLength(0);
                    sql = db.getDialect().printSequenceInit(this, out).toString();
                    if (LOGGER.isLoggable(Level.INFO)) { LOGGER.log(Level.INFO, sql + "; ["+tableName+']'); }
                    statement = connection.prepareStatement(sql);
                    statement.setString(1, tableName);
                    statement.executeUpdate();
                }

                // SELECT UPDATE:
                out.setLength(0);
                sql = db.getDialect().printSequenceCurrentValue(this, out).toString();
                if (LOGGER.isLoggable(Level.INFO)) { LOGGER.log(Level.INFO, sql + "; ["+tableName+']'); }
                statement = connection.prepareStatement(sql);
                statement.setString(1, tableName);
                res = statement.executeQuery();
                res.next();
                seqLimit = res.getLong(1);
                int step = res.getInt(2);
                sequence = (seqLimit - step) + 1; // Get the last assigned number + 1;
                connection.commit();


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

    /** Returns the database schema */
    public String getDatabaseSchema() {
        return MetaDatabase.SCHEMA.of(getDatabase());
    }

    /** The UJO cache is the number of pre-allocated numbers inside the OrmUjo framework. */
    public int getIncrement() {
        final int result = MetaParams.SEQUENCE_CACHE.of(table.getDatabase().getParams());
        return result;
    }

    /** The cache of a database sequence is zero by default. */
    public int getInitDbCache() {
        return 1;
    }

    /** Returns model of the database */
    public MetaDatabase getDatabase() {
        return MetaTable.DATABASE.of(table);
    }

    /** Returns a related table or null if sequence is general for the all MetaDatabase space */
    public MetaTable getTable() {
        return table;
    }

}
