/*
 *  Copyright 2008-2014 Pavel Ponec
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

package org.ujorm.implementation.field;

import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.AbstractUjoExt;
import org.ujorm.extensions.ValueAgent;


/**
 * The abstract Ujo implementation use a direct access to its private object fiels.
 * An performance is similar like a MapUjo implementation however the FieldUjo solution
 * have got smaller memore consumption.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="keyword-directive">public</span> <span class="keyword-directive">class</span> Person <span class="keyword-directive">extends</span> FieldUjoExt {
 *   
 *   <span class="keyword-directive">private</span> Long cash;
 *   <span class="keyword-directive">private</span> List&lt;Person&gt; children;
 *   
 *   <span class="keyword-directive">public static</span> Key&lt;Person,Long&gt; CASH
 *     = newProperty(<span class="character">"CASH"</span>, Long.<span class="keyword-directive">class</span>
 *     , <span class="keyword-directive">new</span> ValueAgent&lt;Person,Long&gt;() {
 *     <span class="keyword-directive">public void</span> writeValue(
 *         Person ujo, Long value) { 
 *            ujo.cash = value; 
 *         }
 *     <span class="keyword-directive">public</span> Long readValue (Person ujo) { 
 *         <span class="keyword-directive">return</span> ujo.cash;  
 *     }
 *   });    
 *   
 *   <span class="keyword-directive">public static</span> UjoPropertyList&lt;Person,Person&gt; CHILDREN
 *     = newListProperty(<span class="character">"</span><span class="character">CHILDREN</span><span class="character">"</span>, Person.<span class="keyword-directive">class</span>
 *     , <span class="keyword-directive">new</span> ValueAgent&lt;Person,List&lt;Person&gt;&gt;() {
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> writeValue(Person ujo, List&lt;Person&gt; value) {
 *         ujo.children = value; 
 *     }
 *     <span class="keyword-directive">public</span> List&lt;Person&gt; readValue(Person ujo) {
 *         <span class="keyword-directive">return</span> ujo.children; 
 *     }
 *   });
 * }</pre>
 *
 * @see FieldProperty
 * @author Pavel Ponec
 * @composed 1 - * FieldProperty
 * @since ujo-tool
 */
abstract public class FieldUjoExt<UJO extends FieldUjoExt> extends AbstractUjoExt<UJO> {
    
    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * FieldProperty.setValue(Ujo,Object) 
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If key is an incorrect then method throws an IllegalArgumentException.
     *
     * @see FieldProperty#setValue(Ujo,Object) FieldProperty.setValue(Ujo,Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void writeValue(final Key key, final Object value) {
        assert UjoManager.assertDirectAssign(key, value, this);
        ((ValueAgent) key).writeValue(this, value);
    }
    
    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * FieldProperty.getValue(Ujo)
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If key is an incorrect then method throws an IllegalArgumentException.
     *
     * @see FieldProperty#getValue(Ujo) FieldProperty.getValue(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object readValue(final Key key) {
        return ((ValueAgent) key).readValue(this);
    }
    
    
    // --------- STATIC METHODS -------------------
    
    /** Returns a new instance of key where the default value is null.
     * @hidden     
     */
    protected static <UJO extends Ujo,VALUE> FieldProperty<UJO, VALUE> newProperty
        ( String name
        , Class<VALUE> type
        , ValueAgent<UJO, VALUE> agent
        ) {
        return new FieldProperty<UJO,VALUE> (name, type, -1, agent);
    }
    
    /** A Property Factory
     * @hidden     
     */
    protected static <UJO extends Ujo, VALUE> FieldProperty<UJO, VALUE> newProperty
        ( String name
        , VALUE value
        , ValueAgent<UJO, VALUE> agent
        ) {
        return new FieldProperty<UJO, VALUE>(name, value, -1, agent);
    }

    /** A ListProperty Factory for a <strong>FieldUjo</strong> object
     * @hidden     
     */
    protected static <UJO extends Ujo, ITEM> FieldPropertyList<UJO, ITEM> newListProperty
        ( String name
        , Class<ITEM> type
        , ValueAgent<UJO, List<ITEM>> agent
        ) {
        return new FieldPropertyList<UJO,ITEM> (name, type, -1, agent);
    }

}    
