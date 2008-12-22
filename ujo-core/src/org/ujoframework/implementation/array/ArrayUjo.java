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

package org.ujoframework.implementation.array;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.SuperUjo;

/**
 * This is a very fast abstract implementation of Ujo.
 * For implementation define only a "public static final ArrayUjoProperty" constants and a "readPropertyCount()" method in a child class.
 * The code syntax is Java 1.5 complied.
 * <br>All properties must be objects (no primitive types) in the current version of UJO Framework.
 * <br>Features: very good performance, an order of properties from "readProperties()" method is guaranteed and independed on a Java implementation.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujoframework.implementation.array.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> ArrayUjo {
 *
 *    <span class="java-keywords">protected</span> <span class="java-keywords">static</span> <span class="java-keywords">int</span> propertyCount = ArrayUjo.propertyCount;
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> ArrayProperty &lt;Person,String &gt; NAME  = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;name&quot;</span> , String.<span class="java-keywords">class</span> , propertyCount++);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> ArrayProperty &lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;male&quot;</span> , Boolean.<span class="java-keywords">class</span>, propertyCount++);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> ArrayProperty &lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;birth&quot;</span>, Date.<span class="java-keywords">class</span>   , propertyCount++);
 *
 *    <span class="java-annotation">@</span>Override
 *    <span class="java-keywords">public</span> <span class="java-keywords">int</span> <span class="java-layer-method">readPropertyCount</span>() {
 *        <span class="java-keywords">return</span> propertyCount;
 *    }
 * }
 * </pre>
 * 
 * @see ArrayProperty
 * @author Pavel Ponec
 * @composed 1 - * ArrayProperty
 */
public abstract class ArrayUjo extends SuperUjo {
    
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
    public ArrayUjo() {
        data = initData();
    }
    
    /** The method is called from top constructor. */
    protected Object[] initData() {
        return new Object[readPropertyCount()];
    }
    
    /** Return a count of properties. */
    abstract public int readPropertyCount();
    
    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * <a href="ArrayProperty.html#setValue(UJO,%20VALUE)">ArrayProperty.setValue(Ujo,Object)</a> 
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see ArrayProperty#setValue(Ujo,Object)
     */

    public void writeValue(final UjoProperty property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        data[property.getIndex()] = value;
    }
    

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * <a href="ArrayProperty.html#getValue(UJO)">ArrayProperty.getValue(Ujo)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see ArrayProperty#getValue(Ujo)
     */    
    public Object readValue(final UjoProperty property) {
        Object result = data[property.getIndex()];
        return result!=null ? result : property.getDefault() ;
    }
    
    // --------- STATIC METHODS -------------------
    
    /** A Property Factory 
     * @hidden     
     */
    protected static <UJO extends Ujo,VALUE> ArrayProperty<UJO,VALUE> newProperty
    ( String name
    , Class<VALUE> type
    , int index
    ) {
        return new ArrayProperty<UJO,VALUE> (name, type, index);
    }

    /** A Property Factory 
     * @hidden     
     */
    protected static <UJO extends Ujo, VALUE> ArrayProperty<UJO, VALUE> newProperty
    ( String name
    , VALUE value
    , int index
    ) {
        return new ArrayProperty<UJO, VALUE>(name, value, index);
    }

    /** A Property Factory 
     * @hidden     
     */
    protected static <UJO extends Ujo, ITEM> ArrayPropertyList<UJO,ITEM> newPropertyList
    ( String name
    , Class<ITEM> type
    , int index
    ) {
        return new ArrayPropertyList<UJO,ITEM> (name, type, index);
    }
    
}
