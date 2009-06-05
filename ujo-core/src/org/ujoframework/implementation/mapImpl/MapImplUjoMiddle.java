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
package org.ujoframework.implementation.mapImpl;

import org.ujoframework.implementation.map.*;
import java.util.HashMap;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoMiddle;

/**
 * This is an middle extended implementation of a setter and getter methods for an easier access for developpers.
 * <br />Notes:
 * <ul>
 *   <li>the NULL key is not supported in this implementation</li>
 *   <li>the map implementation is a proxy to an internal Map&lt;String,Object&gt; object</li>
 * </ul>
 * <br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapImplUjoMiddle {
 *
 *  <span class="java-keywords">public static final</span> UjoProperty&lt;Person, String &gt; NAME = newProperty(<span class="java-string-literal">&quot;Name&quot;</span> , String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static final</span> UjoProperty&lt;Person, Double &gt; CASH = newProperty(<span class="java-string-literal">&quot;Cash&quot;</span> , Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static final</span> UjoProperty&lt;Person, Person&gt; CHILD = newProperty(<span class="java-string-literal">&quot;Child&quot;</span>, Person.<span class="java-keywords">class</span>);
 *    
 *  <span class="java-keywords">public</span> <span class="java-keywords">void</span> init() {
 *    set(NAME, <span class="java-string-literal">&quot;</span><span class="java-string-literal">George</span><span class="java-string-literal">&quot;</span>);
 *    String name = get(NAME);
 *  }
 *}</pre>
 * 
 * @see MapProperty
 * @author Paul Ponec
 * @since UJO release 0.85
 */
abstract public class MapImplUjoMiddle<UJO_IMPL extends MapImplUjoMiddle>
    extends MapImplUjo
    implements UjoMiddle<UJO_IMPL>
{

    public MapImplUjoMiddle(HashMap<String,Object> aData) {
        super(aData);
    }

    /** No parameters constuctor */
    public MapImplUjoMiddle() {
    }

    /** Getter based on one UjoProperty */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> VALUE get(final UjoProperty<UJO, VALUE> property) {
        return (VALUE) ((UjoProperty)property).of(this);
    }

    /** Setter  based on UjoProperty. Type of value is checked in the runtime. */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> Ujo set(final UjoProperty<UJO, VALUE> property, final VALUE value) {
        assert readUjoManager().assertDirectAssign(property, value);
        ((UjoProperty)property).setValue(this, value);
        return this;
    }

    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     *
     * @param property A Property
     * @return If property type is "container" then result is null.
     */
    public String getText(final UjoProperty property) {
        return readUjoManager().getText(this, property, null);
    }

    /**
     * Set value from a String format by a NULL context. Types Ujo, List, Object[] are not supported by default.
     * <br>The method is an alias for a method writeValueString(...)
     * @param property Property
     * @param value String value
     */
    public void setText(final UjoProperty property, final String value) {
        readUjoManager().setText(this, property, value, null, null);
    }




}
