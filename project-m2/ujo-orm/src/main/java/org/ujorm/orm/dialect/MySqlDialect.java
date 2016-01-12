/*
 *  Copyright 2009-2014 Pavel Ponec
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
import java.util.List;
import org.ujorm.orm.CriterionDecoder;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.TableWrapper;
import org.ujorm.orm.impl.TableWrapperImpl;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.orm.metaModel.MoreParams;
import static org.ujorm.core.UjoTools.SPACE;

/** Dialect for the MySQL since release 5.0 for the InnoDB engine.
 * <br><a href="http://dev.mysql.com/">http://dev.mysql.com/</a>
 */
public class MySqlDialect extends SqlDialect {

    /** The Max length of VARCHAR database type */
    public static final int VARCHAR_MAX_LENGTH = 21845;

    @Override
    public String getJdbcUrl() {
        return "jdbc:mysql://127.0.0.1:3306/db1";
    }

    @Override
    public String getJdbcDriver() {
        return "com.mysql.jdbc.Driver";
    }

    /** Does the database support a catalog?
     * The feature supports: MySqlDialect and MSSqlDialect.
     * @return The default value is  {@code true}.
     */
    @Override
    public boolean isCatalog() {
        return true;
    }

   /** Print an SQL DELETE statement. */
    @Override
    public Appendable printDelete
        ( CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        out.append("DELETE ");
        printQuotedName(decoder.getBaseTable().getAlias(), out);
        out.append(" FROM ");

        final TableWrapper[] tables = decoder.getTablesSorted();
        if (tables.length==1) {
            tables[0] = new TableWrapperImpl(decoder.getBaseTable(), "");
        }
        for (int i = 0; i < tables.length; i++) {
            if (i > 0) { out.append(", "); }
            printTableAliasDefinition(tables[i], out);
        }
        return printWhere(decoder, tables, out);
    }

    @Override
    public Appendable printUpdate
            ( List<MetaColumn> changedColumns
            , CriterionDecoder decoder
            , Appendable out
            ) throws IOException {

        final TableWrapper[] tables = decoder.getTablesSorted();
        if (tables.length==1) {
            tables[0] = new TableWrapperImpl(decoder.getBaseTable(), "");
        }

        out.append("UPDATE ");
        for (int i = 0; i < tables.length; i++) {
            if (i > 0) { out.append(", "); }
            printTableAliasDefinition(tables[i], out);
        }
        out.append("\n\tSET ");

        for (int i=0; i<changedColumns.size(); i++) {
            MetaColumn ormColumn = changedColumns.get(i);
            if (ormColumn.isPrimaryKey()) {
                throw new IllegalStateException("Primary key can not be changed: " + ormColumn);
            }
            out.append(i==0 ? "" :  ", ");
            out.append(ormColumn.getColumnAlias());
            out.append("=?");
        }
        return printWhere(decoder, tables, out);
    }

    /** Print where condition for DELETE / UPDATE
     * TODO: FIX THE IMPLEMENTATION - probably in the CriterionDecoder class
     */
    protected Appendable printWhere(CriterionDecoder decoder, final TableWrapper[] tables, Appendable out) throws IOException {
        out.append(" WHERE ");
        String where = decoder.getWhere();
        if (tables.length==1) {
            String fullTableName = printFullTableName(decoder.getBaseTable(), new StringBuilder(64)).toString();
            String tableAlias = printQuotedName(decoder.getBaseTable().getAlias(), new StringBuilder(64)).toString();
            where = where.replace(tableAlias + '.', fullTableName + '.');
        }
        out.append(where);
        return out;
    }

    @Override
    public Appendable printSequenceTable(MetaDatabase db, Appendable out) throws IOException {
        return super.printSequenceTable(db, out)
              .append(SPACE)
              .append(getEngine(null))
              ;
    }

    @Override
    public Appendable printTable(MetaTable table, Appendable out) throws IOException {
        return super.printTable(table, out)
              .append(SPACE)
              .append(getEngine(table))
              ;
    }

    /** Returns a MySQL engine. <br>
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
            case VARCHAR:
                return column.getMaxLength() > VARCHAR_MAX_LENGTH
                        ? "TEXT"
                        : super.getColumnType(column);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Appendable printQuotedNameAlways(CharSequence name, Appendable sql) throws IOException {
        sql.append('`'); // quotation start character based on SQL dialect
        sql.append(name);
        sql.append('`'); // quotation end character based on SQL dialect
        return sql;
    }

    /** Create a SQL script for the NEXT SEQUENCE from a native database sequencer
     * <br/>TIP: SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = $dbName AND TABLE_NAME = $tblName.
     */
    @Override
    public Appendable printNextSequence(String sequenceName, MetaTable table, Appendable out) throws IOException {
        out.append("SELECT AUTO_INCREMENT ");
        out.append("FROM information_schema.TABLES WHERE TABLE_SCHEMA = '");
        out.append(MetaTable.SCHEMA.of(table));
        out.append("' AND TABLE_NAME = '");
        out.append(MetaTable.NAME.of(table));
        out.append("'");
        return out;
    }
}