/*
 * Copyright 2007-2022 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

public interface CriterionProvider<DOMAIN, VALUE> {

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
    Criterion where
        ( @NotNull Operator operator
        , @Nullable VALUE value
        );

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
    @NotNull Criterion where
        ( @NotNull Operator operator
        , @NotNull ProxyValue<VALUE> proxyValue
        );

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
    @NotNull Criterion where
        ( @NotNull Operator operator
        , @NotNull Key<?,VALUE> value
        );

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @return The new immutable Criterion
     */
    @NotNull Criterion whereEq(@Nullable VALUE value);

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @param proxyValue An function for the value where the {@null} value is not supported in ORM.
     * @return The new immutable Criterion
     */
    @NotNull Criterion whereEq(@NotNull ProxyValue<VALUE> proxyValue);

    /**
     * Create a new Criterion where this key value equals the parameter value.
     * @param key Key can be type a direct of indirect (for a relation) key
     * @return The new immutable Criterion
     */
    @NotNull Criterion whereEq(@NotNull Key<?,VALUE> key);

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull Criterion whereIn
        ( @NotNull Collection<VALUE> list
        );

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull Criterion whereNotIn
        ( @NotNull Collection<VALUE> list
        );

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion
     */
    @NotNull Criterion whereIn
        ( @NotNull VALUE... list
        );

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull Criterion whereNotIn
        ( @NotNull VALUE... list
        );

    /** Create a new Criterion where this key value is not equals the value
     * @see Operator#NOT_EQ */
    @NotNull Criterion whereNeq(@Nullable VALUE value);

    /** Create a new Criterion where this key is great then the value
     * @see Operator#GT */
    Criterion whereGt(@NotNull VALUE value);

    /** Create a new Criterion where this key is great or equals the value
     * @see Operator#GE */
    Criterion whereGe(@NotNull VALUE value);

    /** Create a new Criterion where this key is less then the value
     * @see Operator#LT */
    Criterion whereLt(@NotNull VALUE value);

    /** Create a new Criterion where this key is less or equals than the value
     * @see Operator#LE */
    Criterion whereLe(@NotNull VALUE value);

    /**
     * Create a new Criterion where this key is {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @see Operator#EQ
     */
    @NotNull Criterion whereNull();

    /**
     * Create a new Criterion where this key is not {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @see Operator#NOT_EQ
     */
    @NotNull Criterion whereNotNull();

    /** Create a new Criterion for all values.
     *  The method evaluate(ujo) returns TRUE always.
     */
    @NotNull Criterion whereAll();

    /** Create a new Criterion for none values.
     *  The  method evaluate(method) returns FALSE always.
     */
    @NotNull Criterion whereNone();

}
