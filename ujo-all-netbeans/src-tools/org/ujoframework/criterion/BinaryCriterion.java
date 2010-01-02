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

/**
 * The BinaryCriterion implementation allows to join two another Criterions into the binary tree.
 * @since 0.90
 * @author Pavel Ponec
 */
public class BinaryCriterion<UJO extends Ujo> extends Criterion<UJO> {

    final private Criterion<UJO> crn1;
    final private Criterion<UJO> crn2;
    final private BinaryOperator operator;
    
    public BinaryCriterion
        ( final Criterion<UJO> criterion1
        , final BinaryOperator operator
        , final Criterion<UJO> criterion2
        ) {
        this.crn1 = criterion1;
        this.crn2 = criterion2;
        this.operator = operator;
    }

    /** Returns the left node of the parrent */
    @Override
    public final Criterion<UJO> getLeftNode() {
        return crn1;
    }

    /** Returns the right node of the parrent */
    @Override
    public final Criterion<UJO> getRightNode() {
        return crn2;
    }

    /** Returns an operator */
    @Override
    public final BinaryOperator getOperator() {
        return operator;
    }

    public boolean evaluate(UJO ujo) {
        boolean e1 = crn1.evaluate(ujo);
        switch (operator) {
            case AND    : return   e1 &&  crn2.evaluate(ujo);
            case OR     : return   e1 ||  crn2.evaluate(ujo);
            case XOR    : return   e1 !=  crn2.evaluate(ujo);
            case NAND   : return !(e1 &&  crn2.evaluate(ujo));
            case NOR    : return !(e1 ||  crn2.evaluate(ujo));
            case EQ     : return   e1 ==  crn2.evaluate(ujo) ;
            case NOT    : return  !e1 ;
            default:  
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    /** Is it a Binary Criterion */
    @Override
    final public boolean isBinary() {
        return true;
    }

    @Override
    public String toString() {
        return "(" + crn1 + ") " + operator.name() + " (" + crn2 + ")";
    }
    
    
}
