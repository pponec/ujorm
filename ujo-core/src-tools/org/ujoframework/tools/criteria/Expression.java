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
 * An abstraction expression provides an factory methods.
 * @author Ponec
 */
public abstract class Expression<UJO extends Ujo> {
    
    public abstract boolean evaluate(UJO ujo);
    
    public Expression<UJO> join(OperatorBinary operator, Expression<UJO> expr) {
        return new ExpressionBinary<UJO>(this, operator, expr);
    }

    public Expression<UJO> and(Expression<UJO> expr) {
        return join(OperatorBinary.AND, expr);
    }

    public Expression<UJO> or(Expression<UJO> expr) {
        return join(OperatorBinary.OR, expr);
    }

    public Expression<UJO> not() {
        return new ExpressionBinary<UJO>(this, OperatorBinary.NOT, this);
    }

    /** Returns the left node of the parrent */
    abstract public Object getLeftNode();
    /** Returns the right node of the parrent */
    abstract public Object getRightNode();
    /** Returns an operator */
    abstract public AbstractOperator getOperator();

    // ------ STATIC FACTORY --------

    /**
     * New expression instance
     * @param property UjoProperty
     * @param operator Operator
     * @param value Value or UjoProperty can be type of
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>UjoProperty - reference to a related entity</li>
     * <li>THE SAME property - the value will be assigned using the property later</li>
     * </ul>
     * @return A new expression
     */
    public static <UJO extends Ujo, TYPE> Expression<UJO> newInstance(UjoProperty<UJO,TYPE> property, Operator operator, Object value) {
        return new ExpressionValue<UJO>(property, operator, value);
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
     * @return A new expression
     */
    public static <UJO extends Ujo, TYPE> Expression<UJO> newInstance(UjoProperty<UJO,TYPE> property, Object value) {
        return new ExpressionValue<UJO>(property, null, value);
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
    public static <UJO extends Ujo, TYPE> Expression<UJO> newInstance(UjoProperty<UJO,TYPE> property) {
        return new ExpressionValue<UJO>(property, Operator.EQ, property);
    }

    /** This is an constane expression independed on an entity. */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> Expression<UJO> newInstance(boolean value) {
        return (Expression<UJO>) (value 
            ? ExpressionValue.TRUE
            : ExpressionValue.FALSE
            );
    }

    /** Is a Binary expression? */
    public boolean isBinary() {
        return false;
    }
}
