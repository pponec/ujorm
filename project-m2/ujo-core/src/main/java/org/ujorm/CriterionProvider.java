/*
 *  Copyright 2007-2010 Pavel Ponec
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

import org.ujorm.criterion.*;
import java.util.Collection;

public interface CriterionProvider<UJO extends Ujo, VALUE> {

    /**
     * Create a new Criterion where this property value is related to a parameter value along the {@link Operator}.
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    public Criterion<UJO> where
        ( Operator operator
        , VALUE value
        );

    /**
     * Create a new Criterion where this property is related to the value along the parameter {@link Operator}.
     * @param property Key
     * @param operator Operator
     * <ul>
     * <li>VALUE - the parameter value</li>
     * <li>Key - reference to a related entity</li>
     * <li>List&lt;TYPE&gt; - list of values (TODO - this type is planned in the future)</li>
     * </ul>
     * @return A new criterion
     */
    public Criterion<UJO> where
        ( Operator operator
        , Key<?,VALUE> value
        );

    /**
     * Create a new Criterion where this property equals the parameter value.
     * @param property Key
     * <ul>
     * <li>TYPE - parameter value</li>
     * <li>List&lt;TYPE&gt; - list of values</li>
     * <li>Key - reference to a related entity</li>
     * </ul>
     * @return A the new immutable Criterion
     */
    public Criterion<UJO> whereEq(VALUE value);

    /**
     * Create a new Criterion where this property value equals the parameter value.
     * @param property Key can be type a direct of indirect (for a relation) property
     * @return A the new immutable Criterion
     */
    public Criterion<UJO> whereEq(Key<UJO,VALUE> property);

    /**
     * Create new Criterion where this property value is in the one of parameter values.
     * @param property A direct or indeirect Ujo property
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return A the new immutable Criterion.
     */
    public Criterion<UJO> whereIn
        ( Collection<VALUE> list
        );

    /**
     * Create new Criterion where this property value is not in any of parameter values.
     * @param property A direct or indeirect Ujo property
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return A the new immutable Criterion.
     */
    public Criterion<UJO> whereNotIn
        ( Collection<VALUE> list
        );

    /**
     * Create new Criterion where this property value is in the one of parameter values.
     * @param property A reference to a related entity
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be FALSE in this case.
     * @return A the new immutable Criterion
     */
    public Criterion<UJO> whereIn
        ( VALUE... list
        );

    /**
     * Create new Criterion where this property value is not in any of parameter values.
     * @param property A property direct or indeirect Ujo property
     * @param list A collection of the values. The collection argument can be the EMPTY, the Criterion result will be TRUE in this case.
     * @return A the new immutable Criterion.
     */
    public Criterion<UJO> whereNotIn
        ( VALUE... list
        );

    /** Create a new Criterion where this property value is not equals the value
     * @see org.ujorm.criterion.Operator#NOT_EQ */
    public Criterion<UJO> whereNeq(VALUE value);

    /** Create a new Criterion where this property is great then the value
     * @see org.ujorm.criterion.Operator#GT */
    public Criterion<UJO> whereGt(VALUE value);

    /** Create a new Criterion where this property is great or equals the value
     * @see org.ujorm.criterion.Operator#GE */
    public Criterion<UJO> whereGe(VALUE value);

    /** Create a new Criterion where this property is less then the value
     * @see org.ujorm.criterion.Operator#LT */
    public Criterion<UJO> whereLt(VALUE value);

    /** Create a new Criterion where this property is less or equals than the value
     * @see org.ujorm.criterion.Operator#LE */
    public Criterion<UJO> whereLe(VALUE value);

    /**
     * Create a new Criterion where this property is {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String property type in this case.
     * @param property Key
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    public Criterion<UJO> whereNull();

    /**
     * Create a new Criterion where this property is not {@code null}.
     * The method is a shortcut to the next full expression:
     * <pre class="pre">
     * Criterin.where(Order.NOTE_PROPERTY, Operator.EQ, (String) null) </pre>
     * for the String property type in this case.
     * @param property Key
     * @see #whereNull(org.ujorm.Key)
     * @see Operator#NOT_EQ
     */
    public Criterion<UJO> whereNotNull();

    /**
     * Create a new Criterion where this property is not {@code null} and is no empty text or empty list.
     * @param property Key
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    public Criterion<UJO> whereFilled();

    /**
     * Create a new Criterion where this property is a {@code null} or it is empty string or list.
     * @param property Key
     * @see #whereNotNull(org.ujorm.Key)
     * @see Operator#EQ
     */
    public Criterion<UJO> whereNotFilled();

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
     * @param property The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     * @param sqlCondition a SQL condition in the String format, the NULL value or empty string is not accepted
     * @see Operator#XSQL
     */
    public Criterion<UJO> forSql(String sqlCondition);

    /** Create a new Criterion for this property where all results will be true (the result is independed on the value).
     *  The method evaluate(ujo) returns TRUE always.
     * @param property The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     */
    public Criterion<UJO> forAll();

    /** Create a new Criterion for this property where all results will be false (the result is independed on the value).
     *  The  method evaluate(method) returns FALSE always.
     * @param property The parameter is required by Ujorm to location a basic database table and the join relations in case a composed Property
     */
    public Criterion<UJO> forNone();

}
