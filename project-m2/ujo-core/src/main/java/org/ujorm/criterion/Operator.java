/*
 *  Copyright 2007-2013 Pavel Ponec
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
package org.ujorm.criterion;

/**
 * The value criterion operator enum.
 * @since 0.90
 * @author Pavel Ponec
 */
public enum Operator implements AbstractOperator {
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
    /** Operator to compare a property to collection */
    IN,
    /** Negation operator to compare a property to collection */
    NOT_IN,
    /** Regular expression */
    REGEXP,
    /** Negation of the regular expression */
    NOT_REGEXP,
    /** Only for a CharSequence subtypes (including String) */
    EQUALS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (including String) */
    STARTS,
    /** Only for a CharSequence subtypes (including String) */
    STARTS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (including String) */
    ENDS,
    /** Only for a CharSequence subtypes (including String) */
    ENDS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (including String) */
    CONTAINS,
    /** Only for a CharSequence subtypes (including String) */
    CONTAINS_CASE_INSENSITIVE,
    /** This operator can have their own SQL condition by a SqlDialect solution.
     * <br>If you need to use more operators, I recommend to implement your own class 
     * by the iterface AbstractOperator and adjust the appropriate SqlDialect.
     * @see org.ujorm.orm.SqlDialect#getCriterionTemplate(org.ujorm.criterion.ValueCriterion)
     */
    USER,
    /** The operator for an internal use only where a result is
     * <strong>not dependent</strong> on the value.
     * @see Criterion#constant(org.ujorm.Key, boolean) 
     */
    XFIXED,
    /** The operator for an indication of the SQL condition in a text format
     * Parameters of the SQL condition are not supported by the Ujorm.
     * @see Criterion#forSql(org.ujorm.Key, java.lang.String) 
     */
    XSQL,
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
