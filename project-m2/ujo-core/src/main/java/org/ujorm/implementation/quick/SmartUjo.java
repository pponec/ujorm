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
package org.ujorm.implementation.quick;

import java.util.List;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.extensions.UjoMiddle;

/**
 * This is a fast implementation of the <code>UjoMiddle</code>.
 * For implementation define only a "public static final Key" constants call a static method init() from the static block located after the latest property.
 * <br>All keys must be objects (no primitive types) in the current version of Ujorm.
 * <br>Features: good performance, simple code.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.implementation.quick.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> SmartUjo&lt;Person&gt; {
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,String &gt; NAME  = <span class="java-layer-method">newKey</span>();
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newKey</span>();
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> Key&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newKey</span>();
 *
 *    <span class="java-keywords">static</span> {
 *        init(Person.<span class="java-keywords">class</span>);
 *    }
 * }</pre>
 *
 * @see UjoMiddle
 * @author Pavel Ponec
 * @composed 1 - * Property
 */
abstract public class SmartUjo<UJO_IMPL extends SmartUjo>
    extends QuickUjo
    implements UjoMiddle<UJO_IMPL>
{

    /** Constructor */
    public SmartUjo(Object[] aData) {
        super(aData);
    }

    /** No constructor parameters  */
    public SmartUjo() {
    }

    /** Getter based on one Key */
    @SuppressWarnings("unchecked")
    final public <UJO extends UJO_IMPL, VALUE> VALUE get(final Key<UJO, VALUE> key) {
        return key.of((UJO) this);
    }

    /** The setter  based on Key. Type of value is checked in the runtime. */
    @SuppressWarnings("unchecked")
    final public <UJO extends UJO_IMPL, VALUE> Ujo set(final Key<UJO, VALUE> key, final VALUE value) {
        key.setValue((UJO)this, value);
        return this;
    }

    /** The setter  based on a composite Key.
     * The method creates all missing relations of the CompositeKey.
     * <h4>See the next correct use case:</h4>
     * <pre class="pre">
     *   Person person = new Person();
     *   person.set(Person.MOTHER.add(Person.MOTHER).add(Person.NAME), "grandMothersName");
     * </pre>
     * Every <strong>set()</strong> method creates a new mother's instance (type of Person)  before assigning its name.
     * @see CompositeKey#setValue(org.ujorm.Ujo, java.lang.Object, boolean)
     */
    @SuppressWarnings("unchecked")
    final public <UJO extends UJO_IMPL, VALUE> Ujo set(final CompositeKey<UJO, VALUE> key, final VALUE value) {
        key.setValue((UJO)this, value, true);
        return this;
    }

    /** Get a not null result */
    @SuppressWarnings("unchecked")
    final public <UJO extends UJO_IMPL, VALUE> List<VALUE> getList(final ListKey<UJO, VALUE> key) {
        return key.getList((UJO)this);
    }

    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     *
     * @param property A Property
     * @return If property type is "container" then result is null.
     */
    public String getText(final Key property) {
        return readUjoManager().getText(this, property, null);
    }

    /**
     * Set value from a String format by a NULL context. Types Ujo, List, Object[] are not supported by default.
     * <br>The method is an alias for a method writeValueString(...)
     * @param property Property
     * @param value String value
     */
    public void setText(final Key property, final String value) {
        readUjoManager().setText(this, property, value, null, null);
    }

}
