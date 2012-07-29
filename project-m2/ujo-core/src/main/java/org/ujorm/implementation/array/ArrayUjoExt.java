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
import org.ujorm.extensions.AbstractUjoExt;
import org.ujorm.extensions.ListProperty;


/**
 * This is an Groovy style implementation of a setter and getter methods for an easier access for developpers,
 * however the methods have got an weaker type control in compare to the MapUjo implementation.
 * <br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> ArrayUjoExt&lt;Person&gt; {
 *
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person, String &gt; NAME = newKey(<span class="java-string-literal">&quot;Name&quot;</span> , String.<span class="java-keywords">class</span>, propertyCount++);
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person, Double &gt; CASH = newKey(<span class="java-string-literal">&quot;Cash&quot;</span> , Double.<span class="java-keywords">class</span>, propertyCount++);
 *  <span class="java-keywords">public static</span> <span class="java-keywords">final</span> Key&lt;Person, Person&gt; CHILD = newKey(<span class="java-string-literal">&quot;Child&quot;</span>, Person.<span class="java-keywords">class</span>, propertyCount++);
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
//@deprecated Use the class {@link  QuickUKjoMid} rather or a better class {@link KeyFactory} to create new Keys.
//@Deprecated
abstract public class ArrayUjoExt<UJO extends ArrayUjoExt> extends AbstractUjoExt<UJO> implements Serializable {

    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 977568L;

    
    /** An Incrementator. Use a new counter for each subclass by sample:
     *<pre class="pre">
     * <span class="java-block-comment">&#47&#42&#42 An Incrementator. Use a new counter for each subclass. &#42&#47</span>
     * <span class="java-keywords">protected</span> <span class="java-keywords">static</span> <span class="java-keywords">int</span> propertyCount = [SuperClass].propertyCount;
     *</pre>
     */
    protected static final int propertyCount = 0;
    
    /** Object data */
    protected Object[] data;
    
    /** Constructor */
    public ArrayUjoExt() {
        data = initData();
    }
    
    /** The method is called from top constructor. */
    protected Object[] initData() {
        return new Object[readPropertyCount()];
    }
    
    /** Return a count of keys. */
    abstract public int readPropertyCount();
    
    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * {@link Property#setValue(org.ujorm.Ujo, java.lang.Object) }
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see Property#setValue(org.ujorm.Ujo, java.lang.Object)
     */

    @Override
    public void writeValue(final Key property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        data[property.getIndex()] = value;
    }
    

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     * {@link Property#getValue(org.ujorm.Ujo)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see Property#getValue(org.ujorm.Ujo)
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
    public static <UJO extends ArrayUjoExt,VALUE> Property<UJO,VALUE> newKey(String name, int index) {
        return Property.newInstance(name, (Class) null, index);
    }

    /** A Property Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends ArrayUjoExt, VALUE> Property<UJO, VALUE> newKey(String name, VALUE value, int index) {
        return Property.newInstance(name, value, index);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends ArrayUjoExt, ITEM> ListProperty<UJO,ITEM> newListKey(String name, int index) {
        return ListProperty.newListProperty(name, null, index);
    }

    // --------- DEPRECATED -------------------

    /** Returns a new instance of property where the default value is null.
     * Method assigns a next property index.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    public static <UJO extends ArrayUjoExt,VALUE> Property<UJO,VALUE> newProperty(String name, Class<VALUE> type, int index) {
        return Property.newInstance(name, type, index);
    }

    /** A Property Factory
     * Method assigns a next property index.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends ArrayUjoExt, VALUE> Property<UJO, VALUE> newProperty(String name, VALUE value, int index) {
        return Property.newInstance(name, value, index);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @deprecated Use method newListKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends ArrayUjoExt, ITEM> ListProperty<UJO,ITEM> newListProperty(String name, Class<ITEM> type, int index) {
        return ListProperty.newListProperty(name, type, index);
    }

}
