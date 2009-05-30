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
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;

/** Derby (http://db.apache.org/derby/) */
public class DerbyRenderer extends SqlRenderer {

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

    /** Print SQL CREATE SEQUENCE. */
    @Override
    public Appendable printCreateSequence(final UjoSequencer sequence, final Appendable out) throws IOException {
        String seqTable = sequence.getDatabasSchema()+'.'+sequence.getSequenceName();
        out.append("CREATE TABLE ");
        out.append(seqTable);
        out.append("\n\t( id VARCHAR(128) NOT NULL PRIMARY KEY");
        out.append("\n\t, seq BIGINT DEFAULT " + 0);
        out.append("\n\t, step INT DEFAULT " + sequence.getInitIncrement());
        out.append("\n\t);");
        println(out);

        // Insert common data:
        out.append("INSERT INTO ");
        out.append(seqTable);
        out.append(" (id) VALUES ('"+COMMON_SEQ_TABLE_KEY+"');");
        println(out);

        for (OrmTable table : OrmDatabase.TABLES.getValue(sequence.getDatabase())) {
            if (table.isTable()) {
                // Insert common data:
                out.append("INSERT INTO ");
                out.append(seqTable);
                out.append(" (id) VALUES ('"+OrmTable.NAME.of(table)+"');");
                println(out);
            }
        }
        return out;
    }

    /** Print SQL NEXT SEQUENCE. */
    @Override
    public Appendable printSeqNextValue(final UjoSequencer sequence, final Appendable out) throws IOException {
        OrmTable table = sequence.getTable();
        String tableKey = table!=null ? OrmTable.NAME.of(table) : COMMON_SEQ_TABLE_KEY ;

        String seqTable = sequence.getDatabasSchema()+'.'+sequence.getSequenceName();
        out.append("SELECT seq, seq+step FROM ");
        out.append(seqTable);
        out.append(" WHERE id='"+tableKey+"'");
        return out;
    }

    /** Print SQL NEXT SEQUENCE Update or return null. The method is intended for an emulator of the sequence. */
    @Override
    public Appendable printSeqNextValueUpdate(final UjoSequencer sequence, final Appendable out) throws IOException {
        String seqTable = sequence.getDatabasSchema()+'.'+sequence.getSequenceName();
        out.append("UPDATE ");
        out.append(seqTable);
        out.append(" SET seq=seq+step");
        out.append(" WHERE id=?");
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
