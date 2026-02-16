/*
 * Copyright 2007-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <a href="http://ujorm.org/" class="undecor"><img style="width: 32px; height: 32px;" alt="UJO Icons" src="doc-files/ujo32.png">
 * UJO</a> means a <i>Unified Java Object</i> and its implementations provides a similar service like a JavaBeans class.
 * Ujo is a basic interface of the <strong>Ujorm</strong> together with an interface <code>Key</code> .
 * <p>Basic two methods are <code>writeProperty(..)</code> and <code>readProperty(..)</code> for a manipulation with a value;
 * next method <code>readAuthorization(..)</code> recommends an authorization for a required action, selected Property and context;
 * the last method returns all keys of current UJO object...</p>
 * See a <a href="package-summary.html#UJO">general prologue</a> for more information or see some implementations.
 *
 *<p>The fastest way to use the interface is to extend an abstract parent:.</p>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.core.*;
 * <span class="java-keywords">public class</span> Person <span class="java-keywords">extends</span> AbstractUjo {
 *    <span class="java-keywords">private static final</span> KeyFactory fa = <span class="java-layer-method">newFactory</span>(Person.<span class="java-keywords">class</span>);
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,String &gt; NAME  = fa.<span class="java-layer-method">newKey</span>();
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Boolean&gt; MALE  = fa.<span class="java-layer-method">newKey</span>();
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Date   &gt; BIRTH = fa.<span class="java-layer-method">newKey</span>();
 *
 *    <span class="java-annotation">@</span>Override
 *    <span class="java-keywords">public</span> <span class="java-keywords">KeyList</span>&lt;?&gt; <span class="java-layer-method">readKeys(</span>() {
 *        <span class="java-keywords">return</span> fa.<span class="java-layer-method">getKeys</span>();
 *    }
 * }
 * </pre>
 *
 * @author Pavel Ponec
 * @assoc - - - Key
 * @composed - - 1 KeyList
 * @opt attributes
 * @opt operations
 */
public interface Ujo<UJO extends Object> {


    /** It is a <strong>common</strong> method for reading all object values, however there is strongly recomended to use a method
     *
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and convertors.
     * <br>NOTE: A reaction on an incorrect key depends on the implementation.
     *
     * @param key The Key must be a direct type only!
     * @return Property value
     * @see Key#getValue(UJO)
     */
    Object readValue(@NotNull Key<?,?> key);


    /** It is a <strong>common</strong> method for writing all object values, however there is strongly recomended to use a method
     * {@link Key#setValue(UJO,Object)}
     * to an external access for a better type safe.
     * The method have got a <strong>strategy place</strong> for an implementation of several listeners and validators.
     * <br>NOTE: A reaction on an incorrect key depends on the implementation.
     *
     * @param key Property must be a direct type only!
     * @param value Value
     * @see Key#setValue(UJO,Object)
     */
    void writeValue(@NotNull Key<?,?> key, @Nullable Object value);

    /** Returns all direct keys.
     * There is recommended to be a "name" of each key is unique (but it is NOT a necessary condition).
     *
     * <br>An index key in the array UJO must be unique a continuous, an order of key array depends on an implementation of UJO object.
     */
    <U extends Ujo> KeyList<U> readKeys();

}
