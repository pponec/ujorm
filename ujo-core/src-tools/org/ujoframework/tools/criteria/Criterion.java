/*
 *  Copyright 2007-2008 Paul Ponec
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
   
package org.ujoframework.tools.criteria;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * An abstraction criterion provides an factory methods.
 * @author Pavel Ponec
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

    // ------ STATIC FACTORY --------

    /**
     * New criterion instance
     * @param property UjoProperty
     * @param operator Operator
     * @param value Value or UjoProperty can be type of
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @return A new criterion
     */
    public static <UJO extends Ujo, TYPE> Criterion<UJO> newInstance
        ( UjoProperty<UJO,TYPE> property
        , Operator operator
        , Object value
        ) {
        return new ValueCriterion<UJO>(property, operator, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * @param value Value or UjoProperty can be type of
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @return A the new Criterion
     */
    public static <UJO extends Ujo, TYPE> Criterion<UJO> newInstance
        ( UjoProperty<UJO,TYPE> property
        , Object value
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
    public static <UJO extends Ujo, TYPE> Criterion<UJO> newInstance(UjoProperty<UJO,TYPE> property) {
        return new ValueCriterion<UJO>(property, Operator.EQ, property);
    }

    /** This is an constane criterion independed on an entity.
     * It is recommended not to use this solution in ORM.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> Criterion<UJO> newInstance(boolean value) {
        return (Criterion<UJO>) (value
            ? ValueCriterion.TRUE
            : ValueCriterion.FALSE
            );
    }

    /** This is a constant criterion independed on the property and the ujo entity. A result is the TRUE allways. */
    public static <UJO extends Ujo> Criterion<UJO> newInstanceTrue(UjoProperty<UJO,?> property) {
        return new ValueCriterion<UJO>(property, Operator.€_FIXED, true);
    }

    /** This is a constant criterion independed on the property and the ujo entity. A result is the FALSE allways. */
    public static <UJO extends Ujo> Criterion<UJO> newInstanceFalse(UjoProperty<UJO,?> property) {
        return new ValueCriterion<UJO>(property, Operator.€_FIXED, false);
    }

    /** Is a Binary criterion? */
    public boolean isBinary() {
        return false;
    }
}
