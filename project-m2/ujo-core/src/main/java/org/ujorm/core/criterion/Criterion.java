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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractKey;

import java.util.Collection;
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
        var rightOp = criterion.getOperator();
        if (rightOp == Operator.ALWAYS_TRUE || rightOp == Operator.ALWAYS_FALSE) {
            return switch (operator) {
                case OR -> rightOp == Operator.ALWAYS_TRUE ? criterion : this;
                case AND -> rightOp == Operator.ALWAYS_TRUE ? this : criterion;
                default -> new BinaryCriterion(this, operator, criterion);
            };
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

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param positive The false value uses the NOT_IN operator.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the U values.
     * @param relatedKey The one key related to the one attribute of TYPE object.
     * @return The new immutable Criterion.
     */
    @NotNull
    private static <U, ITEM, TYPE> Criterion whereIn(
            final boolean positive,
            @NotNull final Key<U,TYPE> key,
            @NotNull final Collection<ITEM> list,
            @NotNull final Key<ITEM, TYPE> relatedKey) {

        if (list.isEmpty()) {
            return Criterion.constant(key, !positive);
        } else {
            var it = list.iterator();
            var values = new Object[list.size()];
            for (int i = 0, max = values.length; i < max; i++) {
                values[i] = relatedKey.getValue(it.next());
            }
            return new ValueCriterion<>(key, positive ? Operator.IN : Operator.NOT_IN, values);
        }
    }

    // ------ STATIC FACTORY --------

    /**
     * New criterion instance
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
     * New criterion instance
     * @param key Key
     * @param operator Operator
     * @param proxyValue A function for the value
     * @return A new criterion
     * @see ProxyValue A proxy for the value
     */
    @NotNull
    public static <U, TYPE> Criterion where(
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
    public static <U, TYPE> Criterion where(
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
    public static <U, TYPE> Criterion where(
            @NotNull final Key<U,TYPE> key,
            @Nullable final TYPE value) {
        return new ValueCriterion<>(key, null, value);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull
    public static <U, TYPE> Criterion whereIn(
            @NotNull final Key<U,TYPE> key,
            @NotNull final Collection<TYPE> list) {
        return list.isEmpty()
                ? Criterion.constant(key, false)
                : new ValueCriterion<>(key, Operator.IN, list.toArray());
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull
    public static <U, TYPE> Criterion whereNotIn(
            @NotNull final Key<U,TYPE> key,
            @NotNull final Collection<TYPE> list) {
        return list.isEmpty()
                ? Criterion.constant(key, true)
                : new ValueCriterion<>(key, Operator.NOT_IN, list.toArray());
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants
     * @param key A reference to a related entity
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion
     */
    @SafeVarargs
    @NotNull
    public static <U, TYPE> Criterion whereIn(
            @NotNull final Key<U,TYPE> key,
            @NotNull final TYPE... list) {
        return list.length == 0
                ? Criterion.constant(key, false)
                : new ValueCriterion<>(key, Operator.IN, list);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A key direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @SafeVarargs
    @NotNull
    public static <U, TYPE> Criterion whereNotIn(
            @NotNull final Key<U,TYPE> key,
            @NotNull final TYPE... list) {
        return list.length == 0
                ? Criterion.constant(key, true)
                : new ValueCriterion<>(key, Operator.NOT_IN, list);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the U values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @param relatedKey The one key related to the one attribute of TYPE object.
     * @return The new immutable Criterion.
     */
    @NotNull
    public static <U, ITEM, TYPE> Criterion whereIn(
            @NotNull final Key<U,TYPE> key,
            @NotNull final Collection<ITEM> list,
            @NotNull final Key<ITEM, TYPE> relatedKey) {
        return whereIn(true, key, list, relatedKey);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the U values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @param relatedKey The one key related to the one attribute of TYPE object.
     * @return The new immutable Criterion.
     */
    @NotNull
    public static <U, ITEM, TYPE> Criterion whereNotIn(
            @NotNull final Key<U,TYPE> key,
            @NotNull final Collection<ITEM> list,
            @NotNull final Key<ITEM, TYPE> relatedKey) {
        return whereIn(false, key, list, relatedKey);
    }

    /**
     * New equals instance
     * @param key Key
     * @param value Value or Key can be type a direct of indirect (for a relation) key
     * @return The new immutable Criterion
     */
    @NotNull
    public static <U, TYPE> Criterion where(
            @NotNull final Key<U,TYPE> key,
            @Nullable final Key<U,TYPE> value) {
        return new ValueCriterion<>(key, null, value);
    }

    /**
     * Create new Criterion where a key value equals to the NULL.
     * @param key Key
     * @see Operator#EQ
     */
    @NotNull
    public static <U, TYPE> Criterion whereNull(@NotNull final Key<U,TYPE> key) {
        return new ValueCriterion<>(key, Operator.EQ, (TYPE) null);
    }

    /**
     * Create new Criterion where a key value not equals to the NULL.
     * @param key Key
     * @see Operator#NOT_EQ
     */
    @NotNull
    public static <U, TYPE> Criterion whereNotNull(@NotNull final Key<U,TYPE> key) {
        return new ValueCriterion<>(key, Operator.NOT_EQ, (TYPE) null);
    }

    /**
     * This is a constant criterion independent of an entity.
     * @deprecated The method is deprecated in the ORM, use an alternative method.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <U> Criterion where(final boolean value) {
        return value
                ? ValueCriterion.TRUE
                : ValueCriterion.FALSE;
    }

    /**
     * The method creates a new Criterion for a native condition (called Native Criterion) in SQL statement format.
     * @param key The parameter is required by Ujorm to location a basic database table
     * @param sqlCondition a SQL condition in the String format, the NULL value or empty string is not accepted
     * @see Operator#CUSTOM_SQL
     */
    @NotNull
    public static <U> Criterion forSql(
            @NotNull final Key<U,?> key,
            @NotNull final String sqlCondition) {
        return new ValueCriterion<>(key, Operator.CUSTOM_SQL, sqlCondition);
    }

    /**
     * This is a constant criterion independent of the key value or the ujo entity.
     * @param key The parameter is required by Ujorm to location a basic database table
     */
    @NotNull
    public static <U> Criterion forAll(@NotNull final Key<U,?> key) {
        return constant(key, true);
    }

    /**
     * This is a constant criterion independent of the key value or the ujo entity.
     */
    @NotNull
    public static Criterion forAll() {
        return constant(DUMMY_KEY, true);
    }

    /**
     * This is a constant criterion independent of the key value or the ujo entity.
     * @param key The parameter is required by Ujorm to location a basic database table
     */
    @NotNull
    public static <U> Criterion forNone(@NotNull final Key<U,?> key) {
        return constant(key, false);
    }

    /**
     * This is a constant criterion independent of the key value or the ujo entity.
     */
    @NotNull
    public static Criterion forNone() {
        return constant(DUMMY_KEY, false);
    }
    /**
     * This is a special constant criterion independent of the key or the ujo entity.
     * @param key The parameter is required by Ujorm to location a basic database table
     */
    @NotNull
    public static <U> Criterion constant(@NotNull final Key<U,?> key, final boolean constant) {
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