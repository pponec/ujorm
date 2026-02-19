/*
 *  Copyright 2007-2026 Pavel Ponec
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
package org.ujorm.implementation.map;

import java.io.Serializable;
import java.util.HashMap;
import org.ujorm.Key;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.AbstractUjoExt;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;

/**
 * This is an Groovy style implementation of a setter and getter methods for an easier access for developpers,
 * however the methods have got an weaker type control in compare to the MapUjo implementation.
 * <br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjoExt&lt;Person&gt; {
 *
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person, String&gt; NAME  = newKey(<span class="java-string-literal">&quot;Name&quot;</span> , String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person, Double&gt; CASH  = newKey(<span class="java-string-literal">&quot;Cash&quot;</span> , Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person, Person&gt; CHILD = newKey(<span class="java-string-literal">&quot;Child&quot;</span>, Person.<span class="java-keywords">class</span>);
 *
 *  <span class="java-keywords">public</span> <span class="java-keywords">void</span> init() {
 *    set(NAME, <span class="java-string-literal">&quot;</span><span class="java-string-literal">George</span><span class="java-string-literal">&quot;</span>);
 *    set(CHILD, <span class="java-keywords">new</span> Person());
 *    set(CHILD, NAME, <span class="java-string-literal">&quot;</span><span class="java-string-literal">Jane</span><span class="java-string-literal">&quot;</span>);
 *    set(CHILD, CASH, 200d);
 *
 *    String name = get(CHILD, NAME);
 *    <span class="java-keywords">double</span> cash = get(CHILD, CASH);
 *  }
 *}</pre>
 *
 * @see Property
 * @author Pavel Ponec
 * @since UJO release 0.80
 */
abstract public class MapUjoExt<UJO extends MapUjoExt> extends AbstractUjoExt<UJO> implements Serializable {

    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 977566L;

    /** Object data */
    final protected HashMap<Key, Object> data;

    /** Constructor */
    public MapUjoExt() {
        data = new HashMap<Key, Object>();
    }

    /** Constructor */
    protected MapUjoExt(HashMap<Key, Object> aData) {
        data = aData;
    }

    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method
     * {@link Key#setValue(org.ujorm.Ujo, java.lang.Object) }
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators.
     * <br>NOTE: If key is an incorrect then no exception is throwed.
     *
     * @see Key#setValue(Ujo,Object)
     */
    @Override
    public void writeValue(final Key key, final Object value) {
        assert UjoManager.assertDirectAssign(key, value, this);
        data.put(key, value);
    }

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     * {@link Key#of(org.ujorm.Ujo)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors.
     * <br>NOTE: If key is an incorrect then method returns a null value.
     *
     * @see Key#of(Ujo)
     */
    @Override
    public Object readValue(final Key key) {
        return data.get(key);
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of key where the default value is null.
     * Method assigns a next key index.
     * @hidden
     */
    public static <UJO extends MapUjoExt,VALUE> Property<UJO,VALUE> newKey(String name) {
        return Property.of(name, (Class)null);
    }

    /** A Property Factory
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends MapUjoExt, VALUE> Property<UJO, VALUE> newKey(String name, VALUE value) {
        return Property.of(name, value);
    }

    /** A ListProperty Factory
     * Method assigns a next key index.
     * @hidden
     */
    @SuppressWarnings("unchecked")
    protected static <UJO extends MapUjoExt, ITEM> ListProperty<UJO,ITEM> newListKey(String name) {
        return ListProperty.newListProperty(name, (Class)null);
    }


}
