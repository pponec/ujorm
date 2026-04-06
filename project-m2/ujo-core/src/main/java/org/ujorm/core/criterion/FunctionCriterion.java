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

import java.util.Objects;
import java.util.function.Supplier;

/**
 * The criterion for a value function where a lambda expression is supported.
 * @since 1.76
 * @author Pavel Ponec
 */
public final class FunctionCriterion<U, T> extends ValueCriterion<U> {

    /**
     * Common constructor
     * @param key Key
     * @param operator Value operator
     * @param proxyValue An function for the value where the {@code null} value is not supported in ORM. The class should be serialized.
     */
    FunctionCriterion(
            @NotNull final Key<U, ?> key,
            @NotNull final Operator operator,
            @NotNull final Supplier<T> proxyValue) {
        super(key, operator, Objects.requireNonNull(proxyValue, "proxyValue"));
        switch (operator) {
            case ALWAYS_TRUE, ALWAYS_FALSE -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    /** Returns the right node of the parent. */
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public T getRightNode() {
        return ((Supplier<T>) super.value).get();
    }

    /** Test a value is an instance of clazz and */
    @Override
    protected void checkType(Class<?> clazz, Key<?,?> key, Object valueSup) throws IllegalArgumentException {
        if (valueSup instanceof Supplier<?> supplier) {
            super.checkType(clazz, key, supplier.get());
        } else {
            var msg = "Only argument type of %s is supported"
                    .formatted(Supplier.class.getSimpleName());
            throw new IllegalArgumentException(msg);
        }
    }

    /** Freeze the criterion to an immutable implementation. */
    @Override
    public ValueCriterion<U> freeze() {
        return new ValueCriterion<>(this);
    }

}