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
 * <br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapImplUjoMiddle {
 *
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> MapProperty&lt;Person, String &gt; NAME = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Name</span><span class="java-string-literal">&quot;</span>, String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> MapProperty&lt;Person, Double &gt; CASH = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Cash</span><span class="java-string-literal">&quot;</span>, Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> MapProperty&lt;Person, Person&gt; CHILD = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Child</span><span class="java-string-literal">&quot;</span>, Person.<span class="java-keywords">class</span>);
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
    public <UJO extends UJO_IMPL, VALUE> VALUE get(final UjoProperty<UJO, VALUE> property) {
        return (VALUE) ((UjoProperty)property).of(this);
    }

    /** Setter  based on UjoProperty. Type of value is checked in the runtime. */
    public <UJO extends UJO_IMPL, VALUE> Ujo set(final UjoProperty<UJO, VALUE> property, final VALUE value) {
        assert readUjoManager().assertDirectAssign(property, value);
        ((UjoProperty)property).setValue(this, value);
        return this;
    }



}
