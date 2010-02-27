/*
 *  Copyright 2007-2010 Pavel Ponec
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
   
package org.ujoframework.criterion;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * An abstract criterion provides a basic interface and static factory methods. You can use it:
 * <ul>
 *    <li>like a generic UJO object validator (2)</li>
 *    <li>to create a query on the UJO list (1)</li>
 *    <li>the class is used to build 'SQL query' in the module </strong>ujo-orm</strong> (sience 0.90)</li>
 * </ul>
 *
 * There is allowed to join two instances (based on the same BO) to a binary tree by a new Criterion.
 * Some common operators (and, or, not) are implemeted into a special join method of the Criteron class.
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
public abstract class Criterion<UJO extends Ujo> {
    
    public abstract boolean evaluate(UJO ujo);
    
    public Criterion<UJO> join(BinaryOperator operator, Criterion<UJO> criterion) {
        return new BinaryCriterion<UJO>(this, operator, criterion);
    }

    public Criterion<UJO> and(Criterion<UJO> criterion) {
        return join(BinaryOperator.AND, criterion);
    }

    public Criterion<UJO> or(Criterion<UJO> criterion) {
        return join(BinaryOperator.OR, criterion);
    }

    public Criterion<UJO> not() {
        return new BinaryCriterion<UJO>(this, BinaryOperator.NOT, this);
    }

    /** Returns the left node of the parrent */
    abstract public Object getLeftNode();
    /** Returns the right node of the parrent */
    abstract public Object getRightNode();
    /** Returns an operator */
    abstract public AbstractOperator getOperator();


    /** Is the class a Binary criterion? */
    public boolean isBinary() {
        return false;
    }

    // ------ STATIC FACTORY --------

    /**
     * New criterion instance
     * @param property UjoProperty
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * <li>THE SAME property - the value will be assigned using the property later (TODO in future)</li>
     * </ul>
     * @return A new criterion
     */
    public static <UJO extends Ujo, TYPE> Criterion<UJO> where
        ( UjoProperty<UJO,TYPE> property
        , Operator operator
        , TYPE value
        ) {
        return new ValueCriterion<UJO>(property, operator, value);
    }

    /**
     * New criterion instance
     * @param property UjoProperty
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * <li>THE SAME property - the value will be assigned using the property later (TODO in future)</li>
     * </ul>
     * @return A new criterion
     */
    public static <UJO extends Ujo, TYPE> Criterion<UJO> where
        ( UjoProperty<UJO,TYPE> property
        , Operator operator
        , UjoProperty<?,TYPE> value
        ) {
        return new ValueCriterion<UJO>(property, operator, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @return A the new Criterion
     */
    public static <UJO extends Ujo, TYPE> Criterion<UJO> where
        ( UjoProperty<UJO,TYPE> property
        , TYPE value
        ) {
        return new ValueCriterion<UJO>(property, null, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * @param value Value or UjoProperty can be type a direct of indirect (for a relation) property
     * @return A the new Criterion
     */
    public static <UJO extends Ujo, TYPE> Criterion<UJO> where
        ( UjoProperty<UJO,TYPE> property
        , UjoProperty<UJO,TYPE> value
        ) {
        return new ValueCriterion<UJO>(property, null, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     */
    public static <UJO extends Ujo, TYPE> Criterion<UJO> where(UjoProperty<UJO,TYPE> property) {
        return new ValueCriterion<UJO>(property, Operator.EQ, property);
    }

    /** This is an constane criterion independed on an entity.
     * It is recommended not to use this solution in ORM.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> Criterion<UJO> where(boolean value) {
        return (Criterion<UJO>) (value
            ? ValueCriterion.TRUE
            : ValueCriterion.FALSE
            );
    }

    /** This is a constant criterion independed on the property and the ujo entity. A result is the TRUE always. */
    public static <UJO extends Ujo> Criterion<UJO> whereTrue(UjoProperty<UJO,?> property) {
        return new ValueCriterion<UJO>(property, Operator.X_FIXED, true);
    }

    /** This is a constant criterion independed on the property and the ujo entity. A result is the FALSE always. */
    public static <UJO extends Ujo> Criterion<UJO> whereFalse(UjoProperty<UJO,?> property) {
        return new ValueCriterion<UJO>(property, Operator.X_FIXED, false);
    }

    // ----------------------- DEPRECATED -----------------------


    /**
     * New criterion instance
     * @param property UjoProperty
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * <li>THE SAME property - the value will be assigned using the property later (TODO in future)</li>
     * </ul>
     * @return A new criterion
     * @deprecated See the {@link Criterion#where(org.ujoframework.UjoProperty, org.ujoframework.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Ujo, TYPE> Criterion<UJO> newInstance
        ( UjoProperty<UJO,TYPE> property
        , Operator operator
        , TYPE value
        ) {
        return where(property, operator, value);
    }

    /**
     * New criterion instance
     * @param property UjoProperty
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * <li>THE SAME property - the value will be assigned using the property later (TODO in future)</li>
     * </ul>
     * @return A new criterion
     * @deprecated See the {@link Criterion#where(org.ujoframework.UjoProperty, org.ujoframework.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Ujo, TYPE> Criterion<UJO> newInstance
        ( UjoProperty<UJO,TYPE> property
        , Operator operator
        , UjoProperty<?,TYPE> value
        ) {
        return where(property, operator, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @return A the new Criterion
     * @deprecated See the {@link Criterion#where(org.ujoframework.UjoProperty, org.ujoframework.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Ujo, TYPE> Criterion<UJO> newInstance
        ( UjoProperty<UJO,TYPE> property
        , TYPE value
        ) {
        return where(property, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * @param value Value or UjoProperty can be type a direct of indirect (for a relation) property
     * @return A the new Criterion
     * @deprecated See the {@link Criterion#where(org.ujoframework.UjoProperty, org.ujoframework.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Ujo, TYPE> Criterion<UJO> newInstance
        ( UjoProperty<UJO,TYPE> property
        , UjoProperty<UJO,TYPE> value
        ) {
        return where(property, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @deprecated See the {@link Criterion#where(org.ujoframework.UjoProperty, org.ujoframework.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Ujo, TYPE> Criterion<UJO> newInstance(UjoProperty<UJO,TYPE> property) {
        return where(property);
    }

    /** This is an constane criterion independed on an entity.
     * It is recommended not to use this solution in ORM.
     * @deprecated See the {@link Criterion#where(org.ujoframework.UjoProperty, org.ujoframework.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <UJO extends Ujo> Criterion<UJO> newInstance(boolean value) {
        return where(value);
    }

    /** This is a constant criterion independed on the property and the ujo entity. A result is the TRUE always.
     * @deprecated See the {@link Criterion#where(org.ujoframework.UjoProperty, org.ujoframework.criterion.Operator, java.lang.Object) where(...) } method.
         */
    @Deprecated
    public static <UJO extends Ujo> Criterion<UJO> newInstanceTrue(UjoProperty<UJO,?> property) {
        return whereTrue(property);
    }

    /** This is a constant criterion independed on the property and the ujo entity. A result is the FALSE always.
     * @param <UJO>
     * @param property
     * @return
     * @deprecated See the {@link Criterion#where(org.ujoframework.UjoProperty, org.ujoframework.criterion.Operator, java.lang.Object) where(...) } method.
     */
    @Deprecated
    public static <UJO extends Ujo> Criterion<UJO> newInstanceFalse(UjoProperty<UJO,?> property) {
        return whereFalse(property);
    }


}
