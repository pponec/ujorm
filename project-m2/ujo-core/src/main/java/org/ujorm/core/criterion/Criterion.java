/*
 * Copyright 2007-2026 Pavel Ponec
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractKey;
import org.ujorm.tools.common.Array;

import java.util.Objects;

/**
 * An abstract immutable criterion provides a basic interface and static factory methods. You can use it:
 * <ul>
 *    <li>like a generic UJO object validator (2)</li>
 *    <li>to create a query on the UJO list (1)</li>
 *    <li>the class is used to build 'SQL query' in the module <strong>ujo-orm</strong> (since 0.90)</li>
 * </ul>
 *
 * There is allowed to join two instances (based on the same BO) to a binary tree by a new Criterion.
 * Some common operators (and, or, not) are implemented into a special join method of the Criterion class.
 *
 * <h3>Example of use</h3>
 * <pre class="pre"><span class="comment">// Make a criterion:</span>
 * Criterion&lt;Person&gt; crn1, crn2, criterion;
 * crn1 = Criterion.where(CASH, Operator.GT, 10.0);
 * crn2 = Criterion.where(CASH, Operator.LE, 20.0);
 * criterion = crn1.and(crn2);
 *
 * <span class="comment">// Use a criterion (1):</span>
 * CriteriaTool&lt;Person&gt; ct = CriteriaTool.where();
 * List&lt;Person&gt; result = ct.select(persons, criterion);
 * assertEquals(1, result.size());
 * assertEquals(20.0, CASH.of(result.get(0)));
 *
 * <span class="comment">// Use a criterion (2):</span>
 * Person person = result.get(0);
 * <span class="keyword-directive">boolean</span> validation = criterion.evaluate(person);
 * assertTrue(validation);
 * </pre>
 *
 * <h3>Using the parentheses</h3>
 * A Criterion instance composed from another criterions works as an expression separated by parentheses.
 * See the next two examples:
 * <pre class="pre"><span class="comment">// Consider instances:</span>
 * Criterion&lt;Person&gt; a, b, c, result;
 * a = Criterion.where(CASH, Operator.GT, 10.0);
 * b = Criterion.where(CASH, Operator.LE, 20.0);
 * c = Criterion.where(NAME, Operator.STARTS, "P");
 *
 * <span class="comment">// Expression #1: (<span class="highlight">a OR b</span>) AND c :</span>
 * result = (<span class="highlight">a.or(b)</span>).and(c); <span class="comment">// or simply:</span>
 * result = <span class="highlight">a.or(b)</span>.and(c);
 *
 * <span class="comment">// Expression #2: a AND (<span class="highlight">b OR c</span>) :</span>
 * result = a.and(<span class="highlight">b.or(c)</span>);
 * </pre>
 *
 * @since 0.90
 * @author Pavel Ponec
 */
public abstract class Criterion {

    /** Dummy key */
    static final Key<?,?> DUMMY_KEY = createDummyKey();

    /** Returns the left node of the parent. */
    @NotNull
    abstract public Object getLeftNode();

    /** Returns the right node of the parent. */
    @Nullable
    abstract public Object getRightNode();

    /** Returns an operator. */
    @NotNull
    abstract public AbstractOperator getOperator();

    /**
     * Find a domain class type of {@code Class<U>} from its keys.
     * @return Method returns the {@code Ujo.class} instance if no domain was found.
     */
    @NotNull
    abstract public Class<?> getDomain();

    /** Is the class a Binary criterion? */
    public boolean isBinary() {
        return false;
    }

    /**
     * Join this instance with a second criterion by an operator with a simple logical optimization.
     * @param operator Binary operator
     * @param criterion Criterion to join
     * @return Result
     */
    public Criterion join(@NotNull final BinaryOperator operator, @NotNull final Criterion criterion) {
        var constL = this.getOperator().isConstant();
        if (constL || criterion.getOperator().isConstant()) {
            var cNode = constL ? this : criterion;
            var oNode = constL ? criterion : this;

            switch (operator) {
                case OR: return cNode.getOperator() == Operator.ALWAYS_TRUE ? cNode : oNode;
                case AND: return cNode.getOperator() == Operator.ALWAYS_FALSE ? cNode : oNode;
            }
        }
        return new BinaryCriterion(this, operator, criterion);
    }

    /**
     * Join a criterion by the {@link BinaryOperator#AND} operator.
     * @param criterion Criterion to join
     * @return Result
     */
    public final Criterion and(@NotNull final Criterion criterion) {
        return join(BinaryOperator.AND, criterion);
    }

    /**
     * Join a criterion by the {@link BinaryOperator#OR} operator.
     * @param criterion Criterion to join
     * @return Result
     */
    public final Criterion or(@NotNull final Criterion criterion) {
        return join(BinaryOperator.OR, criterion);
    }

    /**
     * Join this criterion by the {@link BinaryOperator#NOT} operator.
     * @return Result
     */
    public final Criterion not() {
        return join(BinaryOperator.NOT, this);
    }

    /** Print the Criterion including the main domain name along the example: Order(id EQ 1) */
    @NotNull
    public String toStringFull() {
        var printer = new SimpleValuePrinter(128).append(getDomain().getSimpleName());
        return toPrinter(printer).toString();
    }

    /**
     * Print the Criterion including the main domain name along the example: Order(id EQ 1)
     * @param out Printer instance
     * @return Printer instance
     */
    @NotNull
    public SimpleValuePrinter toPrinter(SimpleValuePrinter out) {
        return out.appendValue(String.valueOf(getDomain()));
    }

    /** Calculate hash. */
    @Override
    public final int hashCode() {
        return Objects.hash(getLeftNode(), getOperator(), getRightNode());
    }

    /**
     * Check a second criterion for equality.
     * The method assumes that the equals() method works correctly for value parameters.
     */
    @Override
    public final boolean equals(@Nullable Object value) {
        if (value instanceof Criterion crn) {
            return Objects.equals(getOperator(), crn.getOperator())
                    && Objects.equals(getLeftNode(), crn.getLeftNode())
                    && Objects.equals(getRightNode(), crn.getRightNode());
        }
        return false;
    }

    // ------ STATIC FACTORY METHODS --------

    /**
     * Common method factory
     * @param key Key
     * @param operator Operator
     * @param value The parameter value
     * @return A new criterion
     */
    @NotNull
    public static <U, TYPE> Criterion where(
            @NotNull final Key<U,TYPE> key,
            @NotNull final Operator operator,
            @Nullable final TYPE value) {
        return new ValueCriterion<>(key, operator, value);
    }

    /**
     * Comon method factory for the Proxy
     * @param key Key
     * @param operator Operator
     * @param proxyValue A function for the value
     * @return A new criterion
     * @see ProxyValue A proxy for the value
     */
    @NotNull
    public static <U, TYPE> Criterion whereProxy(
            @NotNull final Key<U,TYPE> key,
            @NotNull final Operator operator,
            @NotNull final ProxyValue<TYPE> proxyValue) {
        return new FunctionCriterion<>(key, operator, proxyValue);
    }

    /**
     * New criterion instance
     * @param key Key
     * @param operator Operator
     * @param value Reference to a related entity key
     * @return A new criterion
     */
    @NotNull
    public static <U, TYPE> Criterion whereKey(
            @NotNull final Key<U,TYPE> key,
            @NotNull final Operator operator,
            @Nullable final Key<?,TYPE> value) {
        return new ValueCriterion<>(key, operator, value);
    }

    /**
     * New equals instance
     * @param key Key
     * @param value Parameter value
     * @return The new immutable Criterion
     */
    @NotNull
    public static <U, TYPE> Criterion whereEq(
            @NotNull final Key<U,TYPE> key,
            @Nullable final TYPE value) {
        return where(key, Operator.EQ, value);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param positive A sign of the Criterion: true for IN, false for NOT IN.
     * @param key A direct or indirect Ujo key
     * @param array A collection of the values. If the argument is EMPTY, the result is TRUE for the negative sign and FALSE for the positive sign.
     * @return The new immutable Criterion.
     */
    @NotNull
    public static <U, TYPE> Criterion whereIn(
            boolean positive,
            @NotNull final Key<U,TYPE> key,
            @NotNull final Array<TYPE> array) {
        return switch (array.size()) {
            case 0 -> Criterion.forConstant(key, !positive);
            case 1 -> new ValueCriterion<>(key, positive ? Operator.EQ : Operator.NOT_EQ, array.get(0));
            default -> new ValueCriterion<>(key, positive ? Operator.IN : Operator.NOT_IN, array);
        };
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param array A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull
    public static <U, TYPE> Criterion whereInArray(
            boolean positive,
            @NotNull final Key<U, TYPE> key,
            @NotNull final TYPE... array) {
        return switch (array.length) {
            case 0 -> Criterion.forConstant(key, !positive);
            case 1 -> new ValueCriterion<>(key, positive ? Operator.EQ : Operator.NOT_EQ, array[0]);
            default -> new ValueCriterion<>(key, positive ? Operator.IN : Operator.NOT_IN, Array.of(array));
        };
    }

    /**
     * New equals instance for the key.
     * @param key Key
     * @param keyValue Value or Key can be type a direct of indirect (for a relation) key
     * @return The new immutable Criterion
     */
    @NotNull
    public static <U, TYPE> Criterion whereKey(
            @NotNull final Key<U,TYPE> key,
            @Nullable final Key<U,TYPE> keyValue) {
        return new ValueCriterion<>(key, Operator.EQ, keyValue);
    }

    /**
     * The method creates a new Criterion for a native condition (called Native Criterion) in SQL statement format.
     * <p>
     * Supported placeholders in the template:
     * <ul>
     *   <li>{@code {0}} : Replaced by the database column name (mapped from the key).</li>
     *   <li>{@code {1}} : Replaced by all values joined by a comma (e.g., for {@code IN} operator).</li>
     * </ul>
     * * Example of use:
     * <pre>{@code
     * Criterion.forSql(MetaEmployee.name, "UPPER({0}) = {1}")
     * Criterion.forSql(MetaEmployee.id, "{0} IN (1})")
     * }</pre>
     * @param key The parameter is required by Ujorm to locate a basic database table
     * @param sqlTemplate A SQL condition in the String format, the NULL value or empty string is not accepted
     * @param values Optional parameters for the placeholders
     * @return A new Criterion instance
     * @see Operator#CUSTOM_SQL
     */
    @NotNull
    public static <U, V> Criterion forSql(
            @NotNull final Key<U, V> key,
            @NotNull final String sqlTemplate,
            @NotNull final V... values) {
        return new ValueCriterion<>(key, Operator.CUSTOM_SQL, new TemplateValue<>(sqlTemplate, Array.of(values)));
    }

    /**
     * This is a constant criterion independent of the key value or the ujo entity.
     * @param key The parameter is required by Ujorm to location a basic database table
     */
    @NotNull
    public static <U> Criterion forAll(@NotNull final Key<U,?> key) {
        return forConstant(key, true);
    }

    /**
     * This is a constant criterion independent of the key value or the ujo entity.
     */
    @NotNull
    @Deprecated
    public static Criterion forAll() {
        return forConstant(DUMMY_KEY, true);
    }

    /**
     * This is a constant criterion independent of the key value or the ujo entity.
     * @param key The parameter is required by Ujorm to location a basic database table
     */
    @NotNull
    public static <U> Criterion forNone(@NotNull final Key<U,?> key) {
        return forConstant(key, false);
    }

    /**
     * This is a constant criterion independent of the key value or the ujo entity.
     */
    @NotNull
    @Deprecated
    public static Criterion forNone() {
        return forConstant(DUMMY_KEY, false);
    }
    /**
     * This is a special constant criterion independent of the key or the ujo entity.
     * @param key The parameter is required by Ujorm to location a basic database table
     */
    @NotNull
    public static <U> Criterion forConstant(@NotNull final Key<U,?> key, final boolean constant) {
        return new ValueCriterion<>(key, constant ? Operator.ALWAYS_TRUE : Operator.ALWAYS_FALSE, constant);
    }

    private static @NotNull AbstractKey<Object, Object> createDummyKey() {
        return new AbstractKey<>(0, "", Object.class, "", false, false, false, false) {
            @Override
            public @NotNull Class<Object> domainClass() {
                return Object.class;
            }

            @Override
            public void setValue(@NotNull Object bean, @Nullable Object o) {
            }

            @Override
            public Object getValue(@NotNull Object bean) {
                return null;
            }
        };
    }
}