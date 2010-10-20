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
package org.ujoframework.orm;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaTable;
import org.ujoframework.orm.metaModel.MetaSelect;
import org.ujoframework.criterion.ValueCriterion;
import org.ujoframework.criterion.Operator;
import org.ujoframework.orm.metaModel.MetaDatabase;
import org.ujoframework.orm.metaModel.MetaIndex;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaProcedure;

/**
 * SQL dialect abstract class. Methods of this class print a SQL statement(s) along a metamodel usually.
 * You may create a subclass of any implementation to create another SQL statement, however just I can't
 * exclude some small changes of this API in the next release.
 * @author Pavel Ponec
 */
@SuppressWarnings("unchecked")
abstract public class SqlDialect {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(SqlDialect.class.getName());

    /** The table key for a common sequence emulator. 
     * <br/>The SQL script for migration to the Ujorm 0.93:
     * <pre>ALTER TABLE ormujo_pk_support RENAME TO ujorm_pk_support;</pre>
     */
    public static final String COMMON_SEQ_TABLE_NAME = "ujorm_pk_support";
    /** The table key for a common sequence emulator. */
    public static final String COMMON_SEQ_TABLE_KEY = "<ALL>";

    /** The ORM handler */
    protected OrmHandler ormHandler;

    /** Set the OrmHandler - the method is for internal call only. */
    public void setHandler(OrmHandler ormHandler) {
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

    /** Print SQL 'CREATE SCHEMA' */
    public Appendable printCreateSchema(String schema, Appendable out) throws IOException {
        out.append("CREATE SCHEMA IF NOT EXISTS ");
        out.append(schema);
        return out;
    }

    /** Print SQL 'SET SCHEMA'. The method is not used yet. */
    @Deprecated
    public Appendable printDefaultSchema(String schema, Appendable out) throws IOException {
        out.append("SET SCHEMA ");
        out.append(schema);
        return out;
    }

    /** Print a full SQL table name by sample: SCHEMA.TABLE  */
    public Appendable printFullTableName(final MetaTable table, final Appendable out) throws IOException {
        final String tableSchema = MetaTable.SCHEMA.of(table);
        final String tableName = MetaTable.NAME.of(table);

        if (isUsable(tableSchema)) {
            out.append(tableSchema);
            out.append('.');
        }
        out.append(tableName);
        return out;
    }

    /** Print a SQL database and table name and an alias definition - by sample: SCHEMA.TABLE ALIAS */
    public void printTableAliasDefinition(final MetaTable table, final Appendable out) throws IOException {
        printFullTableName(table, out);
        out.append(' ');
        out.append(table.getAlias());
    }


    /** Print a full SQL column alias name by sample: TABLE_ALIAS.COLUMN */
    public Appendable printColumnAlias(final MetaColumn column, final Appendable out) throws IOException {
        final MetaTable table = MetaColumn.TABLE.of(column);

        out.append(table.getAlias());
        out.append('.');
        out.append(MetaColumn.NAME.of(column));
        
        return out;
    }

    /** Print a SQL sript to create table */
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

    /** Print a SQL sript to add a new column to the table */
    public Appendable printAlterTable(MetaColumn column, Appendable out) throws IOException {
        out.append("ALTER TABLE ");
        printFullTableName(column.getTable(), out);
        out.append(" ADD COLUMN ");

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
        boolean isDefault = value!=null;
        String quotMark = "";
        if (value instanceof String) {
            isDefault = ((String) value).length() > 0;
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



    /**
     * Print foreign key for the parameter column
     * @return More statements separated by the ';' charactes are enabled
     */
    public Appendable printForeignKey(MetaColumn column, MetaTable table, Appendable out) throws IOException {

        List<MetaColumn> fColumns = column.getForeignColumns();
        MetaTable foreignTable = fColumns.get(0).getTable();
        int columnsSize = fColumns.size();

        out.append("ALTER TABLE ");
        printFullTableName(table, out);
        out.append("\n\tADD FOREIGN KEY");

        for (int i=0; i<columnsSize; ++i) {
            out.append(i==0 ? "(" : ", ");
            final String name = column.getForeignColumnName(i);
            out.append(name);
        }

        out.append(")\n\tREFERENCES ");
        printFullTableName(foreignTable, out);
        String separator = "(";

        for (MetaColumn fColumn : fColumns) {
            out.append(separator);
            separator = ", ";
            out.append(MetaColumn.NAME.of(fColumn));
        }

        out.append(")");
        //out.append("\tON DELETE CASCADE");
        return out;
    }

    /**
     * Print an INDEX for the parameter column.
     * @return More statements separated by the ';' charactes are enabled
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
            out.append(MetaColumn.NAME.of(column));
            separator = ", ";
        }
        out.append(')');
        return out;
    }


    /**
     *  Print a SQL to create column
     * @param column Database Column
     * @param aName The name parameter is not mandatory, the not null value means a foreign key.
     * @throws java.io.IOException
     */
    public Appendable printColumnDeclaration(MetaColumn column, String aName, Appendable out) throws IOException {

        String name = aName!=null ? aName : MetaColumn.NAME.of(column);
        out.append(name);
        out.append(' ');
        out.append(getColumnType(column));

        if (!MetaColumn.MAX_LENGTH.isDefault(column)) {
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

        List<MetaColumn> columns = column.getForeignColumns();

        for (int i=0; i<columns.size(); ++i) {
            MetaColumn col = columns.get(i);
            out.append(i==0 ? "" : "\n\t, ");
            String name = column.getForeignColumnName(i);
            printColumnDeclaration(col, name, out);
            if (MetaColumn.MANDATORY.of(column)) {
                out.append(" NOT NULL");
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

        printTableColumns(MetaTable.COLUMNS.getList(table), values, out);

        out.append(") VALUES (")
           .append(values)
           .append(")")
           ;

        return out;
    }

    /** Print an SQL UPDATE statement.  */
    public Appendable printUpdate
        ( MetaTable table
        , List<MetaColumn> changedColumns
        , CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        out.append("UPDATE ");
        printTableAliasDefinition(table, out);
        out.append("\n\tSET ");

        for (int i=0; i<changedColumns.size(); i++) {
            MetaColumn ormColumn = changedColumns.get(i);
            if (ormColumn.isPrimaryKey()) {
                throw new IllegalStateException("Primary key can not be changed: " + ormColumn);
            }
            out.append(i==0 ? "" :  ", ");
            out.append(MetaColumn.NAME.of(ormColumn));
            out.append("=?");
        }
        out.append("\n\tWHERE ");
        out.append(decoder.getWhere());
        return out;
    }

    /** Print an SQL DELETE statement. */
    public Appendable printDelete
        ( MetaTable table
        , CriterionDecoder decoder
        , Appendable out
        ) throws IOException
    {
        out.append("DELETE FROM ");
        printTableAliasDefinition(table, out);
        out.append(" WHERE ");
        out.append(decoder.getWhere());

        return out;
    }

    /** Returns an SQL criterion template. The result is a tempate by the next sample: "{0}={1}" . 
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
                return crit.evaluate(null)
                    ? "1=1" // "true"
                    : "1=0" // "false"
                    ;
            case REGEXP: 
            case NOT_REGEXP:
            default:
                throw new UnsupportedOperationException("Unsupported: " + crit.getOperator());
        }
    }

    /**
     * Print table columns
     * @param columns List of tablel columns
     * @param values Print columns include alias.
     * @param out Table columns output.
     * @throws java.io.IOException
     */
    public void printTableColumns(List<MetaColumn> columns, Appendable values, Appendable out) throws IOException {
        String separator = "";
        boolean select = values==null; // SELECT
        for (MetaColumn column : columns) {
            if (column.isForeignKey()) {
                for (int i = 0; i < column.getForeignColumns().size(); ++i) {
                    out.append(separator);
                    if (select) {
                        out.append(MetaColumn.TABLE.of(column).getAlias());
                        out.append('.');
                    }
                    out.append(column.getForeignColumnName(i));
                    if (values != null) {
                        values.append(separator);
                        values.append("?");
                    }
                    separator = ", ";
                }
            } else if (column.isColumn()) {
                out.append(separator);
                if (select) {
                    printColumnAlias(column, out);
                } else {
                    out.append(MetaColumn.NAME.of(column));
                }
                if (values != null) {
                    values.append(separator);
                    values.append("?");
                }
                separator = ", ";
            }
        }
    }


    /** Print a conditon phrase by the criterion.
     * @return A value criterion to assign into the SQL query.
     */
    public ValueCriterion printCriterion(ValueCriterion crit, Appendable out) throws IOException {
        Operator operator = crit.getOperator();
        UjoProperty property = crit.getLeftNode();
        Object right = crit.getRightNode();

        MetaColumn column = (MetaColumn) ormHandler.findColumnModel(property);

        if (right==null ) {
            String columnName = MetaColumn.NAME.of(column);
            switch (operator) {
                case EQ:
                case EQUALS_CASE_INSENSITIVE:
                    out.append(columnName);
                    out.append(" IS NULL");
                    return null;
                case NOT_EQ:
                    out.append(columnName);
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

        if (crit.isConstant()) {
            out.append( template );
        } else if (right instanceof UjoProperty) {
            final UjoProperty rightProperty = (UjoProperty) right;
            final MetaColumn col2 = (MetaColumn) ormHandler.findColumnModel(rightProperty);

            if (!rightProperty.isDirect()) {
                throw new UnsupportedOperationException("Two tables is not supported yet");
            }
            if (col2.isForeignKey()) {
                throw new UnsupportedOperationException("Foreign key is not supported yet");
            }
            if (true) {
                String f = String.format(template, column.getAliasName(), col2.getAliasName());
                out.append(f);
            }
        } else if (column.isForeignKey()) {
           printForeignKey(crit, column, template, out);
           return crit;
        } else if (right instanceof Object[]) {
            StringBuilder sb = new StringBuilder(64);
            for (Object o : (Object[]) right) {
                sb.append(sb.length()>0 ? ",?" : "?");
            }
            String f = MessageFormat.format(template, column.getAliasName(), sb.toString());
            out.append(f);
            return crit;
        } else {
            String f = MessageFormat.format(template, column.getAliasName(), "?");
            out.append(f);
            return crit;
        }
        return null;
    }

    /** Print all items of the foreign key */
    public void printForeignKey
        ( final ValueCriterion crit
        , final MetaColumn column
        , final String template
        , final Appendable out
        ) throws IOException
    {
        int size = column.getForeignColumns().size();
        for (int i=0; i<size; i++) {
            if (i>0) {
                out.append(' ');
                out.append(crit.getOperator().name());
                out.append(' ');
            }
            
            String alias = MetaColumn.TABLE.of(column).getAlias();
            String columnName = column.getForeignColumnName(i);
            if (isUsable(alias)) {
                columnName = alias + '.' + columnName;
            }
            String f = MessageFormat.format(template, columnName, "?");
            out.append(f);
        }
    }

    /** Print a SQL SELECT by table model and query
     * @param query The UJO query
     * @param count only count of items is required;
     */
    public Appendable printSelect
        ( final MetaTable table
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
    protected Appendable printSelectView(MetaTable table, Query query, boolean count, Appendable out) throws IOException {
        MetaSelect select = MetaTable.SELECT_MODEL.of(table);
        String where = query.getDecoder().getWhere();
        List<UjoProperty> order = query.getOrderBy();

        for (UjoProperty p : select.readProperties()) {
            String value = (String) p.of(select);

            if (p==MetaSelect.SELECT && count) {
                out.append(p.toString());
                out.append( "COUNT(*)" );
            } else if (p==MetaSelect.WHERE && value.length()+where.length()>0) {
                out.append(p.toString());
                out.append( value );
                out.append( value.isEmpty() || where.isEmpty() ? "" : " AND " );
                out.append( where );
            } else if (p==MetaSelect.ORDER && !order.isEmpty()){
                out.append(p.toString());
                out.append( value );
                out.append( value.isEmpty() || order.isEmpty() ? "" : " AND " );
                printSelectOrder(query, out);
            } else if (value.length()>0) {
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
    protected Appendable printSelectTable(Query query, boolean count, Appendable out) throws IOException {
        out.append("SELECT ");
        if (count) {
            out.append("COUNT(*)");
        } else {
            printTableColumns(query.getColumns(), null, out);
        }
        out.append("\n\tFROM ");

        if (query.getCriterion() != null) {
            CriterionDecoder ed = query.getDecoder();

            MetaTable[] tables = ed.getTables(query.getTableModel());

            for (int i=0; i<tables.length; ++i) {
                MetaTable table = tables[i];
                if (i>0) out.append(", ");
                printTableAliasDefinition(table, out);
            }

            String sql = ed.getWhere();
            if (!sql.isEmpty()) {
                out.append(" WHERE ");
                out.append(ed.getWhere());
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
        return out;
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
    public void printSelectOrder(Query query, Appendable out) throws IOException {
        
        out.append(" ORDER BY ");
        final List<UjoProperty> props = query.getOrderBy();
        for (int i=0; i<props.size(); i++) {
            MetaColumn column = query.readOrderColumn(i);
            boolean ascending = props.get(i).isAscending();
            if (i>0) {
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
        int limit = query.getLimit()>0
            ? query.getLimit()
            : Integer.MAX_VALUE
            ;
        out.append(" LIMIT " + limit);
        out.append(" OFFSET " + query.getOffset());
    }

    /** Prinnt the full sequence table */
    protected Appendable printSequenceTableName(final UjoSequencer sequence, final Appendable out) throws IOException {
        String schema = sequence.getDatabaseSchema();
        if (isUsable(schema)) {
            out.append(schema);
            out.append('.');
        }
        out.append(COMMON_SEQ_TABLE_NAME);
        return out;
    }

    /** Print SQL CREATE SEQUENCE. No JDBC parameters. */
    public Appendable printSequenceTable(final MetaDatabase db, final Appendable out) throws IOException {
        String schema = MetaDatabase.SCHEMA.of(db);
        Integer cache = MetaParams.SEQUENCE_CACHE.of(db.getParams());

        out.append("CREATE TABLE ");
        if (isUsable(schema)) {
            out.append(schema);
            out.append('.');
        }

        MetaColumn pkType = new MetaColumn();
        MetaColumn.DB_TYPE.setValue(pkType, DbType.BIGINT);

        out.append(COMMON_SEQ_TABLE_NAME);
        out.append("\n\t( id VARCHAR(96) NOT NULL PRIMARY KEY");
        out.append("\n\t, seq "+getColumnType(pkType)+" DEFAULT " + cache + " NOT NULL");
        out.append("\n\t, cache INT DEFAULT " + cache + " NOT NULL");
        out.append("\n\t, maxvalue "+getColumnType(pkType)+" DEFAULT 0 NOT NULL");
        out.append("\n\t)");
        return out;
    }

    /** Print SQL CREATE SEQUENCE (insert sequence row). No JDBC parameters. */
    public Appendable printSequenceInit(final UjoSequencer sequence, final Appendable out) throws IOException {
        Integer cache = MetaParams.SEQUENCE_CACHE.of(sequence.getDatabase().getParams());
        out.append("INSERT INTO ");
        printSequenceTableName(sequence, out);
        out.append(" (id,seq,cache) VALUES (?,"+cache+","+cache+")");
        return out;
    }

    /** Print SQL UPDATE NEXT SEQUENCE value. */
    public Appendable printSequenceNextValue(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append("UPDATE ");
        printSequenceTableName(sequence, out);
        out.append(" SET seq=seq+cache");
        out.append(" WHERE id=?");
        return out;
    }

    /** Set sequence to the max value. */
    public Appendable printSetMaxSequence(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append("UPDATE ");
        printSequenceTableName(sequence, out);
        out.append(" SET seq=maxValue");
        out.append(" WHERE id=?");
        return out;
    }


    /** Print SQL CURRENT SEQUENCE VALUE. Returns a new sequence limit and the current cache. */
    public Appendable printSequenceCurrentValue(final UjoSequencer sequence, final Appendable out) throws IOException {
        out.append("SELECT seq, cache, maxValue FROM ");
        printSequenceTableName(sequence, out);
        out.append(" WHERE id=?");
        return out;
    }

    /** Returns true, if the argument text is not null and not empty. */
    protected boolean isUsable(final CharSequence text) {
        final boolean result = text!=null && text.length()>0;
        return result;
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

    /** Print a Comment on the table */
    public Appendable printComment(MetaTable table, Appendable out) throws IOException {
        out.append("COMMENT ON TABLE ");
        printFullTableName(table, out);
        out.append(" IS '");
        escape(MetaTable.COMMENT.of(table), out);
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
            reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/org/ujoframework/orm/sql-keywords.txt"), "UTF8"));
            assignKeywords(result, reader);

        } catch (Throwable e) {
            LOGGER.log(Level.WARNING, "Can't read SQL keywords", e);
        } finally {
            if (reader!=null) try {
                reader.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can't close reader", e);
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

}
