/*
 *  Copyright 2009-2010 Pavel Ponec
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

import java.io.Serializable;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.extensions.AbstractUjo;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.ListProperty;

/**
 * This is a fast implementation of the <code>Ujo</code>.
 * For implementation define only a "public static final Key" constants call a static method init() from the static block located after the latest property.
 * <br>All keys must be objects (no primitive types) in the current version of Ujorm.
 * <br>Features: good performance, simple code.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="java-keywords">import</span> org.ujorm.implementation.quick.*;
 * <span class="java-keywords">public</span> <span class="java-keywords">class</span> Person <span class="java-keywords">extends</span> QuickUjo {
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
 * @see Property
 * @author Pavel Ponec
 * @composed 1 - * Property
 */
public abstract class QuickUjo extends AbstractUjo implements Serializable {

    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 754967L;

    /** {@see Property#UNDEFINED_INDEX} */
    private static final int UNDEFINED_INDEX = -1;
       
    /** Constructor */
    public QuickUjo() {
        super();
    }

    /** Constructor */
    public QuickUjo(Object[] data) {
        super(data);
    }
    
    // --------- STATIC METHODS -------------------


    /** A Property Factory creates new property and assigns a next property index.
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey
    ( String name
    , Class<VALUE> type
    , VALUE defaultValue
    , int index
    , boolean lock
    ) {
        return Property.newInstance(name, type, defaultValue, index, lock);
    }

    
    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newKey(String name) {
        return newKey(name, null, null, UNDEFINED_INDEX, false);
    }
    
    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newKey
    ( String name
    , VALUE value
    ) {
        return newKey(name, null, value, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newKey
    ( VALUE value
    ) {
        return newKey(null, null, value, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newKey() {
        return newKey(null, null, null, UNDEFINED_INDEX, false);
    }

    /** Returns a new instance of property where the default value is null.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends QuickUjo,VALUE> Property<UJO,VALUE> newKey(Key p) {
        return Property.newInstance(p.getName(), p.getType(), p.getDefault(), -1, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListKey( String name) {
        return ListProperty.newListProperty(name, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a name and next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    protected static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListKey() {
        return ListProperty.newListProperty((String)null, null);
    }

    // ------------- DEPRECATED METHODS ---------------------

    /** A Property Factory creates new property and assigns a next property index.
     * @deprecated Use the method newKey(...)
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( String name
    , Class<VALUE> type
    , VALUE defaultValue
    , int index
    , boolean lock
    ) {
        return Property.newInstance(name, type, defaultValue, index, lock);
    }


    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use rather a method {@link QuickUjo#newProperty(java.lang.String)} instead of this.
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( String name
    , Class<VALUE> type
    ) {
        return newProperty(name, type, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use the method newKey(...)
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty(String name) {
        return newProperty(name, null, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use the method newKey(...)
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( String name
    , VALUE value
    ) {
        return newProperty(name, null, value, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use rather a method {@link QuickUjo#newProperty()} instead of this,
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newProperty
    ( Class<VALUE> type
    ) {
        return newProperty(null, type, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty
    ( VALUE value
    ) {
        return newProperty(null, null, value, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newProperty() {
        return newProperty(null, null, null, UNDEFINED_INDEX, false);
    }

    /** Returns a new instance of property where the default value is null.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use the method newKey(...)
     * @hidden
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static <UJO extends QuickUjo,VALUE> Property<UJO,VALUE> newProperty(Key p) {
        return Property.newInstance(p.getName(), p.getType(), p.getDefault(), -1, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use rather {@link #newListProperty(java.lang.String) }
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListProperty
    ( String name
    , Class<ITEM> itemType
    ) {
        return ListProperty.newListProperty(name, itemType, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListProperty( String name) {
        return ListProperty.newListProperty(name, null, UNDEFINED_INDEX, false);
    }

    /** A Property Factory creates new property and assigns a name and next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @deprecated Use rather {@link #newListProperty() }
     * @hidden
     */
    @Deprecated
    protected static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListProperty
    ( Class<ITEM> itemType
    ) {
        return newListProperty(null, itemType);
    }

    /** A Property Factory creates new property and assigns a name and next property index.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     * @deprecated Use the method newKey(...)
     */
    @Deprecated
    protected static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListProperty() {
        return ListProperty.newListProperty((String)null, null);
    }
 
}
