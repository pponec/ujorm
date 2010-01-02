/*
 *  Copyright 2009-2010 Pavel Ponec
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

package org.ujoframework.orm.dialect;

import java.io.IOException;
import org.ujoframework.orm.CriterionDecoder;
import org.ujoframework.orm.SqlDialect;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaDatabase;
import org.ujoframework.orm.metaModel.MetaTable;

/** Dialect for the MySQL since release 5.0 for the InnoDB engine.
 * <br><a href="http://dev.mysql.com/">http://dev.mysql.com/</a>
 */
public class MySqlDialect extends SqlDialect {

    @Override
    public String getJdbcUrl() {
        return "jdbc:mysql://127.0.0.1:3306/db1";
    }

    @Override
    public String getJdbcDriver() {
        return "com.mysql.jdbc.Driver";
    }

    /** Print an SQL DELETE statement. */
    @Override
    public Appendable printDelete
        ( MetaTable table
        , CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {

        MetaTable[] tables = decoder.getTablesSorted(table);

        if (tables.length>1) {
            out.append("DELETE FROM ");
            for (int i = 0; i < tables.length; i++) {
                if (i > 0) { out.append(", "); }
                printTableAliasDefinition(tables[i], out);
            }
            out.append(" WHERE ");
            out.append(decoder.getWhere());
            
        } else {
            String fullTableName = printFullTableName(table, new StringBuilder(64)).toString();
            String where = decoder.getWhere().replace(table.getAlias()+'.', fullTableName+'.');
            //
            out.append("DELETE FROM ");
            out.append(fullTableName);
            out.append(" WHERE ");
            out.append(where);
        }
        return out;
    }

    @Override
    public Appendable printSequenceTable(MetaDatabase db, Appendable out) throws IOException {
        return super.printSequenceTable(db, out).append(getEngine());
    }

    @Override
    public Appendable printTable(MetaTable table, Appendable out) throws IOException {
        return super.printTable(table, out).append(getEngine());
    }

    /** Returns a MySQL enginge. <br>
     * The default value is " ENGINE = InnoDB". */
    protected String getEngine() {
        return " ENGINE = InnoDB";
    }

    /** MySQL dialect uses a database type DATETIME (instead of the TIMESTAMP) for the java.util.Date. */
    @Override
    protected String getColumnType(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {
            case TIMESTAMP:
                return "DATETIME";
            default:
                return super.getColumnType(column);
        }
    }

}
