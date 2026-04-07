/*
 * Copyright 2007-2022 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.core.criterion;

/**
 * The value criterion operator enumerations.
 * @since 0.90
 * @author Pavel Ponec
 */
public enum Operator implements AbstractOperator {
    /** Equals the value */
    EQ("="),
    /** Not equals the value */
    NOT_EQ("<>"),
    /** Great then the value */
    GT(">"),
    /** Great or equals the value */
    GE(">="),
    /** Less then the value */
    LT("<"),
    /** Less or equals the value */
    LE("<="),
    /** Operator to compare a key to collection */
    IN("IN"),
    /** Negation operator to compare a key to collection */
    NOT_IN("NOT IN"),
    /** Regular expression. Note: Not a standard SQL operator. */
    REGEXP("REGEXP"),
    /** Negation of the regular expression. Note: Not a standard SQL operator. */
    NOT_REGEXP("NOT REGEXP"),
    /** Only for a CharSequence subtypes (including String). Note: Not a standard SQL operator. */
    EQUALS_CASE_INSENSITIVE("="),
    /** Only for a CharSequence subtypes (including String) */
    STARTS("LIKE"),
    /** Only for a CharSequence subtypes (including String). Note: Not a standard SQL operator. */
    STARTS_CASE_INSENSITIVE("ILIKE"),
    /** Only for a CharSequence subtypes (including String) */
    ENDS("LIKE"),
    /** Only for a CharSequence subtypes (including String). Note: Not a standard SQL operator. */
    ENDS_CASE_INSENSITIVE("ILIKE"),
    /** Only for a CharSequence subtypes (including String) */
    CONTAINS("LIKE"),
    /** Only for a CharSequence subtypes (including String). Note: Not a standard SQL operator. */
    CONTAINS_CASE_INSENSITIVE("ILIKE"),
    /** This operator can have their own SQL condition by a SqlDialect solution.
     * <br>If you need to use more operators, I recommend to implement your own class
     * by the interface AbstractOperator and adjust the appropriate SqlDialect.
     * <br>Note: Not a standard SQL operator.
     */
    USER(""),

    /**
     * A custom SQL operator where the placeholder {@code {0} is replaced by the real column name and {1} is repaced by the first value.
     * The right-hand value of the Criterion is used as the SQL template with values.
     * <br>Note: Not a standard SQL operator.
     */
    CUSTOM_SQL("{0}"),

    /** The operator for an internal use only where a result is
     * <strong>not dependent</strong> on the value. Allways return true  */
    ALWAYS_TRUE("1=1"),

    /** The operator for an internal use only where a result is
     * <strong>not dependent</strong> on the value. Always return false. */
    ALWAYS_FALSE("1=0");

    /** The SQL standard string representation of the operator */
    private final String term;

    /** Creates a new Operator with its SQL string representation */
    Operator(String term) {
        this.term = term;
    }

    /** Returns the SQL string representation of the operator */
    @Override
    public String term() {
        return term;
    }

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