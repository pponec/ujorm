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

package org.ujorm.implementation.bean;

import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.AbstractUjoExt;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.ValueAgent;


/**
 * This is an Groovy style implementation of a setter and getter methods for an easier access for developpers,
 * however the methods have got an weaker type control in compare to the MapUjo implementation.
 * <br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjoExt {
 *
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person, String &gt; NAME = newKey(<span class="java-string-literal">&quot;Name&quot;</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person, Double &gt; CASH = newKey(<span class="java-string-literal">&quot;Cash&quot;</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person, Person&gt; CHILD = newKey(<span class="java-string-literal">&quot;Child&quot;</span>);
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
 * @see BeanProperty
 * @author Pavel Ponec
 * @since UJO release 0.80 
 */
abstract public class BeanUjoExt<UJO extends BeanUjoExt> extends AbstractUjoExt<UJO> {
    
    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * BeanProperty.setValue(Ujo,Object) 
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then method throws an IllegalArgumentException.
     *
     * @see BeanProperty#setValue(Ujo,Object) BeanProperty.setValue(Ujo,Object)
     */
    @SuppressWarnings("unchecked")
    public void writeValue(final Key property, final Object value) {
        assert UjoManager.assertDirectAssign(property, value, this);
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
    @Override
    public Object readValue(final Key property) {
        return ((ValueAgent) property).readValue(this);
    }
    
    
    // --------- STATIC METHODS -------------------
    
    /** A Property Factory
     * @hidden     
     */
    protected static <UJO extends Ujo,VALUE> BeanProperty<UJO, VALUE> newKey(String name, Class<VALUE> type) {
        return new BeanProperty<UJO,VALUE> (name, type, Property.UNDEFINED_INDEX);
    }
    
    /** A Property Factory, a property type is related from the default value.
     * @hidden     
     */
    protected static <UJO extends Ujo, VALUE> BeanProperty<UJO, VALUE> newKey(String name, VALUE value) {
        return new BeanProperty<UJO, VALUE>(name, value, Property.UNDEFINED_INDEX);
    }

    /** A ListProperty Factory for a <strong>BeanUjo</strong> object
     * @hidden     
     */
    protected static <UJO extends Ujo, ITEM> BeanPropertyList<UJO, ITEM> newKeyList(String name, Class<ITEM> type) {
        return new BeanPropertyList<UJO,ITEM> (name, type, Property.UNDEFINED_INDEX);
    }

    // --------- DEPRECATED -------------------

    /** A Property Factory
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> BeanProperty<UJO, VALUE> newProperty(String name, Class<VALUE> type) {
        return new BeanProperty<UJO,VALUE> (name, type, Property.UNDEFINED_INDEX);
    }

    /** A Property Factory, a property type is related from the default value.
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> BeanProperty<UJO, VALUE> newProperty(String name, VALUE value) {
        return new BeanProperty<UJO, VALUE>(name, value, Property.UNDEFINED_INDEX);
    }

    /** A ListProperty Factory for a <strong>BeanUjo</strong> object
     * @deprecated Use method newKey(..) rather
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo, ITEM> BeanPropertyList<UJO, ITEM> newPropertyList(String name, Class<ITEM> type) {
        return new BeanPropertyList<UJO,ITEM> (name, type, Property.UNDEFINED_INDEX);
    }
        
}
