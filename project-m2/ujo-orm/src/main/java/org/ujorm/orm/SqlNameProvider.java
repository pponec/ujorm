/*
 *  Copyright 2013-2014 Pavel Ponec
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
import java.util.List;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.Check;

/**
 * SQL name provider
 */
public class SqlNameProvider {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(SqlNameProvider.class);

    public String getUniqueConstraintName(List<MetaColumn> columns) {
        return getUniqueConstraintName(columns.toArray(new MetaColumn[]{} ));
    }

    /** getUniqueConstraintName */
    public String getUniqueConstraintName(MetaColumn... columns) {
        assert columns.length > 0;

        MetaTable table = columns[0].getTable();
        String constName = "uq__" + MetaTable.NAME.of(table);
        for (MetaColumn column : columns) {
            constName += "__";
            constName += column.getName();
        }
        return constName;
    }

    /** Create an exact index name */
    public String getIndexName(MetaColumn... columns) {
        assert columns.length > 0;

        MetaTable table = columns[0].getTable();

        String constName = "ix__" + MetaTable.NAME.of(table);
        for (MetaColumn column : columns) {
            constName += "__";
            constName += column.getName();
        }
        return constName;
    }

    /** Print a constraint name */
    protected void printConstraintName(final MetaTable table, final MetaColumn column, final Appendable out) throws IOException {
        final String cn = column.getConstraintName();
        if (Check.hasLength(cn)) {
            out.append(cn);
        } else {
            out.append("fk_");
            out.append(MetaTable.NAME.of(table));
            out.append("__"); // two characters before a column
            out.append(MetaColumn.NAME.of(column));
        }
    }

    /** SQL Name Provider */
    public String buildDefaultConstraintName(final MetaTable table, final MetaColumn column) {
        String result =
                "fk__"
                + MetaTable.NAME.of(table)
                + "__" // two characters before a column
                + MetaColumn.NAME.of(column);
        return result;
    }

    /** buildDefaultConstraintForDefaultValueName */
    public String buildDefaultConstraintForDefaultValueName(final MetaTable table, final MetaColumn column) {
        String result =
                "df__"
                + MetaTable.NAME.of(table)
                + "__" // two characters before a column
                + MetaColumn.NAME.of(column);
        return result;
    }

    /** BuildPrimaryKeyName */
    public String buildPrimaryKeyName(MetaTable table, List<MetaColumn> columns) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("pk__").append(MetaTable.NAME.of(table));
        for (MetaColumn col : columns) {
            String name = MetaColumn.NAME.of(col);
            out.append("__").append(name);
        }
        return out.toString();
    }
}
