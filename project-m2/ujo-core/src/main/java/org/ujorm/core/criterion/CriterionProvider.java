/*
 * Copyright 2007-2022 Pavel Ponec, https://github.com/pponec
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

import java.util.Collection;
import java.util.List;

public interface CriterionProvider<VALUE> {

    /**
     * Create a new Criterion where this key value is related to a parameter value along the {@link Operator}.
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    @NotNull Criterion where(@NotNull Operator operator, @Nullable VALUE value);

    /**
     * Create a new Criterion where this key value is related to a parameter value along the {@link Operator}.
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    @NotNull Criterion where(@NotNull Operator operator, @NotNull ProxyValue<VALUE> proxyValue);

    /**
     * Create a new Criterion where this key is related to the value along the parameter {@link Operator}.
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    @NotNull Criterion where(@NotNull Operator operator, @NotNull Key<?, VALUE> value);

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull Criterion whereIn(@NotNull Collection<VALUE> list);

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull Criterion whereNotIn(@NotNull Collection<VALUE> list);

    /** Create a new Criterion for all values. The method evaluate(ujo) always returns TRUE. */
    @NotNull Criterion whereTrue();

    /** Create a new Criterion for no values. The method evaluate(method) always returns FALSE. */
    @NotNull Criterion whereFalse();

    /**
     * Creates a new {@code Criterion} with a custom SQL template.
     * <p>
     * Supported placeholders in the template:
     * <ul>
     * <li>{@code {0}} : Replaced by the database column name (including its alias).</li>
     * <li>{@code {1}} : Replaced by the first value from the {@code values} array.</li>
     * <li>{@code {2}} : Replaced by the second value, and so on.</li>
     * <li>{@code {*}} : Replaced by all values joined by a comma.</li>
     * </ul>
     *
     * Example of use:
     * <pre>{@code
     *   employee.whereSql("UPPER({0}) = {1}", "JOE")
     *   employee.whereSql("{0} IN ({*})", 1, 2, 3)
     * }</pre>
     *
     * @param template SQL template (e.g., {@code "UPPER({0}) = {1}"})
     * @param values Optional parameters for the placeholders
     * @return A new immutable Criterion
     */
    @NotNull
    Criterion whereSql(@NotNull String template, @NotNull VALUE... values);
    // --- DEFAULT METHODS ---

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @return The new immutable Criterion
     */
    @NotNull default Criterion whereEq(@Nullable VALUE value) {
        return where(Operator.EQ, value);
    }

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @param proxyValue A function for the value where the {@code null} value is not supported in ORM.
     * @return The new immutable Criterion
     */
    @NotNull default Criterion whereEq(@NotNull ProxyValue<VALUE> proxyValue) {
        return where(Operator.EQ, proxyValue);
    }

    /**
     * Create a new Criterion where this key value equals the parameter value.
     * @param key Key can be type a direct or indirect (for a relation) key
     * @return The new immutable Criterion
     */
    @NotNull default Criterion whereEq(@NotNull Key<?, VALUE> key) {
        return where(Operator.EQ, key);
    }

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param values A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion
     */
    @SuppressWarnings("unchecked")
    @NotNull default Criterion whereIn(@NotNull final VALUE... values) {
        return whereIn(List.of(values));
    }

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param values A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @SuppressWarnings("unchecked")
    @NotNull default Criterion whereNotIn(@NotNull final VALUE... values) {
        return whereNotIn(List.of(values));
    }

    /** Create a new Criterion where this key value does not equal the value. @see Operator#NOT_EQ */
    @NotNull default Criterion whereNeq(@Nullable VALUE value) {
        return where(Operator.NOT_EQ, value);
    }

    /** Create a new Criterion where this key is greater than the value. @see Operator#GT */
    @NotNull default Criterion whereGt(@NotNull VALUE value) {
        return where(Operator.GT, value);
    }

    /** Create a new Criterion where this key is greater than or equal to the value. @see Operator#GE */
    @NotNull default Criterion whereGe(@NotNull VALUE value) {
        return where(Operator.GE, value);
    }

    /** Create a new Criterion where this key is less than the value. @see Operator#LT */
    @NotNull default Criterion whereLt(@NotNull VALUE value) {
        return where(Operator.LT, value);
    }

    /** Create a new Criterion where this key is less than or equal to the value. @see Operator#LE */
    @NotNull default Criterion whereLe(@NotNull VALUE value) {
        return where(Operator.LE, value);
    }

    /**
     * Create a new Criterion where this key is {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterion.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @see Operator#EQ
     */
    @NotNull default Criterion whereNull() {
        return where(Operator.EQ, (VALUE) null);
    }

    /**
     * Create a new Criterion where this key is not {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterion.where(Order.NOTE_PROPERTY, Operator.NOT_EQ, (String) null) </pre>
     * for the String key type in this case.
     * @see Operator#NOT_EQ
     */
    @NotNull default Criterion whereNotNull() {
        return where(Operator.NOT_EQ, (VALUE) null);
    }
}