/*
 *  Copyright 2007-2022 Pavel Ponec
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

package org.ujorm.implementation.mapImpl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.ujorm.Key;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.SuperAbstractUjo;

/**
 * The abstract String Map Ujo imlementation is an implementation of
 * the <code>UjoMiddle</code>
 * and <code>Map&lt;CharSequence,Object&gt;</code> interfaces.
 * A child implementation can use "public static final Key" constants for its KeyProperties.
 * The map key can be any CharSequence object including a <strong>direct</strong> Key from its implementation.
 * <br>Notes:
 * <ul>
 *   <li>the NULL key is not supported in this implementation</li>
 *   <li>the map implementation is a proxy to an internal Map&lt;String,Object&gt; object</li>
 *   <li>this class will be moved into a module Tools in future</li>
 * </ul>
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.implementation.map.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapImplUjo {
 *
 *    <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person,String &gt; NAME  = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;name&quot;</span> );
 *    <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;male&quot;</span>);
 *    <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;birth&quot;</span>);
 *
 * }
 * </pre>
 *
 * @author Pavel Ponec
 * @see Property
 * @since UJO release 0.85
 * @composed 1 - * Property
 */
public abstract class MapImplUjo extends SuperAbstractUjo implements Map<CharSequence,Object>, Serializable {

    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 977565L;

    /** Object data. Unauthorized writing is not allowed. */
    final private HashMap<String,Object> data;

    /** Constructor */
    public MapImplUjo() {
        data = new HashMap<String,Object>();
    }

    /** Constructor */
    protected MapImplUjo(HashMap<String,Object> aData) {
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
        put(key, value);
    }


    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     * {@link Key#of(org.ujorm.Ujo)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors.
     * <br>NOTE: If key is an incorrect then method returns a null value.
     *
     * @see Key#of(org.ujorm.Ujo)
     */
    @Override
    public Object readValue(final Key key) {
        return get(key);
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
     * @param key Any CharSequence includes a <strong>direct</strong> Key
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
     * @param key Any CharSequence include a <strong>direct</strong> Key
     */
    @Override public Object get(Object key) {
        return data.get(key.toString());
    }

    /**
     * Associates the specified value with the specified key in this map.
     * @param key Any CharSequence include a <strong>direct</strong> Key
     * @param value
     * @return the previous value associated with <tt>key</tt>,
     *         or <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    @Override public Object put(CharSequence key, Object value) {
        return data.put(key.toString(), value);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * @param key Any CharSequence include a <strong>direct</strong> Key
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

    /** Returns a new instance of key where the default value is null.
     * Method assigns a next key index.
     * @hidden
     */
    public static <UJO extends MapImplUjo,VALUE> Property<UJO,VALUE> newKey(String name) {
        return Property.of(name, (Class)null, (Class)null);
    }

    /** A Property Factory
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends MapImplUjo, VALUE> Property<UJO, VALUE> newKey(String name, VALUE value) {
        return Property.of(name, value);
    }

    /** A ListProperty Factory
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends MapImplUjo, ITEM> ListProperty<UJO,ITEM> newListKey(String name) {
        return ListProperty.newListProperty(name, (Class)null);
    }

    // --------- DEPRECATED -------------------

    /** Returns a new instance of key where the default value is null.
     * Method assigns a next key index.
     * @hidden
     */
    public static <UJO extends MapImplUjo,VALUE> Property<UJO,VALUE> newProperty(String name, Class<VALUE> type) {
        return Property.of(name, type);
    }

    /** A Property Factory
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends MapImplUjo, VALUE> Property<UJO, VALUE> newProperty(String name, VALUE value) {
        return Property.of(name, value);
    }

    /** A ListProperty Factory
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends MapImplUjo, ITEM> ListProperty<UJO,ITEM> newListProperty(String name, Class<ITEM> type) {
        return ListProperty.newListProperty(name, type);
    }

}
