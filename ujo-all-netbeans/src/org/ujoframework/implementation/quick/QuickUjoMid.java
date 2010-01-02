/*
 *  Copyright 2007-2010 Pavel Ponec
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
package org.ujoframework.implementation.quick;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.UjoMiddle;

/**
 * This is a fast implementation of the <code>UjoMiddle</code>.
 * For implementation define only a "public static final UjoProperty" constants call a static method init() from the static block located after the latest property.
 * <br>All properties must be objects (no primitive types) in the current version of UJO Framework.
 * <br>Features: good performance, simple code.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujoframework.implementation.quick.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> QuickUjo {
 *
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,String &gt; NAME  = <span class="java-layer-method">newProperty</span>(String.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,Boolean&gt; MALE  = <span class="java-layer-method">newProperty</span>(Boolean.<span class="java-keywords">class</span>);
 *    <span class="java-keywords">public</span> <span class="java-keywords">static final</span> UjoProperty&lt;Person,Date   &gt; BIRTH = <span class="java-layer-method">newProperty</span>(Date.<span class="java-keywords">class</span>);
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
abstract public class QuickUjoMid<UJO_IMPL extends QuickUjoMid>
    extends QuickUjo
    implements UjoMiddle<UJO_IMPL>
{

    /** Constructor */
    public QuickUjoMid(Object[] aData) {
        super(aData);
    }

    /** Constructor */
    /** No parameters constuctor */
    public QuickUjoMid() {
    }

    /** Getter based on one UjoProperty */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> VALUE get(final UjoProperty<UJO, VALUE> property) {
        return property.of((UJO) this);
    }

    /** Setter  based on UjoProperty. Type of value is checked in the runtime. */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> Ujo set(final UjoProperty<UJO, VALUE> property, final VALUE value) {
        assert readUjoManager().assertDirectAssign(property, value);
        property.setValue((UJO)this, value);
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
