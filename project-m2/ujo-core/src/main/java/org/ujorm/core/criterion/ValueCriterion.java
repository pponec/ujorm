/*
 *  Copyright 2007-2022 Pavel Ponec
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

package org.ujorm.core.criterion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.tools.Assert;

import java.util.Objects;



/**
 * The value criterion implementation.
 * @since 0.90
 * @author Pavel Ponec
 */
public class ValueCriterion<U> extends Criterion  {
    static final long serialVersionUID = 2017_12_04;

    /** Simple space */
    public static final char SPACE = ' ';

    /** True constant criterion */
    public static final Criterion TRUE  = new ValueCriterion<>(true);
    /** False constant criterion */
    public static final Criterion FALSE = new ValueCriterion<>(false);

    private Key<U, Object> key;
    private Operator operator;
    protected Object value;

    /** Create an Criterion constant */
    protected ValueCriterion(final boolean value) {
        this(null, Operator.XFIXED, value);
    }

    /** An undefined operator (null) is replaced by EQ. */
    protected ValueCriterion
        ( @Nullable final Key<U, ? extends Object> key
        , @Nullable final Operator operator
        , @Nullable final Key<?, Object> value) {
        this(key, operator, (Object) value);
    }

    /** An constructor for an internal cloning */
    protected ValueCriterion(@NotNull final ValueCriterion criterion) {
        this.key = criterion.key;
        this.operator = criterion.operator;
        this.value = criterion.getRightNode();
    }

    /** An undefined operator (null) is replaced by EQ. */
    protected ValueCriterion
        ( @Nullable final Key<U,? extends Object> key
        , @Nullable Operator operator
        , @Nullable Object value) {

        if (key==null) {
            value = value; // Type test for the CriterionConstant.
        }
        if (operator==null) {
            operator = Operator.EQ;  // The default operator.
        }

        // A validation test:
        switch (operator) {
            case EQUALS_CASE_INSENSITIVE:
            case STARTS:
            case STARTS_CASE_INSENSITIVE:
            case ENDS:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS:
            case CONTAINS_CASE_INSENSITIVE:
                 makeCharSequenceTest(key);
                 makeCharSequenceTest(value);
                 break;
            case IN:
            case NOT_IN:
                 makeArrayTest(value);
                 break;
            case XSQL:
                 String template = value instanceof TemplateValue
                      ? ((TemplateValue)value).getTemplate()
                      : String.valueOf(value);

                 Assert.isFalse(value==null || template.trim().isEmpty(), "Value must not be empty");
                 break;
        }

        this.key = (Key<U, Object>) key;
        this.value = value;
        this.operator = operator;
    }

    /** Returns the left node of the parent */
    @Override
    public final Key<?,?> getLeftNode() {
        return key;
    }

    /** Returns the right node of the parent */
    @Override
    public Object getRightNode() {
        return value;
    }

    /** Returns an operator */
    @Override
    public final Operator getOperator() {
        return operator;
    }

    /** Join this instance with a second criterion by an operator with a simple logical optimization. */
    @Override
    public Criterion join(final BinaryOperator operator, final Criterion criterion) {
        if (this.operator == Operator.XFIXED) {
            switch (operator) {
                case OR : return (Boolean) value ? this : criterion;
                case AND: return (Boolean) value ? criterion : this;
            }
        }
        return super.join(operator, criterion);
    }

    /** Test a value is an instance of CharSequence or a type Key is type of CharSequence.
     * If parameter is not valid than method throws Exception.
     */
    protected void makeCharSequenceTest(Object value) throws IllegalArgumentException {
        final boolean ok = value instanceof CharSequence
        || value instanceof Key
        && ((Key)value).isTypeOf(CharSequence.class);

        Assert.isTrue(ok, "Key type must be a {}", CharSequence.class);
    }

    /** Test a value is an instance of Iterable.
     * If parameter is not valid than method throws Exception.
     */
    protected final void makeArrayTest(Object value) throws IllegalArgumentException {
        Assert.isTrue(value instanceof Object[], "Value must be an Array type only");
    }

    /** This instance is an immutable implementation. */
    public ValueCriterion freeze() {
        return this;
    }

    /** Compare two object */
    @SuppressWarnings("unchecked")
    protected int compare
        ( final Comparable o1
        , final Comparable o2
    ) {
        if (o1==o2  ) { return  0; }
        if (o1==null) { return +1; }
        if (o2==null) { return -1; }
        return o1.compareTo(o2);
    }

    /** Is the operator insensitive. */
    public boolean isInsensitive() {
        switch (operator) {
            case EQUALS_CASE_INSENSITIVE:
            case STARTS_CASE_INSENSITIVE:
            case ENDS_CASE_INSENSITIVE:
            case CONTAINS_CASE_INSENSITIVE:
                 return true;
            default:
                 return false;
        }
    }

    /** Is the operator have got value XFIXED or XSQL ? */
    public final boolean isConstant() {
        return operator==Operator.XFIXED || operator==Operator.XSQL;
    }

   @Override
    public String toString() {
        final SimpleValuePrinter result = new SimpleValuePrinter(128)
                .append(getDomain().getSimpleName());
        return toPrinter(result).toString();
    }

    @Override
    public SimpleValuePrinter toPrinter(@NotNull final SimpleValuePrinter out) {
        out.append('(');
        if (operator == Operator.XSQL) {
            out.appendValue(getRightNode());
            return out.append(')');
        }
        else if (operator != Operator.XFIXED) {
            out
            .append(key)
            .append(SPACE)
            .append(operator.name())
            .append(SPACE);
        }
        out.appendValue(getRightNode());
        return out.append(')');
    }

    /** Find a domain class type of {@code Class<UJO>} from its keys.
     * @return returns Method returns the {@code Ujo.class} instance if no domain was found.
     */
    @Override
    public Class<?> getDomain() {
        final Key key = getLeftNode();
        final Class<?> result = key != null ? key.domainClass() : null;
        return result != null ? result : Objects.class;
    }


}
