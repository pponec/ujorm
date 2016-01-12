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
package org.ujorm.orm.dialect;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.Key;
import org.ujorm.core.UjoTools;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.CriterionDecoder;
import org.ujorm.orm.DbType;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.TableWrapper;
import org.ujorm.orm.UjoSequencer;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;
import static org.ujorm.core.UjoTools.SPACE;

/** Dialect for the MSSQL tested on SQL Server 2008 R2 with Microsoft SQL Server JDBC Driver 3.0
 *  from <a href="http://msdn.microsoft.com/data/jdbc">http://msdn.microsoft.com/data/jdbc</a>
 * <br><a href="http://www.microsoft.com/sqlserver/">http://www.microsoft.com/sqlserver/</a>
 * <br>Note: This dialect the is an early release 1.10.beta.
 * @since 1.10
 */
public class MSSqlDialect extends SqlDialect {
    //maximum allowed size for any data type (8000)

    private final Integer MSSQL_MAX_ALLOWED_SIZE = 8000;

    @Override
    public String getJdbcUrl() {
        return "jdbc:sqlserver://localhost:1433";
    }

    @Override
    public String getJdbcDriver() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }


    /** Does the database support a catalog?
     * The feature supports: MySqlDialect and MSSqlDialect.
     * @return The value is  {@code true}.
     */
    @Override
    public boolean isCatalog() {
        return true;
    }

    /** Print an SQL UPDATE statement.  */
    @Override
    public Appendable printUpdate(List<MetaColumn> changedColumns, CriterionDecoder decoder, Appendable out) throws IOException {
        final MetaTable table = decoder.getBaseTable();
        out.append("UPDATE ");
        printQuotedName(table.getAlias(), out);
        out.append("\n\tSET ");

        for (int i = 0; i < changedColumns.size(); i++) {
            MetaColumn ormColumn = changedColumns.get(i);
            if (ormColumn.isPrimaryKey()) {
                throw new IllegalStateException("Primary key can not be changed: " + ormColumn);
            }
            out.append(i == 0 ? "" : ", ");
            printQuotedName(ormColumn.getName(), out);
            out.append("=?");
        }
        out.append("\n\tFROM ");
        printTableAliasDefinition( decoder, out);
        out.append("\n\tWHERE ");
        out.append(decoder.getWhere());
        return out;
    }

    /** Print an SQL DELETE statement. */
    @Override
    public Appendable printDelete(CriterionDecoder decoder, Appendable out) throws IOException {
        final MetaTable table = decoder.getBaseTable();
        out.append("DELETE ");
        printQuotedName(table.getAlias(), out);
        out.append("\n\tFROM ");
        printTableAliasDefinition(decoder, out);
        out.append(" WHERE ");
        out.append(decoder.getWhere());

        return out;
    }

    /** Print a full SQL column alias name by sample: <TABLE>_<ALIAS_COLUMN> */
    public Appendable printColumnUnderAlias(final MetaColumn column, final Appendable out) throws IOException {
        final MetaTable table = MetaColumn.TABLE.of(column);

        out.append(table.getAlias());
        out.append('_');
        out.append(column.getName());

        return out;
    }

    /** Print a full SQL column alias name by sample: o_<TABLE>_<ALIAS_COLUMN> - used for as order alias */
    public Appendable printColumnOrderAlias(final ColumnWrapper column, final Appendable out) throws IOException {
        out.append("o_");
        out.append(column.getTableAlias());
        out.append('_');
        out.append(MetaColumn.NAME.of(column.getModel()));

        return out;
    }

    /**  prints columns in "<TABLE>.<COLUMN_NAME> AS <TABLE>_<COLUMN_NAME>" format separated by comma */
    protected void printTableColumnsWithUnderAliases(Collection<ColumnWrapper> columnWrappers, Appendable out) throws IOException {
        String separator = "";
        for (ColumnWrapper columnWrapper : columnWrappers) {
            MetaColumn column = columnWrapper.getModel();
            if (column.isForeignKey()) {
                for (int i = 0; i < column.getForeignColumns().size(); ++i) {
                    out.append(separator);

                    printQuotedName(column.getTableAlias(), out);
                    out.append('.');
                    printQuotedName(column.getForeignColumnName(i), out);

                    out.append(" AS ");
                    printColumnUnderAlias(column, out);

                    separator = ", ";
                }
            } else if (column.isColumn()) {
                out.append(separator);
                printColumnAlias(columnWrapper, out);

                out.append(" AS ");
                printColumnUnderAlias(column, out);

                separator = ", ";
            }
        }
    }

    /** prints columns in <TABLE>_<COLUMN_NAME> separated by comma */
    protected void printTableColumnsUnderAliases(Collection<ColumnWrapper> columnWrappers, Appendable out) throws IOException {
        String separator = "";
        for (ColumnWrapper columnWrapper : columnWrappers) {
            MetaColumn column = columnWrapper.getModel();
            if (column.isForeignKey()) {
                for (int i = 0; i < column.getForeignColumns().size(); ++i) {
                    out.append(separator);

                    printColumnUnderAlias(column, out);

                    separator = ", ";
                }
            } else if (column.isColumn()) {
                out.append(separator);

                printColumnUnderAlias(column, out);

                separator = ", ";
            }
        }
    }

    /** Inner select select with under aliases assignment and order columns (have to propagate to outer select) */
    protected void createInnerSelectPart(Query query, Appendable out) throws IOException {
        out.append("SELECT ");
        if (query.isDistinct()) {
            out.append("DISTINCT ");
        }
        printTableColumnsWithUnderAliases(query.getColumns(), out);

        // add order props
        out.append(", ");
        printOrderColumns(query, out, false, true, false);

    }

    /** Creating RowNumber column */
    protected void createRowOrderPart(Query query,  Appendable out, boolean asOrderAlias) throws IOException {
        out.append(", ROW_NUMBER() OVER (");
        if (query.getOrderBy().isEmpty()) {
            MetaColumn column = query.getColumnArray()[0].getModel();
            out.append(" ORDER BY ");
            if (asOrderAlias) {
                printColumnOrderAlias(column, out);
            } else {
                printColumnAlias(column, out);
            }
        } else {
            printSelectOrder(query, out, asOrderAlias);
        }
        out.append(") AS RowNum ");
    }

    /* Prints order columns from input Query separated by comma */
    protected void printOrderColumns(Query query, Appendable out, boolean asOrderAlias, boolean addOrderAlias, boolean showDesc) throws IOException {
        final List<Key> props = query.getOrderBy();
        for (int i=0; i<props.size(); i++) {
            ColumnWrapper column = query.readOrderColumn(i);
            boolean ascending = props.get(i).isAscending();
            if (i>0) {
                out.append(", ");
            }
            if (asOrderAlias) {
                printColumnOrderAlias(column, out);
            } else {
                printColumnAlias(column, out);
                if (addOrderAlias) {
                    out.append(" AS ");
                    printColumnOrderAlias(column, out);
                }
            }
            if (!ascending && showDesc) {
                out.append(" DESC");
            }
        }
    }

    /** Print SQL ORDER BY */
    public void printSelectOrder(Query query, Appendable out, boolean orderAlias) throws IOException {
        out.append(" ORDER BY ");
        printOrderColumns(query, out, orderAlias, false, true);
    }

    /** Where clause for inner select */
    protected void createWherePart(Query query, Appendable out) throws IOException {
        Map<String, MetaTable> tables = new LinkedHashMap<String, MetaTable>();
        List<Key> props = query.getOrderBy();
        for (int i = 0; i < props.size(); i++) {
            ColumnWrapper column = query.readOrderColumn(i);
            String alias = column.getTableAlias();
            tables.put(alias, column.getModel().getTable());
        }
        if (query.getCriterion() != null) {
            CriterionDecoder ed = query.getDecoder();
            getTablesFromCriterion(ed, tables);
            printTablesWithAlias(tables.values(), out);
            String sql = ed.getWhere();
            if (!sql.isEmpty()) {
                out.append(" WHERE ");
                out.append(ed.getWhere());
            }
        } else {
            printTablesWithAlias(tables.values(), out);
        }
    }

    private void getTablesFromCriterion(CriterionDecoder ed, Map<String, MetaTable> tables) {
        TableWrapper[] critTables = ed.getTables();
        for (int i = 0; i < critTables.length; ++i) {
            TableWrapper table = critTables[i];
            String alias = table.getAlias();
            tables.put(alias, table.getModel());
        }
    }

    protected void printTablesWithAlias(Collection<MetaTable> tables, Appendable out) throws IOException {
        boolean first = true;
        for (MetaTable tab : tables) {
            if (!first) {
                out.append(", ");
            }
            printTableAliasDefinition(tab, out);
            first = false;
        }
    }

    /** Outer part of select with sorting */
    protected void createOuterPart(String innerSelect, Query query, Appendable out) throws IOException {
        out.append("SELECT ");
        Collection<ColumnWrapper> columnWrappers = query.getColumns();
        boolean first = true;
        for (ColumnWrapper columnWrapper : columnWrappers) {
            MetaColumn column = columnWrapper.getModel();
            if (!first) {
                out.append(", ");
            }
            printColumnUnderAlias(column, out);
            first = false;
        }

        out.append("\n\tFROM (");
        out.append(innerSelect);
        out.append("\n) AS MyInnerTable ");
        if (query.isLimit()) {
            out.append("WHERE MyInnerTable.RowNum ");
            // MS-SQL's first index is 1 !!!
            long start = query.isOffset() ? (query.getOffset() + 1) : 1;
            out.append("BETWEEN " + start + " AND ");
            // exclusive - between 1 and 2 returns 2 rows
            long end = start + query.getLimit() - 1;
            out.append(String.valueOf(end));

        } else if (query.isOffset()) {
            out.append("WHERE MyInnerTable.RowNum ");
            out.append("> ");
            out.append(String.valueOf(query.getOffset()));
        }

        // order by part
        if (UjoTools.isFilled(query.getOrderBy())) {
            printSelectOrder(query, out, true);
            /* When creating the ROW_NUMBER (), he shall transmit the parameter
             * of the column by which to sort -> RowNum is now sorted itself
             * (otherwise the paging WHERE RowNum BETWEEN 1 AND 200 didn't make sense)
             */
            out.append(" ORDER BY MyInnerTable.RowNum");
        }
    }

    /** Custom implementation of MS-SQL dialect due to different offset and limit usage */
    @Override
    protected Appendable printSelectTable(Query query, boolean count, Appendable out) throws IOException {
        if (count || (!query.isLimit() && !query.isOffset())) {
            out = super.printSelectTable(query, count, out);
        } else {
            // we have to order over some column...
            if (!UjoTools.isFilled(query.getOrderBy())) {
                query.orderBy(query.getColumnArray()[0].getKey());
            }
            StringBuilder innerPart = new StringBuilder(256);
            createInnerSelectPart(query, innerPart);
            // row + order by
            if (!query.isDistinct()) {
                createRowOrderPart(query, innerPart, false);
            }
            // from + where cond
            innerPart.append("\n\t\tFROM ");
            createWherePart(query, innerPart);

            // for distinct request we have to insert one more select...
            if (query.isDistinct()) {
                innerPart = createMiddlePart(query, innerPart.toString());
            }

            // add limit + offset
            createOuterPart(innerPart.toString(), query, out);
        }
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
            case BLOB:
                return "VARBINARY";
            case TIMESTAMP:
                return "DATETIME";
            case BOOLEAN:
                return "TINYINT";
            case VARCHAR:
                return "NVARCHAR";
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
        printQuotedName(schema, out);
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

        if (isFilled(tableSchema)) {
            if (printSymbolSchema && table.isDefaultSchema()) {
                out.append(DEFAULT_SCHEMA_SYMBOL);
            } else {
                printQuotedName(tableSchema, out);
            }
            out.append('.');
        }
        out.append("dbo.");
        printQuotedName(tableName, out);
        return out;
    }

    /** Print SQL CREATE SEQUENCE. No JDBC parameters. */
    @Override
    public Appendable printSequenceTable(final MetaDatabase db, final Appendable out) throws IOException {
        String schema = MetaDatabase.SCHEMA.of(db);
        Integer cache = MetaParams.SEQUENCE_CACHE.of(db.getParams());

        out.append("CREATE TABLE ");
        if (isFilled(schema)) {
            out.append(schema);
            out.append('.');
        }
        out.append("dbo.");

        final MetaColumn pkType = new MetaColumn(db.getParams().getConverter(null));
        MetaColumn.DB_TYPE.setValue(pkType, DbType.BIGINT);

        printQuotedName(getSeqTableModel().getTableName(), out);
        out.append ( ""
            + "\n\t( " + getQuotedName(getSeqTableModel().getId()) + " VARCHAR(96) NOT NULL PRIMARY KEY"
            + "\n\t, " + getQuotedName(getSeqTableModel().getSequence()) + SPACE + getColumnType(pkType) + " DEFAULT " + cache + " NOT NULL"
            + "\n\t, " + getQuotedName(getSeqTableModel().getCache()) + " INT DEFAULT " + cache + " NOT NULL"
            + "\n\t, " + getQuotedName(getSeqTableModel().getMaxValue()) + SPACE + getColumnType(pkType) + " DEFAULT 0 NOT NULL"
            + "\n\t)");
        return out;
    }

    @Override
    protected Appendable printSequenceTableName(final UjoSequencer sequence, final Appendable out) throws IOException {
        String schema = sequence.getDatabaseSchema();
        if (isFilled(schema)) {
            printQuotedNameAlways(schema, out);
            out.append('.');
        }
        out.append("dbo.");
        printQuotedNameAlways(getSeqTableModel().getTableName(), out);
        return out;
    }

    /** Print a SQL sript to add a new column to the table */
    @Override
    public Appendable printAlterTableAddColumn(MetaColumn column, Appendable out) throws IOException {
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

    @Override
    public Appendable printDefaultConstraint(MetaColumn column, StringBuilder out) throws IOException {
        if (!column.isMandatory() || column.hasDefaultValue()) {
            MetaTable table = column.getTable();
            out.append("ALTER TABLE ");
            printFullTableName(table, out);
            out.append(" ADD CONSTRAINT ");
            String constName = getNameProvider().buildDefaultConstraintForDefaultValueName(column.getTable(), column);
            out.append(constName);
            // FIXME - something more general, this works only for simple number primary key
            if (column.isPrimaryKey()) {
                out.append(" DEFAULT 0 ");
            } else if (column.hasDefaultValue()) {
                printDefaultValue(column, out);
            } else {
                out.append(" DEFAULT NULL");
            }
            out.append(" FOR ");
            out.append(MetaColumn.NAME.of(column));
        }
        return out;
    }

    /** Print a SQL phrase for the DEFAULT VALUE, for example: DEFAULT 777 */
    @Override
    public Appendable printDefaultValue(final MetaColumn column, final Appendable out) throws IOException {
        Object value = column.getJdbcFriendlyDefaultValue();
        boolean isDefault = value != null;
        String quotMark = "";
        if (value instanceof String) {
            isDefault = ((String) value).length() > 0;
            quotMark = "'";
        } else if (value instanceof java.sql.Date) {
            isDefault = true;
            quotMark = "'";
        }
        if (isDefault) {
            out.append(" DEFAULT ");
            out.append(quotMark);
            if (value instanceof Boolean) {
                out.append((Boolean) value ? '1' : '0'); // << MS-SQL change
            } else {
                out.append(value.toString());
            }
            out.append(quotMark);
        }
        return out;
    }

    @Override
    public Appendable printInsert(List<? extends OrmUjo> bo, int idxFrom, int idxTo, Appendable out) throws IOException {
        return printInsertBySelect(bo, idxFrom, idxTo, "", out);
    }

    @Override
    public Appendable printColumnDeclaration(MetaColumn column, String aName, Appendable out) throws IOException {
        if (!MetaColumn.MAX_LENGTH.isDefault(column)) {
            //TODO : probably MAX_ALLOWED_SIZE is used to all types not only for BLOB
            if ((column.getType().equals(Blob.class) || MetaColumn.DB_TYPE.of(column).equals(DbType.VARCHAR)) && (MetaColumn.MAX_LENGTH.of(column) > MSSQL_MAX_ALLOWED_SIZE)) {

                String name = aName != null ? aName : MetaColumn.NAME.of(column);
                printQuotedName(name, out);
                out.append(SPACE);
                out.append(getColumnType(column));

                out.append("( MAX");
                if (!MetaColumn.PRECISION.isDefault(column)) {
                    out.append("," + MetaColumn.PRECISION.of(column));
                }
                out.append(")");

                if (MetaColumn.MANDATORY.of(column) && aName == null) {
                    out.append(" NOT NULL");
                }
                if (MetaColumn.PRIMARY_KEY.of(column) && aName == null) {
                    out.append(" PRIMARY KEY");
                }
                return out;
            }
        }

        return super.printColumnDeclaration(column, aName, out);
    }

    /** Middle select with RowNumber for sorting */
    private StringBuilder createMiddlePart(Query query, String innerPart) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("SELECT ");
        printTableColumnsUnderAliases(query.getColumns(), out);
        out.append(", ");
        printOrderColumns(query, out, true, false, false);
        createRowOrderPart(query, out, true);
        out.append(" FROM (");
        out.append(innerPart);
        out.append(") AS DistinctTable");

        return out;
    }

    /** Perform the method: {@link Connection#releaseSavepoint(java.sql.Savepoint) ?
     * @see http://technet.microsoft.com/en-us/library/ms378791%28v=sql.110%29.aspx
     * @see Connection#releaseSavepoint(java.sql.Savepoint)
     */

    @Override
    public void releaseSavepoint(final Connection conn, final Savepoint savepoint, final boolean afterRollback) throws SQLException {
        // This method is not currently supported by the Microsoft JDBC Driver for SQL Server:
        // conn.releaseSavepoint(savepoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Appendable printQuotedNameAlways(CharSequence name, Appendable sql) throws IOException {
        sql.append('['); // quotation start character based on SQL dialect
        sql.append(name);
        sql.append(']'); // quotation end character based on SQL dialect
        return sql;
    }

    /** Print table alias definition */
    protected void printTableAliasDefinition(CriterionDecoder decoder, Appendable out) throws IOException {
        Map<String, MetaTable> tables = new LinkedHashMap<String, MetaTable>();
        getTablesFromCriterion(decoder, tables);
        printTablesWithAlias(tables.values(), out);
    }
}