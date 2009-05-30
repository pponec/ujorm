/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm;

import java.sql.ResultSet;
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmTable;

/**
 * Sequence provider
 * @author pavel
 */
public class UjoSequencer {

    //** Logger */
    //private static final java.util.logging.Logger.Logger LOGGER = java.util.logging.Logger.Logger.getLogger(UjoSequencer.class.toString());

    final private OrmDatabase database;
    final private OrmTable table;
    private long sequence = 0;
    private long seqLimit = 0;
    private int step = 20;

    public UjoSequencer(OrmDatabase database) {
        this.database = database;
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
                sql = database.getRenderer().printSeqNextValue(this, out).toString();
                statement = session.getStatement(database, sql);
                res = statement.executeQuery();
                res.next();
                sequence = res.getLong(1) + 1; // Get the last assigned number + 1;
                seqLimit = res.getLong(2);

                // update sequence:
                out.setLength(0);
                sql = database.getRenderer().printSeqNextValueUpdate(this, out).toString();
                if (sql.length()>0) {
                    // TODO: update must be updated by a different DB connection !!!
                    String tableKey = table!=null ? OrmTable.NAME.of(table) : SqlRenderer.COMMON_SEQ_TABLE_KEY ;
                    statement = session.getStatement(database, sql);
                    statement.getPreparedStatement().setString(1, tableKey);
                    statement.executeUpdate();
                }

            } catch (Throwable e) {
                throw new IllegalStateException("ILLEGAL SQL: " + sql, e);
            } finally {
                OrmDatabase.close(null, statement, res, true);
            }
            return sequence;
        }
    }

    /** Returns sequence name */
    public String getSequenceName() {
        return "OrmUjoSequence";
    }

    /** Returns a database schema */
    public String getDatabasSchema() {
        return OrmDatabase.SCHEMA.of(database);
    }

    /** The init sequence value is zero by default */
    public long getInitValue() {
        return getInitIncrement();
    }

    /** The UJO cache is the number of pre-allocated numbers inside the OrmUjo framework. */
    public int getInitIncrement() {
        return 32;
    }

    /** The cache of a database sequence is zero by default. */
    public int getInitDbCache() {
        return 0;
    }

    /** Returns model of the database */
    public OrmDatabase getDatabase() {
        return database;
    }

    /** Returns a related table or null if sequence is general for the all OrmDatabase space */
    public OrmTable getTable() {
        return table;
    }

}
