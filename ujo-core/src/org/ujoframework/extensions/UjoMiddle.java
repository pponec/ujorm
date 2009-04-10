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

package org.ujoframework.extensions;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * This is an <strong>middle extended Ujo</strong> interface designed for a more conventional property access evaluated by developers.
 * 
 *<br>Sample of usage:
 *<pre class="pre"><span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> MapUjoExt {
 *
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> MapProperty&lt;Person, String &gt; NAME = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Name</span><span class="java-string-literal">&quot;</span>, String.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> MapProperty&lt;Person, Double &gt; CASH = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Cash</span><span class="java-string-literal">&quot;</span>, Double.<span class="java-keywords">class</span>);
 *  <span class="java-keywords">public</span> <span class="java-keywords">static final</span> MapProperty&lt;Person, Person&gt; CHILD = newProperty(<span class="java-string-literal">&quot;</span><span class="java-string-literal">Child</span><span class="java-string-literal">&quot;</span>, Person.<span class="java-keywords">class</span>);
 *    
 *  <span class="java-keywords">public</span> <span class="java-keywords">void</span> init() {
 *    set(NAME, <span class="java-string-literal">&quot;George&quot;</span>);
 *    String name = get(NAME);
 *    <span class="java-keywords">double</span> cash = get(CHILD, CASH);
 *  }
 *}</pre>
 * @author Paul Ponec
 * @since UJO release 0.85
 */
public interface UjoMiddle<UJO_IMPL extends UjoMiddle> extends Ujo {
    
    /** Getter based on one UjoProperty */
    public <UJO extends UJO_IMPL, VALUE> VALUE get
        ( UjoProperty<UJO, VALUE> property);

    /** Setter  based on UjoProperty. Type of value is checked in the runtime. */
    public <UJO extends UJO_IMPL, VALUE> Ujo set
        ( UjoProperty<UJO, VALUE> property
        , VALUE value);
        
}
