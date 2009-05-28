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
package org.ujoframework.orm.renderers;

import java.io.IOException;
import java.util.List;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.SqlRenderer;
import org.ujoframework.orm.UjoSequencer;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;

/** Derby (http://db.apache.org/derby/) */
public class DerbyRenderer extends SqlRenderer {

    /** Syntax: jdbc:derby:[subsubprotocol:][databaseName][;attribute=value]* */
    @Override
    public String getJdbcUrl() {
        return "jdbc:derby:c:\\temp\\derby-sample;create=true";
    }

    /** Embeded driver */
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

    /** Print SQL CREATE SEQUENCE. */
    @Override
    public Appendable printCreateSequence(final UjoSequencer sequence, final Appendable out) throws IOException {
        // TODO:
        out.append("CREATE SEQUENCE ");
        out.append(sequence.getSequenceName());
        out.append(" START WITH " + sequence.getInitValue());
        out.append(" INCREMENT BY " + sequence.getInitIncrement());
        out.append(" CACHE " + sequence.getInitCacheSize());
        return out;
    }



    /** Print foreign key for the parameter column */
    @Override
    @SuppressWarnings("unchecked")
    public Appendable printForeignKey(OrmColumn column, OrmTable table, Appendable out) throws IOException {
        final UjoProperty property = column.getProperty();
        final OrmTable foreignTable = ormHandler.findTableModel(property.getType());
        OrmPKey foreignKeys = OrmTable.PK.of(foreignTable);

        out.append("ALTER TABLE ");
        printFullName(table, out);
        out.append("\n\tADD CONSTRAINT fk_");
        out.append(OrmTable.NAME.of(table));
        out.append('_');
        out.append(OrmColumn.NAME.of(column));
        out.append(" FOREIGN KEY");

        List<OrmColumn> columns = OrmPKey.COLUMNS.of(foreignKeys);
        int columnsSize = columns.size();

        for (int i=0; i<columnsSize; ++i) {
            out.append(i==0 ? "(" : ", ");
            final String name = column.getForeignColumnName(i);
            out.append(name);
        }

        out.append(")\n\tREFERENCES ");
        printFullName(foreignTable, out);
        String separator = "(";

        for (OrmColumn fkColumn : OrmPKey.COLUMNS.of(foreignKeys)) {
            out.append(separator);
            separator = ", ";
            out.append(OrmColumn.NAME.of(fkColumn));
        }

        out.append(")");
        //out.append("\n\tON DELETE CASCADE");
        return out;
    }


}
