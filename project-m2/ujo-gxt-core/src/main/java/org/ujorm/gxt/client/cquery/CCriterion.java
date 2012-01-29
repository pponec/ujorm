/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.cquery;

import java.util.Collection;
import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import java.io.Serializable;

/**
 * An abstract immutable criterion provides a basic interface and static factory methods. You can use it:
 * <ul>
 *    <li>like a generic UJO object validator (2)</li>
 *    <li>to create a query on the UJO list (1)</li>
 *    <li>the class is used to build 'SQL query' in the module </strong>ujo-orm</strong> (sience 0.90)</li>
 * </ul>
 *
 * There is allowed to join two instances (based on the same BO) to a binary tree by a new CCriterion.
 * Some common operators (and, or, not) are implemeted into a special join method of the Criteron class.
 *
 * <h3>Example of use</h3>
 * <pre class="pre"><span class="comment">// Make a criterion:</span>
 * CCriterion&lt;Person&gt; crn1, crn2, criterion;
 * crn1 = CCriterion.where(CASH, COperator.GT, 10.0);
 * crn2 = CCriterion.where(CASH, COperator.LE, 20.0);
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
 * A CCriterion instance composed from another criterions works as an expression separated by parentheses.
 * See the next two examples:
 * <pre class="pre"><span class="comment">// Consider instances:</span>
 * CCriterion&lt;Person&gt; a, b, c, result;
 * a = CCriterion.where(CASH, COperator.GT, 10.0);
 * b = CCriterion.where(CASH, COperator.LE, 20.0);
 * c = CCriterion.where(NAME, COperator.STARTS, "P");
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
public abstract class CCriterion<UJO extends Cujo> implements Serializable {

    /** Apply the criterion to the UJO object
     * @return Returns the value {@code true} in case the ujo object satisfies the condition.
     */
    public abstract boolean evaluate(UJO ujo);

    public CCriterion<UJO> join(CBinaryOperator operator, CCriterion<UJO> criterion) {
        return new CBinaryCriterion<UJO>(this, operator, criterion);
    }

    public CCriterion<UJO> and(CCriterion<UJO> criterion) {
        return join(CBinaryOperator.AND, criterion);
    }

    public CCriterion<UJO> or(CCriterion<UJO> criterion) {
        return join(CBinaryOperator.OR, criterion);
    }

    public CCriterion<UJO> not() {
        return new CBinaryCriterion<UJO>(this, CBinaryOperator.NOT, this);
    }

    /** Returns the left node of the parrent */
    abstract public Object getLeftNode();
    /** Returns the right node of the parrent */
    abstract public Object getRightNode();
    /** Returns an operator */
    abstract public AbstractCOperator getOperator();

    /** Initialization after deserialization. */
    abstract public void restore(Class<UJO> ujoType);

    /** Is the class a Binary criterion? */
    public boolean isBinary() {
        return false;
    }

    // ------ STATIC FACTORY --------

    /**
     * New criterion instance
     * @param property CujoProperty
     * @param operator COperator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>CujoProperty - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where
        ( CujoProperty<UJO,TYPE> property
        , COperator operator
        , TYPE value
        ) {
        return new CValueCriterion<UJO>(property, operator, value);
    }

    /**
     * New criterion instance
     * @param property CujoProperty
     * @param operator COperator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>CujoProperty - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where
        ( CujoProperty<UJO,TYPE> property
        , COperator operator
        , CujoProperty<?,TYPE> value
        ) {
        return new CValueCriterion<UJO>(property, operator, value);
    }

    /**
     * New equals instance
     * @param property CujoProperty
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>CujoProperty - reference to a related entity</li>
     * </ul>
     * @return A the new immutable CCriterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where
        ( CujoProperty<UJO,TYPE> property
        , TYPE value
        ) {
        return new CValueCriterion<UJO>(property, null, value);
    }

    /**
     * Create new CCriterion for operator IN to compare value to a list of constants.
     * @param property A direct or indeirect Ujo property
     * @param list A collection of the values. The collection argument can be the EMPTY, the CCriterion result will be FALSE in this case.
     * @return A the new immutable CCriterion.
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> whereIn
        ( CujoProperty<UJO,TYPE> property
        , Collection<TYPE> list
        ) {

        if (list.isEmpty()) {
            return CCriterion.constant(property, false);
        } else {
            return new CValueCriterion<UJO>(property, COperator.IN, list.toArray());
        }
    }

    /**
     * Create new CCriterion for operator IN to compare value to a list of constants.
     * @param property A direct or indeirect Ujo property
     * @param list A collection of the values. The collection argument can be the EMPTY, the CCriterion result will be TRUE in this case.
     * @return A the new immutable CCriterion.
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> whereNotIn
        ( CujoProperty<UJO,TYPE> property
        , Collection<TYPE> list
        ) {
        return new CValueCriterion<UJO>(property, COperator.NOT_IN, list.toArray());
    }

    /**
     * Create new CCriterion for operator IN to compare value to a list of constants
     * @param property A reference to a related entity
     * @param list A collection of the values. The collection argument can be the EMPTY, the CCriterion result will be FALSE in this case.
     * @return A the new immutable CCriterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> whereIn
        ( CujoProperty<UJO,TYPE> property
        , TYPE... list
        ) {
        return new CValueCriterion<UJO>(property, COperator.IN, list);
    }

    /**
     * Create new CCriterion for operator IN to compare value to a list of constants.
     * @param property A property direct or indeirect Ujo property
     * @param list A collection of the values. The collection argument can be the EMPTY, the CCriterion result will be TRUE in this case.
     * @return A the new immutable CCriterion.
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> whereNotIn
        ( CujoProperty<UJO,TYPE> property
        , TYPE... list
        ) {
        return new CValueCriterion<UJO>(property, COperator.NOT_IN, list);
    }


    /**
     * New equals instance
     * @param property CujoProperty
     * @param value Value or CujoProperty can be type a direct of indirect (for a relation) property
     * @return A the new immutable CCriterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where
        ( CujoProperty<UJO,TYPE> property
        , CujoProperty<UJO,TYPE> value
        ) {
        return new CValueCriterion<UJO>(property, null, value);
    }

    /**
     * Create new CCriterion where a property value equals to the NULL.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, COperator.EQ, (String) null) </pre>
     * for the String property type in this case.
     * @param property CujoProperty
     * @see #whereNotNull(org.ujorm.CujoProperty)
     * @see COperator#EQ
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> whereNull(CujoProperty<UJO,TYPE> property) {
        return new CValueCriterion<UJO>(property, COperator.EQ, (TYPE)null);
    }

    /**
     * Create new CCriterion where a property value not equals to the NULL.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, COperator.EQ, (String) null) </pre>
     * for the String property type in this case.
     * @param property CujoProperty
     * @see #whereNull(org.ujorm.CujoProperty)
     * @see COperator#NOT_EQ
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> whereNotNull(CujoProperty<UJO,TYPE> property) {
        return new CValueCriterion<UJO>(property, COperator.NOT_EQ, (TYPE)null);
    }

    /** This is an constane criterion independed on an entity.
     * The method is <strong>deprecated</strong> in the ORM, use rather a one method from
     * {@line #forAll(org.ujorm.CujoProperty) forAll} or
     * {@line #forNone(org.ujorm.CujoProperty) forNone} .
     * @see #forAll(org.ujorm.CujoProperty)
     * @see #forNone(org.ujorm.CujoProperty)
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Cujo> CCriterion<UJO> where(boolean value) {
        return (CCriterion<UJO>) (value
            ? CValueCriterion.TRUE
            : CValueCriterion.FALSE
            );
    }

    /** This is a special constant criterion independed on the property or the ujo entity. A result is the same like the parameter constant allways.
     * @param property The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @see COperator#XFIXED
     */
    public static <UJO extends Cujo> CCriterion<UJO> constant(CujoProperty<UJO,?> property, boolean constant) {
        return new CValueCriterion<UJO>(property, COperator.XFIXED, constant);
    }

    /** The method creates a new CCriterion for a native condition (called Native CCriterion) in SQL statejemt format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native CCriterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param property The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlCondition a SQL condition in the String format, the NULL value or empty string is not accepted
     * @see COperator#XSQL
     */
    public static <UJO extends Cujo> CCriterion<UJO> forSql(CujoProperty<UJO,?> property, String sqlCondition) {
        return new CValueCriterion<UJO>(property, COperator.XSQL, sqlCondition);
    }

    /** This is a constant criterion independed on the property value or the ujo entity.
     *  The method evaluate(ujo) returns TRUE always.
     * @param property The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     */
    public static <UJO extends Cujo> CCriterion<UJO> forAll(CujoProperty<UJO,?> property) {
        return constant(property, true);
    }

    /** This is a constant criterion independed on the property value or the ujo entity.
     *  The  method evaluate(method) returns FALSE always.
     * @param property The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     */
    public static <UJO extends Cujo> CCriterion<UJO> forNone(CujoProperty<UJO,?> property) {
        return constant(property, false);
    }

    // ----------------------- DEPRECATED -----------------------


    /**
     * New criterion instance
     * @param property CujoProperty
     * @param operator COperator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>CujoProperty - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     * @deprecated See the {@link CCriterion#where(org.ujorm.CujoProperty, org.ujorm.criterion.COperator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> newInstance
        ( CujoProperty<UJO,TYPE> property
        , COperator operator
        , TYPE value
        ) {
        return where(property, operator, value);
    }

    /**
     * New criterion instance
     * @param property CujoProperty
     * @param operator COperator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>CujoProperty - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     * @deprecated See the {@link CCriterion#where(org.ujorm.CujoProperty, org.ujorm.criterion.COperator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> newInstance
        ( CujoProperty<UJO,TYPE> property
        , COperator operator
        , CujoProperty<?,TYPE> value
        ) {
        return where(property, operator, value);
    }

    /**
     * New equals instance
     * @param property CujoProperty
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>CujoProperty - reference to a related entity</li>
     * </ul>
     * @return A the new immutable CCriterion
     * @deprecated See the {@link CCriterion#where(org.ujorm.CujoProperty, org.ujorm.criterion.COperator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> newInstance
        ( CujoProperty<UJO,TYPE> property
        , TYPE value
        ) {
        return where(property, value);
    }

    /**
     * New equals instance
     * @param property CujoProperty
     * @param value Value or CujoProperty can be type a direct of indirect (for a relation) property
     * @return A the new immutable CCriterion
     * @deprecated See the {@link CCriterion#where(org.ujorm.CujoProperty, org.ujorm.criterion.COperator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> newInstance
        ( CujoProperty<UJO,TYPE> property
        , CujoProperty<UJO,TYPE> value
        ) {
        return where(property, value);
    }

}
