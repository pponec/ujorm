/*
 * Copyright 2012-2017 Pavel Ponec, https://github.com/pponec
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

import java.util.List;
import java.util.Map;
import org.ujorm.core.WeakKeyFactory;

/**
 * The WeakKey is a simplified Key interface without the generic parameter for the domain type.
 * The default implementation does not have the full support of several methods:
 * <ul>
 *   <li>{@link Key#getDomainType()} - method return the result {@code Ujo.class} always </li>
 *   <li>{@link Key#add(org.ujorm.Key)} - method is not supported by the default implementation</li>
 *   <li>{@link Key#descending() } - result is not type of WeakKey</li>
 *   <li>{@link org.ujorm.core.KeyRing} serializaton is not supported</li>
 * </ul>
 * , and some more methods are not fully type-safe.
 * On the other side the interface supports some operations with the {@link List} and {@link Map} interfaces.
 * To create new keys use an instance of the factory class {@link WeakKeyFactory}.
 * <h3>The sample of use</h3>
 * <pre class="pre">
 * <span class="keyword-directive">public</span> <span class="keyword-directive">class</span> MyService {
 *     <span class="keyword-directive">private</span> <span class="keyword-directive">static</span> <span class="keyword-directive">final</span> WeakKeyFactory f = <span class="keyword-directive">new</span> WeakKeyFactory(MyService.<span class="keyword-directive">class</span>);
 *
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">static</span> <span class="keyword-directive">final</span> WeakKey&lt;String&gt;     NAME = f.newKey();
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">static</span> <span class="keyword-directive">final</span> WeakKey&lt;Date&gt;       BORN = f.newKey();
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">static</span> <span class="keyword-directive">final</span> WeakKey&lt;Boolean&gt;    WIFE = f.newKeyDefault(Boolean.TRUE);
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">static</span> <span class="keyword-directive">final</span> WeakKey&lt;BigDecimal&gt; CASH = f.newKeyDefault(BigDecimal.ZERO);
 *
 *     <span class="keyword-directive">static</span> {
 *         f.lock();
 *     }
 *
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> testWeakKeys() {
 *         List&lt;Object&gt; list = <span class="keyword-directive">new</span> ArrayList&lt;Object&gt;();
 *
 *         <span class="keyword-directive">assert</span> NAME.of(list) == <span class="keyword-directive">null</span>;
 *         <span class="keyword-directive">assert</span> BORN.of(list) == <span class="keyword-directive">null</span>;
 *         <span class="keyword-directive">assert</span> WIFE.of(list) == Boolean.TRUE;
 *         <span class="keyword-directive">assert</span> CASH.of(list) == BigDecimal.ZERO;
 *
 *         <span class="keyword-directive">final</span> String name = <span class="character">&quot;</span><span class="character">Eva</span><span class="character">&quot;</span>;
 *         <span class="keyword-directive">final</span> Boolean wife = <span class="keyword-directive">true</span>;
 *         <span class="keyword-directive">final</span> Date today = <span class="keyword-directive">new</span> Date();
 *         <span class="keyword-directive">final</span> BigDecimal cash = BigDecimal.TEN;
 *
 *         NAME.setValue(list, name);
 *         BORN.setValue(list, today);
 *         WIFE.setValue(list, wife);
 *         CASH.setValue(list, cash);
 *
 *         <span class="keyword-directive">assert</span> NAME.of(list).equals(name);
 *         <span class="keyword-directive">assert</span> BORN.of(list).equals(today);
 *         <span class="keyword-directive">assert</span> WIFE.of(list).equals(wife);
 *         <span class="keyword-directive">assert</span> CASH.of(list).equals(cash);
 *     }
 * }
 * </pre>
 * <h3>Where to use the WeakKey?</h3>
 * <ul>
 *     <li>the WeakKey can be useful on support any Map with a String key for better type-safe features</li>
 *     <li>a Map implementation can be simply replaced by the List if you like</li>
 *     <li>support to building URL parameters</li>
 *     <li>Spring module provides a special class <code>AbstractAplicationContextAdapter</code> to get Spring services by the WeakKey, see <a href="http://sourceforge.net/p/ujoframework/svn/HEAD/tree/trunk/project-m2/ujo-spring/src/test/java/org/ujorm/spring/AplicationContextAdapter.java?view=markup">the example</a></li>
 *     <li>understanding the WeakKey serves as a great introduction to the Ujo architecture.</li>
 * </ul>
 *
 * @author Pavel Ponec
 */
public interface WeakKey<VALUE> extends Key<Ujo, VALUE> {

    /**
     * It is an extended method for setting an appropriate type safe value to a common Map object.
     */
    public void setValue(Map<String,? super VALUE> map, VALUE value);

    /**
     * An alias for the method {@link #of(java.util.Map) } .
     * @see #getValue(java.util.Map)
     */
    public VALUE getValue(Map<String,? super VALUE> map);


   /**
     * It is an extended method for getting an appropriate type safe value from a common Map object.
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the map object.
     */
    public VALUE of(Map<String,? super VALUE> map);

    /**
     * It is an extended method for setting an appropriate type safe value to a common Map object.
     * In case the size of the list is insufficient, the list is filled by the {@code null} values.
     */
    public void setValue(List<? super VALUE> list, VALUE value);

    /**
     * An alias for the method {@link #of(java.util.List) } .
     * @see #getValue(java.util.List)
     */
    public VALUE getValue(List<? super VALUE> list);

    /**
     * It is an extended method for getting an appropriate type safe value from a common Map object.
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the map object.
     */
    public VALUE of(List<? super VALUE> list);

    /**
     * Returns an value from the Servlet Request.
     * @param An object type of: javax.servlet.ServletRequest, where the {@code null} result is replaced for a default value
     * @return Returns object converted to a required type.
     */
    public VALUE getRequestValue(Object servletReqest) throws IllegalArgumentException;

}
