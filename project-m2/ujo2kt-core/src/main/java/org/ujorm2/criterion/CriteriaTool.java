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

package org.ujorm2.criterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;


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
public class CriteriaTool<D> {

    /** Find the first UJO by an criterion or return NULL if any object was not found. */
    @Nullable
    public D findFirst(List<D> list, Criterion<D> criterion) {
        for (D ujo : list) {
            if (criterion.evaluate(ujo)) {
                return ujo;
            }
        }
        return null;
    }

    /** Create a copy of the list and sort it. */
    public List<D> select(List<D> list, UjoComparator<D> comparator) {
        return comparator.sort(new ArrayList<>(list));
    }

    /** Filter the list from parameter by a Criterion.
     * @see Criterion#select(java.lang.Iterable)
     */
    public List<D> select(List<D> list, Criterion<D> criterion) {
        return criterion.select(list);
    }

    /** Filter the list from parameter by a Criterion and sort the result.
     * @see Criterion#select(java.lang.Iterable)
     */
    public List<D> select(List<D> list, Criterion<D> criterion, UjoComparator<D> comparator) {
        final List<D> result = criterion.select(list);
        if (comparator != null) {
            Collections.sort(result, comparator);
        }
        return result;
    }

    // ----------- STATIC -------------

    /** Create a new instance */
    public static <UJO> CriteriaTool<UJO> newInstance() {
        return new CriteriaTool<>();
    }

}
