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

package org.ujorm.orm.dialect;

import java.io.IOException;
import org.ujorm.orm.CriterionDecoder;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.orm.metaModel.MoreParams;

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
        return super.printSequenceTable(db, out)
              .append(' ')
              .append(getEngine(null))
              ;
    }

    @Override
    public Appendable printTable(MetaTable table, Appendable out) throws IOException {
        return super.printTable(table, out)
              .append(' ')
              .append(getEngine(table))
              ;
    }

    /** Returns a MySQL enginge. <br>
     * The default value is " ENGINE = InnoDB". */
    protected String getEngine(final MetaTable table) {
        final String result = MetaParams.MORE_PARAMS.add(MoreParams.DIALECT_MYSQL_ENGINE_TYPE)
              .of(ormHandler.getParameters());
        return result;
    }

    /** MySQL dialect uses a database type DATETIME (instead of the TIMESTAMP) for the java.util.Date. */
    @Override
    protected String getColumnType(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {
            case TIMESTAMP:
                return "DATETIME";
            case CLOB:
                //mysql dont have clob but text
                //http://dev.mysql.com/doc/refman/5.0/en/blob.html
               //http://www.htmlite.com/mysql003.php
                return "LONGTEXT";
            case BLOB:
                return "LONGBLOB";
                
            default:
                return super.getColumnType(column);
        }
    }

    /** Print a Comment on the table */
    @Override
    public Appendable printComment(MetaTable table, Appendable out) throws IOException {
        out.append("ALTER TABLE ");
        printFullTableName(table, out);
        out.append(" COMMENT = '");
        escape(MetaTable.COMMENT.of(table), out);
        out.append("'");
        return out;
    }

    /** Important note for MySQL: the change of column modifies all another column attributes so the comment update can revert some hand-made changes different from meta-model.<br>
     * See the official MySQL <a href="http://dev.mysql.com/doc/refman/5.1/en/alter-table.html">documentation</a> for more information.
     * The column comments can be suppresed by the overwritting the method with an empty body.
     */
    @Override
    public Appendable printComment(MetaColumn column, Appendable out) throws IOException {
        out.append("ALTER TABLE ");
        printFullTableName(column.getTable(), out);
        out.append(" MODIFY COLUMN ");

        if (column.isPrimaryKey()) {
            String pk = " PRIMARY KEY"; // Due:  Multiple primary key defined.
            String statement = printColumnDeclaration(column, null, new StringBuilder()).toString();
            out.append(statement.replaceAll(pk, " "));
        } else if(column.isForeignKey()) {
            printFKColumnsDeclaration(column, out); 
        } else {
            printColumnDeclaration(column, null, out);
        }

        out.append(" COMMENT '");
        escape(MetaColumn.COMMENT.of(column), out);
        out.append("'");
        return out;
    }

}
