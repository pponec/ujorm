/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.cquery;

import org.ujorm.gxt.client.Cujo;
import org.ujorm.gxt.client.CujoProperty;
import java.io.Serializable;

/**
 * An abstract criterion provides a basic interface and static factory methods. You can use it:
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
 * CCriterion&lt;Person&gt; crn1 = CCriterion.where(CASH, COperator.GT, 10.0);
 * CCriterion&lt;Person&gt; crn2 = CCriterion.where(CASH, COperator.LE, 20.0);
 * CCriterion&lt;Person&gt; criterion = crn1.and(crn2);
 *
 * <span class="comment">// Use a criterion (1):</span>
 * CriteriaTool&lt;Person&gt; ct = CriteriaTool.newInstance();
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
 * @since 0.90
 * @author Pavel Ponec
 * @composed 1 - 1 AbstractCOperator
 */
public abstract class CCriterion<UJO extends Cujo> implements Serializable {

    private static final long serialVersionUID = 42001L;
    //

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
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @return A new criterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where(CujoProperty<UJO, TYPE> property, COperator operator, TYPE value) {
        return new CValueCriterion<UJO>(property, operator, value);
    }

    /**
     * New criterion instance
     * @param property CujoProperty
     * @param operator COperator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>CujoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @return A new criterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where(CujoProperty<UJO, TYPE> property, COperator operator, CujoProperty<?, TYPE> value) {
        return new CValueCriterion<UJO>(property, operator, value);
    }

    /**
     * New equals instance
     * @param property CujoProperty
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>CujoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @return A the new CCriterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where(CujoProperty<UJO, TYPE> property, TYPE value) {
        return new CValueCriterion<UJO>(property, null, value);
    }

    /**
     * New equals instance
     * @param property CujoProperty
     * @param value Value or CujoProperty can be type a direct of indirect (for a relation) property
     * @return A the new CCriterion
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where(CujoProperty<UJO, TYPE> property, CujoProperty<UJO, TYPE> value) {
        return new CValueCriterion<UJO>(property, null, value);
    }

    /**
     * New equals instance
     * @param property CujoProperty
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>CujoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     */
    public static <UJO extends Cujo, TYPE> CCriterion<UJO> where(CujoProperty<UJO, TYPE> property) {
        return new CValueCriterion<UJO>(property, COperator.EQ, property);
    }

    /** This is a constant criterion independed on the property and the ujo entity. A result is the TRUE allways. */
    public static <UJO extends Cujo> CCriterion<UJO> constantTrue(CujoProperty<UJO, ?> property) {
        return new CValueCriterion<UJO>(property, COperator.X_FIXED, true);
    }

    /** This is a constant criterion independed on the property and the ujo entity. A result is the FALSE allways. */
    public static <UJO extends Cujo> CCriterion<UJO> constantFalse(CujoProperty<UJO, ?> property) {
        return new CValueCriterion<UJO>(property, COperator.X_FIXED, false);
    }

}
