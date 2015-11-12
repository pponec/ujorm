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

package org.ujorm;

import java.util.Collection;
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
    public Criterion<U> where
        ( Operator operator
        , VALUE value
        );

    /**
     * Create a new Criterion where this key is related to the value along the parameter {@link Operator}.
     * @param key Key
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    public Criterion<U> where
        ( Operator operator
        , Key<?,VALUE> value
        );

    /**
     * Create a new Criterion where this key equals the parameter value.
     * @param key Key
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>Key - reference to a related entity</li>
     * </ul>
     * @return The new immutable Criterion
     */
    public Criterion<U> whereEq(VALUE value);

    /**
     * Create a new Criterion where this key value equals the parameter value.
     * @param key Key can be type a direct of indirect (for a relation) key
     * @return The new immutable Criterion
     */
    public Criterion<U> whereEq(Key<U,VALUE> key);

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion.
     */
    public Criterion<U> whereIn
        ( Collection<VALUE> list
        );

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param key A direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    public Criterion<U> whereNotIn
        ( Collection<VALUE> list
        );

    /**
     * Create new Criterion where this key value is in the one of parameter values.
     * @param key A reference to a related entity
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return The new immutable Criterion
     */
    public Criterion<U> whereIn
        ( VALUE... list
        );

    /**
     * Create new Criterion where this key value is not in any of parameter values.
     * @param key A key direct or indirect Ujo key
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return The new immutable Criterion.
     */
    public Criterion<U> whereNotIn
        ( VALUE... list
        );

    /** Create a new Criterion where this key value is not equals the value
     * @see org.ujorm.criterion.Operator#NOT_EQ */
    public Criterion<U> whereNeq(VALUE value);

    /** Create a new Criterion where this key is great then the value
     * @see org.ujorm.criterion.Operator#GT */
    public Criterion<U> whereGt(VALUE value);

    /** Create a new Criterion where this key is great or equals the value
     * @see org.ujorm.criterion.Operator#GE */
    public Criterion<U> whereGe(VALUE value);

    /** Create a new Criterion where this key is less then the value
     * @see org.ujorm.criterion.Operator#LT */
    public Criterion<U> whereLt(VALUE value);

    /** Create a new Criterion where this key is less or equals than the value
     * @see org.ujorm.criterion.Operator#LE */
    public Criterion<U> whereLe(VALUE value);

    /**
     * Create a new Criterion where this key is {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @param key Key
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    public Criterion<U> whereNull();

    /**
     * Create a new Criterion where this key is not {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String key type in this case.
     * @param key Key
     * @see #whereNull(org.ujorm.Key)
     * @see Operator#NOT_EQ
     */
    public Criterion<U> whereNotNull();

    /**
     * Create a new Criterion where this key is not {@code null} and is no empty text or empty list.
     * @param key Key
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    public Criterion<U> whereFilled();

    /**
     * Create a new Criterion where this key is a {@code null} or it is empty string or list.
     * @param key Key
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    public Criterion<U> whereNotFilled();

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
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlCondition a SQL condition in the String format, the NULL value or empty string is not accepted.
     * A substring {@code {0}} will be replaced for the current column name;
     * @see Operator#XSQL
     */
    public Criterion<U> forSql(String sqlCondition);
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
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlTemplate a SQL condition in the String format, the NULL value or empty string is not accepted.
     * A substring {@code {0}} will be replaced for the current column name
     * and the substring {@code {1}} will be replaced for the required value.
     * @param value a condition value, array, list or an another key
     * @see Operator#XSQL
     */
    public Criterion<U> forSql(String sqlTemplate, VALUE value);
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
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlTemplate a SQL condition in the String format, the NULL value or empty string is not accepted.
     * A substring {@code {0}} will be replaced for the current column name
     * and the substring {@code {1}} will be replaced for the required value.
     * @param value a condition value, array, list or an another key
     * @see Operator#XSQL
     */
    public Criterion<U> forSqlUnchecked(String sqlTemplate, Object value);

    /** Create a new Criterion for this key where all results will be true (the result is independed on the value).
     *  The method evaluate(ujo) returns TRUE always.
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     */
    public Criterion<U> forAll();

    /** Create a new Criterion for this key where all results will be false (the result is independed on the value).
     *  The  method evaluate(method) returns FALSE always.
     * @param key The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     */
    public Criterion<U> forNone();

}
