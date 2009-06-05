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

import java.util.HashMap;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.AbstractUjo;

/**
 * This is a simple abstract implementation of <code>Ujo</code>.<br>
 * Child implementation can use "public static final MapProperty" constants for its UjoProperties.
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
 * @see MapProperty
 * @author Pavel Ponec
 * @composed 1 - * MapProperty
  */
public abstract class MapUjo extends AbstractUjo {
    
    /** Object data */
    final protected HashMap<UjoProperty,Object> data;
    
    /** Constructor */
    public MapUjo() {
        data = new HashMap<UjoProperty,Object>();
    }

    /** Constructor */
    protected MapUjo(HashMap<UjoProperty,Object> aData) {
        data = aData;
    }
    

    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method 
     * <a href="MapProperty.html#setValue(UJO,%20VALUE)">MapProperty.setValue(Ujo,Object)</a> 
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators. 
     * <br>NOTE: If property is an incorrect then no exception is throwed.
     *
     * @see MapProperty#setValue(Ujo,Object)
     */
    public void writeValue(final UjoProperty property, final Object value) {
        assert readUjoManager().assertDirectAssign(property, value);       
        data.put(property, value);
    }
    

    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method 
     * <a href="MapProperty.html#getValue(UJO)">MapProperty.getValue(Ujo)</a>
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors. 
     * <br>NOTE: If property is an incorrect then method returns a null value.
     *
     * @see MapProperty#getValue(Ujo)
     */
    public Object readValue(final UjoProperty property) {
        Object result = data.get(property);
        return result!=null ? result : property.getDefault() ;
    }
    
    // --------- STATIC METHODS -------------------
    
    /** Returns a new instance of property where the default value is null.
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> MapProperty<UJO,VALUE> newProperty(String name, Class<VALUE> type) {
        return new MapProperty<UJO,VALUE> (name, type, _nextPropertyIndex());
    }
    
    /** A Property Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> MapProperty<UJO, VALUE> newProperty(String name, VALUE value) {
        return new MapProperty<UJO, VALUE>(name, value, _nextPropertyIndex());
    }
    
    /** A PropertyList Factory
     * Method assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo, ITEM> MapPropertyList<UJO,ITEM> newPropertyList(String name, Class<ITEM> type) {
        return new MapPropertyList<UJO,ITEM> (name, type, _nextPropertyIndex());
    }
    
}
