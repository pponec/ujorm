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
package org.ujoframework.orm;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;
import org.ujoframework.tools.criteria.Expression;
import org.ujoframework.tools.criteria.ExpressionValue;
import org.ujoframework.tools.criteria.Operator;

/**
 * SQL renderer API
 * @author Ponec
 */
@SuppressWarnings("unchecked")
abstract public class SqlRenderer {

    /** Returns a default JDBC Driver */
    abstract public String getJdbcUrl();

    /** Returns a JDBC Driver */
    abstract public String getJdbcDriver();

    /** Print a SQL script to crate database */
    public void printCreateDatabase(OrmDatabase database, Appendable writer) throws IOException {
        for (OrmTable table : OrmDatabase.TABLES.getList(database)) {
            if (table.isPersistent()) {
                printTable(table, writer);
                printForeignKey(table, writer);
            }
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

            if (column.isForeignKey()) {
                printFKColumnsDeclaration(column, writer);
            } else {
                printColumnDeclaration(column, writer, null);
            }
        }
        writer.append("\n\t);\n");
    }

    /** Print foreign key */
    public void printForeignKey(OrmTable table, Appendable writer) throws IOException {
        for (OrmColumn column : OrmTable.COLUMNS.getList(table)) {
            if (column.isForeignKey()) {
                printForeignKey(column, table, writer);
            }
        }
    }

    /** Print foreign key for  */
    public void printForeignKey(OrmColumn column, OrmTable table, Appendable writer) throws IOException {
        final UjoProperty property = column.getProperty();
        final OrmTable foreignTable = OrmHandler.getInstance().findTableModel(property.getType());
        OrmPKey foreignKeys = OrmTable.PK.of(foreignTable);

        writer.append("ALTER TABLE ");
        writer.append(OrmTable.NAME.of(table));
        writer.append("\n\tADD FOREIGN KEY");

        String separator = "(";
        List<OrmColumn> columns = OrmPKey.COLUMNS.of(foreignKeys);
        int columnsSize = columns.size();

        for (int i = 0; i < columnsSize; ++i) {
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
    public void printColumnDeclaration(OrmColumn column, Appendable writer, String name) throws IOException {

        if (name == null) {
            name = OrmColumn.NAME.of(column);
        }

        writer.append(name);
        writer.append(' ');
        writer.append(OrmColumn.DB_TYPE.of(column).name());

        if (!OrmColumn.MAX_LENGTH.isDefault(column)) {
            writer.append("(" + OrmColumn.MAX_LENGTH.of(column));
            if (!OrmColumn.PRECISION.isDefault(column)) {
                writer.append(", " + OrmColumn.PRECISION.of(column));
            }
            writer.append(")");
        }
        if (!OrmColumn.MANDATORY.isDefault(column)) {
            writer.append(" NOT NULL");
        }
        if (OrmColumn.PRIMARY_KEY.of(column) && name == null) {
            writer.append(" PRIMARY KEY");
        }
    }

    /** Print a SQL to create foreign keys. */
    public void printFKColumnsDeclaration(OrmColumn column, Appendable writer) throws IOException {

        List<OrmColumn> columns = column.getForeignColumns();
        String separator = "";

        for (int i = 0; i < columns.size(); ++i) {
            OrmColumn col = columns.get(i);
            writer.append(separator);
            separator = "\n\t, ";
            String name = column.getForeignColumnName(i);
            printColumnDeclaration(col, writer, name);
        }
    }

    /** Print an SQL INSERT statement.  */
    public void printInsert(TableUjo ujo, Appendable writer) throws IOException {

        OrmTable table = OrmHandler.getInstance().findTableModel((Class) ujo.getClass());
        StringBuilder values = new StringBuilder();

        writer.append("INSERT INTO ");
        writer.append(table.getFullName());
        writer.append(" (");

        printTableColumns(OrmTable.COLUMNS.getList(table), writer, values);

        writer.append(") VALUES (");
        writer.append(values);
        writer.append(");");
    }

    /** Print an SQL UPDATE statement.  */
    public void printUpdate
        ( OrmTable table
        , List<OrmColumn> changedColumns
        , ExpressionDecoder decoder
        , Appendable writer
        ) throws IOException
    {

        writer.append("UPDATE ");
        writer.append(table.getFullName());
        writer.append("\n\tSET ");

        for (int i=0; i<changedColumns.size(); i++) {
            OrmColumn ormColumn = changedColumns.get(i);
            if (ormColumn.isPrimaryKey()) {
                throw new IllegalStateException("Primary key can not be changed: " + ormColumn);
            }
            if (i>0) {
                writer.append(", ");
            }
            writer.append(ormColumn.getFullName());
            writer.append("=? ");
        }
        writer.append("\n\tWHERE ");
        writer.append(decoder.getSql());
        writer.append(";");
    }

    /** Print an SQL DELETE statement.  */
    public void printDelete
        ( OrmTable table
        , ExpressionDecoder decoder
        , Appendable writer
        ) throws IOException
    {
        writer.append("DELETE FROM ");
        writer.append(table.getFullName());
        writer.append(" WHERE ");
        writer.append(decoder.getSql());
        writer.append(";");
    }

    /** Returns an SQL expression template. */
    public String getExpressionTemplate(ExpressionValue expr) {

        switch (expr.getOperator()) {
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
            case €_FIXED:
                return expr.evaluate(null)
                    ? "1=1"
                    : "1=0"
                    ;
            case REGEXP: 
            case NOT_REGEXP:
            default:
                throw new UnsupportedOperationException("Unsupported: " + expr.getOperator());
        }
    }

    /** Print table columns */
    public void printTableColumns(List<OrmColumn> columns, Appendable writer, Appendable values) throws IOException {
        String separator = "";
        for (OrmColumn column : columns) {
            if (column.isForeignKey()) {
                for (int i = 0; i < column.getForeignColumns().size(); ++i) {
                    writer.append(separator);
                    writer.append(column.getForeignColumnName(i));
                    if (values != null) {
                        values.append(separator);
                        values.append("?");
                    }
                    separator = ", ";
                }
            } else if (column.isColumn()) {
                writer.append(separator);
                writer.append(OrmColumn.NAME.of(column));
                if (values != null) {
                    values.append(separator);
                    values.append("?");
                }
                separator = ", ";
            }
        }
    }


    /** Print a conditon phrase by the expression.
     * @return A value expression to assign into the SQL query.
     */
    public ExpressionValue printCondition(ExpressionValue expr, Appendable writer) throws IOException {
        Operator operator = expr.getOperator();
        UjoProperty property = expr.getLeftNode();
        Object right = expr.getRightNode();

        OrmColumn column = (OrmColumn) OrmHandler.getInstance().findColumnModel(property);

        if (right==null ) {
            String columnName = OrmColumn.NAME.of(column);
            switch (operator) {
                case EQ:
                case EQUALS_CASE_INSENSITIVE:
                    writer.append(columnName);
                    writer.append(" IS NULL");
                    return null;
                case NOT_EQ:
                    writer.append(columnName);
                    writer.append(" IS NOT NULL");
                    return null;
                default:
                    throw new UnsupportedOperationException("Comparation the NULL value is forbiden by a operator: " + operator);
            }
        }

        String template = getExpressionTemplate(expr);
        if (template == null) {
            throw new UnsupportedOperationException("Unsupported SQL operator: " + operator);
        }

        if (expr.isConstant()) {
            writer.append( template );
        } else if (right instanceof UjoProperty) {
            final UjoProperty rightProperty = (UjoProperty) right;
            final OrmColumn col2 = (OrmColumn) OrmHandler.getInstance().findColumnModel(rightProperty);

            if (!rightProperty.isDirect()) {
                throw new UnsupportedOperationException("Two tables is not supported yet");
            }
            if (col2.isForeignKey()) {
                throw new UnsupportedOperationException("Foreign key is not supported yet");
            }
            if (true) {
                String f = String.format(template, column.getFullName(), col2.getFullName());
                writer.append(f);
            }
        } else if (column.isForeignKey()) {
           printForeignKey(expr, column, template, writer);
           return expr;
        } else if (right instanceof List) {
            throw new UnsupportedOperationException("List is not supported yet: " + operator);
        } else {
            String f = MessageFormat.format(template, column.getFullName(), "?");
            writer.append(f);
            return expr;
        }
        return null;
    }

    /** Print all items of the foreign key */
    public void printForeignKey
        ( final ExpressionValue expr
        , final OrmColumn column
        , final String template
        , final Appendable writer
        ) throws IOException
    {
        int size = column.getForeignColumns().size();
        for (int i=0; i<size; i++) {
            if (i>0) {
                writer.append(' ');
                writer.append(expr.getOperator().name());
                writer.append(' ');
            }

            String f = MessageFormat.format(template, column.getForeignColumnName(i), "?");
            writer.append(f);
        }
    }

    /** Print SQL SELECT */
    public ExpressionDecoder printSelect(Query query, Appendable writer, boolean count) throws IOException {
        ExpressionDecoder result = null;
        writer.append("SELECT ");
        if (count) {
            writer.append("COUNT(*)");
        } else {
            printTableColumns(query.getColumns(), writer, null);
        }
        writer.append("\n\tFROM ");
        writer.append(OrmTable.NAME.of(query.getTableModel()));

        Expression e = query.getExpression();
        if (e != null) {
            ExpressionDecoder ed = new ExpressionDecoder(e, this);
            String sql = ed.getSql();
            if (!sql.isEmpty()) {
                writer.append(" WHERE ");
                writer.append(ed.getSql());
            }
            result = ed;
        }
        writer.append(";");
        return result;
    }

}
