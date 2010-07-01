/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client.cquery;

/**
 * The value criterion operator enum.
 * @since 0.90
 * @author Pavel Ponec
 */
public enum COperator implements AbstractCOperator {

    /** Equals the value */
    EQ,
    /** Not equals the value */
    NOT_EQ,
    /** Great then the value */
    GT,
    /** Great or equals the value */
    GE,
    /** Less then the value */
    LT,
    /** Less or equals the value */
    LE,
    /** Regular expression */
    REGEXP,
    /** Negation of the regular expression */
    NOT_REGEXP,
    /** Only for a CharSequence subtypes (include String) */
    EQUALS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (include String) */
    STARTS,
    /** Only for a CharSequence subtypes (include String) */
    STARTS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (include String) */
    ENDS,
    /** Only for a CharSequence subtypes (include String) */
    ENDS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (include String) */
    CONTAINS,
    /** Only for a CharSequence subtypes (include String) */
    CONTAINS_CASE_INSENSITIVE,
    /** This operator can have their own SQL condition by a SqlDialect solution.
     * <br>If you need to use more operators, I recommend to implement your own class 
     * by the iterface AbstractCOperator and adjust the appropriate SqlDialect.
     * @see org.ujoframework.orm.SqlDialect#getCriterionTemplate(org.ujoframework.criterion.ValueCriterion)
     */
    USER,
    /** The operator for an internal use only where a result is
     * <strong>not dependent</strong> on the value. */
    X_FIXED,
    ;


    /** The implementace is a VALUE type (not a binary one) */
    @Override
    public final boolean isBinary() {
        return false;
    }

    /** Returns Enum */
    @Override
    public final Enum getEnum() {
        return this;
    }

}
