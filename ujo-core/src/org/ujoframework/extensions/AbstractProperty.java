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
 * An AbstractProperty property implementation.
 * @see AbstractUjo
 * @author Pavel Ponec
 */
public abstract class AbstractProperty<UJO extends Ujo,VALUE> implements UjoProperty<UJO,VALUE> {
    
    private final String name;
    private final Class<VALUE> type;
    private final int index;
    private VALUE defaultValue;
    
    /**
     * Property constructor.
     * @param name Name of property
     * @param type Type of property
     */
    public AbstractProperty(String name, Class<VALUE> type) {
        this(name, type, -1);
    }

    /**
     * Property constructor.
     * @param name Name of property
     * @param type Type of property
     * @param index Default value is -1
     */
    public AbstractProperty(String name, Class<VALUE> type, int index) {
        if (name==null) { throw new IllegalArgumentException("Name must not be null."); }
        if (type==null) { throw new IllegalArgumentException("Type must not be null."); }
        
        this.name  = name;
        this.type  = type;
        this.index = index;
    }
      
    /** Name of Property */
    final public String getName() {
        return name;
    }
    
    /** Type of Property */
    final public Class<VALUE> getType() {
        return type;
    }

    /** Index of Property */
    final public int getIndex() {
        return index;
    }
    
    /**
     * It is a basic method for setting an appropriate type safe value to an MapUjo object. 
     * <br>For the setting value is used internally a method <a href="MapUjo.html#writeValue(org.ujoframework.UjoProperty,%20java.lang.Object)">MapUjo.writeValue(UjoProperty, Object)</a>.
     * @see AbstractUjo#writeValue(UjoProperty,Object)
     */
    final public void setValue(final UJO ujo, final VALUE value) {
        ujo.writeValue(this, value);
    }
    
    /**
     * It is a basic method for getting an appropriate type safe value from an MapUjo object. 
     * <br>For the getting value is used internally a method <a href="MapUjo.html#readValue(org.ujoframework.UjoProperty)">MapUjo.readValue(UjoProperty)</a>.
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see AbstractUjo#readValue(UjoProperty)
     */
    @SuppressWarnings("unchecked")
    final public VALUE getValue(final UJO ujo) {
        final Object result = ujo.readValue(this);
        return (VALUE) result;
    }
    
    /**
     * An alias for a method getValue(Ujo) .
     * @see #getValue(Ujo)
     */
    final public VALUE of(final UJO ujo) {
        return getValue(ujo);
    }
    
    /** Returns a Default property value. The value replace the <code>null<code> value in the method Ujo.readValue(...). 
     * If the default value is not modified, returns the <code>null<code>.
     */
    public VALUE getDefault() {
        return defaultValue;
    }
    
    /** Assign a Default value.
     * <br />WARNING: the change of the default value modifies all values in all instances with the null value of the current property!
     */
    @SuppressWarnings("unchecked")
    public <PROPERTY extends AbstractProperty> PROPERTY setDefault(VALUE value) {
        defaultValue = value;
        return (PROPERTY) this;
    }

    /** Assing the value from the default value. */
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

    /** A flag for a direction of sorting. This method returns true allways.
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
    public UjoProperty<UJO, VALUE> descending() {
        return new SortingProperty<UJO, VALUE>(this, false);
    }

    /** Returns true if the property type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
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
    public boolean equals(final UJO ujo, final VALUE value) {
        Object myValue = ujo.readValue(this);
        if (myValue==value) { return true; }
        
        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    /** A char from Name */
    public char charAt(int index) {
        return name.charAt(index);
    }

    /** Length of the Name */
    public int length() {
        return name.length();
    }

    /** Sub sequence from the Name */
    public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
    }
    
    /** Returns a name of Property */
    @Override
    public final String toString() {
        return name;
    }

}
