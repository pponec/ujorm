/*
 * Copyright 2007-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.core.criterion;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.tools.Assert;
import org.ujorm.tools.common.Array;

import java.util.Objects;

/**
 * The value criterion implementation.
 * @since 0.90
 * @author Pavel Ponec
 */
public class ValueCriterion<U> extends Criterion  {

    /** Simple space */
    public static final char SPACE = ' ';

    /** True constant criterion */
    public static final Criterion TRUE  = new ValueCriterion<>(true);

    /** False constant criterion */
    public static final Criterion FALSE = new ValueCriterion<>(false);

    private final Key<U, Object> key;
    private final Operator operator;
    protected final Object value;

    /** Create an Criterion constant */
    protected ValueCriterion(final boolean value) {
        this(null, value ? Operator.ALWAYS_TRUE : Operator.ALWAYS_FALSE, value);
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

        if (operator == null) {
            operator = Operator.EQ;  // The default operator.
        }

        // Validation tests:
        switch (operator) {
            case EQUALS_CASE_INSENSITIVE,
                 STARTS,
                 STARTS_CASE_INSENSITIVE,
                 ENDS,
                 ENDS_CASE_INSENSITIVE,
                 CONTAINS,
                 CONTAINS_CASE_INSENSITIVE -> checkType(String.class, key, value);
            case IN, NOT_IN -> checkArray(value);
            case CUSTOM_SQL -> Assert.isTrue(value instanceof TemplateValue,
                    () -> "%s is expected".formatted(TemplateValue.class));
        }

        this.key = (Key<U, Object>) key;
        this.value = value;
        this.operator = operator;
    }

    /** Returns the left node of the parent */
    @Override
    public final @NotNull Key<?,?> getLeftNode() {
        return key;
    }

    /** Returns the right node of the parent */
    @Override
    public Object getRightNode() {
        return value;
    }

    /** Returns an operator */
    @Override
    public final @NotNull Operator getOperator() {
        return operator;
    }

    /** Join this instance with a second criterion by an operator with a simple logical optimization. */
    @Override
    public Criterion join(final BinaryOperator operator, final Criterion criterion) {
        return switch (this.operator) {
            case ALWAYS_TRUE, ALWAYS_FALSE -> switch (operator) {
                case OR -> this.operator == Operator.ALWAYS_TRUE ? this : criterion;
                case AND -> this.operator == Operator.ALWAYS_TRUE ? criterion : this;
                default -> super.join(operator, criterion);
            };
            default -> super.join(operator, criterion);
        };
    }

    /** Test a value is an instance of clazz and */
    protected void checkType(Class<?> clazz, Key<?,?> key, Object value) throws IllegalArgumentException {
        if (!key.isTypeOf(clazz)) {
            throw new IllegalArgumentException("The Key must be type of " + clazz.getSimpleName());
        }
        if (value != null && !key.isInstanceOf(value)) {
            throw new IllegalArgumentException("The Value must be type of " + key.type().getSimpleName());
        }
    }

    /**
     * Test a value is an instance of Iterable.
     * If parameter is not valid than method throws Exception.
     */
    protected final void checkArray(Object value) throws IllegalArgumentException {
        Assert.isTrue(value instanceof Array<?>, "Value must be an Array type");
    }

    /** This instance is an immutable implementation. */
    public ValueCriterion freeze() {
        return this;
    }

    /** Compare two object */
    @SuppressWarnings("unchecked")
    protected int compare(final Comparable o1, final Comparable o2) {
        if (o1 == o2  ) { return 0; }
        if (o1 == null) { return +1; }
        if (o2 == null) { return -1; }
        return o1.compareTo(o2);
    }

    /** Is the operator insensitive. */
    public boolean isInsensitive() {
        return switch (operator) {
            case EQUALS_CASE_INSENSITIVE,
                 STARTS_CASE_INSENSITIVE,
                 ENDS_CASE_INSENSITIVE,
                 CONTAINS_CASE_INSENSITIVE -> true;
            default -> false;
        };
    }

    /** Is the operator have got value ALWAYS_TRUE, ALWAYS_FALSE or CUSTOM_SQL  */
    public final boolean isConstant() {
        return switch (operator) {
            case ALWAYS_TRUE, ALWAYS_FALSE, CUSTOM_SQL -> true;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return toPrinter(new SimpleValuePrinter(128).append(getDomain().getSimpleName())).toString();
    }

    @Override
    public SimpleValuePrinter toPrinter(@NotNull final SimpleValuePrinter writer) {
        writer.append('(');
        if (operator == Operator.CUSTOM_SQL) {
            writer.appendValue(getRightNode());
            return writer.append(')');
        }
        if (operator != Operator.ALWAYS_TRUE && operator != Operator.ALWAYS_FALSE) {
            writer
                    .append(key)
                    .append(SPACE)
                    .append(operator.name())
                    .append(SPACE);
        }
        writer.appendValue(getRightNode());
        return writer.append(')');
    }

    /** Find a domain class type of {@code Class<UJO>} from its keys. */
    @Override
    public @NotNull Class<?> getDomain() {
        var keyNode = getLeftNode();
        return keyNode != null ? keyNode.domainClass() : Objects.class;
    }
}