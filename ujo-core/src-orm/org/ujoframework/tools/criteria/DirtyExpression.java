/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.tools.criteria;

import org.ujoframework.Ujo;

/**
 * A ditry expression is inteded for a SQL statements only.
 * @author pavel
 */
public class DirtyExpression<UJO extends Ujo> extends Expression<UJO>  {

    private final Object[] sqlStatement;

    public DirtyExpression(Object[] sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    /** The method is forbidden */
    @Override
    @Deprecated
    public boolean evaluate(UJO ujo) {
        throw new UnsupportedOperationException("The method is forbidden");
    }

    // --- STATIC METHODS ---

    /**
     * Create new plain SQL statement however all UjoProperties are replaced by a DB metamodel.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, TYPE> Expression<UJO> newInstance(Object... sqlStatement) {
        return new DirtyExpression(sqlStatement);
    }

    /** It is not omplemented */
    @Override
    public Object getLeftNode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** It is not omplemented */
    @Override
    public Object getRightNote() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Returns NULL value */
    @Override
    public AbstractOperator getOperator() {
        return null;
    }
}
