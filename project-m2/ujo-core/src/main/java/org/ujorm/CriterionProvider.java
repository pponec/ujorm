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

package org.ujorm;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.criterion.*;

public interface CriterionProvider<U extends Ujo, VALUE> {

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
    Criterion<U> where
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
     * @see proxyValue
     */
    @NotNull Criterion<U> where
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
    @NotNull Criterion<U> where
        ( @NotNull Operator operator
        , @NotNull Key<?,VALUE> value
        );

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @return The new immutable Criterion
     */
    @NotNull Criterion<U> whereEq(@Nullable VALUE value);

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @param proxyValue An function for the value where the {@null} value is not supported in ORM.
     * @return The new immutable Criterion
     * @see SerialSupplier
     */
    @NotNull Criterion<U> whereEq(@NotNull ProxyValue<VALUE> proxyValue);

    /**
     * Create a new Criterion where this key value equals the parameter value.
     * @param key Key can be type a direct of indirect (for a relation) key
     * @return The new immutable Criterion
     */
    @NotNull Criterion<U> whereEq(@NotNull Key<U,VALUE> key);

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull Criterion<U> whereIn
        ( @NotNull Collection<VALUE> list
        );

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull Criterion<U> whereNotIn
        ( @NotNull Collection<VALUE> list
        );

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion
     */
    @NotNull Criterion<U> whereIn
        ( @NotNull VALUE... list
        );

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @NotNull Criterion<U> whereNotIn
        ( @NotNull VALUE... list
        );

    /** Create a new Criterion where this key value is not equals the value
     * @see org.ujorm.criterion.Operator#NOT_EQ */
    @NotNull Criterion<U> whereNeq(@Nullable VALUE value);

    /** Create a new Criterion where this key is great then the value
     * @see org.ujorm.criterion.Operator#GT */
    Criterion<U> whereGt(@NotNull VALUE value);

    /** Create a new Criterion where this key is great or equals the value
     * @see org.ujorm.criterion.Operator#GE */
    Criterion<U> whereGe(@NotNull VALUE value);

    /** Create a new Criterion where this key is less then the value
     * @see org.ujorm.criterion.Operator#LT */
    Criterion<U> whereLt(@NotNull VALUE value);

    /** Create a new Criterion where this key is less or equals than the value
     * @see org.ujorm.criterion.Operator#LE */
    Criterion<U> whereLe(@NotNull VALUE value);

    /**
     * Create a new Criterion where this key is {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    @NotNull Criterion<U> whereNull();

    /**
     * Create a new Criterion where this key is not {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @see #whereNull(org.ujorm.Key)
     * @see Operator#NOT_EQ
     */
    @NotNull Criterion<U> whereNotNull();

    /**
     * Create a new Criterion where this key is not {@code null} and is no empty text or empty list.
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    @NotNull Criterion<U> whereHasLength();

    /**
     * Create a new Criterion where this key is a {@code null} or it is empty string or list.
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    @NotNull Criterion<U> whereIsEmpty();

    /** Create a new Criterion for a Native Criterion in SQL statement format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native Criterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param sqlCondition a SQL condition in the String format, the NULL value or empty string is not accepted.
     * A substring {@code {0}} will be replaced for the current column name;
     * @see SerialSupplier
     * @see Operator#XSQL
     */
    @NotNull Criterion<U> forSql(@NotNull String sqlCondition);
    /** Create a new Criterion for a Native Criterion in SQL statement format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native Criterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param sqlTemplate a SQL condition in the String format, the NULL value or empty string is not accepted.
     * A substring {@code {0}} will be replaced for the current column name
     * and the substring {@code {1}} will be replaced for the required value.
     * @param value a condition value, array, list or an another key
     * @see Operator#XSQL
     */
    @NotNull Criterion<U> forSql
        ( @NotNull String sqlTemplate
        , @NotNull VALUE value);
    /** Create a new Criterion for a Native Criterion in SQL statement format.
     * Special features:
     * <ul>
     *   <li>parameters of the SQL_condition are not supported by the Ujorm</li>
     *   <li>your own implementation of SQL the parameters can increase
     *       a risk of the <a href="http://en.wikipedia.org/wiki/SQL_injection">SQL injection</a> attacks</li>
     *   <li>method {@link #evaluate(org.ujorm.Ujo)} is not supported and throws UnsupportedOperationException in the run-time</li>
     *   <li>native Criterion dependents on a selected database so application developers should to create support for each supported database
     *       of target application to ensure database compatibility</li>
     * </ul>
     * @param sqlTemplate a SQL condition in the String format, the NULL value or empty string is not accepted.
     * A substring {@code {0}} will be replaced for the current column name
     * and the substring {@code {1}} will be replaced for the required value.
     * @param value a condition value, array, list or an another key
     * @see Operator#XSQL
     */
    @NotNull Criterion<U> forSqlUnchecked
        ( @NotNull String sqlTemplate
        , @NotNull Object value);

    /** Create a new Criterion for this key where all results will be true (the result is independed on the value).
     *  The method evaluate(ujo) returns TRUE always.
     */
    @NotNull Criterion<U> forAll();

    /** Create a new Criterion for this key where all results will be false (the result is independed on the value).
     *  The  method evaluate(method) returns FALSE always.
     */
    @NotNull Criterion<U> forNone();

    /** An alias for the method: {@link #forAll() } */
    @NotNull Criterion<U> whereAll();

    /** An alias for the method: {@link #forNone() } */
    @NotNull Criterion<U> whereNone();

}
