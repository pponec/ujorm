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
 * An expression
 * @author pavel
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
        return new ExpressionNot<UJO>(this);
    }

    // ------ STATIC FACTORY --------

    /**
     * New expression instance
     * @param property UjoProperty
     * @param operator Operator
     * @param value Value or UjoProperty
     * @return
     */
    public static <UJO extends Ujo, TYPE> Expression<UJO> newInstance(UjoProperty<UJO,?> property, Operator operator, Object value) {
        return new ExpressionValue<UJO>(property, operator, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * @param value Value or UjoProperty
     * @return
     */
    public static <UJO extends Ujo, TYPE> Expression<UJO> newInstance(UjoProperty<UJO,?> property, Object value) {
        return new ExpressionValue<UJO>(property, Operator.EQ, value);
    }

    /**
     * New equals instance
     * @param property UjoProperty
     * @param value Value or UjoProperty
     * @return
     */
    public static <UJO extends Ujo, TYPE> Expression<UJO> newInstance(UjoProperty<UJO,?> property) {
        return new ExpressionValue<UJO>(property, Operator.EQ, property);
    }



    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> Expression<UJO> newInstance(boolean value) {
        return (Expression<UJO>) (value 
            ? ConstantExpression.TRUE
            : ConstantExpression.FALSE
            );
    }

}
