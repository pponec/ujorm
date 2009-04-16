/*
 *  Copyright 2007-2008 Paul Ponec
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

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * A <strong>PathProperty</strong> class is an composite of a UjoProperty objects.
 * The PathProperty class can be used wherever is used UjoProperty - with a one important <strong>exception</strong>: 
 * do not send the PathProperty object to methods Ujo.readValue(...) and Ujo.writeValue(...) !!!
 * <p/>You can use the preferred methods UjoManager.setValue(...) / UjoManager.getValue(...) 
 * to write and read a value instead of or use some type safe solution by UjoExt or a method of UjoProperty.
 * <p/>Note that method isDirect() returns a false in this class. For this reason, the property is not included 
 * in the list returned by Ujo.readProperties().
 * 
 * @author Pavel Ponec
 * @since 0.81
 */
public class PathProperty<UJO extends Ujo, VALUE> implements UjoProperty<UJO, VALUE> {

    private final UjoProperty[] properties;

    public PathProperty(UjoProperty... properties) {
        this.properties = properties;
    }

    /** Get the last property of the current object. The result can't be a direct property. */
    @SuppressWarnings("unchecked")
    final public<UJO_IMPL extends Ujo> UjoProperty<UJO_IMPL, VALUE> getLastProperty() {
        return properties[properties.length - 1];
    }

    /** Get a property from selected positon. */
    final public UjoProperty getProperty(int index) {
        return properties[index];
    }

    /** Returns a count of properties */
    final public int getPropertyCount() {
        return properties.length;
    }

    /** Full property name */
    public String getName() {
        StringBuilder result = new StringBuilder(32);
        for (UjoProperty p : properties) {
            if (result.length() > 0) {
                result.append('.');
            }
            result.append(p.getName());
        }
        return result.toString();
    }

    /** Property type */
    public Class<VALUE> getType() {
        return getLastProperty().getType();
    }

    /** Get a semifinal value from an Ujo object by a chain of properties.
     * If a value  (not getLastProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    public Ujo getSemifinalValue(UJO ujo) {

        Ujo result = ujo;
        for (int i=0; i<properties.length-1; i++) {
            if (result==null) { return result; }
            result = (Ujo) properties[i].getValue(result);
        }
        return result;
    }

    /** Get a value from an Ujo object by a chain of properties.
     * If a value  (not getLastProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    public VALUE getValue(UJO ujo) {
        Ujo u = getSemifinalValue(ujo);
        return  u!=null ? (VALUE) getLastProperty().of(u) : null ;
    }

    public void setValue(UJO ujo, VALUE value) {
        final Ujo u = getSemifinalValue(ujo);
        getLastProperty().setValue(u, value);
    }

    final public int getIndex() {
        return -1;
    }

    /** Returns a default value */
    public VALUE getDefault() {
        return (VALUE) getLastProperty().getDefault();
    }

    /** Indicates whether a parameter value of the ujo "equal to" this default value. */
    @Override
    public boolean isDefault(UJO ujo) {
        VALUE value = getValue(ujo);
        VALUE defaultValue = getDefault();
        final boolean result
        =  value==defaultValue
        || (defaultValue!=null && defaultValue.equals(value))
        ;
        return result;
    }

    /** Returns true if the property type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    public boolean isTypeOf(final Class type) {
        return type.isAssignableFrom(getType());
    }
    
    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     * 
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    public boolean equals(final UJO ujo, final VALUE value) {
        Object myValue = getValue(ujo);
        if (myValue==value) { return true; }
        
        final boolean result
        =  myValue!=null
        && value  !=null
        && myValue.equals(value)
        ;
        return result;
    }

    /**
     * An alias for a method getValue(Ujo) .
     * @see #getValue(Ujo)
     */
    final public VALUE of(final UJO ujo) {
        return getValue(ujo);
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
    /** Length of the Name */
    public int length() {
        return getName().length();
    }

    /** A char from Name */
    public char charAt(int index) {
        return getName().charAt(index);
    }

    /** Sub sequence from the Name */
    public CharSequence subSequence(int start, int end) {
        return getName().subSequence(start, end);
    }
    
    /**
     * Method returns a false because this is a property of the another UJO class.
     * The composite property is excluded from from function Ujo.readProperties() by default.
     */
    public final boolean isDirect() {
        return false;
    }

    /** A flag for an ascending direction of order. For the result is significant only the last property.
     * @see org.ujoframework.core.UjoComparator
     */
    public boolean isAscending() {
        return getLastProperty().isAscending();
    }

    /** Create a new instance of the property with a descending direction of order.
     * @see org.ujoframework.core.UjoComparator
     */
    public UjoProperty<UJO,VALUE> descending() {
        return isAscending() ? new SortingProperty<UJO,VALUE>(this, false) : this ;
    }

    /** Add all direct properties to the list form parameter. */
    @SuppressWarnings("unchecked")
    public void addDirectProperties(List<UjoProperty> result) {
        for (UjoProperty p : properties) {
            if (p.isDirect()) {
                result.add(p);
            } else {
                ((PathProperty)p).addDirectProperties(result);
            }
        }
    }

    // ================ STATIC ================
    
    /** Create new instance
     * @hidden 
     */
    public static final <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> newInstance(final UjoProperty<UJO, VALUE> property) {
        return new PathProperty<UJO, VALUE>(property);
    }

    /** Create new instance
     * @hidden 
     */
    public static final <UJO1 extends Ujo, UJO2 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final UjoProperty<UJO1, UJO2> property1
        , final UjoProperty<UJO2, VALUE> property2
        ) {
        return new PathProperty<UJO1, VALUE>(property1, property2);
    }

    /** Create new instance
     * @hidden 
     */
    public static final <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final UjoProperty<UJO1, UJO2> property1
        , final UjoProperty<UJO2, UJO3> property2
        , final UjoProperty<UJO3, VALUE> property3
        ) {
        return new PathProperty<UJO1, VALUE>(property1, property2, property3);
    }

    /** Create new instance
     * @hidden 
     */
    public static final <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, UJO4 extends Ujo, VALUE> PathProperty<UJO1, VALUE> newInstance
        ( final UjoProperty<UJO1, UJO2> property1
        , final UjoProperty<UJO2, UJO3> property2
        , final UjoProperty<UJO3, UJO4> property3
        , final UjoProperty<UJO4, VALUE> property4
        ) {
        return new PathProperty<UJO1, VALUE>(property1, property2, property3, property4);
    }

    /** Create new instance
     * @hidden 
     */
    @SuppressWarnings("unchecked")
    public static final <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> create(UjoProperty<UJO, ? extends Object>... properties) {
        return new PathProperty(properties);
    }

}
