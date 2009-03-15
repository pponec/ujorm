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
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.SqlRenderer;
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;

/** H2 (http://www.h2database.com) */
public class H2Renderer implements SqlRenderer {

    /** Returns a default JDBC Driver */
    public String getJdbcUrl() {
        return "jdbc:h2:mem:";
    }

    /** Returns a JDBC Driver */
    public String getJdbcDriver() {
        return "org.h2.Driver";
    }

    /** Print a SQL script to crate database */
    public void createDatabase(OrmDatabase database, Appendable writer) throws IOException {
        for (OrmTable table : OrmDatabase.TABLES.getList(database)) {
            printTable(table, writer);
            printForeignKey(table, writer);
        }
    }

    /** Print a SQL sript to create table */
    public void printTable(OrmTable table, Appendable writer) throws IOException {
        writer.append("CREATE TABLE ");
        writer.append(OrmTable.NAME.of(table));
        String separator = "\n\t( ";
        for (OrmColumn column : OrmTable.COLUMNS.getList(table)) {
            writer.append(separator);
            separator = "\n\t, ";

            if ( column.isForeignKey() ) {
                printFKColumns(column, writer);
            } else {
                printColumn(column, writer, null);
            }
        }
        writer.append("\n\t);\n");
    }

    /** Print foreign key */
    public void printForeignKey(OrmTable table, Appendable writer) throws IOException {
        for (OrmColumn column : OrmTable.COLUMNS.getList(table)) {
            if ( column.isForeignKey() ) {
                printForeignKey(column, table, writer);
            }
        }
    }

    /** Print foreign key for  */
    @SuppressWarnings("unchecked")
    public void printForeignKey(OrmColumn column, OrmTable table, Appendable writer) throws IOException {
        final UjoProperty property = OrmColumn.TABLE_PROPERTY.of(column);
        final OrmTable foreignTable = OrmHandler.getInstance().findTableModel(property.getType());
        OrmPKey foreignKeys = OrmTable.PK.of(foreignTable);

        writer.append("ALTER TABLE ");
        writer.append(OrmTable.NAME.of(table));
        writer.append("\n\tADD FOREIGN KEY");

        String separator = "(";
        List<OrmColumn> columns = OrmPKey.COLUMNS.of(foreignKeys);
        int columnsSize = columns.size();

        for (int i=0; i<columnsSize; ++i) {
            writer.append(separator);
            separator = ", ";
            final String name = column.getForeignColumnName(i);
            writer.append(name);
        }

        writer.append(")\n\tREFERENCES ");
        writer.append(OrmTable.NAME.of(foreignTable));
        separator = "(";

        for (OrmColumn fkColumn : OrmPKey.COLUMNS.of(foreignKeys)) {
            writer.append(separator);
            separator = ", ";
            writer.append(OrmColumn.NAME.of(fkColumn));
        }

        writer.append(")");
        //writer.append("\n\tON DELETE CASCADE");
        writer.append("\n\t;");

    }

    /**
     *  Print a SQL to create column
     * @param column Database Column
     * @param name The name parameter is not mandatory, in case a null value the column name is used.
     * @throws java.io.IOException
     */
    public void printColumn(OrmColumn column, Appendable writer, String name) throws IOException {

        if (name==null) {
            name = OrmColumn.NAME.of(column);
        }

        writer.append( name );
        writer.append( ' ' );
        writer.append( OrmColumn.DB_TYPE.of(column).name() );

        if (!OrmColumn.MAX_LENGTH.isDefault(column)) {
           writer.append( "(" + OrmColumn.MAX_LENGTH.of(column) );
           if (!OrmColumn.PRECISION.isDefault(column)) {
               writer.append( ", " + OrmColumn.PRECISION.of(column) );
           }
           writer.append( ")" );
        }
        if (!OrmColumn.MANDATORY.isDefault(column)) {
           writer.append( " NOT NULL" );
        }
        if (OrmColumn.PRIMARY_KEY.of(column) && name==null) {
           writer.append(" PRIMARY KEY");
        }
    }

    /** Print a SQL to create foreign keys. */
    public void printFKColumns(OrmColumn column, Appendable writer) throws IOException {

        List<OrmColumn> columns = column.getForeignColumns();
        String separator = "";

        for (int i=0; i<columns.size(); ++i) {
            OrmColumn col = columns.get(i);
            writer.append(separator);
            separator = "\n\t, ";
            String name = column.getForeignColumnName(i);
            printColumn(col, writer, name );
        }
    }


    /** Print an SQL INSERT statement.  */
    @SuppressWarnings("unchecked")
    public void printInsert(TableUjo ujo, Appendable writer) throws IOException {
        
         OrmTable table = OrmHandler.getInstance().findTableModel((Class) ujo.getClass());
         StringBuilder values = new StringBuilder();

         writer.append("INSERT INTO ");
         writer.append(table.getFullName());
         writer.append("\n\t(");

         printTableColumns(OrmTable.COLUMNS.getList(table), writer, values);

         writer.append(")\n\tVALUES (");
         writer.append(values);
         writer.append(");");
    }

    /** Print an SQL INSERT statement.  */
    @SuppressWarnings("unchecked")
    public void printSelect(TableUjo ujo, Appendable writer) throws IOException {

         OrmTable table = OrmHandler.getInstance().findTableModel((Class) ujo.getClass());
         StringBuilder values = null;

         writer.append("SELECT ");
         printTableColumns(OrmTable.COLUMNS.getList(table), writer, values);
         writer.append("\n\tFROM ");
         writer.append(table.getFullName());
         writer.append("\n\t");

         if (!true) {
             writer.append(" WHERE ...");
         }
         writer.append(";");
    }



    /** Print table columns */
    @SuppressWarnings("unchecked")
    protected void printTableColumns(List<OrmColumn> columns, Appendable writer, Appendable values) throws IOException {
        String separator = "";
        for (OrmColumn column : columns) {
            if (column.isForeignKey()) {
                for (int i=0; i<column.getForeignColumns().size(); ++i) {
                    writer.append(separator);
                    writer.append(column.getForeignColumnName(i));
                    if (values!=null) {
                        values.append(separator);
                        values.append("?");
                    }
                    separator = ", ";
                }
            } else if (column.isColumn()) {
                writer.append(separator);
                writer.append(OrmColumn.NAME.of(column));
                if (values!=null) {
                    values.append(separator);
                    values.append("?");                    
                }
                separator = ", ";
            }
        }
    }


}
