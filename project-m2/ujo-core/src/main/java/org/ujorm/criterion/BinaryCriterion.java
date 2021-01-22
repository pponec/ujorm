/*
 *  Copyright 2007-2014 Pavel Ponec
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

package org.ujorm.criterion;

import javax.annotation.Nonnull;
import org.ujorm.Ujo;
import static org.ujorm.core.UjoTools.SPACE;

/**
 * The BinaryCriterion implementation allows to join two another Criterions into the binary tree.
 * @since 0.90
 * @author Pavel Ponec
 */
public class BinaryCriterion<UJO extends Ujo> extends Criterion<UJO> {

    /** Left Criterion */
    final private Criterion<UJO> crn1;
    /** Right Criterion */
    final private Criterion<UJO> crn2;
    /** Operator */
    final private BinaryOperator operator;
    /** A domain class of sub Criterions like a backup */
    private Class<?> domain;

    protected BinaryCriterion
        ( final Criterion<UJO> criterion1
        , final BinaryOperator operator
        , final Criterion<UJO> criterion2
        ) {
        this.crn1 = criterion1;
        this.crn2 = criterion2;
        this.operator = operator;
    }

    /** Returns the left node of the parent */
    @Override
    public final Criterion<UJO> getLeftNode() {
        return crn1;
    }

    /** Returns the right node of the parent */
    @Override
    public final Criterion<UJO> getRightNode() {
        return crn2;
    }

    /** Returns an operator */
    @Override
    public final BinaryOperator getOperator() {
        return operator;
    }

    @Override
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
    public final boolean isBinary() {
        return true;
    }

    /** Print the condition in a human reading format. */
    @Override
    public String toString() {
        final SimpleValuePrinter result = new SimpleValuePrinter(128)
                .append(getDomain().getSimpleName());
        return toPrinter(result).toString();
    }

    /** Print the condition in a human reading format. */
    @Override
    public SimpleValuePrinter toPrinter(@Nonnull SimpleValuePrinter out) {
        final boolean parentheses = operator != BinaryOperator.AND;
        final boolean notOperator = operator == BinaryOperator.NOT;
        if ( parentheses) out.append('(');
        if (!notOperator) crn1.toPrinter(out).append(SPACE);
        out.append(operator.name()).append(SPACE);
        crn2.toPrinter(out);
        if (parentheses) out.append(')');
        return out;
    }

    /** Find a domain class type of {@code Class<UJO>} from its keys.
     * @return returns Method returns the {@code Ujo.class} instance if no domain was found.
     */
    @Override
    public Class<?> getDomain() {
        if (domain == null) {
            final Class<?> c1 = getLeftNode().getDomain();
            final Class<?> c2 = getRightNode().getDomain();
            domain = c2.isAssignableFrom(c1) ? c1 : c2;
        }
        return domain;
    }
}
