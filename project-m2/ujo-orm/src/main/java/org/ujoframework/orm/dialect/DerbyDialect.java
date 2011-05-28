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
import java.util.List;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.SqlDialect;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaTable;

/** Derby (http://db.apache.org/derby/)
 * <br/>NOTE: Dialect supports a LIMIT and OFFSET of the Derby release 10.5
 * @see <a href="http://db.apache.org/derby/docs/10.5/ref/">Derby documentation</a>
 */
public class DerbyDialect extends SqlDialect {

    /** Syntax: jdbc:derby:[subsubprotocol:][databaseName][;attribute=value]* */
    @Override
    public String getJdbcUrl() {
        return "jdbc:derby:c:\\temp\\derby-sample;create=true";
    }

    /** Embeded driver is default */
    @Override
    public String getJdbcDriver() {
        return "org.apache.derby.jdbc.EmbeddedDriver";
    }

    /** Print SQL 'CREATE SCHEMA' */
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA ");
        out.append(schema);
        return out;
    }


    /** Print foreign key for the parameter column */
    @Override
    @SuppressWarnings("unchecked")
    public Appendable printForeignKey(MetaColumn column, MetaTable table, Appendable out) throws IOException {

        List<MetaColumn> fColumns = column.getForeignColumns();
        MetaTable foreignTable = fColumns.get(0).getTable();
        int columnsSize = fColumns.size();

        out.append("ALTER TABLE ");
        printFullTableName(table, out);
        out.append("\n\tADD CONSTRAINT fk_");
        out.append(MetaTable.NAME.of(table));
        out.append('_');
        out.append(MetaColumn.NAME.of(column));
        out.append(" FOREIGN KEY");

        for (int i=0; i<columnsSize; ++i) {
            out.append(i==0 ? "(" : ", ");
            final String name = column.getForeignColumnName(i);
            out.append(name);
        }

        out.append(")\n\tREFERENCES ");
        printFullTableName(foreignTable, out);
        String separator = "(";

        for (MetaColumn fColumn : fColumns) {
            out.append(separator);
            separator = ", ";
            out.append(MetaColumn.NAME.of(fColumn));
        }

        out.append(")");
        //out.append("\tON DELETE CASCADE");
        return out;
    }


    @Override
    public void printOffset(Query query, Appendable out) throws IOException {
        out.append(" OFFSET " + query.getOffset());
        out.append(" ROWS");
    }

    /** COMMENT is not supported in HSQLDB database */
    @Override
    public Appendable printComment(MetaTable table, Appendable out) throws IOException {
        return out;
    }

    /** COMMENT is not supported in HSQLDB database */
    @Override
    public Appendable printComment(MetaColumn table, Appendable out) throws IOException {
        return out;
    }
    
}
