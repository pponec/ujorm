/*
 *  Copyright 2009-2018 Pavel Ponec
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
import java.util.Locale;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.TemplateValue;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.ao.CheckReport;
import org.ujorm.orm.ao.QuoteEnum;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaIndex;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaProcedure;
import org.ujorm.orm.metaModel.MetaSelect;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.msg.MsgFormatter;
import static org.ujorm.core.UjoTools.SPACE;
import static org.ujorm.tools.Check.*;

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
    protected static final String COMMON_SEQ_TABLE_KEY = "<ALL>";
    /** The new line separator for SQL statements */
    protected static final String NEW_LINE_SEPARATOR = "\n\t";
    /** The default quote character */
    private static final char QUOTE_CHARACTER = '"';

    /** The ORM handler */
    protected OrmHandler ormHandler;
    /** The name provider */
    private SqlNameProvider nameProvider;
    /** Quoting policy */
    private CheckReport quotingPolicy;

    /** Set the OrmHandler - the method is for internal call only. */
    public void setHandler(@NotNull final OrmHandler ormHandler) {
        Assert.isNull(this.ormHandler, "The OrmHandler is assigned yet.");
        this.ormHandler = ormHandler;
    }

    /** Returns a default JDBC URL */
    abstract public String getJdbcUrl();

    /** Returns a JDBC driver class name. */
    abstract public String getJdbcDriver();

    /** Create a new database connection */
    public Connection createConnection(@NotNull final MetaDatabase db) throws Exception {
        return db.createInternalConnection();
    }

    /** Get or create an Initial Context for the JNDI lookup. */
    public InitialContext createJndiInitialContext(@NotNull final MetaDatabase db) throws NamingException {
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
    public Appendable printCreateSchema(String schema, @NotNull final Appendable out) throws IOException {
        out.append("CREATE SCHEMA IF NOT EXISTS ");
        printQuotedName(schema, QuoteEnum.BY_CONFIG, out);
        return out;
    }

    /** Print SQL 'SET SCHEMA'. The method is not used yet. */
    @Deprecated
    public Appendable printDefaultSchema(String schema, @NotNull final Appendable out) throws IOException {
        out.append("SET SCHEMA ");
        printQuotedName(schema, QuoteEnum.BY_CONFIG, out);
        return out;
    }

    /** Print a extended SQL table name by sample: SCHEMA.TABLE */
    public Appendable printFullTableName(final MetaTable table, @NotNull final Appendable out) throws IOException {
        return printFullTableName(table, table.get(MetaTable.QUOTED), out);
    }

    /** Print a extended SQL table name by sample: SCHEMA.TABLE_NAME */
    public Appendable printFullTableName(final MetaTable table, @NotNull final QuoteEnum quoted, @NotNull final Appendable out) throws IOException {
        final String tableSchema = MetaTable.SCHEMA.of(table);
        final String tableName = MetaTable.NAME.of(table);

        if (hasLength(tableSchema)) {
            printQuotedName(tableSchema, quoted, out).append('.');
        }
        printQuotedName(tableName, quoted, out);
        return out;
    }

    /** Print a SQL database and table name and an alias definition - by sample: SCHEMA.TABLE ALIAS */
    public void printTableAliasDefinition(final TableWrapper table, @NotNull final Appendable out) throws IOException {
        printFullTableName(table.getModel(), out);
        final String alias = table.getAlias();
        if (hasLength(alias)) {
            out.append(SPACE);
            printQuotedName(alias, QuoteEnum.BY_CONFIG, out);
        }
    }

    /** Print a full SQL column alias name by sample: "TABLE_ALIAS"."ORIG_COLUMN" */
    public Appendable printColumnAlias(final ColumnWrapper column, @NotNull final Appendable out) throws IOException {
        printQuotedName(column.getTableAlias(), QuoteEnum.BY_CONFIG, out);
        out.append('.');
        printColumnName(column, out);
        return out;
    }

    /** Print a SQL script to create table */
    public Appendable printTable(MetaTable table, @NotNull final Appendable out) throws IOException {
        out.append("CREATE TABLE ");
        printFullTableName(table, out);
        String separator = NEW_LINE_SEPARATOR.concat("( ");
        for (MetaColumn column : MetaTable.COLUMNS.getList(table)) {
            out.append(separator);
            separator = NEW_LINE_SEPARATOR + ", ";

            if (column.isForeignKey()) {
                printFKColumnsDeclaration(column, out);
            } else {
                printColumnDeclaration(column, out);
            }
        }
        out.append(NEW_LINE_SEPARATOR).append(')');
        return out;
    }

    /** Print a SQL script to add a new column to the table */
    public Appendable printAlterTableAddColumn(MetaColumn column, @NotNull final Appendable out) throws IOException {
        return printAlterTableColumn(column, true, out);
    }

    /** Print a SQL script to add a new column to the table */
    public Appendable printAlterTableColumn(MetaColumn column, boolean add, @NotNull final Appendable out) throws IOException {
        out.append("ALTER TABLE ");
        printFullTableName(column.getTable(), out);
        out.append(add
            ? " ADD COLUMN "
            : " ALTER COLUMN ");
        if (column.isForeignKey()) {
            printFKColumnsDeclaration(column, out);
        } else {
            printColumnDeclaration(column, out);
        }
        if (column.hasDefaultValue()) {
            printDefaultValue(column, out);
        }
        return out;
    }

    /** Print a SQL phrase for the DEFAULT VALUE, for example: DEFAULT 777 */
    public Appendable printDefaultValue(final MetaColumn column, @NotNull final Appendable out) throws IOException {
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
            printColumnName(column, out);
            out.append(" SET ");
            printDefaultValue(column, out);
        }
        return out;
    }

    /**
     * Print foreign key for the parameter column
     * @return More statements separated by the ';' characters are enabled
     */
    public Appendable printForeignKey(MetaColumn column, @NotNull final Appendable out) throws IOException {

        MetaTable table = column.getTable();
        List<MetaColumn> fColumns = column.getForeignColumns();
        MetaTable foreignTable = fColumns.get(0).getTable();
        int columnsSize = fColumns.size();

        out.append("ALTER TABLE ");
        printFullTableName(table, out);
        out.append(NEW_LINE_SEPARATOR).append("ADD CONSTRAINT ");
        getNameProvider().printConstraintName(table, column, out);
        out.append(" FOREIGN KEY ");

        for (int i=0; i<columnsSize; ++i) {
            out.append(i==0 ? "(" : ", ");
            printQuotedName(column.getForeignColumnName(i), column.get(MetaColumn.QUOTED), out);
        }

        out.append(')').append(NEW_LINE_SEPARATOR).append("REFERENCES ");
        printFullTableName(foreignTable, out);

        for (int i = 0, max = fColumns.size(); i < max; i++) {
            out.append(i == 0 ? "(" : ", ");
            printColumnName(fColumns.get(i), out);
        }

        out.append(')');
        //out.append("\tON DELETE CASCADE");
        return out;
    }

    /**
     * Print an INDEX for the parameter column.
     * @return More statements separated by the ';' characters are enabled
     */
    public Appendable printIndex(final MetaIndex index, @NotNull final Appendable out) throws IOException {

        out.append("CREATE ");
        if (MetaIndex.UNIQUE.of(index)) {
            out.append("UNIQUE ");
        }
        out.append("INDEX ");
        out.append(MetaIndex.NAME.of(index));
        out.append(" ON ");
        printFullTableName(MetaIndex.TABLE.of(index), out);
        String separator = " (";

        for (MetaColumn column : MetaIndex.COLUMNS.getList(index)) {
            out.append(separator);
            printColumnName(column, out);
            separator = ", ";
        }
        out.append(')');
        return out;
    }

    /** Is allowed a column length in a SQL phrase for creating column? The length example can be: NAME CHAR(10) */
    protected boolean isColumnLengthAllowed(final MetaColumn column) {
        switch (MetaColumn.DB_TYPE.of(column)) {
            case INTEGER:
            case SMALLINT:
            case BIGINT:
            case DATE:
            case TIME:
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
            case CLOB:
            case BLOB:
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
     * @param columnWrapper Database Column
     * @throws java.io.IOException
     */
    public Appendable printColumnDeclaration
        ( @NotNull final ColumnWrapper columnWrapper
        , @NotNull final Appendable out) throws IOException {

        printColumnName(columnWrapper, out);
        final MetaColumn column = columnWrapper.getModel();
        out.append(SPACE);
        out.append(getColumnType(column));

        if (isColumnLengthAllowed(column)) {
            out.append('(').append(MetaColumn.MAX_LENGTH.of(column).toString());
            if (!MetaColumn.PRECISION.isDefault(column)) {
                out.append(',').append(MetaColumn.PRECISION.of(column).toString());
            }
            out.append(')');
        }

        final boolean isModel = columnWrapper instanceof MetaColumn;
        if (isModel && MetaColumn.MANDATORY.of(column)) {
            out.append(" NOT NULL");
        }
        if (isModel && MetaColumn.PRIMARY_KEY.of(column)) {
            out.append(" PRIMARY KEY");
        }
        return out;
    }

    /** Returns a database column type */
    protected String getColumnType(final MetaColumn column) {
        final String sqlType = MetaColumn.DB_TYPE.of(column).name();
        switch (MetaColumn.DB_TYPE.of(column)) {
            case TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP WITH TIME ZONE";
            default:
                return sqlType;
        }
    }

    /** Print a SQL to create foreign keys. */
    public Appendable printFKColumnsDeclaration
        ( @NotNull final MetaColumn column
        , @NotNull final Appendable out) throws IOException {

        final List<MetaColumn> columns = column.getForeignColumns();

        for (int i = 0; i < columns.size(); ++i) {
            if (i > 0) {
                out.append(COMMON_SEQ_TABLE_KEY).append(", ");
            }
            printColumnDeclaration(ColumnWrapper.forName(columns.get(i), column.getForeignColumnName(i)), out);
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
    public Appendable printInsert
            ( @NotNull final OrmUjo bo
            , @NotNull final Appendable out) throws IOException {

        MetaTable table = ormHandler.findTableModel((Class) bo.getClass());
        StringBuilder values = new StringBuilder();

        out.append("INSERT INTO ");
        printFullTableName(table, out);
        out.append(" (");

        printTableColumns(table.getColumns(), values, out);

        out.append(") VALUES (")
           .append(values)
           .append(')')
           ;

        return out;
    }

    /** Print an SQL INSERT statement.
     * @param bos Business object list
     * @param idxFrom Start index from list
     * @param idxTo Finished index from list (excluded)
     * @see #isMultiRowInsertSupported()
     */
    public Appendable printInsert(final List<? extends OrmUjo> bos, final int idxFrom, final int idxTo, @NotNull final Appendable out) throws IOException {

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
        out.append(')');
        return out;
    }

    /** Print an batch SQL INSERT statement using SELECT UNION statement.
     * @param bos Business object list
     * @param idxFrom Start index from list
     * @param idxTo Finished index from list (excluded)
     * @param fromPhrase For example the Oracle syntax: SELECT 1,2,3 FROM DUAL;
     * @see #isMultiRowInsertSupported()
     */
    public Appendable printInsertBySelect
            ( @NotNull final List<? extends OrmUjo> bos
            , final int idxFrom
            , final int idxTo
            , @NotNull final String fromPhrase
            , @NotNull final Appendable out) throws IOException {

        final MetaTable table = ormHandler.findTableModel(bos.get(idxFrom).getClass());
        final StringBuilder values = new StringBuilder(32);

        out.append("INSERT INTO ");
        printFullTableName(table, out);
        out.append(" (");

        printTableColumns(table.getColumns(), values, out);

        for (int i=idxFrom; i<idxTo; ++i) {
            out.append(i==idxFrom ? ")\nSELECT " : " UNION ALL\nSELECT ")
               .append(values);
            if (hasLength(fromPhrase)) {
                out.append(SPACE).append(fromPhrase);
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

    /** Print an SQL UPDATE statement. */
    public Appendable printUpdate
        ( @NotNull final List<MetaColumn> changedColumns
        , @NotNull final CriterionDecoder decoder
        , @NotNull final Appendable out
        ) throws IOException
    {
        final MetaTable table = decoder.getBaseTable();
        out.append("UPDATE ");
        printTableAliasDefinition(table, out);
        out.append(NEW_LINE_SEPARATOR).append("SET ");

        for (int i=0; i<changedColumns.size(); i++) {
            final MetaColumn ormColumn = changedColumns.get(i);
            Assert.isFalse(ormColumn.isPrimaryKey(), "Primary key can not be changed: {}", ormColumn);
            out.append(i==0 ? "" :  ", ");
            printColumnName(ormColumn, out);
            out.append("=?");
        }
        out.append(NEW_LINE_SEPARATOR).append("WHERE ");

        if (decoder.getTableCount() > 1) {
            printColumnName(table.getFirstPK(), out);
            out.append(" IN (");
            printSelectTableBase(createSubQuery(decoder), false, out);
            out.append(')');
        } else {
            out.append(decoder.getWhere());
        }
        return out;
    }

    /** Print an SQL DELETE statement. */
    public Appendable printDelete
        ( @NotNull final CriterionDecoder decoder
        , @NotNull final Appendable out
        ) throws IOException
    {
        final MetaTable table = decoder.getBaseTable();
        out.append("DELETE FROM ");
        printTableAliasDefinition(table, out);
        out.append(" WHERE ");

        if (decoder.getTableCount() > 1) {
            printColumnName(table.getFirstPK(), out);
            out.append(" IN (");
            printSelectTableBase(createSubQuery(decoder), false, out);
            out.append(')');
        } else {
            //String where = decoder.getWhere().replace(tableAlias + '.', fullTableName + '.');
            out.append(decoder.getWhere());
        }
        return out;
    }

    /** Create a sub-query for the DELETE/UPDATE statement */
    protected Query createSubQuery(@NotNull final CriterionDecoder decoder) {
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
     *         return "{0}&lt;&gt;{1}";
     *     case GT:
     *         return "{0}&gt;{1}";
     *     ...
     * </pre>
     * @return Template with arguments type of {@code {1}={2}}
     */
    @NotNull
    public String getCriterionTemplate(@NotNull final ValueCriterion crit) {

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
                return "LOWER({0})={1}";
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE:
                return "LOWER({0}) LIKE {1}";
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
     * @param columns List of table columns
     * @param values Print columns including its aliases.
     * @param out Table columns output.
     * @throws java.io.IOException
     */
    public void printTableColumns
        ( @NotNull final Collection<? extends ColumnWrapper> columns
        , @NotNull final Appendable values
        , @NotNull final Appendable out) throws IOException {
        String separator = "";
        boolean select = values==null; // SELECT
        for (ColumnWrapper wColumn : columns) {
            final MetaColumn column = wColumn.getModel();
            if (column.isForeignKey()) {
                for (int i = 0; i < column.getForeignColumns().size(); ++i) {
                    out.append(separator);
                    if (select) {
                        printQuotedName(wColumn.getTableAlias(), QuoteEnum.BY_CONFIG, out);
                        out.append('.');
                    }
                    printQuotedName(column.getForeignColumnName(i), wColumn.getModel().get(MetaColumn.QUOTED), out);
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
                    printColumnName(column, out);
                }
                if (values != null) {
                    values.append(separator);
                    values.append("?");
                }
                separator = ", ";
            }
        }
    }


    /** Print a <strong>value condition phrase</strong> from the criterion.
     * @return A nullable value criterion to assign into the SQL query.
     */
    @Nullable
    public ValueCriterion printCriterion(@NotNull final ValueCriterion crn, @NotNull final Appendable out) throws IOException {
        final Key key = crn.getLeftNode();
        final Operator operator = crn.getOperator();
        final Object right = crn.getRightNode();
        final ColumnWrapper column = key != null
            ? AliasKey.getLastKey(key).getColumn(ormHandler)
            : null;
        if (right == null ) {
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
                    final String msg = MsgFormatter.format
                            ( "The NULL comparison by the {} operator is forbidden."
                            , operator);
                    throw new UnsupportedOperationException(msg);
            }
        }

        final String template = getCriterionTemplate(crn);
        Assert.notNull(template, "Unsupported SQL operator: {}", operator);

        switch (operator) {
            case XFIXED:
                out.append( template );
                break;
            case XSQL:
                if (right instanceof TemplateValue) {
                    final Object myValue = ((TemplateValue) right).getRightVale();
                    final ValueCriterion myCrn = (ValueCriterion) Criterion.where
                            ( key
                            , Operator.EQ // An hack
                            , myValue);
                    return printCriterionValue(template, column, myCrn, out);
                }
                if (template.contains("{0}")) {
                    out.append(MessageFormat.format(template, getAliasColumnName(column)));
                } else {
                    out.append(template);
                }
                break;
            default:
                return printCriterionValue(template, column, crn, out);
        }
        return null;
    }

    /**
     * Write a right value form criterion
     * @return A value criterion to assign into the SQL query.
     * @param template Template with arguments type of {@code {1} = {2}}
     * @param column Column model
     * @param crit Condition model
     * @param out Output value
     * @throws IOException
     */
    @Nullable
    protected ValueCriterion printCriterionValue
        ( @NotNull final String template
        , @NotNull final ColumnWrapper column
        , @NotNull final ValueCriterion crit
        , @NotNull final Appendable out) throws IOException {
        final Object right = crit.getRightNode();
        if (right instanceof Key) {
            final Key rightProperty = (Key) right;
            final ColumnWrapper col2 = AliasKey.getLastKey(rightProperty).getColumn(ormHandler);

            if (col2.getModel().isForeignKey()) {
                throw new UnsupportedOperationException("Foreign key is not supported yet");
            }
            out.append(MessageFormat.format(template, getAliasColumnName(column), getAliasColumnName(col2)));
        } else if (right instanceof Object[]) {
            final Object[] os = (Object[]) right;
            final StringBuilder sb = new StringBuilder(2 * os.length);
            for (int i = 0; i < os.length; i++) {
                sb.append(i > 0 ? ",?" : "?");
            }
            final String f = MessageFormat.format(template, getAliasColumnName(column), sb.toString());
            out.append(f);
            return crit;
        } else if (column.getModel().isForeignKey()) {
            printForeignKey(crit, column, template, out);
            return crit;
        } else {
            final String f = MessageFormat.format(template, getAliasColumnName(column), "?");
            out.append(f);
            return crit;
        }
        return null;
    }

    /** Returns quoted column name including the alias table */
    protected String getAliasColumnName(ColumnWrapper column) throws IOException {
        final Appendable out = new StringBuilder(32);
        printQuotedName(column.getTableAlias(), QuoteEnum.BY_CONFIG, out);
        out.append('.');
        printColumnName(column, out);
        return out.toString();
    }

    /** Print all items of the foreign key */
    public void printForeignKey
        ( @NotNull final ValueCriterion crit
        , @NotNull final ColumnWrapper column
        , @NotNull final String template
        , @NotNull final Appendable out
        ) throws IOException
    {
        int size = column.getModel().getForeignColumns().size();
        for (int i=0; i<size; i++) {
            if (i>0) {
                out.append(SPACE);
                out.append(crit.getOperator().name());
                out.append(SPACE);
            }

            StringBuilder columnName = new StringBuilder(256);
            String alias = column.getTableAlias();
            if (hasLength(alias)) {
                printQuotedName(alias, QuoteEnum.BY_CONFIG, columnName);
                columnName.append('.');
            }
            printQuotedName(column.getModel().getForeignColumnName(i), QuoteEnum.BY_CONFIG, columnName);
            String f = MessageFormat.format(template, columnName, "?");
            out.append(f);
        }
    }

    /** Print a SQL SELECT by table model and query
     * @param query The UJO query
     * @param count only count of items is required;
     */
    final public Appendable printSelect
        ( @NotNull final TableWrapper table
        , @NotNull final Query query
        , final boolean count
        , @NotNull final Appendable out
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
    protected Appendable printSelectView
        ( @NotNull final TableWrapper table
        , @NotNull final Query query
        , final boolean count
        , @NotNull final Appendable out) throws IOException {
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
            } else if (hasLength(value)) {
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
    protected Appendable printSelectTable
        ( @NotNull final  Query query
        , final boolean count
        , @NotNull final Appendable out) throws IOException {
        if (count && query.isDistinct()) {
            out.append("SELECT COUNT(*) FROM (");
            printSelectTableBase(query, count, out);
            out.append(NEW_LINE_SEPARATOR).append("GROUP BY ");
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
    protected void printSelectTableBase
        ( @NotNull final Query query
        , final boolean count
        , @NotNull final Appendable out) throws IOException {
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
        out.append(NEW_LINE_SEPARATOR).append("FROM ");

        if (query.getCriterion() != null) {
            final CriterionDecoder ed = query.getDecoder();

            printTableAliasDefinition(ed.getBaseTable(), out);
            for (CriterionDecoder.Relation relation : ed.getRelations()) {
                out.append(NEW_LINE_SEPARATOR);
                out.append(query.getOuterJoins().contains(relation.getLeft()) // TODO.pop ?
                        ? "LEFT OUTER"
                        : "INNER");
                out.append(" JOIN ");
                printTableAliasDefinition(relation.getRight().buildTableWrapper(), out);
                out.append(" ON ");
                printColumnAlias(relation.getRight(), out);
                out.append('=');
                printColumnAlias(relation.getLeft(), out);
            }

            final String where = ed.getWhere();
            if (!where.isEmpty()) {
                out.append(NEW_LINE_SEPARATOR).append("WHERE ");
                out.append(where);
            }
        } else {
            printTableAliasDefinition(query.getTableModel(), out);
        }

        if (!count) {
            if (!query.getOrderBy().isEmpty()) {
               printSelectOrder(query, out);
            }
            printLimitAndOffset(query, out);
            if (query.isLockRequest()) {
               out.append(SPACE);
               printLockForSelect(query, out);
            }
        }
    }

    /** Print a 'lock phrase' to the end of SQL SELECT statement to use a pessimistic lock.
     * The current database does not support the feature, throw an exception UnsupportedOperationException.
     * <br>The method prints a text "FOR UPDATE".
     * @param query The UJO query
     */
    protected Appendable printLockForSelect
        ( @NotNull final Query query
        , @NotNull final Appendable out) throws IOException, UnsupportedOperationException {
        out.append("FOR UPDATE");
        return out;
    }

    /** Print SQL ORDER BY */
    protected void printSelectOrder
        ( @NotNull final Query query
        , @NotNull final Appendable out) throws IOException {

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
    public Appendable printCall
        ( @NotNull final MetaProcedure procedure
        , @NotNull final Appendable out) throws IOException {

        final List<MetaColumn> propList = MetaProcedure.PARAMETERS.of(procedure);

        out.append('{').append(SPACE);
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
        out.append(SPACE).append('}');
        return out;
    }

    /** Print an OFFSET of the statement SELECT. 
     * @see <a href="https://bit.ly/3paHwNS">Note about an implementation Statement.setMaxRow() method (Stackoverflow).</a>
     * 
     */
    public void printLimitAndOffset
        ( @NotNull final Query query
        , @NotNull final Appendable out) throws IOException {  
        // int requiredLimit = query.isLimit() ? query.getLimit() : Integer.MAX_VALUE ;
        
        if (query.isLimit()) {
            out.append(" LIMIT ").append(String.valueOf(query.getLimit()));
        }
        if (query.isOffset()) {
            out.append(" OFFSET ").append(String.valueOf(query.getOffset()));
        }
    }

    /** Print the full sequence table */
    protected Appendable printSequenceTableName
        ( @NotNull final UjoSequencer sequence
        , @NotNull final Appendable out) throws IOException {
            return printSequenceTableName(sequence.getDatabaseSchema(), out);
    }

    /** Print the full sequence table */
    public Appendable printSequenceTableName
        ( @NotNull final String schema
        , @NotNull final Appendable out) throws IOException {
        if (hasLength(schema)) {
            printQuotedName(schema, QuoteEnum.BY_CONFIG, out);
            out.append('.');
        }
        printQuotedName(getSeqTableModel().getTableName(), QuoteEnum.BY_CONFIG, out);
        return out;
    }

    /** Print SQL CREATE SEQUENCE. No JDBC parameters. */
    public Appendable printSequenceTable
        ( @NotNull final MetaDatabase db
        , @NotNull final Appendable out) throws IOException {
        final Integer cache = MetaParams.SEQUENCE_CACHE.of(db.getParams());
        final MetaColumn pkType = new MetaColumn();
        MetaColumn.DB_TYPE.setValue(pkType, DbType.BIGINT);

        out.append("CREATE TABLE ");
        printSequenceTableName(MetaDatabase.SCHEMA.of(db), out);

        out.append ( ""
        + NEW_LINE_SEPARATOR.concat("( ") + getQuotedName(getSeqTableModel().getId()) + " VARCHAR(96) NOT NULL PRIMARY KEY"
        + NEW_LINE_SEPARATOR.concat(", ") + getQuotedName(getSeqTableModel().getSequence()) + SPACE + getColumnType(pkType) + " DEFAULT " + cache + " NOT NULL"
        + NEW_LINE_SEPARATOR.concat(", ") + getQuotedName(getSeqTableModel().getCache()) + " INT DEFAULT " + cache + " NOT NULL"
        + NEW_LINE_SEPARATOR.concat(", ") + getQuotedName(getSeqTableModel().getMaxValue()) + SPACE + getColumnType(pkType) + " DEFAULT 0 NOT NULL"
        + NEW_LINE_SEPARATOR.concat(")"));
        return out;
    }

    /**
     * Print SQL CREATE SEQUENCE (insert sequence row). No JDBC parameters.
     */
    public Appendable printSequenceInit
        ( @NotNull final UjoSequencer sequence
        , @NotNull final Appendable out) throws IOException {
        final Integer cache = MetaParams.SEQUENCE_CACHE.of(sequence.getDatabase().getParams());
        return printSequenceInitWithValues(sequence, cache, cache, out);
    }

    public Appendable printSequenceInitWithValues
        ( @NotNull final UjoSequencer sequence
        , final long seq
        , final int cache
        , @NotNull final Appendable out) throws IOException {
        out.append("INSERT INTO ");
        printSequenceTableName(sequence, out);
        out.append(" (");
        printQuotedName(getSeqTableModel().getId(), out);
        out.append(',');
        printQuotedName(getSeqTableModel().getSequence(), out);
        out.append(',');
        printQuotedName(getSeqTableModel().getCache(), out);
        out.append(',');
        printQuotedName(getSeqTableModel().getMaxValue(), out);
        out.append(") VALUES (?," + seq).append(',').append(Integer.toString(cache)).append(",0)");

        return out;
    }

    /**
     * Print SQL UPDATE NEXT SEQUENCE value.
     */
    public Appendable printSequenceNextValue
        ( @NotNull final UjoSequencer sequence
        , @NotNull final Appendable out) throws IOException {
        out.append("UPDATE ");
        printSequenceTableName(sequence, out);
        out.append(" SET ");
        printQuotedName(getSeqTableModel().getSequence(), out);
        out.append("=");
        printQuotedName(getSeqTableModel().getSequence(), out);
        out.append("+");
        printQuotedName(getSeqTableModel().getCache(), out);
        out.append(" WHERE ");
        printQuotedName(getSeqTableModel().getId(), out);
        out.append("=?");
        return out;
    }

    /** Set sequence to the max value. 
     * @TODO.pop: use JDBCV arguments
     */
    public Appendable printSetMaxSequence
        ( @NotNull final UjoSequencer sequence
        , @NotNull final Appendable out) throws IOException {
        out.append("UPDATE ");
        printSequenceTableName(sequence, out);
        out.append(" SET ");
        printQuotedName(getSeqTableModel().getSequence(), out);
        out.append("=");
        printQuotedName(getSeqTableModel().getMaxValue(), out);
        out.append(" WHERE ");
        printQuotedName(getSeqTableModel().getId(), out);
        out.append("=?");
        return out;
    }

    /**
     * Print SQL CURRENT SEQUENCE VALUE. Returns a new sequence limit and the.
     * The SQL columns are always selected in the order: sequence, cache, maxValue.
     * current cache.
     */
    public Appendable printSequenceCurrentValue
        ( @NotNull final UjoSequencer sequence
        , @NotNull final Appendable out) throws IOException {
        final SeqTableModel tm = getSeqTableModel();

        out.append("SELECT ");
        printQuotedName(tm.getSequence(), out);
        out.append(", ");
        printQuotedName(tm.getCache(), out);
        out.append(", ");
        printQuotedName(tm.getMaxValue(), out);
        out.append(" FROM ");

        printSequenceTableName(sequence, out);
        out.append(" WHERE ");
        printQuotedName(tm.getId(), out);
        out.append("=?");

        return out;
    }

    /** Print SQL DELETE SEQUENCE BY ID. */
    public Appendable printSequenceDeleteById
        ( @NotNull final UjoSequencer sequence
        , @NotNull final String id
        , @NotNull final Appendable out) throws IOException {
        final SeqTableModel tm = getSeqTableModel();

        out.append("DELETE FROM ");
        printSequenceTableName(sequence, out);
        out.append(" WHERE ");
        printQuotedName(tm.getId(), out);
        out.append("=?");

        return out;
    }

    /** Returns true, if the argument text is not null and not empty. */
    @Deprecated
    protected final boolean isFilled(final CharSequence text) {
        return Check.hasLength(text);
    }

    /** Print the new line. */
    public final void println(final Appendable out) throws IOException {
        out.append('\n');
    }

    /** Print SQL 'COMMIT' */
    public Appendable printCommit(Appendable out) throws IOException {
        out.append("COMMIT");
        return out;
    }

    /** Print a Comment to a database Table */
    public Appendable printComment
        ( @NotNull final MetaTable table
        , @NotNull final Appendable out) throws IOException {
        out.append("COMMENT ON TABLE ");
        printFullTableName(table, out);
        out.append(" IS '");
        escape(MetaTable.COMMENT.of(table), out);
        out.append("'");
        return out;
    }

    /** Print a Comment to a database Column */
    public Appendable printComment
        ( @NotNull final MetaColumn column
        , @NotNull final Appendable out) throws IOException {
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
    public Set<String> getKeywordSet(@NotNull final Connection conn) {
        Set<String> result = new HashSet<String>(128);
        Reader reader = null;
        try {
            // Get keywords from a JDBC meta-data object:
            reader = new CharArrayReader(conn.getMetaData().getSQLKeywords().concat(",").toCharArray());
            assignKeywords(result, reader);

            // Get keywords from a text file:
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/org/ujorm/orm/sql-keywords.txt"), "UTF8"));
            assignKeywords(result, reader);

        } catch (RuntimeException | SQLException | IOException | OutOfMemoryError e) {
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
    private void assignKeywords(@NotNull final Set<String> result, @NotNull final Reader reader) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final char separator = ',';
        int c;
        while ((c = reader.read()) != -1) {
            if (c == separator) {
                final String keyword = builder.toString().trim().toUpperCase(Locale.ENGLISH);
                if (!keyword.startsWith("--")) {
                    result.add(keyword);
                }
                builder.setLength(0);
            } else {
                builder.append((char)c);
            }
        }
    }

    /** Escape the special character: "'" */
    protected final void escape(@NotNull final CharSequence text, @NotNull final Appendable out) throws IOException {
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
    public void releaseSavepoint
        ( @NotNull final Connection conn
        , @NotNull final Savepoint savepoint
        , final boolean afterRollback) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    /**
     * Prints database column using its model.
     * @param column Column model
     * @param sql Target SQL for printing new quoted name
     * @return SQL with printed quoted name
     */
    protected final Appendable printColumnName
        ( @NotNull final ColumnWrapper column
        , @NotNull final Appendable sql) throws IOException {
        return printQuotedName(column.getName(), column.getModel().get(MetaColumn.QUOTED), sql);
    }

    /**
     * Prints quoted name (identifier) to SQL according the parameter {@link MetaParams.QUOTE_SQL_NAMES}.
     * @param name Name (identifier) for quoting
     * @param quotingPolicy quoting Policy
     * @param sql Target SQL for printing new quoted name
     * @return SQL with printed quoted name
     */
    public Appendable printQuotedName
        ( @NotNull final CharSequence name
        , @NotNull final QuoteEnum quotingPolicy
        , @NotNull final Appendable sql) throws IOException {

        switch (quotingPolicy) {
            case BY_CONFIG:
                switch (getQuotingPolicy()) {
                    case QUOTE_SQL_NAMES:
                        printQuotedName(name, sql);
                        break;
                    case QUOTE_ONLY_SQL_KEYWORDS:
                        if (ormHandler.getParameters().get(MetaParams.KEYWORD_SET).contains(name.toString().toUpperCase(Locale.ENGLISH))) {
                           printQuotedName(name, sql);
                        } else {
                           sql.append(name);
                        }
                        break;
                    default:
                        sql.append(name);
                }
                break;
            case YES:
                printQuotedName(name, sql);
                break;
            case NO:
                sql.append(name);
                break;
            default:
                throw new IllegalStateException("Unsupported policy: " + quotingPolicy);
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
    public Appendable printQuotedName
        ( @NotNull final CharSequence name
        , @NotNull final Appendable sql) throws IOException {
        sql.append(getQuoteChar(true)); // quotation start character based on SQL dialect
        sql.append(name);
        sql.append(getQuoteChar(false)); // quotation end character based on SQL dialect
        return sql;
    }

    /**
     * Returns a quote character
     * @param first Value {@code true} means the FIRST character and value {@code false} means the LAST one.
     * @return
     */
    protected char getQuoteChar(final boolean first) {
        return QUOTE_CHARACTER;
    }

    /** Get quoted policy */
    @NotNull
    protected CheckReport getQuotingPolicy() {
        if (quotingPolicy == null) {
            quotingPolicy = ormHandler.getParameters().getQuotationPolicy();
        }
        return quotingPolicy;
    }

    /** Prints quoted name (identifier) to SQL - always. */
    public final String getQuotedName(@NotNull final CharSequence name) throws IOException {
        final StringBuilder result = new StringBuilder(name.length()+4);
        printQuotedName(name, result);
        return result.toString();
    }

    /**
     * Returns a name provider
     * @return Current SQL name provider
     * @throws IllegalStateException A problem during creating an instance.
     */
    public SqlNameProvider getNameProvider() throws IllegalUjormException {
        if (nameProvider==null) {
            try {
                nameProvider = MetaParams.SQL_NAME_PROVIDER.of(ormHandler.getParameters()).newInstance();
            } catch (RuntimeException | ReflectiveOperationException e) {
                throw new IllegalUjormException("Can't create an instance of the " + ormHandler.getParameters(), e);
            }
        }
        return nameProvider;
    }

    /** Create a SQL script for the NEXT SEQUENCE from a native database sequencer */
    public Appendable printNextSequence
        ( @NotNull final String sequenceName
        , @NotNull final MetaTable table
        , @NotNull final Appendable out) throws IOException {
        out.append("SELECT NEXTVAL('");
        out.append(sequenceName);
        out.append("')");
        return out;
    }
}
