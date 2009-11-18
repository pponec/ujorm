/*
 *  Copyright 2007 Paul Ponec
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
package org.ujoframework.implementation.map;

import java.util.HashMap;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.extensions.AbstractUjoExt;
import org.ujoframework.extensions.ListProperty;

/**
 * This is an Groovy style implementation of a setter and getter methods for an easier access for developpers,
 * however the methods have got an weaker type control in compare to the MapUjo implementation.
 * <br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjoExt&lt;Person&gt; {
 *
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person, String&gt; NAME  = newProperty(<span class="java-string-literal">&quot;Name&quot;</span> , String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person, Double&gt; CASH  = newProperty(<span class="java-string-literal">&quot;Cash&quot;</span> , Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person, Person&gt; CHILD = newProperty(<span class="java-string-literal">&quot;Child&quot;</span>, Person.<span class="java-keywords">class</span>);
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
 * @author Paul Ponec
 * @since UJO release 0.80 
 */
abstract public class MapUjoExt<UJO extends MapUjoExt> extends AbstractUjoExt<UJO> {

    /** Object data */
    final protected HashMap<UjoProperty, Object> data;

    /** Constructor */
    public MapUjoExt() {
        data = new HashMap<UjoProperty, Object>();
    }

    /** Constructor */
    protected MapUjoExt(HashMap<UjoProperty, Object> aData) {
        data = aData;
    }

    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * <a href="../../extensions/Property.html#setValue(UJO,%20VALUE)">Property.setValue(Ujo,Object)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then no exception is throwed.
     *
     * @see Property#setValue(Ujo,Object)
     */
    public void writeValue(final UjoProperty property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);
        data.put(property, value);
    }

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * <a href="../../extensions/Property.html#getValue(UJO)">Property.getValue(Ujo)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method returns a null value.
     *
     * @see Property#getValue(Ujo)
     */
    public Object readValue(final UjoProperty property) {
        return data.get(property);
    }
    
    // --------- STATIC METHODS -------------------

    /** Returns a new instance of property where the default value is null.
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends MapUjoExt,VALUE> Property<UJO,VALUE> newProperty(String name, Class<VALUE> type) {
        return Property.newInstance(name, type);
    }

    /** A Property Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends MapUjoExt, VALUE> Property<UJO, VALUE> newProperty(String name, VALUE value) {
        return Property.newInstance(name, value);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends MapUjoExt, ITEM> ListProperty<UJO,ITEM> newListProperty(String name, Class<ITEM> type) {
        return ListProperty.newListProperty(name, type);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @deprecated Use newListProperty(...) instead of.
     * @hidden
     */
    protected static final <UJO extends MapUjo, ITEM> ListProperty<UJO,ITEM> newPropertyList(String name, Class<ITEM> type) {
        return ListProperty.newListProperty(name, type);
    }

}
