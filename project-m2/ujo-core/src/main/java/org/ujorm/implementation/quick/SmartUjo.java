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
import javax.annotation.Nullable;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.extensions.UjoMiddle;

/**
 * This is a fast implementation of the <code>UjoMiddle</code>.
 * For implementation define only a "public static final Key" constants call a static method init() from the static block located after the latest Key.
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
abstract public class SmartUjo<UJO extends SmartUjo>
    extends QuickUjo
    implements UjoMiddle<UJO>
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
    @Override
    public final <VALUE> VALUE get(final Key<? super UJO, VALUE> key) {
        return key.of((UJO) this);
    }

    /** The setter  based on a composite Key.
     * If the {@link Key} argument is type of {@link CompositeKey} the method creates all missing relations.
     * <h4>See the next correct use case:</h4>
     * <pre class="pre">
     *   Person person = new Person();
     *   person.set(Person.MOTHER.add(Person.MOTHER).add(Person.NAME), "grandMothersName");
     * </pre>
     * Every <strong>set()</strong> method creates a new mother's instance (type of Person)  before assigning its name.
     * @see CompositeKey#setValue(org.ujorm.Ujo, java.lang.Object, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <VALUE> Ujo set(final Key<? super UJO, VALUE> key, final VALUE value) {
        key.setValue((UJO)this, value);
        return this;
    }

    /** Get a not null result */
    @SuppressWarnings("unchecked")
    @Override
    public final <VALUE> List<VALUE> getList(final ListKey<? super UJO, VALUE> key) {
        return key.getList((UJO)this);
    }

    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     *
     * @param key A Property
     * @return If key type is "container" then result is null.
     */
    @Override
    public String getText(final Key key) {
        return readUjoManager().getText(this, key, null);
    }

    /**
     * Set value from a String format by a NULL context. Types Ujo, List, Object[] are not supported by default.
     * <br>The method is an alias for a method writeValueString(...)
     * @param key Property
     * @param value String value
     */
    @Override
    public void setText(final Key key, final String value) {
        readUjoManager().setText(this, key, value, null, null);
    }

    @Override
    public KeyList<UJO> readKeyList() {
        return super.readKeys();
    }

    /** Clone the Ujo object */
    @Override
    public UJO clone(int depth, @Nullable Object context) {
        return (UJO) super.clone(depth, context);
    }
}
