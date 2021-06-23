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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;

/**
 * This is an <strong>middle extended Ujo</strong> interface designed for a more conventional key access evaluated by developers.
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
 *    String name = getList(NAME);
 *    <span class="java-keywords">double</span> cash = getList(CHILD, CASH);
 *  }
 *}</pre>
 * @author Pavel Ponec
 * @since UJO release 0.85
 */
public interface UjoMiddle<U extends UjoMiddle> extends Ujo {

    /** Getter based on one Key */
    @SuppressWarnings("unchecked")
    @Nullable
    default <VALUE> VALUE get
        ( @Nonnull final Key<? super U, VALUE> key) {
            return key.of((U)this);
        }

    /** The setter  based on a composite Key.
     * If the {@code Key} argument is type of {@link CompositeKey} the method creates all missing relations.
     * <h4>See the next correct use case:</h4>
     * <pre class="pre">
     *   Person person = new Person();
     *   person.set(Person.MOTHER.add(Person.MOTHER).add(Person.NAME), "grandMothersName");
     * </pre>
     * Every <strong>set()</strong> method creates a new mother's instance (type of Person)  before assigning its name.
     * @see CompositeKey#setValue(org.ujorm.Ujo, java.lang.Object, boolean)
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    default <VALUE> UjoMiddle<?> set
        ( @Nonnull final Key<? super U, VALUE> key
        , VALUE value) {
            key.setValue((U)this, value);
            return this;
        }

    /** Get a <strong>not null</strong> list result */
    @SuppressWarnings("unchecked")
    default <VALUE> List<VALUE> getList(final ListKey<? super U, VALUE> key) {
        return key.getList((U)this);
    }

    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     * <br>The method can be an alias for a method readValueString(...)
     *
     * @param key A Property
     * @return If key type is "container" then result is null.
     */
    public String getText(final Key key);

    /**
     * Set value from a String format by a NULL context. Property can't be an "container" type (Ujo, List, Object[]).
     * <br>The method can be an alias for a method writeValueString(...)
     * @param key Property
     * @param Key String value
     */
    public void setText(final Key key, final String Key);

    /** The same method as the {@link #readKeys()} with a different generic type.
     * @see #readKeys()
     */
    public KeyList<U> readKeyList();
}
