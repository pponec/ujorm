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

package org.ujoframework.implementation.mapImpl;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.AbstractUjo;
import org.ujoframework.extensions.ListProperty;

/**
 * The abstract String Map Ujo imlementation is an implementation of
 * the <code>UjoMiddle</code>
 * and <code>Map&lt;CharSequence,Object&gt;</code> interfaces.
 * A child implementation can use "public static final UjoProperty" constants for its UjoProperties.
 * The map key can be any CharSequence object include a <strong>direct</strong> UjoProperty from its implementation.
 * <br>Notes:
 * <ul>
 *   <li>the NULL key is not supported in this implementation</li>
 *   <li>the map implementation is a proxy to an internal Map&lt;String,Object&gt; object</li>
 *   <li>this class will be moved into a module Tools in future</li>
 * </ul>
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujoframework.implementation.map.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapImplUjo {
 *
 *    <span class="java-keywords">public static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person,String &gt; NAME  = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;name&quot;</span> , String.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;male&quot;</span> , Boolean.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public static</span> <span class="java-keywords">final</span> UjoProperty&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;birth&quot;</span>, Date.<span class="java-keywords">class</span>);
 *
 * }
 * </pre>
 *
 * @see MapProperty
 * @author Pavel Ponec
 * @since UJO release 0.85
 * @composed 1 - * MapProperty
 */
public abstract class MapImplUjo extends AbstractUjo implements Map<CharSequence,Object> {
    
    /** Object data */
    final protected HashMap<String,Object> data;
    
    /** Constructor */
    public MapImplUjo() {
        data = new HashMap<String,Object>();
    }

    /** Constructor */
    protected MapImplUjo(HashMap<String,Object> aData) {
        data = aData;
    }
    

    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * <a href="MapProperty.html#setValue(UJO,%20VALUE)">MapProperty.setValue(Ujo,Object)</a> 
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then no exception is throwed.
     *
     * @see MapProperty#setValue(Ujo,Object)
     */
    public void writeValue(final UjoProperty property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        put(property, value);
    }
    

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * <a href="MapProperty.html#getValue(UJO)">MapProperty.getValue(Ujo)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method returns a null value.
     *
     * @see MapProperty#getValue(Ujo)
     */
    public Object readValue(final UjoProperty property) {
        Object result = get(property);
        return result!=null ? result : property.getDefault() ;
    }
    
    // --------- MAP IMPLEMENTATION -------------------

    /**  Returns the number of key-value mappings in this map */
    @Override public int size() {
        return data.size();
    }

    /** Returns the number of key-value mappings in this map. */
    @Override public boolean isEmpty() {
        return data.isEmpty();
    }

    /** Returns <tt>true</tt> if this map contains a mapping for the specified key.
     * @param key Any CharSequence include a <strong>direct</strong> UjoProperty
     */
    @Override public boolean containsKey(Object key) {
        return data.containsKey(key.toString());
    }

    /**  Returns <tt>true</tt> if this map maps one or more keys to the specified value. */
    @Override public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    /** Returns the value to which the specified key is mapped, the {@code null}
     * is not supported
     * @param key Any CharSequence include a <strong>direct</strong> UjoProperty
     */
    @Override public Object get(Object key) {
        return data.get(key.toString());
    }

    /**
     * Associates the specified value with the specified key in this map.
     * @param key Any CharSequence include a <strong>direct</strong> UjoProperty
     * @param value
     * @return the previous value associated with <tt>key</tt>,
     *         or <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    @Override public Object put(CharSequence key, Object value) {
        return data.put(key.toString(), value);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * @param key Any CharSequence include a <strong>direct</strong> UjoProperty
     * @return the previous value associated with <tt>key</tt>,
     *         or <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    @Override public Object remove(Object key) {
        return data.remove(key.toString());
    }

    @Override public void putAll(Map<? extends CharSequence, ? extends Object> m) {
        for (CharSequence cs : m.keySet()) {
            put(cs.toString(), m.get(cs));
        }
    }

    /** Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    @Override public void clear() {
        data.clear();
    }

    /** Returns a set of String keys. */
    @SuppressWarnings("unchecked")
    @Override public Set<CharSequence> keySet() {
        return (Set<CharSequence>) (Set) data.keySet();
    }


   /** Returns a {@link Collection} view of the values contained in this map */
    @Override public Collection<Object> values() {
        return data.values();
    }

    /** Returns a {@link Set} view of the mappings contained in this map.  */
    @SuppressWarnings("unchecked")
    @Override public Set<Entry<CharSequence, Object>> entrySet() {
        return (Set<Entry<CharSequence, Object>>)  (Set) data.entrySet();
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of property where the default value is null.
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends MapImplUjo,VALUE> Property<UJO,VALUE> newProperty(String name, Class<VALUE> type) {
        return Property.newInstance(name, type);
    }

    /** A Property Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends MapImplUjo, VALUE> Property<UJO, VALUE> newProperty(String name, VALUE value) {
        return Property.newInstance(name, value);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends MapImplUjo, ITEM> ListProperty<UJO,ITEM> newListProperty(String name, Class<ITEM> type) {
        return ListProperty.newListProperty(name, type);
    }

}
