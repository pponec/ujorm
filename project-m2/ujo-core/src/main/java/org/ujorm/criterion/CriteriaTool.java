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

package org.ujorm.criterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.ujorm.Ujo;
import org.ujorm.core.UjoComparator;


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
 * Criterion&lt;Person&gt; exp = Criterion.where(NAME, <span class="character">&quot;</span><span class="character">John</span><span class="character">&quot;</span>);
 * UjoComparator&lt;Person&gt; sort = UjoComparator.create(HIGH, NAME);
 * List&lt;Person&gt; <strong style="color:blue;">result</strong> = CriteriaTool.where().select(persons, exp, sort);
</pre>
 *
 * @author Pavel Ponec
 * @since 0.90
 */
public class CriteriaTool<UJO extends Ujo> {

    /** Find the first UJO by an criterion or return NULL if any object was not found. */
    @Nullable
    public UJO findFirst(List<UJO> list, Criterion<UJO> criterion) {
        for (UJO ujo : list) {
            if (criterion.evaluate(ujo)) {
                return ujo;
            }
        }
        return null;
    }

    /** Create a copy of the list and sort it. */
    public List<UJO> select(List<UJO> list, UjoComparator<UJO> comparator) {
        return comparator.sort(new ArrayList<>(list));
    }

    /** Filter the list from parameter by a Criterion.
     * @see Criterion#evaluate(java.lang.Iterable)
     */
    public List<UJO> select(List<UJO> list, Criterion<UJO> criterion) {
        return criterion.evaluate(list);
    }

    /** Filter the list from parameter by a Criterion and sort the result.
     * @see Criterion#evaluate(java.lang.Iterable)
     */
    public List<UJO> select(List<UJO> list, Criterion<UJO> criterion, UjoComparator<UJO> comparator) {
        final List<UJO> result = criterion.evaluate(list);
        if (comparator != null) {
            Collections.sort(result, comparator);
        }
        return result;
    }

    // ----------- STATIC -------------

    /** Create a new instance */
    public static <UJO extends Ujo> CriteriaTool<UJO> newInstance() {
        return new CriteriaTool<>();
    }

}
