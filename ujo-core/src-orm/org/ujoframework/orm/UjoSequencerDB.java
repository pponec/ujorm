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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.orm.metaModel.MetaDatabase;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaTable;

/**
 * A special sequence provider pro a native database sequences
 * @author Pavel Ponec
 * @deprecated The sequencer is only a sample of use
 */
public class UjoSequencerDB extends UjoSequencer {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(UjoSequencerDB.class.getName());

    public UjoSequencerDB(MetaTable table) {
        super(table);
    }

    /** Returns the sequence name */
    @Override
    public String getSequenceName() {
        return "CommonSequence";
    }

    /** Returns the <strong>next sequence value</strong> by a synchronized method. */
    @Override
    public synchronized long nextValue(final Session session) {

        if (sequence<seqLimit) {
            return ++sequence;
        }

		final MetaDatabase db = MetaTable.DATABASE.of(table);
		Connection connection;
		ResultSet res = null;
		String sql = null;
		PreparedStatement statement = null;
		StringBuilder out = new StringBuilder(64);

       	try {
            connection = session.getSeqConnection(db);
            String tableName = MetaTable.NAME.of(table);

            // get next sequence number from db generator:
            Integer cache = MetaParams.SEQUENCE_CACHE.of(this.getDatabase().getParams());
            sql = printSequenceNextValue(out).toString();

            if (LOGGER.isLoggable(Level.INFO)) { LOGGER.log(Level.INFO, sql + "; ["+tableName+']'); }
            statement = connection.prepareStatement(sql);

            res = statement.executeQuery();
            res.next();
            seqLimit = res.getLong(1);
            sequence = (seqLimit - cache) + 1; // Get the last assigned number + 1;
            connection.commit();

        } catch (Throwable e) {
            throw new IllegalStateException("ILLEGAL SQL: " + sql, e);
        } finally {
            MetaDatabase.close(null, statement, res, true);
        }
        return sequence;
    }


    /** Print SQL NEXT SEQUENCE Update or print none. The method is intended for an emulator of the sequence. */
    public Appendable printSequenceNextValue(final Appendable out) throws IOException {
        out.append("select gen_id (");
        out.append(MetaTable.NAME.of(table));
        out.append(") from rdb$database");
        return out;
    }

}
