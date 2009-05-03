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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;
import org.ujoframework.tools.criteria.Expression;
import org.ujoframework.tools.criteria.ExpressionBinary;
import org.ujoframework.tools.criteria.ExpressionValue;
import org.ujoframework.tools.criteria.Operator;

/**
 * SQL Expression Decoder.
 * @author Pavel Ponec
 * @composed 1 - 1 SqlRenderer
 */
public class ExpressionDecoder {

    final private OrmHandler handler;
    final private SqlRenderer renderer;

    final private Expression e;
    final private StringBuilder sql;
    final private List<ExpressionValue> values;
    final private Set<OrmTable> tables;

    public ExpressionDecoder(Expression e, OrmTable ormTable) {
        this(e, ormTable.getDatabase());
    }

    public ExpressionDecoder(Expression expr, OrmDatabase database) {
        this.e = expr;
        this.renderer = database.getRenderer();
        this.handler  = database.getOrmHandler();
        this.sql = new StringBuilder(64);
        this.values = new ArrayList<ExpressionValue>();
        this.tables = new HashSet<OrmTable>();

        if (expr!=null) {
            unpack(expr);
            writeRelations();
        }
    }

    /** Unpack expression. */
    protected void unpack(final Expression e) {
        if (e.isBinary()) {
            unpackBinary((ExpressionBinary)e);
        } else try {
            ExpressionValue value = renderer.printCondition((ExpressionValue) e, sql);
            if (value!=null) {
                values.add(value);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Unpack expression. */
    private void unpackBinary(final ExpressionBinary eb) {

        boolean or = false;
        switch (eb.getOperator()) {
            case OR:
                or = true;
            case AND:
                if (or) sql.append(" (");
                unpack(eb.getLeftNode());
                sql.append(" ");
                sql.append(eb.getOperator().name());
                sql.append(" ");
                unpack(eb.getRightNode());
                if (or) sql.append(") ");
                break;
            default:
                String message = "Opearator is not supported in SQL statement: " + eb.getOperator();
                throw new UnsupportedOperationException(message);
        }
    }

    /** Returns a column count */
    public int getColumnCount() {
        return values.size();
    }

    /** Returns direct column */
    public OrmColumn getColumn(int i) {
        UjoProperty p = values.get(i).getLeftNode();
        OrmColumn ormColumn = (OrmColumn) handler.findColumnModel(p);
        return ormColumn;
    }

    /** Returns operator. */
    public Operator getOperator(int i) {
        final Operator result = values.get(i).getOperator();
        return result;
    }

    /** Returns value */
    public Object getValue(int i) {
        Object result = values.get(i).getRightNode();
        return result;
    }

    /** Returns an extended value to the SQL statement */
    public Object getValueExtended(int i) {
        ExpressionValue expr = values.get(i);
        Object value = expr.getRightNode();

        if (value==null) {
            return value;
        }
        if (expr.isInsensitive()) {
            value = value.toString().toUpperCase();
        }
        switch (expr.getOperator()) {
            case CONTAINS:
            case CONTAINS_CASE_INSENSITIVE:
                return "%"+value+"%";
            case STARTS:
            case STARTS_CASE_INSENSITIVE:
                return value+"%";
            case ENDS:
            case ENDS_CASE_INSENSITIVE:
                return "%"+value;
            default:
                return value;
        }
    }

    public Expression getExpression() {
        return e;
    }

    /** Returns a SQL WHERE 'expression' of an empty string if no conditon is found. */
    public String getWhere() {
        return sql.toString();
    }

    /** Is the SQL statement empty?  */
    public boolean isEmpty() {
        return sql.length()==0;
    }

    /** Returns the first direct property. */
    public UjoProperty getBaseProperty() {
        UjoProperty result = null;
        for (ExpressionValue eval : values) {
            if (eval.getLeftNode()!=null) {
                result = eval.getLeftNode();
                break;
            }
        }
        while(result!=null
        &&   !result.isDirect()
        ){
            result = ((PathProperty)result).getProperty(0);
        }
        return result;
    }

    /** Writer a relation conditions: */
    @SuppressWarnings("unchecked")
    protected void writeRelations() {
        UjoProperty[] relations = getPropertyRelations();

        boolean parenthesis = sql.length()>0 && relations.length>0;
        if (parenthesis) {
            sql.append(" AND (");
        }

        boolean andOperator = false;
        for (UjoProperty property : relations) try {
            OrmColumn fk1 = (OrmColumn) handler.findColumnModel(property);
            OrmTable   t2 = handler.findTableModel(property.getType());
            OrmPKey   pk2 = OrmTable.PK.of(t2);
            //
            tables.add(OrmColumn.TABLE.of(fk1));
            tables.add(t2);

            for (int i=fk1.getForeignColumns().size()-1; i>=0; i--) {

                if (andOperator) {
                    sql.append(" AND ");
                } else {
                    andOperator=true;
                }

                fk1.printForeignColumnFullName(i, sql);
                sql.append(" = ");
                renderer.printFullName(pk2.getColumn(i), sql);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (parenthesis) {
            sql.append(")");
        }
    }

    /** Returns the unique direct property relations. */
    @SuppressWarnings("unchecked")
    protected UjoProperty[] getPropertyRelations() {
        Set<UjoProperty> result = new HashSet<UjoProperty>();
        ArrayList<UjoProperty> dirs = new ArrayList<UjoProperty>();

        for (ExpressionValue value : values) {
            UjoProperty p1 = value.getLeftNode();
            Object      p2 = value.getRightNode();

            if (!p1.isDirect()) {
                ((PathProperty) p1).addDirectProperties(dirs);
                dirs.remove(dirs.size()-1); // remove the last direct property
            }
            if (p2 instanceof PathProperty) {
                ((PathProperty) p2).addDirectProperties(dirs);
                dirs.remove(dirs.size()-1); // remove the last direct property
            }
        }

        result.addAll(dirs);
        return result.toArray(new UjoProperty[result.size()]);
    }

    /** Returns all participated tables include the parameter table. */
    public OrmTable[] getTables(OrmTable baseTable) {
        tables.add(baseTable);
        return tables.toArray(new OrmTable[tables.size()]);
    }

}
