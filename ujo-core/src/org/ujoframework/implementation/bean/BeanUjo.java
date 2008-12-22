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

package org.ujoframework.implementation.bean;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.SuperUjo;
import org.ujoframework.extensions.ValueAgent;

/**
 * This abstract class is inteded for an easy implementation of UJO features to a completed JavaBean object.<br>
 * A "name" of a BeanProperty is a JavaBean property name. By the "name" is deduced a setter and getter method name.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: an easy support of BEAN objects and the smallest memory footprint per one object.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujoframework.implementation.BEAN.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> BeanUjo {
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> BeanProperty &lt;Person,String &gt; NAME  = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;name&quot;</span> , String.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> BeanProperty &lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;male&quot;</span> , Boolean.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static</span> <span class="java-keywords">final</span> BeanProperty &lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;birth&quot;</span>, Date.<span class="java-keywords">class</span>);
 *
 *    <span class="java-block-comment">// ---- A STANDARD BEAN IMPLEMENTATION ----</span>
 *
 *    <span class="java-keywords">private</span> String  name;
 *    <span class="java-keywords">private</span> Boolean male;
 *    <span class="java-keywords">private</span> Integer age ;
 *
 *    <span class="java-keywords">public</span> String <span class="java-layer-method">getName</span>() {
 *        <span class="java-keywords">return</span> name;
 *    }
 *    <span class="java-keywords">public</span> <span class="java-keywords">void</span> <span class="java-layer-method">setName</span>(String name) {
 *        <span class="java-keywords">this</span>.name = name;
 *    }
 *    <span class="java-keywords">public</span> Boolean <span class="java-layer-method">getMale</span>() {
 *        <span class="java-keywords">return</span> male;
 *    }
 *    <span class="java-keywords">public</span> <span class="java-keywords">void</span> <span class="java-layer-method">setMale</span>(Boolean male) {
 *        <span class="java-keywords">this</span>.male = male;
 *    }
 *    <span class="java-keywords">public</span> Integer <span class="java-layer-method">getAge</span>() {
 *        <span class="java-keywords">return</span> age;
 *    }
 *    <span class="java-keywords">public</span> <span class="java-keywords">void</span> <span class="java-layer-method">setAge</span>(Integer age) {
 *        <span class="java-keywords">this</span>.age = age;
 *    }
 * }
 * </pre>
 *
 * @see BeanProperty
 * @author Pavel Ponec
 * @composed 1 - * BeanProperty
  */
public abstract class BeanUjo extends SuperUjo {
    
    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * BeanProperty.setValue(Ujo,Object) 
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then method throws an IllegalArgumentException.
     *
     * @see BeanProperty#setValue(Ujo,Object) BeanProperty.setValue(Ujo,Object)
     */
    @SuppressWarnings("unchecked")
    public void writeValue(final UjoProperty property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        ((ValueAgent) property).writeValue(this, value);
    }
    
    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * BeanProperty.getValue(Ujo)
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method throws an IllegalArgumentException.
     *
     * @see BeanProperty#getValue(Ujo) BeanProperty.getValue(Ujo)
     */
    @SuppressWarnings("unchecked")
    public Object readValue(final UjoProperty property) {
        Object result = ((ValueAgent) property).readValue(this);
        return result!=null ? result : property.getDefault() ;
    }
    
    // --------- STATIC METHODS -------------------
    
    /** A Property Factory, a default value is null.
     * @hidden     
     */
    protected static <UJO extends Ujo,VALUE> BeanProperty<UJO, VALUE> newProperty(String name, Class<VALUE> type) {
        return new BeanProperty<UJO,VALUE> (name, type);
    }
    
    /** A Property Factory, a property type is related from the default value.
     * @hidden     
     */
    protected static <UJO extends Ujo, VALUE> BeanProperty<UJO, VALUE> newProperty(String name, VALUE value) {
        return new BeanProperty<UJO, VALUE>(name, value);
    }

    /** A PropertyList Factory for a <strong>BeanUjo</strong> object
     * @hidden     
     */
    protected static <UJO extends Ujo, ITEM> BeanPropertyList<UJO, ITEM> newPropertyList(String name, Class<ITEM> type) {
        return new BeanPropertyList<UJO,ITEM> (name, type);
    }
    
}
