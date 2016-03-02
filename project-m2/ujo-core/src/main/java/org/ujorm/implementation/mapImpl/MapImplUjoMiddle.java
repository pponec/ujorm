/*
 *  Copyright 2007-2014 Pavel Ponec
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
package org.ujorm.implementation.mapImpl;

import java.io.Serializable;
import java.util.HashMap;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.core.UjoManager;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.UjoMiddle;

/**
 * This is an middle extended implementation of a setter and getter methods for an easier access for developpers.
 * <br />Notes:
 * <ul>
 *   <li>the NULL key is not supported in this implementation</li>
 *   <li>the map implementation is a proxy to an internal Map&lt;String,Object&gt; object</li>
 * </ul>
 * <br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapImplUjoMiddle&lt;Person&gt; {
 *
 *  <span class="java-keywords">public static final</span> Key&lt;Person, String &gt; NAME = newKey(<span class="java-string-literal">&quot;Name&quot;</span> , String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static final</span> Key&lt;Person, Double &gt; CASH = newKey(<span class="java-string-literal">&quot;Cash&quot;</span> , Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public static final</span> Key&lt;Person, Person&gt; CHILD = newKey(<span class="java-string-literal">&quot;Child&quot;</span>, Person.<span class="java-keywords">class</span>);
 *
 *  <span class="java-keywords">public</span> <span class="java-keywords">void</span> init() {
 *    set(NAME, <span class="java-string-literal">&quot;</span><span class="java-string-literal">George</span><span class="java-string-literal">&quot;</span>);
 *    String name = get(NAME);
 *  }
 *}</pre>
 *
 * @see Property
 * @author Pavel Ponec
 * @since UJO release 0.85
 */
abstract public class MapImplUjoMiddle<UJO extends MapImplUjoMiddle>
    extends MapImplUjo
    implements UjoMiddle<UJO>, Serializable
{

    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 977565L;

    public MapImplUjoMiddle(HashMap<String,Object> aData) {
        super(aData);
    }

    /** No parameters constructor */
    public MapImplUjoMiddle() {
    }

    /** Getter based on one Key */
    @SuppressWarnings("unchecked")
    public <VALUE> VALUE get(final Key<? super UJO, VALUE> key) {
        return key.of((UJO)this);
    }

    /** Setter  based on Key. Type of value is checked in the runtime. */
    @SuppressWarnings("unchecked")
    public <VALUE> Ujo set(final Key<? super UJO, VALUE> key, final VALUE value) {
        assert UjoManager.assertDirectAssign(key, value, this);
        key.setValue((UJO)this, value);
        return this;
    }

    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     *
     * @param key A Property
     * @return If key type is "container" then result is null.
     */
    public String getText(final Key key) {
        return readUjoManager().getText(this, key, null);
    }

    /**
     * Set value from a String format by a NULL context. Types Ujo, List, Object[] are not supported by default.
     * <br>The method is an alias for a method writeValueString(...)
     * @param key Property
     * @param value String value
     */
    public void setText(final Key key, final String value) {
        readUjoManager().setText(this, key, value, null, null);
    }

}
