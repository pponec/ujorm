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

package org.ujoframework.implementation.map;

import java.io.Serializable;
import java.util.HashMap;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.AbstractUjo;

/**
 * This is a simple abstract implementation of <code>Ujo</code>.<br>
 * Child implementation can use "public static final UjoProperty" constants for its UjoProperties.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: very simple implementaton and a sufficient performance for common tasks. The architecture is useful for a rare assignment of values in object too.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujoframework.implementation.map.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjo {
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,String &gt; NAME  = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;name&quot;</span> , String.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;male&quot;</span> , Boolean.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newProperty</span>(<span class="java-string-literal">&quot;birth&quot;</span>, Date.<span class="java-keywords">class</span>);
 *
 * }
 * </pre>
 *
 * @see Property
 * @author Pavel Ponec
 * @composed 1 - * Property
  */
public abstract class MapUjo extends AbstractUjo implements Serializable {
    
    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 977567L;

    /** Object data. Unauthorized writing is not allowed. */
    final private HashMap<UjoProperty,Object> data;
    
    /** Constructor */
    public MapUjo() {
        data = new HashMap<UjoProperty,Object>();
    }

    /** Constructor */
    protected MapUjo(HashMap<UjoProperty,Object> aData) {
        data = aData;
    }
    

    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * <a href="../../extensions/Property.html#setValue(UJO,%20VALUE)">Property.setValue(Ujo,Object)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then no exception is throwed.
     *
     * @see Property#setValue(Ujo,Object)
     */
    public void writeValue(final UjoProperty property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        data.put(property, value);
    }
    

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * <a href="../../extensions/Property.html#getValue(UJO)">Property.getValue(Ujo)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method returns a null value.
     *
     * @see Property#getValue(Ujo)
     */
    public Object readValue(final UjoProperty property) {
        return data.get(property);
    }
    
    // --------- STATIC METHODS -------------------
    
    /** Returns a new instance of property where the default value is null.
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends MapUjo,VALUE> Property<UJO,VALUE> newProperty(String name, Class<VALUE> type) {
        return Property.newInstance(name, type);
    }

    /** A Property Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends MapUjo, VALUE> Property<UJO, VALUE> newProperty(String name, VALUE value) {
        return Property.newInstance(name, value);
    }
    
    /** Returns a new instance of property where the default value is null.
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends MapUjo,VALUE> Property<UJO,VALUE> newProperty(UjoProperty p, int index) {
        return Property.newInstance(p.getName(), p.getType(), p.getDefault(), index, true);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends MapUjo, ITEM> ListProperty<UJO,ITEM> newListProperty(String name, Class<ITEM> type) {
        return ListProperty.newListProperty(name, type);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @deprecated Use newListProperty(...) instead of.
     * @hidden
     */
    protected static final <UJO extends MapUjo, ITEM> ListProperty<UJO,ITEM> newPropertyList(String name, Class<ITEM> type) {
        return ListProperty.newListProperty(name, type);
    }


}
