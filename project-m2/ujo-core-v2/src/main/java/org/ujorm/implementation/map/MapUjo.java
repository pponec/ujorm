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
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.SuperAbstractUjo;

/**
 * This is a simple abstract implementation of <code>Ujo</code>.<br>
 * Child implementation can use "public static final Key" constants for its KeyProperties.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: very simple implementation and a sufficient performance for common tasks. The architecture is useful for a rare assignment of values in object too.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.implementation.map.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjo {
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,String &gt; NAME  = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;name&quot;</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;male&quot;</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;birth&quot;</span>);
 *
 * }
 * </pre>
 *
 * @see Property
 * @author Pavel Ponec
 * @composed 1 - * Property
 */
public abstract class MapUjo extends SuperAbstractUjo implements Serializable {

    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 977567L;

    /** Object data. Unauthorized writing is not allowed. */
    final private HashMap<Key,Object> data;

    /** Constructor */
    public MapUjo() {
        data = new HashMap<Key,Object>();
    }

    /** Constructor */
    protected MapUjo(HashMap<Key,Object> aData) {
        data = aData;
    }


    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method
     * {@link Key#setValue(org.ujorm.Ujo, java.lang.Object) }
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators.
     * <br>NOTE: If the Key is an incorrect then no exception is throwed.
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
        assert !key.isComposite() : "Property must be direct only.";
        return data.get(key);
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of key where the default value is null.
     * Method assigns a next key index.
     * @hidden
     */
    public static <UJO extends MapUjo,VALUE> Property<UJO,VALUE> newKey(String name) {
        return Property.of(name, (Class)null);
    }

    /** A Property Factory
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends MapUjo, VALUE> Property<UJO, VALUE> newKey(String name, VALUE value) {
        return Property.of(name, value);
    }

    /** Returns a new instance of key where the default value is null.
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends MapUjo,VALUE> Property<UJO,VALUE> newKey(Key<UJO,VALUE> p, int index) {
        return Property.of(p.getName(), p.getType(), p.getDefault(), index, true);
    }

    /** A ListProperty Factory
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends MapUjo, ITEM> ListProperty<UJO,ITEM> newListKey(String name) {
        return ListProperty.newListProperty(name, (Class)null);
    }

    // --------- DEPRECATED -------------------

    /** Returns a new instance of key where the default value is null.
     * Method assigns a next key index.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    public static <UJO extends MapUjo,VALUE> Property<UJO,VALUE> newProperty(String name, Class<VALUE> type) {
        return Property.of(name, type);
    }

    /** A Property Factory
     * Method assigns a next key index.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends MapUjo, VALUE> Property<UJO, VALUE> newProperty(String name, VALUE value) {
        return Property.of(name, value);
    }

    /** Returns a new instance of key where the default value is null.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <UJO extends MapUjo,VALUE> Property<UJO,VALUE> newProperty(final Key<UJO,VALUE> p, int index) {
        return Property.of(p.getName(), p.getType(), p.getDefault(), index, true);
    }

    /** A ListProperty Factory
     * Method assigns a next key index.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends MapUjo, ITEM> ListProperty<UJO,ITEM> newListProperty(String name, Class<ITEM> type) {
        return ListProperty.newListProperty(name, type);
    }

    /** A ListProperty Factory
     * Method assigns a next key index.
     * @deprecated Use newListProperty(...) instead of this.
     * @hidden
     */
    @Deprecated
    protected static <UJO extends MapUjo, ITEM> ListProperty<UJO,ITEM> newPropertyList(String name, Class<ITEM> type) {
        return ListProperty.newListProperty(name, type);
    }



}
