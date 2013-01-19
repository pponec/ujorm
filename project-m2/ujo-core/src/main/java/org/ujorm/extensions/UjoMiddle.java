/*
 *  Copyright 2007-2013 Pavel Ponec
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

package org.ujorm.extensions;

import org.ujorm.Ujo;
import org.ujorm.Key;

/**
 * This is an <strong>middle extended Ujo</strong> interface designed for a more conventional property access evaluated by developers.
 * 
 *<br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjoExt {
 *
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person, String &gt; NAME = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Name</span><span class="java-string-literal">&quot;</span>, String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person, Double &gt; CASH = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Cash</span><span class="java-string-literal">&quot;</span>, Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person, Person&gt; CHILD = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Child</span><span class="java-string-literal">&quot;</span>, Person.<span class="java-keywords">class</span>);
 *    
 *  <span class="java-keywords">public</span> <span class="java-keywords">void</span> init() {
 *    set(NAME, <span class="java-string-literal">&quot;George&quot;</span>);
 *    String name = get(NAME);
 *    <span class="java-keywords">double</span> cash = get(CHILD, CASH);
 *  }
 *}</pre>
 * @author Pavel Ponec
 * @since UJO release 0.85
 */
public interface UjoMiddle<UJO_IMPL extends UjoMiddle> extends Ujo {
    
    /** Getter based on one Key */
    public <UJO extends UJO_IMPL, VALUE> VALUE get
        ( Key<UJO, VALUE> key);

    /** Setter  based on Key. Type of value is checked in the runtime. */
    public <UJO extends UJO_IMPL, VALUE> Ujo set
        ( Key<UJO, VALUE> key
        , VALUE value);

    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     * <br>The method can be an alias for a method readValueString(...)
     *
     * @param key A Property
     * @return If property type is "container" then result is null.
     */
    public String getText(final Key key);



    /**
     * Set value from a String format by a NULL context. Property can't be an "container" type (Ujo, List, Object[]).
     * <br>The method can be an alias for a method writeValueString(...)
     * @param property Property
     * @param Key String value
     */
    public void setText(final Key property, final String Key);


}
