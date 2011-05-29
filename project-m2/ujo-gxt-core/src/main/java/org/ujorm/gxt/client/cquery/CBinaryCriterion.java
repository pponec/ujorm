/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client.cquery;

import org.ujorm.gxt.client.Cujo;
import java.io.Serializable;

/**
 * The CBinaryCriterion implementation allows to join two another Criterions into the binary tree.
 * @since 0.90
 * @author Pavel Ponec
 */
public class CBinaryCriterion<UJO extends Cujo> extends CCriterion<UJO> implements Serializable {

    private static final long serialVersionUID = 42003L;
    //
    private CCriterion<UJO> crn1;
    private CCriterion<UJO> crn2;
    private CBinaryOperator operator;

    protected CBinaryCriterion() {
    }

    public CBinaryCriterion
        ( final CCriterion<UJO> criterion1
        , final CBinaryOperator operator
        , final CCriterion<UJO> criterion2
        ) {
        this.crn1 = criterion1;
        this.crn2 = criterion2;
        this.operator = operator;
    }

    /** Returns the left node of the parrent */
    @Override
    public final CCriterion<UJO> getLeftNode() {
        return crn1;
    }

    /** Returns the right node of the parrent */
    @Override
    public final CCriterion<UJO> getRightNode() {
        return crn2;
    }

    /** Returns an operator */
    @Override
    public final CBinaryOperator getOperator() {
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

    /** Is it a Binary CCriterion */
    @Override
    final public boolean isBinary() {
        return true;
    }

    @Override
    public String toString() {
        return "(" + crn1 + ") " + operator.name() + " (" + crn2 + ")";
    }

    @Override
    public void restore(Class<UJO> ujoType) {
        crn1.restore(ujoType);
        if (crn2!=null) {
           crn2.restore(ujoType);
        }
    }
    
    
}
