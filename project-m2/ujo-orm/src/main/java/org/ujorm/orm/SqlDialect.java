/*
 *  Copyright 2009-2016 Pavel Ponec
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaIndex;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaProcedure;
import org.ujorm.orm.metaModel.MetaSelect;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.orm.metaModel.MoreParams;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
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
    public static final String COMMON_SEQ_TABLE_KEY = "<ALL>";
    /** The default schema symbol */
    public static final String DEFAULT_SCHEMA_SYMBOL = "~";
    /** The new line separator for SQL statements */
    public static final String NEW_LINE_SEPARATOR = "\n\t";

    /** The ORM handler */
    protected OrmHandler ormHandler;
    /** The name provider */
    private SqlNameProvider nameProvider;
    /** Extended dialect */
    private SqlDialectEx extentedDialect;

    /** Prints quoted name (identifier) to SQL */
    private Boolean quoteRequest;

    /** An INNER JOIN syntax request */
    private Boolean _innerJoin;

    /** Inner join */
    public boolean isInnerJoin() {
        if (_innerJoin == null) {
            _innerJoin = MetaParams.MORE_PARAMS.add(MoreParams.JOIN_PHRASE).of(ormHandler.getParameters());
        }
        return _innerJoin;
    }

    /** Set the OrmHandler - the method is for internal call only. */
    public void setHandler(@Nonnull final OrmHandler ormHandler) {
        Assert.isNull(this.ormHandler, "The OrmHandler is assigned yet.");
        this.ormHandler = ormHandler;
    }

    /** Returns a default JDBC URL */
    abstract public String getJdbcUrl();

    /** Returns a JDBC driver class name. */
    abstract public String getJdbcDriver();

    /** Create a new database connection */
    public Connection createConnection(@Nonnull final MetaDatabase db) throws Exception {
        return db.createInternalConnection();
    }

    /** Get or create an Initial Context for the JNDI lookup. */
    public InitialContext createJndiInitialContext(@Nonnull final MetaDatabase db) throws NamingException {
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
    public Appendable printCreateSchema(String schema, @Nonnull final Appendable out) throws IOException {
        out.append("CREATE SCHEMA IF NOT EXISTS ");
        printQuotedName(schema, out);
        return out;
    }

    /** Print SQL 'SET SCHEMA'. The method is not used yet. */
    @Deprecated
    public Appendable printDefaultSchema(String schema, @Nonnull final Appendable out) throws IOException {
        out.append("SET SCHEMA ");
        printQuotedName(schema, out);
        return out;
    }

    /** Print a full SQL table name by sample: SCHEMA.TABLE  */
    public final Appendable printFullTableName(final MetaTable table, @Nonnull final Appendable out) throws IOException {
        return printFullTableName(table, false, out);
    }

    /** Print a extended SQL table name by sample: SCHEMA.TABLE
     * @param printSymbolSchema True parameter replaces a <strong>default schema</strong> name for the symbol "~" by the example: ~.TABLE
     * @throws IOException
     */
    public Appendable printFullTableName(final MetaTable table, final boolean printSymbolSchema, @Nonnull final Appendable out) throws IOException {
        final String tableSchema = MetaTable.SCHEMA.of(table);
        final String tableName = MetaTable.NAME.of(table);

        if (hasLength(tableSchema)) {
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
    public void printTableAliasDefinition(final TableWrapper table, @Nonnull final Appendable out) throws IOException {
        printFullTableName(table.getModel(), out);
        final String alias = table.getAlias();
        if (hasLength(alias)) {
            out.append(SPACE);
            printQuotedName(alias, out);
        }
    }

    /** Print a full SQL column alias name by sample: "TABLE_ALIAS"."ORIG_COLUMN" */
    public Appendable printColumnAlias(final ColumnWrapper column, @Nonnull final Appendable out) throws IOException {
        printQuotedName(column.getTableAlias(), out);
        out.append('.');
        printQuotedName(column.getName(), out);
        return out;
    }

    /** Print a SQL script to create table */
    public Appendable printTable(MetaTable table, @Nonnull final Appendable out) throws IOException {
        out.append("CREATE TABLE ");
        printFullTableName(table, out);
        String separator = NEW_LINE_SEPARATOR.concat("( ");
        for (MetaColumn column : MetaTable.COLUMNS.getList(table)) {
            out.append(separator);
            separator = NEW_LINE_SEPARATOR + ", ";

            if (column.isForeignKey()) {
                printFKColumnsDeclaration(column, out);
            } else {
                printColumnDeclaration(column, null, out);
            }
        }
        out.append(NEW_LINE_SEPARATOR).append(')');
        return out;
    }

    /** Print a SQL script to add a new column to the table */
    public Appendable printAlterTableAddColumn(MetaColumn column, @Nonnull final Appendable out) throws IOException {
        return printAlterTableColumn(column, true, out);
    }

    /** Print a SQL script to add a new column to the table */
    public Appendable printAlterTableColumn(MetaColumn column, boolean add, @Nonnull final Appendable out) throws IOException {
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
    public Appendable printDefaultValue(final MetaColumn column, @Nonnull final Appendable out) throws IOException {
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
    public Appendable printForeignKey(MetaColumn column, @Nonnull final Appendable out) throws IOException {

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
            final String name = column.getForeignColumnName(i);
            printQuotedName(name, out);
        }

        out.append(')').append(NEW_LINE_SEPARATOR).append("REFERENCES ");
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
    public Appendable printIndex(final MetaIndex index, @Nonnull final Appendable out) throws IOException {

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
     * @param column Database Column
     * @param aName The name parameter is not mandatory, the not null value means a foreign key.
     * @throws java.io.IOException
     */
    public Appendable printColumnDeclaration
        ( @Nonnull final MetaColumn column
        , @Nonnull final String aName
        , @Nonnull final Appendable out) throws IOException {

        String name = aName!=null ? aName : column.getName();
        printQuotedName(name, out);
        out.append(SPACE);
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
        ( @Nonnull final MetaColumn column
        , @Nonnull final Appendable out) throws IOException {

        final List<MetaColumn> columns = column.getForeignColumns();

        for (int i=0; i<columns.size(); ++i) {
            final MetaColumn col = columns.get(i);
            final String name = column.getForeignColumnName(i);
            if (i > 0) {
                out.append(COMMON_SEQ_TABLE_KEY).append(", ");
            }
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
    public Appendable printInsert
            ( @Nonnull final OrmUjo bo
            , @Nonnull final Appendable out) throws IOException {

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
    public Appendable printInsert(final List<? extends OrmUjo> bos, final int idxFrom, final int idxTo, @Nonnull final Appendable out) throws IOException {

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
    public Appendable printInsertBySelect
            ( @Nonnull final List<? extends OrmUjo> bos
            , final int idxFrom
            , final int idxTo
            , @Nonnull final String fromPhrase
            , @Nonnull final Appendable out) throws IOException {

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
        ( @Nonnull final List<MetaColumn> changedColumns
        , @Nonnull final CriterionDecoder decoder
        , @Nonnull final Appendable out
        ) throws IOException
    {
        final MetaTable table = decoder.getBaseTable();
        out.append("UPDATE ");
        printTableAliasDefinition(table, out);
        out.append(NEW_LINE_SEPARATOR).append("SET ");

        for (int i=0; i<changedColumns.size(); i++) {
            MetaColumn ormColumn = changedColumns.get(i);
            if (ormColumn.isPrimaryKey()) {
                throw new IllegalUjormException("Primary key can not be changed: " + ormColumn);
            }
            out.append(i==0 ? "" :  ", ");
            printQuotedName(ormColumn.getName(), out);
            out.append("=?");
        }
        out.append(NEW_LINE_SEPARATOR).append("WHERE ");

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
        ( @Nonnull final CriterionDecoder decoder
        , @Nonnull final Appendable out
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
    protected Query createSubQuery(@Nonnull final CriterionDecoder decoder) {
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
    @SuppressWarnings("unchecked")
    @Nonnull
    public String getCriterionTemplate(@Nonnull final ValueCriterion crit) {

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
        ( @Nonnull final Collection<? extends ColumnWrapper> columns
        , @Nonnull final Appendable values
        , @Nonnull final Appendable out) throws IOException {
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


    /** Print a <strong>value condition phrase</strong> from the criterion.
     * @return A nullable value criterion to assign into the SQL query.
     */
    @Nullable
    public ValueCriterion printCriterion(@Nonnull final ValueCriterion crn, @Nonnull final Appendable out) throws IOException {
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
                    throw new UnsupportedOperationException("Comparation the NULL value is forbiden by the operator: " + operator);
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
        ( @Nonnull final String template
        , @Nonnull final ColumnWrapper column
        , @Nonnull final ValueCriterion crit
        , @Nonnull final Appendable out) throws IOException {
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
        printQuotedName(column.getTableAlias(), out);
        out.append('.');
        printQuotedName(column.getName(), out);
        return out.toString();
    }

    /** Print all items of the foreign key */
    public void printForeignKey
        ( @Nonnull final ValueCriterion crit
        , @Nonnull final ColumnWrapper column
        , @Nonnull final String template
        , @Nonnull final Appendable out
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
        ( @Nonnull final TableWrapper table
        , @Nonnull final Query query
        , final boolean count
        , @Nonnull final Appendable out
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
        ( @Nonnull final TableWrapper table
        , @Nonnull final Query query
        , final boolean count
        , @Nonnull final Appendable out) throws IOException {
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
        ( @Nonnull final  Query query
        , final boolean count
        , @Nonnull final Appendable out) throws IOException {
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
        ( final @Nonnull Query query
        , final boolean count
        , @Nonnull final Appendable out) throws IOException {
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

            if (isInnerJoin()) {
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
            } else {
                final TableWrapper[] tables = ed.getTables();
                for (int i=0; i<tables.length; ++i) {
                    if (i>0) {
                        out.append(", ");
                    }
                    printTableAliasDefinition(tables[i], out);
                }
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
            if (query.isOffset()) {
                printOffset(query, out);
            }
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
        ( @Nonnull final Query query
        , @Nonnull final Appendable out) throws IOException, UnsupportedOperationException {
        out.append("FOR UPDATE");
        return out;
    }

    /** Print SQL ORDER BY */
    protected void printSelectOrder
        ( @Nonnull final Query query
        , @Nonnull final Appendable out) throws IOException {

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
        ( @Nonnull final MetaProcedure procedure
        , @Nonnull final Appendable out) throws IOException {

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

    /** Print an OFFSET of the statement SELECT. */
    public void printOffset
        ( @Nonnull final Query query
        , @Nonnull final Appendable out) throws IOException {
        int limit = query.isLimit()
            ? query.getLimit()
            : Integer.MAX_VALUE
            ;
        out.append(" LIMIT " + limit);
        out.append(" OFFSET " + query.getOffset());
    }

    /** Print the full sequence table */
    protected Appendable printSequenceTableName
        ( final @Nonnull UjoSequencer sequence
        , @Nonnull final Appendable out) throws IOException {
        String schema = sequence.getDatabaseSchema();
        if (hasLength(schema)) {
            printQuotedName(schema, out);
            out.append('.');
        }
        printQuotedName(getSeqTableModel().getTableName(), out);
        return out;
    }

    /** Print SQL CREATE SEQUENCE. No JDBC parameters. */
    public Appendable printSequenceTable
        ( @Nonnull final MetaDatabase db
        , @Nonnull final Appendable out) throws IOException {
        String schema = MetaDatabase.SCHEMA.of(db);
        Integer cache = MetaParams.SEQUENCE_CACHE.of(db.getParams());

        out.append("CREATE TABLE ");
        if (hasLength(schema)) {
            printQuotedName(schema, out);
            out.append('.');
        }

        final MetaColumn pkType = new MetaColumn();
        MetaColumn.DB_TYPE.setValue(pkType, DbType.BIGINT);

        printQuotedName(getSeqTableModel().getTableName(), out);
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
        ( @Nonnull final UjoSequencer sequence
        , @Nonnull final Appendable out) throws IOException {
        final Integer cache = MetaParams.SEQUENCE_CACHE.of(sequence.getDatabase().getParams());
        return printSequenceInitWithValues(sequence, cache, cache, out);
    }

    public Appendable printSequenceInitWithValues
        ( @Nonnull final UjoSequencer sequence
        , final long seq
        , final int cache
        , @Nonnull final Appendable out) throws IOException {
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
    public Appendable printSequenceNextValue
        ( @Nonnull final UjoSequencer sequence
        , @Nonnull final Appendable out) throws IOException {
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
    public Appendable printSetMaxSequence
        ( @Nonnull final UjoSequencer sequence
        , @Nonnull final Appendable out) throws IOException {
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
    public Appendable printSequenceCurrentValue
        ( @Nonnull final UjoSequencer sequence
        , @Nonnull final Appendable out) throws IOException {
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
    public Appendable printSequenceDeleteById
        ( @Nonnull final UjoSequencer sequence
        , @Nonnull final String id
        , @Nonnull final Appendable out) throws IOException {
        final SeqTableModel tm = getSeqTableModel();

        out.append("DELETE FROM ");
        printSequenceTableName(sequence, out);
        out.append(" WHERE ");
        printQuotedNameAlways(tm.getId(), out);
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
        ( @Nonnull final MetaTable table
        , @Nonnull final Appendable out) throws IOException {
        out.append("COMMENT ON TABLE ");
        printFullTableName(table, out);
        out.append(" IS '");
        escape(MetaTable.COMMENT.of(table), out);
        out.append("'");
        return out;
    }

    /** Print a Comment to a database Column */
    public Appendable printComment
        ( @Nonnull final MetaColumn column
        , @Nonnull final Appendable out) throws IOException {
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
    public Set<String> getKeywordSet(@Nonnull final Connection conn) {
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
    private void assignKeywords(@Nonnull final Set<String> result, @Nonnull final Reader reader) throws IOException {
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
    protected final void escape(@Nonnull final CharSequence text, @Nonnull final Appendable out) throws IOException {
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
        ( @Nonnull final Connection conn
        , @Nonnull final Savepoint savepoint
        , final boolean afterRollback) throws SQLException {
        conn.releaseSavepoint(savepoint);
    }

    /**
     * Prints quoted name (identifier) to SQL according the parameter {@link MetaParams.QUOTE_SQL_NAMES}.
     * @param name Name (identifier) for quoting
     * @param sql Target SQL for printing new quoted name
     * @return SQL with printed quoted name
     */
    public final Appendable printQuotedName
        ( @Nonnull final CharSequence name
        , @Nonnull final Appendable sql) throws IOException {
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
    protected Appendable printQuotedNameAlways
        ( @Nonnull final CharSequence name
        , @Nonnull final Appendable sql) throws IOException {
        sql.append('"'); // quotation start character based on SQL dialect
        sql.append(name);
        sql.append('"'); // quotation end character based on SQL dialect
        return sql;
    }

    /** Prints quoted name (identifier) to SQL - always. */
    protected final String getQuotedName(@Nonnull final CharSequence name) throws IOException {
        final StringBuilder result = new StringBuilder(name.length()+4);
        printQuotedNameAlways(name, result);
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

    /** Get an Extended dialect */
    public SqlDialectEx getExtentedDialect() {
        if (extentedDialect == null) {
            extentedDialect = new SqlDialectEx(this);
        }
        return extentedDialect;
    }

    /** Create a SQL script for the NEXT SEQUENCE from a native database sequencer */
    public Appendable printNextSequence
        ( @Nonnull final String sequenceName
        , @Nonnull final MetaTable table
        , @Nonnull final Appendable out) throws IOException {
        out.append("SELECT NEXTVAL('");
        out.append(sequenceName);
        out.append("')");
        return out;
    }
}
