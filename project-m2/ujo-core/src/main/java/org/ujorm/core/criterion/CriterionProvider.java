/*
 * Copyright 2007-2026 Pavel Ponec, https://github.com/pponec
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
import org.ujorm.tools.common.Array;
import java.util.Collection;

public interface CriterionProvider<DOMAIN, VALUE> {

    /** Returns a current instance of the Key for the default Criterion implementations */
    @NotNull Key<DOMAIN, VALUE> self();

    /**
     * Create a new Criterion where this key value is related to a parameter value along the {@link Operator}.
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * </ul>
     * @return A new criterion
     */
    @NotNull default Criterion where(@NotNull Operator operator, @Nullable VALUE value) {
        return Criterion.where(self(), operator, value);
    }

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
    @NotNull default Criterion where(@NotNull Operator operator, @NotNull ProxyValue<VALUE> proxyValue) {
        return Criterion.whereProxy(self(), operator, proxyValue);
    }

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
    @NotNull default Criterion where(@NotNull Operator operator, @NotNull Key<?, VALUE> value) {
        return Criterion.whereKey(self(), operator, value);
    }

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param array A collection of the values. If the argument is EMPTY, the result is always FALSE.
     * @return The new immutable Criterion.
     */
    @NotNull default Criterion whereIn(@NotNull Array<VALUE> array) {
        return Criterion.whereIn(true, self(), array);
    }

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param array A collection of the values. If the argument is EMPTY, the result is always TRUE.
     * @return The new immutable Criterion.
     */
    @NotNull default Criterion whereNotIn(@NotNull Array<VALUE> array) {
        return Criterion.whereIn(false, self(), array);
    }

    /**
     * Create new Criterion where this key value is in one of parameter values.
     * @param values A collection of values. If the argument is EMPTY, the result is always FALSE.
     * @return The new immutable Criterion.
     */
    @NotNull default Criterion whereIn(@NotNull Collection<? extends VALUE> values) {
        return Criterion.whereInCollection(true, self(), values);
    }

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param values A collection of values. If the argument is EMPTY, the result is always TRUE.
     * @return The new immutable Criterion.
     */
    @NotNull default Criterion whereNotIn(@NotNull Collection<? extends VALUE> values) {
        return Criterion.whereInCollection(false, self(), values);
    }

    /** Create a new Criterion for all values. The method evaluate(ujo) always returns TRUE. */
    @NotNull default Criterion whereTrue() {
        return Criterion.forConstant(self(), true);
    }

    /** Create a new Criterion for no values. The method evaluate(method) always returns FALSE. */
    @NotNull default Criterion whereFalse() {
        return  Criterion.forConstant(self(), false);
    }

    /**
     * Creates a new {@code Criterion} with a custom SQL template.
     * <p>
     * Supported placeholders in the template:
     * <ul>
     * <li>{@code {0}} : Replaced by the database column name (including its alias).</li>
     * <li>{@code {1}} : Replaced by all values joined by a comma.</li>
     * </ul>
     *
     * Example of use:
     * <pre>{@code
     *   employee.whereSql("UPPER({0}) = {1}")
     *   employee.whereSql("{0} IN ({1})")
     * }</pre>
     *
     * @param template SQL template (e.g., {@code "UPPER({0}) = {1}"})
     * @param values Optional parameters for the placeholders
     * @return A new immutable Criterion
     */
    @NotNull
    default Criterion whereSql(@NotNull String template, @NotNull VALUE... values) {
        return  Criterion.forSql(self(), template, values);
    }

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
        return Criterion.whereInArray(true, self(), values);
    }

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param values A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @SuppressWarnings("unchecked")
    @NotNull default Criterion whereNotIn(@NotNull final VALUE... values) {
        return Criterion.whereInArray(false, self(), values);
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