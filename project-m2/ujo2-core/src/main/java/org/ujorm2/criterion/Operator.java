/*
 *  Copyright 2007-2016 Pavel Ponec
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
package org.ujorm2.criterion;

/**
 * The value criterion operator enumerations.
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
    /** Operator to compare a key to collection */
    IN,
    /** Negation operator to compare a key to collection */
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
     * by the interface AbstractOperator and adjust the appropriate SqlDialect.
     * @see org.ujorm.orm.SqlDialect#getCriterionTemplate(org.ujorm.criterion.ValueCriterion)
     */
    USER,
    /** The operator for an internal use only where a result is
     * <strong>not dependent</strong> on the value.
     * The result of the {@link Criterion#getRightNode()} method is the {@link Boolean} type always.
     * @see Criterion#constant(org.ujorm.Key, boolean)
     */
    XFIXED,
    /** The operator for an indication of the SQL condition in a text format
     * Parameters of the SQL condition are not supported by the Ujorm.
     * @see Criterion#forSql(org.ujorm.Key, java.lang.String)
     */
    XSQL,
    ;

    /** The implementation is a VALUE type (not a binary one) */
    @Override
    public final boolean isBinary() {
        return false;
    }

    /** Returns an Enumeration */
    @Override
    public final Enum getEnum() {
        return this;
    }

}
