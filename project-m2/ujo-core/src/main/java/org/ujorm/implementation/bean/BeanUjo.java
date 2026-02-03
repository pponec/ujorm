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

package org.ujorm.implementation.bean;

import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.SuperAbstractUjo;
import org.ujorm.extensions.ValueAgent;

/**
 * This abstract class is inteded for an easy implementation of UJO features to a completed JavaBean object.<br>
 * A "name" of a BeanProperty is a JavaBean property name. By the "name" is deduced a setter and getter method name.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: an easy support of BEAN objects and the smallest memory footprint per one object.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.implementation.BEAN.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> BeanUjo {
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,String &gt; NAME  = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;name&quot;</span> );
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;male&quot;</span> );
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newKey</span>(<span class="java-string-literal">&quot;birth&quot;</span>);
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
public abstract class BeanUjo extends SuperAbstractUjo {

    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method
     * BeanProperty.setValue(Ujo,Object)
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators.
     * <br>NOTE: If key is an incorrect then method throws an IllegalArgumentException.
     *
     * @see BeanProperty#setValue(Ujo,Object) BeanProperty.setValue(Ujo,Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void writeValue(final Key key, final Object value) {
        assert UjoManager.assertDirectAssign(key, value, this);
        ((ValueAgent) key).writeValue(this, value);
    }

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     * BeanProperty.getValue(Ujo)
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors.
     * <br>NOTE: If key is an incorrect then method throws an IllegalArgumentException.
     *
     * @see BeanProperty#getValue(Ujo) BeanProperty.getValue(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object readValue(final Key key) {
        return ((ValueAgent) key).readValue(this);
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of key where the default value is null.
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> BeanProperty<UJO, VALUE> newKey(String name) {
        return new BeanProperty<UJO,VALUE> (name, (Class)null, Property.UNDEFINED_INDEX);
    }

    /** A Property Factory, a key type is related from the default value.
     *  Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> BeanProperty<UJO, VALUE> newKey(String name, VALUE value) {
        return new BeanProperty<>(name, value, Property.UNDEFINED_INDEX);
    }

    /** A ListProperty Factory for a <strong>BeanUjo</strong> object.
     * Method assigns a next key index.
     * @hidden
     */
    protected static <UJO extends Ujo, ITEM> BeanPropertyList<UJO, ITEM> newListKey(String name) {
        return new BeanPropertyList<> (name, null, Property.UNDEFINED_INDEX);
    }

}
