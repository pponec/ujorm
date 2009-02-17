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
import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.DbHandler;
import org.ujoframework.orm.SqlRenderer;
import org.ujoframework.orm.metaModel.Db;
import org.ujoframework.orm.metaModel.DbColumn;
import org.ujoframework.orm.metaModel.DbPK;
import org.ujoframework.orm.metaModel.DbTable;

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
    public void createDatabase(Db database, Appendable writer) throws IOException {
        for (DbTable table : Db.TABLES.getList(database)) {
            printTable(table, writer);
        }
    }

    /** Print a SQL sript to create table */
    public void printTable(DbTable table, Appendable result) throws IOException {
        result.append("CREATE TABLE ");
        result.append(DbTable.NAME.of(table));
        String separator = "\n\t( ";
        for (DbColumn column : DbTable.COLUMNS.getList(table)) {
            result.append(separator);
            separator = "\n\t, ";

            if ( column.isForeignKey() ) {
                printColumnFK(column, result, "FK_" + DbTable.NAME.of(table) + "_");
            } else {
                printColumn(column, result, null);
            }
        }
        result.append(");\n");
    }


    /** Print a SQL to create column */
    public void printColumn(DbColumn column, Appendable writer, String prefix) throws IOException {

        if (prefix!=null) {
            writer.append( prefix );
        }

        writer.append( DbColumn.NAME.of(column) );
        writer.append( ' ' );
        writer.append( DbColumn.DB_TYPE.of(column).name() );

        if (!DbColumn.MAX_LENGTH.isDefault(column)) {
           writer.append( "(" + DbColumn.MAX_LENGTH.of(column) );
           if (!DbColumn.PRECISION.isDefault(column)) {
               writer.append( ", " + DbColumn.PRECISION.of(column) );
           }
           writer.append( ")" );
           if (DbColumn.PRIMARY_KEY.of(column)) {
               writer.append(" PRIMARY KEY");
           }
        }
    }

    /** Print a SQL to create a Foreign key. */
    public void printColumnFK(DbColumn column, Appendable writer, String prefix) throws IOException {

        final UjoProperty property = DbColumn.TABLE_PROPERTY.of(column);
        final Class type = property.getType();
        final DbTable table = DbHandler.getInstance().findTable(property);
        final DbPK pk = DbTable.PK.of(table);
        
        for (DbColumn col : DbPK.COLUMNS.getList(pk)) {
            printColumn(col, writer, prefix );
        }
    }



}
