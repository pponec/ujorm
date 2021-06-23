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

package org.ujorm2.criterion;

import javax.annotation.Nonnull;

/**
 * The BinaryCriterion implementation allows to join two another Criterions into the binary tree.
 * @since 0.90
 * @author Pavel Ponec
 */
public class BinaryCriterion<D> extends Criterion<D> {

    private static final char SPACE = ' ';

    /** Left Criterion */
    final private Criterion<D> crn1;
    /** Right Criterion */
    final private Criterion<D> crn2;
    /** Operator */
    final private BinaryOperator operator;
    /** A domain class of sub Criterions like a backup */
    private Class<?> domain;

    protected BinaryCriterion
        ( final Criterion<D> criterion1
        , final BinaryOperator operator
        , final Criterion<D> criterion2
        ) {
        this.crn1 = criterion1;
        this.crn2 = criterion2;
        this.operator = operator;
    }

    /** Returns the left node of the parent */
    @Override
    public final Criterion<D> getLeftNode() {
        return crn1;
    }

    /** Returns the right node of the parent */
    @Override
    public final Criterion<D> getRightNode() {
        return crn2;
    }

    /** Returns an operator */
    @Override
    public final BinaryOperator getOperator() {
        return operator;
    }

    @Override
    public boolean evaluate(D ujo) {
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
        return toPrinter(new SimpleValuePrinter(256)).toString();
    }

    /** Print the condition in a human reading format. */
    @Override
    public SimpleValuePrinter toPrinter(@Nonnull SimpleValuePrinter out) {
            throw new UnsupportedOperationException("TODO");

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
