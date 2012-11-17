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

package org.ujorm.implementation.array;

import java.io.Serializable;
import org.ujorm.Key;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.SuperAbstractUjo;

/**
 * This is a very fast abstract implementation of <code>Ujo</code>.
 * For implementation define only a "public static final Key" constants and a "readPropertyCount()" method in a child class.
 * The code syntax is Java 1.5 complied.
 * <br>All keys must be objects (no primitive types) in the current version of Ujorm.
 * <br>Features: very good performance, an order of keys from "<code>readKeys()</code>" method is guaranteed and independed on a Java implementation.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.implementation.array.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> ArrayUjo {
 *
 *    <span class="java-keywords">protected</span> <span class="java-keywords">static</span> <span class="java-keywords">int</span> propertyCount = ArrayUjo.propertyCount;
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,String &gt; NAME  = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;name&quot;</span> , String.<span class="java-keywords">class</span> , propertyCount++);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;male&quot;</span> , Boolean.<span class="java-keywords">class</span>, propertyCount++);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;birth&quot;</span>, Date.<span class="java-keywords">class</span>   , propertyCount++);
 *
 *    <span class="java-annotation">@</span>Override
 *    <span class="java-keywords">public</span> <span class="java-keywords">int</span> <span class="java-layer-method">readPropertyCount</span>() {
 *        <span class="java-keywords">return</span> propertyCount;
 *    }
 * }
 * </pre>
 * 
 * @see Property
 * @author Pavel Ponec
 * @composed 1 - * Property
 * @deprecated Use the class {@link AbstractUjo} rather with the class {@link KeyFactory} to create new Keys.
 */
@Deprecated
public abstract class ArrayUjo extends SuperAbstractUjo implements Serializable {
    

    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 977569L;

    /** An Incrementator. Use a new counter for each subclass by sample:
     *<pre class="pre">
     * <span class="java-block-comment">&#47&#42&#42 An Incrementator. Use a new counter for each subclass. &#42&#47</span>
     * <span class="java-keywords">protected</span> <span class="java-keywords">static</span> <span class="java-keywords">int</span> propertyCount = [SuperClass].propertyCount;
     *</pre>
     */
    protected static final int propertyCount = 0;
    
    /** Object data. Unauthorized writing is not allowed. */
    final private Object[] data;
    
    /** Constructor */
    public ArrayUjo() {
        data = initData();
    }

    public ArrayUjo(Object[] data) {
        this.data = data;
    }
    
    /** The method is called from top constructor. */
    protected Object[] initData() {
        return new Object[readPropertyCount()];
    }
    
    /** Return a count of keys. */
    abstract public int readPropertyCount();
    
    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * {@link Key#setValue(Ujo,Object)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see Key#setValue(Ujo,Object)
     */

    @Override
    public void writeValue(final Key property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        data[property.getIndex()] = value;
    }
    

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * {@link Key#of(Ujo)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see Key#of(Ujo)
     */    
    @Override
    public Object readValue(final Key property) {
        return data[property.getIndex()];
    }
    
    
    // --------- STATIC METHODS -------------------

    /** Returns a new instance of property where the default value is null.
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends ArrayUjo,VALUE> Property<UJO,VALUE> newKey(String name, int index) {
        return Property.newInstance(name, null, null, index, false);
    }

    /** A Property Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends ArrayUjo, VALUE> Property<UJO, VALUE> newKey(String name, VALUE value, int index) {
        return Property.newInstance(name, value, index);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends ArrayUjo, ITEM> ListProperty<UJO,ITEM> newListKey(String name, int index) {
        return ListProperty.newListProperty(name, null, index);
    }

    // --------- DEPRECATED -------------------

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     * @deprecated Use method newListKey
     */
    @Deprecated
    protected static <UJO extends ArrayUjo, ITEM> ListProperty<UJO,ITEM> newPropertyList_(String name, Class<ITEM> type, int index) {
        return ListProperty.newListProperty(name, type, index);
    }

    /** Returns a new instance of property where the default value is null.
     * Method assigns a next property index.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    public static <UJO extends ArrayUjo,VALUE> Property<UJO,VALUE> newProperty(String name, Class<VALUE> type, int index) {
        return Property.newInstance(name, type, null, index);
    }

    /** A Property Factory
     * Method assigns a next property index.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends ArrayUjo, VALUE> Property<UJO, VALUE> newProperty(String name, VALUE value, int index) {
        return Property.newInstance(name, value, index);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @deprecated Use method newListKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends ArrayUjo, ITEM> ListProperty<UJO,ITEM> newListProperty(String name, Class<ITEM> type, int index) {
        return ListProperty.newListProperty(name, type, index);
    }
    
}
