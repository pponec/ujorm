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
 * @author Pavel Ponec
 */
public class ExpressionBinary<UJO extends Ujo> extends Expression<UJO> {

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

    /** Returns the left node of the parrent */
    @Override
    public final Expression<UJO> getLeftNode() {
        return expr1;
    }

    /** Returns the right node of the parrent */
    @Override
    public final Expression<UJO> getRightNode() {
        return expr2;
    }

    /** Returns an operator */
    @Override
    public final OperatorBinary getOperator() {
        return operator;
    }

    public boolean evaluate(UJO ujo) {
        boolean e1 = expr1.evaluate(ujo);
        switch (operator) {
            case AND    : return   e1 &&  expr2.evaluate(ujo);
            case OR     : return   e1 ||  expr2.evaluate(ujo);
            case XOR    : return   e1 !=  expr2.evaluate(ujo);
            case NAND   : return !(e1 &&  expr2.evaluate(ujo));
            case NOR    : return !(e1 ||  expr2.evaluate(ujo));
            case EQ     : return   e1 ==  expr2.evaluate(ujo) ;
            case NOT    : return  !e1 ;
            default:  
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    /** Is it a Binary Expression */
    @Override
    final public boolean isBinary() {
        return true;
    }

    @Override
    public String toString() {
        return "{" + expr1 + ") " + operator.name() + " (" + expr2 + ")";
    }
    
    
}
