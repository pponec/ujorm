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
import org.ujoframework.extensions.PathProperty;
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
    abstract public void printCreateDatabase(OrmDatabase database, Appendable writer) throws IOException;

    /** Print a SQL sript to create table */
    abstract public void printTable(OrmTable table, Appendable result) throws IOException;

    /** Print a SQL to create column */
    abstract public void printColumnDeclaration(OrmColumn column, Appendable writer, String name) throws IOException;

    /** Print a SQL to create a Foreign Key. */
    abstract public void printFKColumnsDeclaration(OrmColumn column, Appendable writer) throws IOException;

    /** Print an INSERT SQL statement.  */
    abstract public void printInsert(TableUjo ujo, Appendable writer) throws IOException;

    /** Returns an SQL expression template. */
    public String getExpressionTemplate(Operator operator) {

        switch (operator) {
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
            case REGEXP: 
            case NOT_REGEXP:
            default:
                throw new UnsupportedOperationException("Unsupported: " + operator);
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


    /** Print expression.
     * @return A value expression to assign into the SQL query.
     */
    public ExpressionValue print(ExpressionValue expr, Appendable writer) throws IOException {
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

        String template = getExpressionTemplate(operator);
        if (template == null) {
            throw new UnsupportedOperationException("Unsupported SQL operator: " + operator);
        }

        if (right instanceof UjoProperty) {
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
           printiForeignKey(expr, column, template, writer);
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
    public void printiForeignKey
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
    public ExpressionDecoder printSelect(Query query, Appendable writer) throws IOException {
        ExpressionDecoder result = null;
        writer.append("SELECT ");
        printTableColumns(query.getColumns(), writer, null);
        writer.append("\n\tFROM ");
        writer.append(OrmTable.NAME.of(query.getTableModel()));

        Expression e = query.getExpression();
        if (e != null) {
            ExpressionDecoder ed = new ExpressionDecoder(e, this);
            String sql = ed.getSql();
            if (!sql.isEmpty()) {
                writer.append(" WHERE ");
                writer.append(ed.getSql());
                result = ed;
            }
        }
        writer.append(";");
        return result;
    }

}
