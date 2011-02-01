/*
 *  Copyright 2009-2010 Tomas Hampl
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
import org.ujoframework.orm.CriterionDecoder;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.SqlDialect;
import org.ujoframework.orm.UjoSequencer;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaDatabase;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaTable;

/** Dialect for the MSSQL tested on SQL Server 2008 R2 with Microsoft SQL Server JDBC Driver 3.0
 *  from <a href="http://msdn.microsoft.com/data/jdbc">http://msdn.microsoft.com/data/jdbc</a>
 * <br><a href="http://www.microsoft.com/sqlserver/">http://www.microsoft.com/sqlserver/</a>
 *
 */
public class MSSqlDialect extends SqlDialect {

    @Override
    public String getJdbcUrl() {
        return "jdbc:sqlserver://localhost:1433";
    }

    @Override
    public String getJdbcDriver() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    /** Print an SQL UPDATE statement.  */
    @Override
    public Appendable printUpdate(MetaTable table, List<MetaColumn> changedColumns, CriterionDecoder decoder, Appendable out) throws IOException {
        out.append("UPDATE ");
        out.append(table.getAlias());
        out.append("\n\tSET ");

        for (int i = 0; i < changedColumns.size(); i++) {
            MetaColumn ormColumn = changedColumns.get(i);
            if (ormColumn.isPrimaryKey()) {
                throw new IllegalStateException("Primary key can not be changed: " + ormColumn);
            }
            out.append(i == 0 ? "" : ", ");
            out.append(MetaColumn.NAME.of(ormColumn));
            out.append("=?");
        }
        out.append("\n\tFROM ");
        printTableAliasDefinition(table, out);
        out.append("\n\tWHERE ");
        out.append(decoder.getWhere());
        return out;
    }

      /** Print an SQL DELETE statement. */
    @Override
    public Appendable printDelete
        ( MetaTable table
        , CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        out.append("DELETE ");
        out.append(table.getAlias());
        out.append("\n\tFROM ");
        printTableAliasDefinition(table, out);
        out.append("\n\tWHERE ");
        out.append(decoder.getWhere());

        return out;
    }



    @Override
    protected String getColumnType(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {

            //timestamp data type has nothing to do with times or dates.
            //SQL Server timestamps are binary numbers that indicate the relative sequence in which data modifications took place in a database.
            //The timestamp data type was originally implemented to support the SQL Server recovery algorithms.
            //It further states Never use timestamp columns in keys, especially primary keys,
            //because the timestamp value changes every time the row is modified.
            case TIMESTAMP:
                return "DATETIME";

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
        } else if (column.isForeignKey()) {
            printFKColumnsDeclaration(column, out);
        } else {
            printColumnDeclaration(column, null, out);
        }

        out.append(" COMMENT '");
        escape(MetaColumn.COMMENT.of(column), out);
        out.append("'");
        return out;
    }

////////////
    @Override
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = '");
        out.append(schema);
        out.append("') ");
        out.append("BEGIN CREATE DATABASE ");
        out.append(schema);
        out.append(" END ");
        return out;
    }

    /** Print a extended SQL table name by sample: SCHEMA.TABLE
     * @param printSymbolSchema True parameter replaces a <string>default schema</string> name for the symbol "~" by the example: ~.TABLE
     * @throws IOException
     */
    @Override
    public Appendable printFullTableName(final MetaTable table, final boolean printSymbolSchema, final Appendable out) throws IOException {

        final String tableSchema = MetaTable.SCHEMA.of(table);
        final String tableName = MetaTable.NAME.of(table);

        if (isUsable(tableSchema)) {
            out.append((printSymbolSchema && table.isDefaultSchema())
                    ? DEFAULT_SCHEMA_SYMBOL
                    : tableSchema);
            out.append('.');
        }
        out.append("dbo.");

        out.append(tableName);
        return out;
    }

    /** Print SQL CREATE SEQUENCE. No JDBC parameters. */
    @Override
    public Appendable printSequenceTable(final MetaDatabase db, final Appendable out) throws IOException {
        String schema = MetaDatabase.SCHEMA.of(db);
        Integer cache = MetaParams.SEQUENCE_CACHE.of(db.getParams());

        out.append("CREATE TABLE ");
        if (isUsable(schema)) {
            out.append(schema);
            out.append('.');
        }
        out.append("dbo.");

        MetaColumn pkType = new MetaColumn();
        MetaColumn.DB_TYPE.setValue(pkType, DbType.BIGINT);

        out.append(COMMON_SEQ_TABLE_NAME);
        out.append("\n\t( id VARCHAR(96) NOT NULL PRIMARY KEY");
        out.append("\n\t, seq " + getColumnType(pkType) + " DEFAULT " + cache + " NOT NULL");
        out.append("\n\t, cache INT DEFAULT " + cache + " NOT NULL");
        out.append("\n\t, maxvalue " + getColumnType(pkType) + " DEFAULT 0 NOT NULL");
        out.append("\n\t)");
        return out;
    }

    @Override
    protected Appendable printSequenceTableName(final UjoSequencer sequence, final Appendable out) throws IOException {
        String schema = sequence.getDatabaseSchema();
        if (isUsable(schema)) {
            out.append(schema);
            out.append('.');
        }
        out.append("dbo.");
        out.append(COMMON_SEQ_TABLE_NAME);
        return out;
    }

    /** Print a SQL sript to add a new column to the table */
    @Override
    public Appendable printAlterTable(MetaColumn column, Appendable out) throws IOException {
        out.append("ALTER TABLE ");
        printFullTableName(column.getTable(), out);
        out.append(" ADD ");

        if (column.isForeignKey()) {
            printFKColumnsDeclaration(column, out);
        } else {
            printColumnDeclaration(column, null, out);
        }
        if (column.hasDefaultValue()) {
            printDefaultValue(column, out);
        }
        return out;
    }
}
