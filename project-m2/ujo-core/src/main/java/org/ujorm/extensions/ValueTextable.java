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

package org.ujorm.extensions;

/**
 * A special interface for a terminology only, an implementation is not reasnonable in the Ujorm.
 * ValueTextable is every one object, wich have got implemented method toString() so that the result can be used
 * to restore a new equal object by its a single constructor parameter type of String.
 * <br>This is a "ValueTextable" test for an <strong>Integer</strong> class:
 * <pre class="pre">
 *  Integer textable1 = <span class="java-keywords">new</span> <span class="java-layer-method">Integer</span>(<span class="java-numeric-literals">7</span>);
 *  Integer textable2 = <span class="java-keywords">new</span> <span class="java-layer-method">Integer</span>(textable1.<span class="java-layer-method">toString()</span>);
 *  <span class="java-keywords">boolean</span> result = textable1.<span class="java-layer-method">equals</span>(textable2);
 * </pre>
 * <br>Some completed ValueTextable classes from a Java API are
 * <ul>
 *   <li>Boolean</li>
 *   <li>Short</li>
 *   <li>Integer</li>
 *   <li>Long</li>
 *   <li>Float</li>
 *   <li>Double</li>
 *   <li>String</li>
 * </ul>
 * These classes have <strong>NOT</strong> behaviour of ValueTextable, <br>however Ujorm supports these types similar like ValueTextable:
 * <ul>
 *   <li>Byte</li>
 *   <li>Character</li>
 *   <li>java.util.Date</li>
 *   <li>java.sql.Date (time 0:00:00.000)</li>
 *   <li>byte[]</li>
 *   <li>char[]</li>
 *   <li>Locale</li>
 *   <li>Color</li>
 *   <li>Dimension</li>
 *   <li>Rectangle</li>
 *   <li>Enum</li>
 *   <li>Class</li>
 *   <li>Charset</li>
 *   <li>List&lt;some-type-above&gt; with some restrictions for <strong>deserialization</strong>:<br>
 *       value key must be type of UjoPropertyList and
 *       the serialized text must not contain a separator character comma ','
 *   </li>
 * </ul>
 * 
 * 
 * @author Pavel Ponec
 * @see UjoTextable
 */
public interface ValueTextable {
    
    /** A result must be acceptable for one constructor parameter (of the same class) to restore an equal object. */
    @Override
    public String toString();
    
}
