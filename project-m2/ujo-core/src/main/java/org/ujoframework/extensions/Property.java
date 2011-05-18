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

package org.ujoframework.extensions;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * The main implementation of the interface UjoProperty.
 * @see AbstractUjo
 * @author Pavel Ponec
 */
public class Property<UJO extends Ujo,VALUE> implements UjoProperty<UJO,VALUE> {

    /** Property name */
    private String name;
    /** Property index */
    private int index;
    /** Property type (class) */
    private Class<VALUE> type;
    /** Property default value */
    private VALUE defaultValue;
    /** Lock the property after initialization */
    private boolean lock;


    /** A property seqeuncer for an index attribute
     * @see #_nextSequence()
     */
    private static int _sequencer = 0;

    /** Returns a next property index by a synchronized method.
     * The UJO property indexed by this method may not be in continuous series
     * however numbers have the <strong>upward direction</strong> always.
     */
    protected static synchronized int _nextSequence() {
        return _sequencer++;
    }


    /**
     * Constructor with an property order
     * @param name
     * @param type
     * @param index On order of the property.
     */
    protected Property(final String name, final Class<VALUE> type, final int index) {
        init(name, type, null, index, true);
    }

    /**
     * Constructor with an property order
     * @param name
     * @param index On order of the property.
     */
    @SuppressWarnings("unchecked")
    protected Property(final String name, final VALUE value, final int index) {
        init(name, null, value, index, true);
    }

    /** Protected constructor */
    protected Property() {
    }

    /**
     * Property initialization.
     * @param name Replace the Name of property if the one is NULL.
     * @param index Replace index always, the value -1 invoke a next number from the internal sequencer.
     * @param type Replace the Type of property if the one is NULL.
     * @param defaultValue Replace the Optional default value if the one is NULL.
     * @param lock Lock the property.
     */
    @SuppressWarnings("unchecked")
    final protected Property<UJO,VALUE> init
    ( final String name
    , Class<VALUE> type
    , final VALUE defaultValue
    , final int index
    , final Boolean lock
    ) {
        if (this.lock) {
            throw new IllegalArgumentException("The property is already initialized: " + this);
        }

        if (defaultValue!=null) {
             this.defaultValue = defaultValue;
             if (type==null) {
                type = (Class) defaultValue.getClass();
             }
        }
        this.index = index==-1 ? _nextSequence() : index ;
        if (type !=null) { this.type  = type ; }
        if (name !=null) { this.name  = name ; }
        if (lock !=null) { this.lock  = lock ; }

        if (this.lock) {
           checkAttribs();
        }
        return this;
    }

    /** Is the property Locked? */
    final boolean isLock() {
        return lock;
    }

    /** Check properties */
    protected void checkAttribs() {
        if (name==null) { throw new IllegalArgumentException("Name must not be null in the " + this); }
        if (type==null) { throw new IllegalArgumentException("Type must not be null in the " + this); }
        if (defaultValue!=null && !type.isInstance(defaultValue)) {
            throw new IllegalArgumentException("Default value have not properly type in the " + this);
        }
    }
      
    /** Name of Property */
    @Override
    final public String getName() {
        return name;
    }
    
    /** Type of Property */
    @Override
    final public Class<VALUE> getType() {
        return type;
    }

    /** Index of Property */
    @Override
    final public int getIndex() {
        return index;
    }
    
    /**
     * It is a basic method for setting an appropriate type safe value to an MapUjo object. 
     * <br>For the setting value is used internally a method 
     *     {@link AbstractUjo#writeValue(org.ujoframework.UjoProperty, java.lang.Object) }
     * @see AbstractUjo#writeValue(org.ujoframework.UjoProperty, java.lang.Object)
     */
    @Override
    final public void setValue(final UJO ujo, final VALUE value) {
        ujo.writeValue(this, value);
    }
    
    /**
     * It is a basic method for getting an appropriate type safe value from an MapUjo object. 
     * <br>For the getting value is used internally a method 
     *     {@link AbstractUjo#readValue(org.ujoframework.UjoProperty)}
     * </a>.
     * <br>Note: this method replaces the value of <strong>null</strong> for default
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see AbstractUjo#readValue(UjoProperty)
     */
    @SuppressWarnings("unchecked")
    @Override
    final public VALUE getValue(final UJO ujo) {
        final Object result = ujo.readValue(this);
        return result!= null ? (VALUE) result : defaultValue;
    }
    
    /**
     * A shortcut for the method getValue(Ujo).
     * @see #getValue(Ujo)
     */
    @SuppressWarnings("unchecked")
    @Override
    final public VALUE of(final UJO ujo) {
        final Object result = ujo.readValue(this);
        return result!= null ? (VALUE) result : defaultValue;
    }
    
    /** Returns a Default property value. The value replace the <code>null<code> value in the method Ujo.readValue(...). 
     * If the default value is not modified, returns the <code>null<code>.
     */
    @Override
    public VALUE getDefault() {
        return defaultValue;
    }

    /** Assign a Default value. The default value may be modified after locking - at your own risk.
     * <br />WARNING: the change of the default value modifies all values in all instances with the null value of the current property!
     */
    @SuppressWarnings("unchecked")
    public <PROPERTY extends Property> PROPERTY writeDefault(VALUE value) {
        defaultValue = value;
        if (lock) checkAttribs();
        return (PROPERTY) this;
    }
    
    /** Assing a value from the default value. */
    public void setValueFromDefault(UJO ujo) {
        setValue(ujo, defaultValue);
    }
        
    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    @Override
    public boolean isDefault(UJO ujo) {
        VALUE value = getValue(ujo);
        final boolean result
        =  value==defaultValue
        || (defaultValue!=null && defaultValue.equals(value))
        ;
        return result;
    }
    
    /**
     * Returns a true value, if the property contains more properties.
     * The composite property is excluded from from function Ujo.readProperties() by default.
     */
    @Override
    public final boolean isDirect() {
        return true;
    }

    /** A flag for a direction of sorting. This method returns true always.
     * @since 0.85
     * @see org.ujoframework.core.UjoComparator
     */
    @Override
    public boolean isAscending() {
        return true;
    }

    /** Create a new instance of the <strong>indirect</strong> property with a descending direction of order.
     * @since 0.85
     * @see #isAscending()
     * @see org.ujoframework.core.UjoComparator
     */
    @Override
    public UjoProperty<UJO, VALUE> descending() {
        return PathProperty.newInstance(this, false);
    }

    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    @SuppressWarnings("unchecked")
    @Override
    public <VALUE_PAR> UjoProperty<UJO, VALUE_PAR> add(final UjoProperty<? extends VALUE, VALUE_PAR> property) {
        return PathProperty.newInstance((UjoProperty)this, property);
    }

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    @Override
    public void copy(final UJO from, final UJO to) {
        to.writeValue(this, from.readValue(this));
    }

    /** Returns true if the property type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    @Override
    public boolean isTypeOf(final Class type) {
        return type.isAssignableFrom(this.type);
    }

    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     * 
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    @Override
    public boolean equals(final UJO ujo, final VALUE value) {
        Object myValue = of(ujo);
        if (myValue==value) { return true; }
        
        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    /** Compare to another UjoProperty object by the index and name of the property.
     * @since 1.20
     */
    public int compareTo(final UjoProperty p) {
        return index<p.getIndex() ? -1
             : index>p.getIndex() ?  1
             : name.compareTo(p.getName()) 
             ;
    }

    /** A char from Name */
    @Override
    public char charAt(int index) {
        return name.charAt(index);
    }

    /** Length of the Name */
    @Override
    public int length() {
        return name.length();
    }

    /** Sub sequence from the Name */
    @Override
    public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
    }
    
    /** Returns a name of Property */
    @Override
    public final String toString() {
        return name;
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of property where the default value is null.
     * The method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, VALUE value, int index, boolean lock) {
        return new Property<UJO,VALUE>().init(name, type, value, index, lock);
    }


    /** Returns a new instance of property where the default value is null.
     * The method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type, int index) {
        return new Property<UJO,VALUE>().init(name, type, null, index, true);
    }

    /** Returns a new instance of property where the default value is null.
     * The method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> Property<UJO,VALUE> newInstance(String name, Class<VALUE> type) {
        return newInstance(name, type, -1);
    }

    /** A Property Factory where a property type is related from from default value.
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(String name, VALUE value, int index) {
        @SuppressWarnings("unchecked")
        Class<VALUE> type = (Class) value.getClass();
        return new Property<UJO, VALUE>().init(name, type, value, index, true);
    }

    /** A Property Factory where a property type is related from from default value.
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(String name, VALUE value) {
         return newInstance(name, value, -1);
    }


    /** A Property Factory where a property type is related from from default value.
     * Method assigns a next property index.
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> Property<UJO, VALUE> newInstance(UjoProperty p, int index) {
         return newInstance(p.getName(), p.getType(), p.getDefault(), index, true);
    }


    /** A Property Factory where a property type is related from from default value.
     * <br />Warning: Method does not lock the property so you must call AbstractUjo.init(..) method after initialization!
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, VALUE> UjoProperty<UJO, VALUE> newInstance(UjoProperty p) {
         return newInstance(p.getName(), p.getType(), p.getDefault(), -1, false);
    }

}
