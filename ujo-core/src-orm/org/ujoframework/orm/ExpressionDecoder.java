/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.tools.criteria.Expression;
import org.ujoframework.tools.criteria.ExpressionBinary;
import org.ujoframework.tools.criteria.ExpressionValue;
import org.ujoframework.tools.criteria.Operator;

/**
 * SQL Expression Decoder.
 * @author pavel
 */
public class ExpressionDecoder {

    final private OrmHandler handler = OrmHandler.getInstance();
    final private SqlRenderer renderer;

    final private Expression e;
    final private StringBuilder sql;
    final private List<ExpressionValue> values;

    public ExpressionDecoder(Expression e, SqlRenderer renderer) {
        this.e = e;
        this.renderer = renderer;
        this.sql = new StringBuilder();
        this.values = new ArrayList<ExpressionValue>();

        if (e!=null) {
            unpack(e);
        }
    }

    /** Unpack expression. */
    protected void unpack(final Expression e) {
        if (e.isBinary()) {
            unpackBinary((ExpressionBinary)e);
        } else try {
            ExpressionValue value = renderer.print((ExpressionValue) e, sql);
            values.add(value);
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
                unpack(eb.getRightNote());
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

    /** Returns column */
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
        Object result = values.get(i).getRightNote();
        return result;
    }

    /** Returns an extended value to the SQL statement */
    public Object getValueExtended(int i) {
        ExpressionValue expr = values.get(i);
        Object value = expr.getRightNote();

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
                return     value+"%";
            case ENDS:
            case ENDS_CASE_INSENSITIVE:
                return "%"+value;
            default:
                return     value;
        }
    }


    public Expression getExpression() {
        return e;
    }

    public String getSql() {
        return sql.toString();
    }

    /** Is the SQL statement empty?  */
    public boolean isEmpty() {
        return sql.length()==0;
    }


}
