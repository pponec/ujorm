/*
 *  Copyright 2007-2008 Paul Ponec
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
   

package org.ujoframework.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoComparator;
import org.ujoframework.tools.criteria.*;

/**
 * The Criteria class is a simple tool to search UJO objects in the list.
 * This class takes full advantage of architecture UJO objects. See the next sample.
 * <pre class="pre">
 * Person child  = <span class="keyword-directive">new</span> Person(<span class="character">&quot;</span><span class="character">Pavel</span><span class="character">&quot;</span>, 140.0);
 * Person mother = <span class="keyword-directive">new</span> Person(<span class="character">&quot;</span><span class="character">Mary</span><span class="character">&quot;</span>, 150.0);
 * Person father = <span class="keyword-directive">new</span> Person(<span class="character">&quot;</span><span class="character">John</span><span class="character">&quot;</span>, 160.0);
 *
 * child.set(MOTHER, mother);
 * child.set(FATHER, father);
 *
 * List&lt;Person&gt; persons = Arrays.asList(child, mother, father);
 *
 * UjoCriteria&lt;Person&gt; criteria = UjoCriteria.create();
 * Expression&lt;Person&gt; exp = criteria.newExpr(NAME, <span class="character">&quot;</span><span class="character">John</span><span class="character">&quot;</span>);
 * UjoComparator&lt;Person&gt; sort = UjoComparator.create(HIGH, NAME);
 * List&lt;Person&gt; <strong style="color:blue;">result</strong> = criteria.select(persons, exp, sort);
</pre>
 * 
 * @author Ponec
 * @since 0.81
 */
public class UjoCriteria<UJO extends Ujo> {

    /** Find a first UJO by an expression or return NULL if any object was not found. */
    public UJO findFirst(List<UJO> list, Expression<UJO> expression) {
        for (UJO ujo : list) {
            if (expression.evaluate(ujo)) {
                return ujo;
            }
        }
        return null;
    }

    /** Create a copy of the list and sort it. */
    public List<UJO> select(List<UJO> list, UjoComparator comparator) {
        List<UJO> result = new ArrayList<UJO>(list);
        Collections.sort(result, comparator);
        return result;
    }

    /** Create a sublist of a list by an Ujo expression. */
    public List<UJO> select(List<UJO> list, Expression<UJO> expression) {
        return select(list, expression, null);
    }

    /** Create a sublist of a list by an Ujo expression. */
    public List<UJO> select(List<UJO> list, Expression<UJO> expression, UjoComparator sorting) {
        List<UJO> result = new ArrayList<UJO>();
        for (UJO ujo : list) {
            if (expression.evaluate(ujo)) {
                result.add(ujo);
            }
        }
        if (sorting != null) {
            Collections.sort(result, sorting);
        }

        return result;
    }

    /** Equals */
    public Expression<UJO> newExpr(UjoProperty<UJO, ? extends Object> property, Object value) {
        return new ExpressionValue<UJO>(property, Operator.EQ, value);
    }

    /** TRUE / FALSE */
    @SuppressWarnings("unchecked")
    public Expression<UJO> newExpr(boolean value) {
        return Expression.newInstance(value);
    }

    
    public Expression<UJO> newExpr(UjoProperty<UJO, ? extends Object> property, Operator operator, Object value) {
        return new ExpressionValue<UJO>(property, operator, value);
    }

    public Expression<UJO> newExpression(UjoProperty<UJO, ?> property1, Operator operator, UjoProperty<UJO, Object> value) {
        return new ExpressionValue<UJO>(property1, operator, value);
    }

    public Expression<UJO> exprAnd(Expression<UJO> expr1, Expression<UJO> expr2) {
        return expr1.join(OperatorBinary.AND, expr2);
    }

    public Expression<UJO> exprOr(Expression<UJO> expr1, Expression<UJO> expr2) {
        return expr1.join(OperatorBinary.OR, expr2);
    }


//    /** Not implemented yet */
//    public void sortBy(UjoComparator comparator) {
//        this.comparator = comparator;
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    // ===========================================================
    
    public static <UJO extends Ujo> UjoCriteria<UJO> create() {
        return new UjoCriteria<UJO>();
    }
}
