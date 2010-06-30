/*
 *  Copyright 2008-2010 Pavel Ponec
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

package org.ujoframework.implementation.field;

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.AbstractUjoExt;
import org.ujoframework.extensions.ValueAgent;


/**
 * The abstract Ujo implementation use a direct access to its private object fiels.
 * An performance is similar like a MapUjo implementation however the FieldUjo solution
 * have got smaller memore consumption.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="keyword-directive">public</span> <span class="keyword-directive">class</span> Person <span class="keyword-directive">extends</span> FieldUjoExt {
 *   
 *   <span class="keyword-directive">private</span> Long cash;
 *   <span class="keyword-directive">private</span> List&lt;Person&gt; childs;
 *   
 *   <span class="keyword-directive">public static</span> UjoProperty&lt;Person,Long&gt; CASH
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
 *   <span class="keyword-directive">public static</span> UjoPropertyList&lt;Person,Person&gt; CHILDS
 *     = newListProperty(<span class="character">"</span><span class="character">CHILDS</span><span class="character">"</span>, Person.<span class="keyword-directive">class</span>
 *     , <span class="keyword-directive">new</span> ValueAgent&lt;Person,List&lt;Person&gt;&gt;() {
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> writeValue(Person ujo, List&lt;Person&gt; value) {
 *         ujo.childs = value; 
 *     }
 *     <span class="keyword-directive">public</span> List&lt;Person&gt; readValue(Person ujo) {
 *         <span class="keyword-directive">return</span> ujo.childs; 
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
     * <br>NOTE: If property is an incorrect then method throws an IllegalArgumentException.
     *
     * @see FieldProperty#setValue(Ujo,Object) FieldProperty.setValue(Ujo,Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void writeValue(final UjoProperty property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        ((ValueAgent) property).writeValue(this, value);
    }
    
    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * FieldProperty.getValue(Ujo)
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method throws an IllegalArgumentException.
     *
     * @see FieldProperty#getValue(Ujo) FieldProperty.getValue(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object readValue(final UjoProperty property) {
        return ((ValueAgent) property).readValue(this);
    }
    
    
    // --------- STATIC METHODS -------------------
    
    /** Returns a new instance of property where the default value is null.
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
