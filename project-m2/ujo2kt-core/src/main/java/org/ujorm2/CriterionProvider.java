/*
 * Copyright 2007-2017 Pavel Ponec, https://github.com/pponec
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

package org.ujorm2;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm2.criterion.Criterion;
import org.ujorm2.criterion.Operator;
import org.ujorm2.criterion.ProxyValue;

public interface CriterionProvider<U, VALUE> {

    /**
     * Create a new Criterion for this key where all results will be true (the
     * result is independed on the value). The method evaluate(ujo) returns TRUE
     * always.
     */
    @Nonnull
    public Criterion<U> forAll();

    /** Create a new Criterion for this key where all results will be false (the result is independed on the value).
     *  The  method evaluate(method) returns FALSE always.
     */
    @Nonnull
    public Criterion<U> forNone();



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
    public Criterion<U> forCriterion
        ( @Nonnull Operator operator
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
    @Nonnull
    public Criterion<U> forCriterion
        ( @Nonnull Operator operator
        , @Nonnull ProxyValue<VALUE> proxyValue
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
    @Nonnull
    public Criterion<U> forCriterion
        ( @Nonnull Operator operator
        , @Nonnull Key<?,VALUE> value
        );

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @return The new immutable Criterion
     */
    @Nonnull
    public Criterion<U> forEq(@Nullable VALUE value);

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @param proxyValue An function for the value where the {@null} value is not supported in ORM.
     * @return The new immutable Criterion
     * @see SerialSupplier
     */
    @Nonnull
    public Criterion<U> forEq(@Nonnull ProxyValue<VALUE> proxyValue);

    /**
     * Create a new Criterion where this key value equals the parameter value.
     * @param key Key can be type a direct of indirect (for a relation) key
     * @return The new immutable Criterion
     */
    @Nonnull
    public Criterion<U> forEq(@Nonnull Key<U,VALUE> key);

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion.
     */
    @Nonnull
    public Criterion<U> forIn
        ( @Nonnull Collection<VALUE> list
        );

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @Nonnull
    public Criterion<U> forNotIn
        ( @Nonnull Collection<VALUE> list
        );

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion
     */
    @Nonnull
    public Criterion<U> forIn
        ( @Nonnull VALUE... list
        );

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    @Nonnull
    public Criterion<U> forNotIn
        ( @Nonnull VALUE... list
        );

    /** Create a new Criterion where this key value is not equals the value
     * @see org.ujorm.criterion.Operator#NOT_EQ */
    @Nonnull
    public Criterion<U> forNeq(@Nullable VALUE value);

    /** Create a new Criterion where this key is great then the value
     * @see org.ujorm.criterion.Operator#GT */
    public Criterion<U> forGt(@Nonnull VALUE value);

    /** Create a new Criterion where this key is great or equals the value
     * @see org.ujorm.criterion.Operator#GE */
    public Criterion<U> forGe(@Nonnull VALUE value);

    /** Create a new Criterion where this key is less then the value
     * @see org.ujorm.criterion.Operator#LT */
    public Criterion<U> forLt(@Nonnull VALUE value);

    /** Create a new Criterion where this key is less or equals than the value
     * @see org.ujorm.criterion.Operator#LE */
    public Criterion<U> forLe(@Nonnull VALUE value);

    /**
     * Create a new Criterion where this key is {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    @Nonnull
    public Criterion<U> forNull();

    /**
     * Create a new Criterion where this key is not {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @see #whereNull(org.ujorm.Key)
     * @see Operator#NOT_EQ
     */
    @Nonnull
    public Criterion<U> forNotNull();

    /**
     * Create a new Criterion where this key is not {@code null} and is no empty text or empty list.
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    @Nonnull
    public Criterion<U> forLength();

    /**
     * Create a new Criterion where this key is a {@code null} or it is empty string or list.
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    @Nonnull
    public Criterion<U> forEmpty();

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
    @Nonnull
    public Criterion<U> forSql(@Nonnull String sqlCondition);
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
    @Nonnull
    public Criterion<U> forSql
        ( @Nonnull String sqlTemplate
        , @Nonnull VALUE value);
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
    @Nonnull
    public Criterion<U> forSqlUnchecked
        ( @Nonnull String sqlTemplate
        , @Nonnull Object value);



}
