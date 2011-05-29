/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujorm.gxt.client.cquery;

import org.ujorm.gxt.client.Cujo;



/**
 * The criterion binary operator
 * @since 0.90
 * @author Pavel Ponec
 */
public enum CBinaryOperator implements AbstractCOperator {
    /** (a AND b) */
    AND,
    /** (a OR b) */
    OR,
    /** (a XOR b) <br>Note: the SQL language may not support the operator. */
    XOR,
    /** NOT (a OR b) <br>Note: the SQL language may not support the operator. */
    NOR,
    /** NOT (a AND b) <br>Note: the SQL language may not support the operator. */
    NAND,
    /** (a == b) <br>Note: the SQL language may not support the operator. */
    EQ,
    /** NOT a */
    NOT,
    ;

    /** Join two criterions. */
    public <UJO extends Cujo> CCriterion<UJO> join(final CCriterion<UJO> a, final CCriterion<UJO> b) {
        return a.join(this, b);
    }

    /** The operator is the BINARY type (not a value one) */
    public final boolean isBinary() {
        return false;
    }

    /** Returns Enum */
    public final Enum getEnum() {
        return this;
    }
}
