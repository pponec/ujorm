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

package org.ujoframework.implementation.quick;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.AbstractUjo;
import org.ujoframework.extensions.ListProperty;

/**
 * This is a very fast abstract implementation of <code>Ujo</code>.
 * For implementation define only a "public static final UjoProperty" constants call a static method init() from the static block located after the latest property.
 * The code syntax is Java 1.5 complied.
 * <br>All properties must be objects (no primitive types) in the current version of UJO Framework.
 * <br>Features: very good performance, quick coding.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujoframework.implementation.quick.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> QuickUjo {
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,String &gt; NAME  = <span class="java-layer-method">newProperty</span>(String.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newProperty</span>(Boolean.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newProperty</span>(Date.<span class="java-keywords">class</span>);
 *
 *    <span class="java-keywords">static</span> {
 *        init(Person.<span class="java-keywords">class</span>);
 *    }
 * }</pre>
 * 
 * @see Property
 * @author Pavel Ponec
 * @composed 1 - * Property
 */
public abstract class QuickUjo extends AbstractUjo {
       
    /** Object data */
    protected Object[] data;
    
    /** Constructor */
    public QuickUjo() {
        data = new Object[readUjoManager().readProperties(getClass()).length];
    }

    /** Constructor */
    public QuickUjo(Object[] data) {
        this.data = data;
    }

    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * <a href="Property.html#setValue(UJO,%20VALUE)">Property.setValue(Ujo,Object)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see Property#setValue(Ujo,Object)
     */

    public void writeValue(final UjoProperty property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        data[property.getIndex()] = value;
    }
    

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * <a href="Property.html#getValue(UJO)">Property.getValue(Ujo)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method can throws an ArrayIndexOutOfBoundsException.
     *
     * @see Property#getValue(Ujo)
     */    
    public Object readValue(final UjoProperty property) {
        Object result = data[property.getIndex()];
        return result!=null ? result : property.getDefault() ;
    }
    
    // --------- STATIC METHODS -------------------


    /** A Property Factory creates new property and assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( String name
    , Class<VALUE> type
    , VALUE defaultValue
    , int index
    , boolean lock
    ) {
        return Property.newInstance(name, type, defaultValue, index, lock);
    }

    
    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( String name
    , Class<VALUE> type
    ) {
        return newProperty(name, type, null, -1, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( String name
    , VALUE value
    ) {
        return newProperty(name, null, value, -1, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( Class<VALUE> type
    ) {
        return newProperty(null, type, null, -1, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( VALUE value
    ) {
        return newProperty(null, null, value, -1, false);
    }

    /** Returns a new instance of property where the default value is null.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends QuickUjo,VALUE> Property<UJO,VALUE> newProperty(UjoProperty p) {
        return Property.newInstance(p.getName(), p.getType(), p.getDefault(), -1, false);
    }



    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListProperty
    ( String name
    , Class<ITEM> itemType
    ) {
        return ListProperty.newListProperty(name, itemType, -1, false);
    }
    
}
