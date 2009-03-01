/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.tools.criteria;

import org.ujoframework.Ujo;

/**
 *
 * @author pavel
 */
public class DirtyExpression<UJO extends Ujo> extends Expression<UJO>  {

    private final Object[] sqlStatement;

    public DirtyExpression(Object[] sqlStatement) {
        this.sqlStatement = sqlStatement;
    }

    @Override
    public boolean evaluate(UJO ujo) {
        throw new UnsupportedOperationException("ONLY SQL statement.");
    }

    // --- STATIC METHODS ---

    /**
     * Create new plain SQL statement however all UjoProperties are replaced by a DB metamodel.
     */
    public static <UJO extends Ujo, TYPE> Expression<UJO> newInstance(Object... sqlStatement) {
        return new DirtyExpression(sqlStatement);
    }
}
