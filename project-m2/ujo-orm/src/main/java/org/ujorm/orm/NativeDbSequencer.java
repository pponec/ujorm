/*
 *  Copyright 2013-2022 Pavel Ponec
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.implementation.orm.OrmProperty;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.annot.Table;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.Check;

/**
 * Support for the native database sequences.
 * The sequence name can be specified in the parameter {@link Table#sequence()}.
 * @author Pavel Ponec
 */
public class NativeDbSequencer extends UjoSequencer {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(OrmProperty.class);

    /** Required constructor */
    public NativeDbSequencer(MetaTable table) {
        super(table);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized long nextValue(Session session) {
        final String sequenceName = MetaTable.SEQUENCE.of(table);
        if (Check.hasLength(sequenceName)) {
            try (Statement statement = session.getFirstConnection().createStatement()) {
                try (ResultSet rs = statement.executeQuery(createNextSequence(sequenceName))) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    } else {
                        throw new IllegalUjormException("No value for sequence: " + sequenceName);
                    }
                 }
            } catch (RuntimeException | SQLException | IOException e) {
                final String msg = "Sequence error for name: " + sequenceName;
                LOGGER.log(UjoLogger.ERROR, msg, e);
                throw new IllegalUjormException(msg, e);
            }
        }
        return super.nextValue(session);
    }

    /** Create a SQL script for the NEXT SEQUENCE from a native database sequencer */
    public String createNextSequence(String sequenceName) throws IOException {
        return table.getDatabase()
              .getDialect()
              .printNextSequence(sequenceName, table, new StringBuilder(128))
              .toString();
    }

    /** Reset is unsupported */
    @Override
    public synchronized void reset() {
        throw new UnsupportedOperationException("Reset is unsupported for the table " + getTableName());
    }
}