/*
 *  Copyright 2009-2015 Pavel Ponec
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
package org.ujorm.orm;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.TemplateValue;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaIndex;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaProcedure;
import org.ujorm.orm.metaModel.MetaSelect;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * SQL dialect abstract class. Methods of this class print a SQL statement(s) along a metamodel usually.
 * You may create a subclass of any implementation to create another SQL statement, however just I can't
 * exclude some small changes of this API in the next release.
 * @author Pavel Ponec
 * @composed - - 1 SeqTableModel
 */
@SuppressWarnings("unchecked")
abstract public class SqlDialect {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(SqlDialect.class);

    /** The table model for an internal sequence table support */
    final private SeqTableModel pkTableModel = new SeqTableModel();

    /** The table key for a common sequence emulator. */
    public static final String COMMON_SEQ_TABLE_KEY = "<ALL>";
    /** The default schema symbol */
    public static final String DEFAULT_SCHEMA_SYMBOL = "~";

    /** The ORM handler */
    protected OrmHandler ormHandler;
    /** The name provider */
    private SqlNameProvider nameProvider;
    /** Extended dialect */
    private SqlDialectEx extentedDialect;

    /** Prints quoted name (identifier) to SQL */
    private Boolean quoteRequest;

    /** Set the OrmHandler - the method is for internal call only. */
    public void setHandler(OrmHandler ormHandler) {
        if (this.ormHandler!=null) {
            throw new IllegalStateException("The OrmHandler is assigned yet.");
        }
        this.ormHandler = ormHandler;
    }

    /** Returns a default JDBC URL */
    abstract public String getJdbcUrl();

    /** Returns a JDBC driver class name. */
    abstract public String getJdbcDriver();

    /** Create a new database connection */
    public Connection createConnection(final MetaDatabase db) throws Exception {
        return db.createInternalConnection();
    }

    /** Get or create an Initial Context for the JNDI lookup. */
    public InitialContext createJndiInitialContext(final MetaDatabase db) throws NamingException {
          return new InitialContext();
    }

    /** Does the database support a catalog?
     * The feature supports: MySqlDialect and MSSqlDialect.
     * @return The default value is  {@code false}.
     */
    public boolean isCatalog() {
        return false;
    }

    /** Print SQL 'CREATE SCHEMA' */
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA IF NOT EXISTS ");
        printQuotedName(schema, out);
        return out;
    }

    /** Print SQL 'SET SCHEMA'. The method is not used yet. */
    @Deprecated
    public Appendable printDefaultSchema(String schema, Appendable out) throws IOException {
        out.append("SET SCHEMA ");
        printQuotedName(schema, out);
        return out;
    }

    /** Print a full SQL table name by sample: SCHEMA.TABLE  */
    final public Appendable printFullTableName(final MetaTable table, final Appendable out) throws IOException {
        return printFullTableName(table, false, out);
    }

    /** Print a extended SQL table name by sample: SCHEMA.TABLE
     * @param printSymbolSchema True parameter replaces a <string>default schema</string> name for the symbol "~" by the example: ~.TABLE
     * @throws IOException
     */
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
        printQuotedName(tableName, out);
        return out;
    }

    /** Print a SQL database and table name and an alias definition - by sample: SCHEMA.TABLE ALIAS */
    public void printTableAliasDefinition(final TableWrapper table, final Appendable out) throws IOException {
        printFullTableName(table.getModel(), out);
        final String alias = table.getAlias();
        if (isFilled(alias)) {
            out.append(' ');
            printQuotedName(alias, out);
        }
    }

    /** Print a full SQL column alias name by sample: "TABLE_ALIAS"."ORIG_COLUMN" */
    public Appendable printColumnAlias(final ColumnWrapper column, final Appendable out) throws IOException {
        printQuotedName(column.getTableAlias(), out);
        out.append('.');
        printQuotedName(column.getName(), out);
        return out;
    }

    /** Print a SQL script to create table */
    public Appendable printTable(MetaTable table, Appendable out) throws IOException {
        out.append("CREATE TABLE ");
        printFullTableName(table, out);
        String separator = "\n\t( ";
        for (MetaColumn column : MetaTable.COLUMNS.getList(table)) {
            out.append(separator);
            separator = "\n\t, ";

            if (column.isForeignKey()) {
                printFKColumnsDeclaration(column, out);
            } else {
                printColumnDeclaration(column, null, out);
            }
        }
        out.append("\n\t)");
        return out;
    }

    /** Print a SQL script to add a new column to the table */
    public Appendable printAlterTableAddColumn(MetaColumn column, Appendable out) throws IOException {
        return printAlterTableColumn(column, true, out);
    }

    /** Print a SQL script to add a new column to the table */
    public Appendable printAlterTableColumn(MetaColumn column, boolean add, Appendable out) throws IOException {
        out.append("ALTER TABLE ");
        printFullTableName(column.getTable(), out);
        out.append(add
            ? " ADD COLUMN "
            : " ALTER COLUMN ");
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

    /** Print a SQL phrase for the DEFAULT VALUE, for example: DEFAULT 777 */
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
            out.append(value.toString());
            out.append(quotMark);
        }
        return out;
    }

    /** Print Default Constraint */
    public Appendable printDefaultConstraint(MetaColumn column, StringBuilder out) throws IOException {
        if (column.hasDefaultValue()) {
            MetaTable table = column.getTable();
            out.append("ALTER TABLE ");
            printFullTableName(table, out);
            out.append(" ALTER COLUMN ");
            printQuotedName(column.getName(), out);
            out.append(" SET ");
            printDefaultValue(column, out);
        }
        return out;
    }

    /**
     * Print foreign key for the parameter column
     * @return More statements separated by the ';' characters are enabled
     */
    public Appendable printForeignKey(MetaColumn column, Appendable out) throws IOException {

        MetaTable table = column.getTable();
        List<MetaColumn> fColumns = column.getForeignColumns();
        MetaTable foreignTable = fColumns.get(0).getTable();
        int columnsSize = fColumns.size();

        out.append("ALTER TABLE ");
        printFullTableName(table, out);
        out.append("\n\tADD CONSTRAINT ");
        getNameProvider().printConstraintName(table, column, out);
        out.append(" FOREIGN KEY ");

        for (int i=0; i<columnsSize; ++i) {
            out.append(i==0 ? "(" : ", ");
            final String name = column.getForeignColumnName(i);
            printQuotedName(name, out);
        }

        out.append(")\n\tREFERENCES ");
        printFullTableName(foreignTable, out);
        String separator = "(";

        for (MetaColumn fColumn : fColumns) {
            out.append(separator);
            separator = ", ";
            printQuotedName(fColumn.getName(), out);
        }

        out.append(")");
        //out.append("\tON DELETE CASCADE");
        return out;
    }

    /**
     * Print an INDEX for the parameter column.
     * @return More statements separated by the ';' characters are enabled
     */
    public Appendable printIndex(final MetaIndex index, final Appendable out) throws IOException {

        out.append("CREATE ");
        if (MetaIndex.UNIQUE.of(index)) {
            out.append("UNIQUE ");
        }
        out.append("INDEX ");
        out.append(MetaIndex.NAME.of(index));
        out.append(" ON ");
        printFullTableName(MetaIndex.TABLE.of(index), out);
        String separator = " (";

        for (MetaColumn column : MetaIndex.COLUMNS.of(index)) {
            out.append(separator);
            printQuotedName(column.getName(), out);
            separator = ", ";
        }
        out.append(')');
        return out;
    }

    /** Is allowed a column length in a SQL phrase for creating column? The length example can be: NAME CHAR(10) */
    protected boolean isColumnLengthAllowed(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {
            case INT:
            case SMALLINT:
            case BIGINT:
            case DATE:
            case TIME:
            case TIMESTAMP:
                 return false;
            case CHAR:
            case VARCHAR:
            case DECIMAL:
          //case NUMERIC:
            default:
                return !MetaColumn.MAX_LENGTH.isDefault(column);
        }
    }

    /**
     *  Print a SQL to create column
     * @param column Database Column
     * @param aName The name parameter is not mandatory, the not null value means a foreign key.
     * @throws java.io.IOException
     */
    public Appendable printColumnDeclaration(MetaColumn column, String aName, Appendable out) throws IOException {

        String name = aName!=null ? aName : column.getName();
        printQuotedName(name, out);
        out.append(' ');
        out.append(getColumnType(column));

        if (isColumnLengthAllowed(column)) {
            out.append("(" + MetaColumn.MAX_LENGTH.of(column));
            if (!MetaColumn.PRECISION.isDefault(column)) {
                out.append("," + MetaColumn.PRECISION.of(column));
            }
            out.append(")");
        }
        if (MetaColumn.MANDATORY.of(column) && aName == null) {
            out.append(" NOT NULL");
        }
        if (MetaColumn.PRIMARY_KEY.of(column) && aName == null) {
            out.append(" PRIMARY KEY");
        }
        return out;
    }

    /** Returns a database column type */
    protected String getColumnType(final MetaColumn column) {
        return MetaColumn.DB_TYPE.of(column).name();
    }

    /** Print a SQL to create foreign keys. */
    public Appendable printFKColumnsDeclaration(MetaColumn column, Appendable out) throws IOException {

        final List<MetaColumn> columns = column.getForeignColumns();

        for (int i=0; i<columns.size(); ++i) {
            MetaColumn col = columns.get(i);
            out.append(i==0 ? "" : "\n\t, ");
            String name = column.getForeignColumnName(i);
            printColumnDeclaration(col, name, out);
            if (MetaColumn.MANDATORY.of(column)) {
                out.append(" NOT NULL");
            }
            if (MetaColumn.PRIMARY_KEY.of(column)) {
                out.append(" PRIMARY KEY");
            }
        }
        return out;
    }

    /** Print an SQL INSERT statement.  */
    public Appendable printInsert(final OrmUjo bo, final Appendable out) throws IOException {

        MetaTable table = ormHandler.findTableModel((Class) bo.getClass());
        StringBuilder values = new StringBuilder();

        out.append("INSERT INTO ");
        printFullTableName(table, out);
        out.append(" (");

        printTableColumns(table.getColumns(), values, out);

        out.append(") VALUES (")
           .append(values)
           .append(")")
           ;

        return out;
    }

    /** Print an SQL INSERT statement.
     * @param bos Business object list
     * @param idxFrom Start index from list
     * @param idxTo Finished index from list (excluded)
     * @see #isMultiRowInsertSupported()
     */
    public Appendable printInsert(final List<? extends OrmUjo> bos, final int idxFrom, final int idxTo, final Appendable out) throws IOException {

        MetaTable table = ormHandler.findTableModel(bos.get(idxFrom).getClass());
        StringBuilder values = new StringBuilder(32);

        out.append("INSERT INTO ");
        printFullTableName(table, out);
        out.append(" (");

        printTableColumns(table.getColumns(), values, out);

        for (int i=idxFrom; i<idxTo; ++i) {
            out.append(i==idxFrom ? ") VALUES \n(" : "),\n(")
               .append(values);
        }
        out.append(")");
        return out;
    }

    /** Print an batch SQL INSERT statement using SELECT UNION statement.
     * @param bos Business object list
     * @param idxFrom Start index from list
     * @param idxTo Finished index from list (excluded)
     * @param fromPhrase For example the Oracle syntax: SELECT 1,2,3 FROM DUAL;
     * @see #isMultiRowInsertSupported()
     */
    public Appendable printInsertBySelect(final List<? extends OrmUjo> bos, final int idxFrom, final int idxTo, final String fromPhrase, final Appendable out) throws IOException {

        MetaTable table = ormHandler.findTableModel(bos.get(idxFrom).getClass());
        StringBuilder values = new StringBuilder(32);

        out.append("INSERT INTO ");
        printFullTableName(table, out);
        out.append(" (");

        printTableColumns(table.getColumns(), values, out);

        for (int i=idxFrom; i<idxTo; ++i) {
            out.append(i==idxFrom ? ")\nSELECT " : " UNION ALL\nSELECT ")
               .append(values);
            if (isFilled(fromPhrase)) {
                out.append(" ").append(fromPhrase);
            }
        }
        return out;
    }


    /** Is supported the
     * <a href="http://en.wikipedia.org/wiki/Insert_%28SQL%29#Multirow_inserts">Multirow inserts</a> ?
     * Default value is true
     * @see #printInsert(java.util.List, int, int, java.lang.Appendable) Multi row insert
     */
    public boolean isMultiRowInsertSupported() {
        return true;
    }

    /** Print an SQL UPDATE statement.  */
    public Appendable printUpdate
        ( List<MetaColumn> changedColumns
        , CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        final MetaTable table = decoder.getBaseTable();
        out.append("UPDATE ");
        printTableAliasDefinition(table, out);
        out.append("\n\tSET ");

        for (int i=0; i<changedColumns.size(); i++) {
            MetaColumn ormColumn = changedColumns.get(i);
            if (ormColumn.isPrimaryKey()) {
                throw new IllegalStateException("Primary key can not be changed: " + ormColumn);
            }
            out.append(i==0 ? "" :  ", ");
            printQuotedName(ormColumn.getName(), out);
            out.append("=?");
        }
        out.append("\n\tWHERE ");

        if (decoder.getTableCount() > 1) {
            printQuotedName(table.getFirstPK().getName(), out);
            out.append(" IN (");
            printSelectTableBase(createSubQuery(decoder), false, out);
            out.append(")");
        } else {
            out.append(decoder.getWhere());
        }
        return out;
    }

    /** Print an SQL DELETE statement. */
    public Appendable printDelete
        ( CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        final MetaTable table = decoder.getBaseTable();
        out.append("DELETE FROM ");
        printTableAliasDefinition(table, out);
        out.append(" WHERE ");

        if (decoder.getTableCount() > 1) {
            printQuotedName(table.getFirstPK().getName(), out);
            out.append(" IN (");
            printSelectTableBase(createSubQuery(decoder), false, out);
            out.append(")");
        } else {
            //String where = decoder.getWhere().replace(tableAlias + '.', fullTableName + '.');
            out.append(decoder.getWhere());
        }
        return out;
    }

    /** Create a sub-query for the DELETE/UPDATE statement */
    protected Query createSubQuery(CriterionDecoder decoder) {
        final MetaTable baseTable = decoder.getBaseTable();
        final Query result = new Query(baseTable, decoder.getCriterion());
        result.setDecoder(decoder);
        result.setColumns(true);
        return result;
    }

    /** Returns an SQL criterion template. The result is a template by the next sample: "{0}={1}" .
     * <br>See an example of the implementation:
     * <pre class="pre">
     * switch (crit.getOperator()) {
     *     case EQ:
     *         return "{0}={1}";
     *     case NOT_EQ:
     *         return "{0}<>{1}";
     *     case GT:
     *         return "{0}>{1}";
     *     ...
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public String getCriterionTemplate(ValueCriterion crit) {

        switch (crit.getOperator()) {
            case EQ:
                return "{0}={1}";
            case NOT_EQ:
                return "{0}<>{1}";
            case GT:
                return "{0}>{1}";
            case GE:
                return "{0}>={1}";
            case LT:
                return "{0}<{1}";
            case LE:
                return "{0}<={1}";
            case EQUALS_CASE_INSENSITIVE:
                return "UPPER({0})={1}";
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE:
                return "UPPER({0}) LIKE {1}";
            case STARTS:
            case ENDS:
            case CONTAINS:
                return "{0} LIKE {1}";
            case IN:
                return "{0} IN ({1})";
            case NOT_IN:
                return "NOT {0} IN ({1})";
            case XFIXED:
                return crit.evaluate((Ujo)null)
                    ? "1=1" // "true"
                    : "1=0" // "false"
                    ;
            case XSQL:
                Object tmp = crit.getRightNode() instanceof TemplateValue
                        ? ((TemplateValue) crit.getRightNode()).getTemplate()
                        : crit.getRightNode();
                return "(" + tmp + ')' ;
            case REGEXP:
            case NOT_REGEXP:
            default:
                throw new UnsupportedOperationException("Unsupported: " + crit.getOperator());
        }
    }

    /**
     * Print table columns
     * @param columns List of tablel columns
     * @param values Print columns including its aliases.
     * @param out Table columns output.
     * @throws java.io.IOException
     */
    public void printTableColumns(Collection<? extends ColumnWrapper> columns, Appendable values, Appendable out) throws IOException {
        String separator = "";
        boolean select = values==null; // SELECT
        for (ColumnWrapper wColumn : columns) {
            final MetaColumn column = wColumn.getModel();
            if (column.isForeignKey()) {
                for (int i = 0; i < column.getForeignColumns().size(); ++i) {
                    out.append(separator);
                    if (select) {
                        printQuotedName(wColumn.getTableAlias(), out);
                        out.append('.');
                    }
                    printQuotedName(column.getForeignColumnName(i), out);
                    if (values != null) {
                        values.append(separator);
                        values.append("?");
                    }
                    separator = ", ";
                }
            } else if (column.isColumn()) {
                out.append(separator);
                if (select) {
                    printColumnAlias(wColumn, out);
                } else {
                    printQuotedName(column.getName(), out);
                }
                if (values != null) {
                    values.append(separator);
                    values.append("?");
                }
                separator = ", ";
            }
        }
    }


    /** Print a <strong>condition phrase</strong> from the criterion.
     * @return A value criterion to assign into the SQL query.
     */
    public ValueCriterion printCriterion(ValueCriterion crit, Appendable out) throws IOException {
        final Operator operator = crit.getOperator();
        final Key key = crit.getLeftNode();
        final ColumnWrapper column = key != null
                ? AliasKey.getLastKey(key).getColumn(ormHandler) : null;
        Object right = crit.getRightNode();

        if (right==null ) {
            switch (operator) {
                case EQ:
                case EQUALS_CASE_INSENSITIVE:
                    printColumnAlias(column, out);
                    out.append(" IS NULL");
                    return null;
                case NOT_EQ:
                    printColumnAlias(column, out);
                    out.append(" IS NOT NULL");
                    return null;
                default:
                    throw new UnsupportedOperationException("Comparation the NULL value is forbiden by a operator: " + operator);
            }
        }

        String template = getCriterionTemplate(crit);
        if (template == null) {
            throw new UnsupportedOperationException("Unsupported SQL operator: " + operator);
        }

        switch (crit.getOperator()) {
            case XFIXED:
                out.append( template );
                break;
            case XSQL:
                if (right instanceof TemplateValue) {
                    right = ((TemplateValue) right).getRightVale();
                    ValueCriterion crit2 = (ValueCriterion) Criterion.where
                            ( crit.getLeftNode()
                            , Operator.EQ // The hack
                            , right);
                    return printCriterionValue(template, column, crit2, out);
                }
                if (template.contains("{0}")) {
                    out.append(MessageFormat.format(template, getAliasColumnName(column)));
                } else {
                    out.append(template);
                }
                break;
            default:
                return printCriterionValue(template, column, crit, out);
        }
        return null;
    }

    /**
     * Write a right value form criterion
     * @return A value criterion to assign into the SQL query.
     */
    protected ValueCriterion printCriterionValue(String template, ColumnWrapper column, ValueCriterion crit, Appendable out) throws IOException {
        final Object right = crit.getRightNode();
        if (right instanceof Key) {
            final Key rightProperty = (Key) right;
            final ColumnWrapper col2 = AliasKey.getLastKey(rightProperty).getColumn(ormHandler);

            if (col2.getModel().isForeignKey()) {
                throw new UnsupportedOperationException("Foreign key is not supported yet");
            }
            if (true) {
                // Better performance:
                String f = MessageFormat.format(template, getAliasColumnName(column), getAliasColumnName(col2));
                //String f=String.format(template, column.getColumnAlias(), col2.getColumnAlias());
                out.append(f);
            }
        } else if (right instanceof Object[]) {
            final Object[] os = (Object[]) right;
            final StringBuilder sb = new StringBuilder(2*os.length);
            for (Object o : os) {
                sb.append(sb.length()>0 ? ",?" : "?");
            }
            String f = MessageFormat.format(template, getAliasColumnName(column), sb.toString());
            out.append(f);
            return crit;
        } else if (column.getModel().isForeignKey()) {
            printForeignKey(crit, column, template, out);
            return crit;
        } else {
            String f = MessageFormat.format(template, getAliasColumnName(column), "?");
            out.append(f);
            return crit;
        }
        return null;
    }

    /** Returns quoted column name including the alias table */
    protected String getAliasColumnName(ColumnWrapper column) throws IOException {
        final Appendable out = new StringBuilder(32);
        printQuotedName(column.getTableAlias(), out);
        out.append('.');
        printQuotedName(column.getName(), out);
        return out.toString();
    }

    /** Print all items of the foreign key */
    public void printForeignKey
        ( final ValueCriterion crit
        , final ColumnWrapper column
        , final String template
        , final Appendable out
        ) throws IOException
    {
        int size = column.getModel().getForeignColumns().size();
        for (int i=0; i<size; i++) {
            if (i>0) {
                out.append(' ');
                out.append(crit.getOperator().name());
                out.append(' ');
            }

            StringBuilder columnName = new StringBuilder(256);
            String alias = column.getTableAlias();
            if (isFilled(alias)) {
                printQuotedName(alias, columnName);
                columnName.append('.');
            }
            printQuotedName(column.getModel().getForeignColumnName(i), columnName);
            String f = MessageFormat.format(template, columnName, "?");
            out.append(f);
        }
    }

    /** Print a SQL SELECT by table model and query
     * @param query The UJO query
     * @param count only count of items is required;
     */
    final public Appendable printSelect
        ( final TableWrapper table
        , final Query query
        , final boolean count
        , final Appendable out
        ) throws IOException {

        return table.isView()
            ? printSelectView(table, query, count, out)
            : printSelectTable(query, count, out)
            ;
    }

    /** Print SQL view SELECT
     * @param query The UJO query
     * @param count only count of items is required;
     */
    protected Appendable printSelectView(TableWrapper table, Query query, boolean count, Appendable out) throws IOException {
        final String userSql = query.getSqlParameters()!=null
                ? query.getSqlParameters().getSqlStatement()
                : null
                ;
        final MetaSelect select = userSql!=null
                ? new MetaSelect(userSql, MetaTable.SCHEMA.of(table.getModel()))
                : MetaTable.SELECT_MODEL.of(table.getModel())
                ;
        final String where = query.getDecoder().getWhere();
        final List<Key> orderByList = query.getOrderBy();

        for (Key p : select.readKeys()) {
            String value = (String) p.of(select);

            if (p==MetaSelect.SELECT && count) {
                out.append(p.toString());
                out.append( "COUNT(*)" );
            } else if (p==MetaSelect.WHERE && value.length()+where.length()>0) {
                out.append(p.toString());
                out.append( value );
                out.append( value.length()==0 || where.length()==0 ? "" : " AND " );
                out.append( where );
            } else if (p==MetaSelect.ORDER && !count && !orderByList.isEmpty()){
                printSelectOrder(query, out);
            } else if (p==MetaSelect.LIMIT && !count && query.getLimit()>0){
                out.append(p.toString());
                out.append(String.valueOf(query.getLimit()));
            } else if (p==MetaSelect.OFFSET && !count && query.getOffset()>0){
                out.append(p.toString());
                out.append(String.valueOf(query.getOffset()));
            } else if (isFilled(value)) {
                out.append(p.toString());
                out.append( value );
            }
        }
        return out;
    }

    /** Print SQL database SELECT
     * @param query The UJO query
     * @param count only count of items is required;
     */
    protected Appendable printSelectTable(final Query query, final boolean count, final Appendable out) throws IOException {
        if (count && query.isDistinct()) {
            out.append("SELECT COUNT(*) FROM (");
            printSelectTableBase(query, count, out);
            out.append("\n\tGROUP BY ");
            printTableColumns(query.getColumns(), null, out);
            out.append(") ujorm_count_");
        } else {
            printSelectTableBase(query, count, out);
        }
        return out;
    }

    /** Print SQL database SELECT
     * @param query The UJO query
     * @param count only count of items is required;
     */
    protected void printSelectTableBase(final Query query, final boolean count, final Appendable out) throws IOException {
        out.append("SELECT ");

        if (count!=query.isDistinct()) {
            out.append(count
                ? "COUNT(*)"
                : "DISTINCT "
                );
        }
        if (!count || query.isDistinct()) {
            printTableColumns(query.getColumns(), null, out);
        }
        out.append("\n\tFROM ");

        if (query.getCriterion() != null) {
            final CriterionDecoder ed = query.getDecoder();
            final TableWrapper[] tables = ed.getTables();

            for (int i=0; i<tables.length; ++i) {
                if (i>0) {
                    out.append(", ");
                }
                printTableAliasDefinition(tables[i], out);
            }

            final String where = ed.getWhere();
            if (where.length()>0) {
                out.append(" WHERE ");
                out.append(where);
            }
        } else {
            printTableAliasDefinition(query.getTableModel(), out);
        }

        if (!count) {
            if (!query.getOrderBy().isEmpty()) {
               printSelectOrder(query, out);
            }
            if (query.isOffset()) {
                printOffset(query, out);
            }
            if (query.isLockRequest()) {
               out.append(' ');
               printLockForSelect(query, out);
            }
        }
    }

    /** Print a 'lock clausule' to the end of SQL SELECT statement to use a pessimistic lock.
     * The current database does not support the feature, throw an exception UnsupportedOperationException.
     * <br>The method prints a text "FOR UPDATE".
     * @param query The UJO query
     */
    protected Appendable printLockForSelect(final Query query, final Appendable out) throws IOException, UnsupportedOperationException {
        out.append("FOR UPDATE");
        return out;
    }

    /** Print SQL ORDER BY */
    protected void printSelectOrder(Query query, Appendable out) throws IOException {

        out.append(" ORDER BY ");
        final List<Key> props = query.getOrderBy();
        for (int i=0; i<props.size(); i++) {
            final ColumnWrapper column = query.readOrderColumn(i);
            final boolean ascending = props.get(i).isAscending();
            if (i > 0) {
                out.append(", ");
            }
            printColumnAlias(column, out);
            if (!ascending) {
                out.append(" DESC");
            }
        }
    }

    /** Print the call of a stored procedure by template: <br>
     * {? = call procedure_when(?,?)}
     */
    public Appendable printCall(MetaProcedure procedure, Appendable out) throws IOException {

        List<MetaColumn> propList = MetaProcedure.PARAMETERS.of(procedure);

        out.append('{').append(' ');
        if (!propList.get(0).isVoid()) {
           out.append("? =");
        }
        out.append(" call ");
        out.append(procedure.getProcedureName());

        // Print all parameters:
        if (propList.size()>1) {
            for (int i=1; i<propList.size(); i++) {
                out.append(i==1 ? "(?" : ",?");
            }
            out.append(')');
        }
        out.append(' ').append('}');
        return out;
    }

    /** Print an OFFSET of the statement SELECT. */
    public void printOffset(Query query, Appendable out) throws IOException {
        int limit = query.isLimit()
            ? query.getLimit()
            : Integer.MAX_VALUE
            ;
        out.append(" LIMIT " + limit);
        out.append(" OFFSET " + query.getOffset());
    }

    /** Prinnt the full sequence table */
    protected Appendable printSequenceTableName(final UjoSequencer sequence, final Appendable out) throws IOException {
        String schema = sequence.getDatabaseSchema();
        if (isFilled(schema)) {
            printQuotedName(schema, out);
            out.append('.');
        }
        printQuotedName(getSeqTableModel().getTableName(), out);
        return out;
    }

    /** Print SQL CREATE SEQUENCE. No JDBC parameters. */
    public Appendable printSequenceTable(final MetaDatabase db, final Appendable out) throws IOException {
        String schema = MetaDatabase.SCHEMA.of(db);
        Integer cache = MetaParams.SEQUENCE_CACHE.of(db.getParams());

        out.append("CREATE TABLE ");
        if (isFilled(schema)) {
            printQuotedName(schema, out);
            out.append('.');
        }

        final MetaColumn pkType = new MetaColumn();
        MetaColumn.DB_TYPE.setValue(pkType, DbType.BIGINT);

        printQuotedName(getSeqTableModel().getTableName(), out);
        out.append ( ""
        + "\n\t( " + getQuotedName(getSeqTableModel().getId()) + " VARCHAR(96) NOT NULL PRIMARY KEY"
        + "\n\t, " + getQuotedName(getSeqTableModel().getSequence()) + " " + getColumnType(pkType) + " DEFAULT " + cache + " NOT NULL"
        + "\n\t, " + getQuotedName(getSeqTableModel().getCache()) + " INT DEFAULT " + cache + " NOT NULL"
        + "\n\t, " + getQuotedName(getSeqTableModel().getMaxValue()) + " " + getColumnType(pkType) + " DEFAULT 0 NOT NULL"
        + "\n\t)");
        return out;
    }

    /**
     * Print SQL CREATE SEQUENCE (insert sequence row). No JDBC parameters.
     */
    public Appendable printSequenceInit(final UjoSequencer sequence, final Appendable out) throws IOException {
        Integer cache = MetaParams.SEQUENCE_CACHE.of(sequence.getDatabase().getParams());
        return printSequenceInitWithValues(sequence, cache, cache, out);
    }

    public Appendable printSequenceInitWithValues(final UjoSequencer sequence, long seq, int cache, final Appendable out) throws IOException {
        out.append("INSERT INTO ");
        printSequenceTableName(sequence, out);
        out.append(" (");
        printQuotedNameAlways(getSeqTableModel().getId(), out);
        out.append(",");
        printQuotedNameAlways(getSeqTableModel().getSequence(), out);
        out.append(",");
        printQuotedNameAlways(getSeqTableModel().getCache(), out);
        out.append(",");
        printQuotedNameAlways(getSeqTableModel().getMaxValue(), out);
        out.append(") VALUES (?," + seq).append("," + cache).append(",0)");

        return out;
    }

    /**
     * Print SQL UPDATE NEXT SEQUENCE value.
     */
    public Appendable printSequenceNextValue(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append("UPDATE ");
        printSequenceTableName(sequence, out);
        out.append(" SET ");
        printQuotedNameAlways(getSeqTableModel().getSequence(), out);
        out.append("=");
        printQuotedNameAlways(getSeqTableModel().getSequence(), out);
        out.append("+");
        printQuotedNameAlways(getSeqTableModel().getCache(), out);
        out.append(" WHERE ");
        printQuotedNameAlways(getSeqTableModel().getId(), out);
        out.append("=?");
        return out;
    }

    /** Set sequence to the max value. */
    public Appendable printSetMaxSequence(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append("UPDATE ");
        printSequenceTableName(sequence, out);
        out.append(" SET ");
        printQuotedNameAlways(getSeqTableModel().getSequence(), out);
        out.append("=");
        printQuotedNameAlways(getSeqTableModel().getMaxValue(), out);
        out.append(" WHERE ");
        printQuotedNameAlways(getSeqTableModel().getId(), out);
        out.append("=?");
        return out;
    }

    /**
     * Print SQL CURRENT SEQUENCE VALUE. Returns a new sequence limit and the.
     * The SQL columns are always selected in the order: sequence, cache, maxValue.
     * current cache.
     */
    public Appendable printSequenceCurrentValue(final UjoSequencer sequence, final Appendable out) throws IOException {
        final SeqTableModel tm = getSeqTableModel();

        out.append("SELECT ");
        printQuotedNameAlways(tm.getSequence(), out);
        out.append(", ");
        printQuotedNameAlways(tm.getCache(), out);
        out.append(", ");
        printQuotedNameAlways(tm.getMaxValue(), out);
        out.append(" FROM ");

        printSequenceTableName(sequence, out);
        out.append(" WHERE ");
        printQuotedNameAlways(tm.getId(), out);
        out.append("=?");

        return out;
    }

    /** Print SQL DELETE SEQUENCE BY ID. */
    public Appendable printSequenceDeleteById(final UjoSequencer sequence, String id, final Appendable out) throws IOException {
        final SeqTableModel tm = getSeqTableModel();

        out.append("DELETE FROM ");
        printSequenceTableName(sequence, out);
        out.append(" WHERE ");
        printQuotedNameAlways(tm.getId(), out);
        out.append("=?");

        return out;
    }

    /** Returns true, if the argument text is not null and not empty. */
    final protected boolean isFilled(final CharSequence text) {
        return text!=null && text.length()>0;
    }

    /** Print the new line. */
    final public void println(final Appendable out) throws IOException {
        out.append('\n');
    }

    /** Print SQL 'COMMIT' */
    public Appendable printCommit(Appendable out) throws IOException {
        out.append("COMMIT");
        return out;
    }

    /** Print a Comment to a database Table */
    public Appendable printComment(MetaTable table, Appendable out) throws IOException {
        out.append("COMMENT ON TABLE ");
        printFullTableName(table, out);
        out.append(" IS '");
        escape(MetaTable.COMMENT.of(table), out);
        out.append("'");
        return out;
    }

    /** Print a Comment to a database Column */
    public Appendable printComment(MetaColumn column, Appendable out) throws IOException {
        out.append("COMMENT ON COLUMN ");
        printFullTableName(MetaColumn.TABLE.of(column), out);
        out.append('.');
        out.append(column.getName());
        out.append(" IS '");
        escape(MetaColumn.COMMENT.of(column), out);
        out.append("'");
        return out;
    }

    /** Return database SQL keyword set in the upper case. */
    public Set<String> getKeywordSet(Connection conn) {
        Set<String> result = new HashSet<String>(128);
        Reader reader = null;
        try {
            // Get keywords from a JDBC meta-data object:
            reader = new CharArrayReader(conn.getMetaData().getSQLKeywords().concat(",").toCharArray());
            assignKeywords(result, reader);

            // Get keywords from a text file:
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/org/ujorm/orm/sql-keywords.txt"), "UTF8"));
            assignKeywords(result, reader);

        } catch (Throwable e) {
            LOGGER.log(UjoLogger.WARN, "Can't read SQL keywords", e);
        } finally {
            if (reader!=null) try {
                reader.close();
            } catch (IOException e) {
                LOGGER.log(UjoLogger.WARN, "Can't close reader", e);
            }
        }
        result.remove("");
        return result;
    }

    /** Assign keywords from reader to a set */
    private void assignKeywords(Set<String> result, Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        int c;
        while ((c=reader.read())!=-1) {
            if (c==',') {
                result.add(builder.toString().trim().toUpperCase());
                builder.setLength(0);
            } else {
                builder.append((char)c);
            }
        }
    }

    /** Escape the special character: "'" */
    final protected void escape(final CharSequence text, final Appendable out) throws IOException {
        for (int i=0; i<text.length(); i++) {
            final char c = text.charAt(i);
            switch(c) {
                case '\'': out.append("''"); break;
                default  : out.append(c);
            }
        }
    }

    /** The table model for an internal sequence table support */
    public SeqTableModel getSeqTableModel() {
        return pkTableModel;
    }

    /** Perform the method: {@link Connection#releaseSavepoint(java.sql.Savepoint) ?
     * @param conn Database Connection
     * @param savepoint Required Savepoint
     * @param afterRollback release is called after a rollback ?
     * @see http://technet.microsoft.com/en-us/library/ms378791%28v=sql.110%29.aspx
     * @see Connection#releaseSavepoint(java.sql.Savepoint)
     */
    public void releaseSavepoint(final Connection conn, final Savepoint savepoint, final boolean afterRollback) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    /**
     * Prints quoted name (identifier) to SQL according the parameter {@link MetaParams.QUOTE_SQL_NAMES}.
     * @param name Name (identifier) for quoting
     * @param sql Target SQL for printing new quoted name
     * @return SQL with printed quoted name
     */
    final public Appendable printQuotedName(final CharSequence name, final Appendable sql) throws IOException {
        if (quoteRequest==null) {
            quoteRequest = ormHandler.getParameters().isQuotedSqlNames();
        }
        if (quoteRequest) {
            printQuotedNameAlways(name, sql);
        } else {
            sql.append(name);
        }
        return sql;
    }

    /**
     * Prints quoted name (identifier) to SQL always.
     * The default quated character is '"', however different SQL dialects must have got a different character-pairs.
     * @param name Name (identifier) for quoting
     * @param sql Target SQL for printing new quoted name
     * @return SQL with printed quoted name
     */
    protected Appendable printQuotedNameAlways(final CharSequence name, final Appendable sql) throws IOException {
        sql.append('"'); // quotation start character based on SQL dialect
        sql.append(name);
        sql.append('"'); // quotation end character based on SQL dialect
        return sql;
    }

    /** Prints quoted name (identifier) to SQL - always. */
    protected final String getQuotedName(final CharSequence name) throws IOException {
        final StringBuilder result = new StringBuilder(name.length()+4);
        printQuotedNameAlways(name, result);
        return result.toString();
    }

    /**
     * Returns a name provider
     * @return Current SQL name provider
     * @throws IllegalStateException A problem during creating an instance.
     */
    public SqlNameProvider getNameProvider() throws IllegalStateException {
        if (nameProvider==null) {
            try {
                nameProvider = MetaParams.SQL_NAME_PROVIDER.of(ormHandler.getParameters()).newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Can't create an instance of the " + ormHandler.getParameters(), e);
            }
        }
        return nameProvider;
    }

    /** Get an Extended dialect */
    public SqlDialectEx getExtentedDialect() {
        if (extentedDialect == null) {
            extentedDialect = new SqlDialectEx(this);
        }
        return extentedDialect;
    }

    /** Create a SQL script for the NEXT SEQUENCE from a native database sequencer */
    public Appendable printNextSequence(String sequenceName, MetaTable table, Appendable out) throws IOException {
        out.append("SELECT NEXTVAL('");
        out.append(sequenceName);
        out.append("')");
        return out;
    }
}
