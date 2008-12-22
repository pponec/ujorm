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

/**
 *
 * @author pavel
 */
public class ExpressionBinary<UJO extends Ujo> implements Expression<UJO> {

    final private Expression<UJO> expr1;
    final private Expression<UJO> expr2;
    final private OperatorBinary operator;
    
    public ExpressionBinary
        ( final Expression<UJO> expr1
        , final OperatorBinary operator
        , final Expression<UJO> expr2
        ) {
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.operator = operator;
    }

    public boolean evaluate(UJO ujo) {
        boolean e1 = expr1.evaluate(ujo);
        switch (operator) {
            case AND    : return e1 &&  expr2.evaluate(ujo);
            case AND_NOT: return e1 && !expr2.evaluate(ujo);
            case OR     : return e1 ||  expr2.evaluate(ujo); 
            case OR_NOT : return e1 || !expr2.evaluate(ujo); 
            default:  
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    public Expression<UJO> join(OperatorBinary operator, Expression<UJO> expr) {
        return new ExpressionBinary<UJO>(this, operator, expr);
    }

    @Override
    public String toString() {
        return "{" + expr1 + ") " + operator.name() + " (" + expr2 + ")";
    }
    
    
}
