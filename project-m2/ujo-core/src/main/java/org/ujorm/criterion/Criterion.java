/*
 *  Copyright 2007-2014 Pavel Ponec
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.Ujo;

/**
 * An abstract immutable criterion provides a basic interface and static factory methods. You can use it:
 * <ul>
 *    <li>like a generic UJO object validator (2)</li>
 *    <li>to create a query on the UJO list (1)</li>
 *    <li>the class is used to build 'SQL query' in the module </strong>ujo-orm</strong> (since 0.90)</li>
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

 * <span class="comment">// Expression #2: a AND (<span class="highlight">b OR c</span>) :</span>
 * result = a.and(<span class="highlight">b.or(c)</span>);
 * </pre>
 *
 * @since 0.90
 * @author Pavel Ponec
 * @composed 1 - 1 AbstractOperator
 */
public abstract class Criterion<U extends Ujo> implements Serializable {

    /** Apply the criterion to the UJO object
     * @return Returns the value {@code true} in case the ujo object satisfies the condition.
     */
    public abstract boolean evaluate(U ujo);

    /** Returns a list of items which satisfies the condition in this Criterion.
     * @see org.ujorm.criterion.CriteriaTool#select(java.util.List, org.ujorm.criterion.Criterion, org.ujorm.core.UjoComparator)
     */
    public List<U> evaluate(final Iterable<U> ujoList) {
        final List<U> result = new ArrayList<U>();
        for (final U ujo : ujoList) {
            if (evaluate(ujo)) {
                result.add(ujo);
            }
        }
        return result;
    }

    /** Returns a list of items which satisfies the condition in this Criterion.
     * @see org.ujorm.criterion.CriteriaTool#select(java.util.List, org.ujorm.criterion.Criterion, org.ujorm.core.UjoComparator)
     */
    public List<U> evaluate(final U ... ujoList) {
        final List<U> result = new ArrayList<U>();
        for (final U ujo : ujoList) {
            if (evaluate(ujo)) {
                result.add(ujo);
            }
        }
        return result;
    }

    /**
     * Evaluate an object along this criterion.
     * If the evaluate method returns false, then the method throws the {@link IllegalArgumentException}
     * with the required message.
     * @param ujo object to validate
     * @param parameters Text parameters for the message template are located by %s expression. See the
     *   <a href="http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html">java.util.Formatter</a>
     *   documentation for more information.
     * @throws IllegalArgumentException Exception, if the method {@link #validate(org.ujorm.Ujo) failed.
     */
    final public void validate(final U ujo, String message, Object ... parameters) throws IllegalArgumentException {
        if (!evaluate(ujo)) {
            final String msg = parameters!=null && parameters.length>0
                    ? String.format(message, parameters)
                    : message
                    ;
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Evaluate an object along this criterion.
     * If the evaluate method returns false, then the method throws the {@link IllegalArgumentException}
     * with the required message.
     * @throws IllegalArgumentException Exception, if the method {@link #validate(org.ujorm.Ujo) failed.
     */
    final public void validate(final U ujo) throws IllegalArgumentException {
        validate(ujo, "Invalid condition (" + toString() + ") for the " + ujo.toString());
    }

    public Criterion<U> join(BinaryOperator operator, Criterion<U> criterion) {
        return new BinaryCriterion<U>(this, operator, criterion);
    }

    public Criterion<U> and(Criterion<U> criterion) {
        return join(BinaryOperator.AND, criterion);
    }

    public Criterion<U> or(Criterion<U> criterion) {
        return join(BinaryOperator.OR, criterion);
    }

    public Criterion<U> not() {
        return new BinaryCriterion<U>(this, BinaryOperator.NOT, this);
    }

    /** Returns the left node of the parent. */
    abstract public Object getLeftNode();
    /** Returns the right node of the parent */
    abstract public Object getRightNode();
    /** Returns an operator */
    abstract public AbstractOperator getOperator();
    /** Find a domain class type of {@code Class<U>} from its keys.
     * @return returns Method returns the {@code Ujo.class} instance if no domain was found.
     */
    abstract public Class<?> getDomain();


    /** Is the class a Binary criterion? */
    public boolean isBinary() {
        return false;
    }

    /** Cast the current criterion to an extented generic domain for the case
     * where the current Key domain is from a parent class.
     */
    public final <T extends U> Criterion<T> cast() {
        return (Criterion<T>) this;
    }

    /** Print the Criterion including the main domain name along the example: Order(id EQ 1)  */
    public String toStringFull() {
        final Class domain = getDomain();
        return domain.getSimpleName() + this;
    }

    // ------ STATIC FACTORY --------

    /**
     * New criterion instance
     * @param key Key
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    public static <U extends Ujo, TYPE> Criterion<U> where
        ( Key<U,TYPE> key
        , Operator operator
        , TYPE value
        ) {
        return new ValueCriterion<U>(key, operator, value);
    }

    /**
     * New criterion instance
     * @param key Key
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    public static <U extends Ujo, TYPE> Criterion<U> where
        ( Key<U,TYPE> key
        , Operator operator
        , Key<?,TYPE> value
        ) {
        return new ValueCriterion<U>(key, operator, value);
    }

    /**
     * New equals instance
     * @param key Key
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>Key - reference to a related entity</li>
     * </ul>
     * @return The new immutable Criterion
     */
    public static <U extends Ujo, TYPE> Criterion<U> where
        ( Key<U,TYPE> key
        , TYPE value
        ) {
        return new ValueCriterion<U>(key, null, value);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion.
     */
    public static <U extends Ujo, TYPE> Criterion<U> whereIn
        ( Key<U,TYPE> key
        , Collection<TYPE> list
        ) {

        return list.isEmpty()
                ? Criterion.constant(key, false)
                : new ValueCriterion<U>(key, Operator.IN, list.toArray())
                ;
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    public static <U extends Ujo, TYPE> Criterion<U> whereNotIn
        ( Key<U,TYPE> key
        , Collection<TYPE> list
        ) {
        return list.isEmpty()
                ? Criterion.constant(key, true)
                : new ValueCriterion<U>(key, Operator.NOT_IN, list.toArray())
                ;
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants
     * @param key A reference to a related entity
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion
     */
    public static <U extends Ujo, TYPE> Criterion<U> whereIn
        ( Key<U,TYPE> key
        , TYPE... list
        ) {
        return list.length==0
                ? Criterion.constant(key, false)
                : new ValueCriterion<U>(key, Operator.IN, list);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A key direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    public static <U extends Ujo, TYPE> Criterion<U> whereNotIn
        ( Key<U,TYPE> key
        , TYPE... list
        ) {
        return list.length==0
                ? Criterion.constant(key, true)
                : new ValueCriterion<U>(key, Operator.NOT_IN, list)
                ;
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the U values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @param relatedKey The one key related to the one attribute of TYPE object.
     * @return The new immutable Criterion.
     */
    public static <U extends Ujo, ITEM extends Ujo, TYPE> Criterion<U> whereIn
        ( Key<U,TYPE> key
        , Collection<ITEM> list
        , Key<ITEM, TYPE> relatedKey
        ) {

        return whereIn(true, key, list, relatedKey);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the U values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @param relatedKey The one key related to the one attribute of TYPE object.
     * @return The new immutable Criterion.
     */
    public static <U extends Ujo, ITEM extends Ujo, TYPE> Criterion<U> whereNotIn
        ( Key<U,TYPE> key
        , Collection<ITEM> list
        , Key<ITEM, TYPE> relatedKey
        ) {

        return whereIn(false, key, list, relatedKey);
    }

    /**
     * Create new Criterion for operator IN to compare value to a list of constants.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the U values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @param relatedKey The one key related to the one attribute of TYPE object.
     * @param positive The false value uses the NOT_IN operator.
     * @return The new immutable Criterion.
     */
    private static <U extends Ujo, ITEM extends Ujo, TYPE> Criterion<U> whereIn
        ( boolean positive
        , Key<U,TYPE> key
        , Collection<ITEM> list
        , Key<ITEM, TYPE> relatedKey
        ) {

        if (list.isEmpty()) {
            return Criterion.constant(key, !positive);
        } else {
            final Iterator<ITEM> it = list.iterator();
            final Object[] values = new Object[list.size()];
            for (int i = 0, max = values.length; i < max; i++) {
                values[i] = relatedKey.of(it.next());
            }
            return new ValueCriterion<U>(key
                    , positive ? Operator.IN : Operator.NOT_IN
                    , values
                    );
        }
    }


    /**
     * New equals instance
     * @param key Key
     * @param value Value or Key can be type a direct of indirect (for a relation) key
     * @return The new immutable Criterion
     */
    public static <U extends Ujo, TYPE> Criterion<U> where
        ( Key<U,TYPE> key
        , Key<U,TYPE> value
        ) {
        return new ValueCriterion<U>(key, null, value);
    }

    /**
     * Create new Criterion where a key value equals to the NULL.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @param key Key
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    public static <U extends Ujo, TYPE> Criterion<U> whereNull(Key<U,TYPE> key) {
        return new ValueCriterion<U>(key, Operator.EQ, (TYPE)null);
    }

    /**
     * Create new Criterion where a key value not equals to the NULL.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @param key Key
     * @see #whereNull(org.ujorm.Key)
     * @see Operator#NOT_EQ
     */
    public static <U extends Ujo, TYPE> Criterion<U> whereNotNull(Key<U,TYPE> key) {
        return new ValueCriterion<U>(key, Operator.NOT_EQ, (TYPE)null);
    }

    /** This is an constane criterion independed on an entity.
     * The method is <strong>deprecated</strong> in the ORM, use rather a one method from
     * {@line #forAll(org.ujorm.Key) forAll} or
     * {@line #forNone(org.ujorm.Key) forNone} .
     * @see #forAll(org.ujorm.Key)
     * @see #forNone(org.ujorm.Key)
     */
    @SuppressWarnings("unchecked")
    public static <U extends Ujo> Criterion<U> where(boolean value) {
        return (Criterion<U>) (value
            ? ValueCriterion.TRUE
            : ValueCriterion.FALSE
            );
    }

    /** This is a special constant criterion independed on the key or the ujo entity. A result is the same like the parameter constant always.
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @see Operator#XFIXED
     */
    public static <U extends Ujo> Criterion<U> constant(Key<U,?> key, boolean constant) {
        return new ValueCriterion<U>(key, Operator.XFIXED, constant);
    }

    /** The method creates a new Criterion for a native condition (called Native Criterion) in SQL statejemt format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native Criterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlCondition a SQL condition in the String format, the NULL value or empty string is not accepted
     * A substring {@code {0}} will be replaced for the current column name;
     * @see Operator#XSQL
     */
    public static <U extends Ujo> Criterion<U> forSql(Key<U,?> key, String sqlCondition) {
        return new ValueCriterion<U>(key, Operator.XSQL, sqlCondition);
    }

    /** The method creates a new Criterion for a native condition (called Native Criterion) in SQL statejemt format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native Criterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlTemplate a SQL condition in the String format, the NULL value or empty string is not accepted
     * A substring {@code {0}} will be replaced for the current column name;
     * @param value a condition value
     * A substring {@code {1}} will be replaced for the current column name;
     * @see Operator#XSQL
     */
    public static <U extends Ujo, VALUE> Criterion<U> forSql(Key<U,VALUE> key, String sqlTemplate, VALUE value) {
        return new ValueCriterion<U>(key, Operator.XSQL, new TemplateValue(sqlTemplate, value));
    }

    /** The method creates a new Criterion for a native condition (called Native Criterion) in SQL statejemt format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native Criterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlTemplate a SQL condition in the String format, the NULL value or empty string is not accepted
     * A substring {@code {0}} will be replaced for the current column name;
     * @param value a condition value, array, list or an another key
     * A substring {@code {1}} will be replaced for the current column name;
     * @see Operator#XSQL
     */
    public static <U extends Ujo, VALUE> Criterion<U> forSqlUnchecked(Key<U,VALUE> key, String sqlTemplate, Object value) {
        return new ValueCriterion<U>(key, Operator.XSQL, new TemplateValue(sqlTemplate, value));
    }

    /** This is a constant criterion independed on the key value or the ujo entity.
     *  The method evaluate(ujo) returns TRUE always.
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     */
    public static <U extends Ujo> Criterion<U> forAll(Key<U,?> key) {
        return constant(key, true);
    }

    /** This is a constant criterion independed on the key value or the ujo entity.
     *  The  method evaluate(method) returns FALSE always.
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     */
    public static <U extends Ujo> Criterion<U> forNone(Key<U,?> key) {
        return constant(key, false);
    }

    // ----------------------- DEPRECATED -----------------------


    /**
     * New criterion instance
     * @param key Key
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     * @deprecated See the {@link Criterion#where(org.ujorm.Key, org.ujorm.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <U extends Ujo, TYPE> Criterion<U> newInstance
        ( Key<U,TYPE> key
        , Operator operator
        , TYPE value
        ) {
        return where(key, operator, value);
    }

    /**
     * New criterion instance
     * @param key Key
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     * @deprecated See the {@link Criterion#where(org.ujorm.Key, org.ujorm.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <U extends Ujo, TYPE> Criterion<U> newInstance
        ( Key<U,TYPE> key
        , Operator operator
        , Key<?,TYPE> value
        ) {
        return where(key, operator, value);
    }

    /**
     * New equals instance
     * @param key Key
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>Key - reference to a related entity</li>
     * </ul>
     * @return The new immutable Criterion
     * @deprecated See the {@link Criterion#where(org.ujorm.Key, org.ujorm.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <U extends Ujo, TYPE> Criterion<U> newInstance
        ( Key<U,TYPE> key
        , TYPE value
        ) {
        return where(key, value);
    }

    /**
     * New equals instance
     * @param key Key
     * @param value Value or Key can be type a direct of indirect (for a relation) key
     * @return The new immutable Criterion
     * @deprecated See the {@link Criterion#where(org.ujorm.Key, org.ujorm.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <U extends Ujo, TYPE> Criterion<U> newInstance
        ( Key<U,TYPE> key
        , Key<U,TYPE> value
        ) {
        return where(key, value);
    }

}
