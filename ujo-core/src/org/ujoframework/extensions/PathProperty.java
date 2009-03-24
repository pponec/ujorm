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
 * @author Ponec
 * @since 0.81
 */
public class PathProperty<UJO extends Ujo, VALUE> implements UjoProperty<UJO, VALUE> {

    private final UjoProperty[] properties;

    public PathProperty(UjoProperty... properties) {
        this.properties = properties;
    }

    /** Get the last property */
    @SuppressWarnings("unchecked")
    final public<UJO_IMPL extends Ujo> UjoProperty<UJO_IMPL, VALUE> lastProperty() {
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
        return lastProperty().getType();
    }

    /** Get a semifinal value from an Ujo object by a chain of properties.
     * If a value  (not lastProperty) is null, then the result is null.
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
     * If a value  (not lastProperty) is null, then the result is null.
     */
    @SuppressWarnings("unchecked")
    public VALUE getValue(UJO ujo) {
        Ujo u = getSemifinalValue(ujo);
        return  u!=null ? (VALUE) lastProperty().of(u) : null ;
    }

    public void setValue(UJO ujo, VALUE value) {
        final Ujo u = getSemifinalValue(ujo);
        lastProperty().setValue(u, value);
    }

    final public int getIndex() {
        return -1;
    }

    /** Returns a default value */
    public VALUE getDefault() {
        return (VALUE) lastProperty().getDefault();
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

//    /**
//     * Similar function like getValue(UJO), however in case a null parameter is used so the result value is null and no NullPointerExeption is throwed.
//     * @param ujo If a null parameter is used then the null value is returned.
//     * @return Returns a type safe value from the ujo object.
//     * @see #getValue(Ujo)
//     */
//    public VALUE takeFrom(UJO ujo) {
//        return getValue(ujo);
//    }
    
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
    
    public UjoProperty[] toArray() {
        UjoProperty[] result = new UjoProperty[properties.length];
        System.arraycopy(properties, 0, result, 0, result.length);
        return result;
    }

    // ================ STATIC ================
    
    /** Create new instance
     * @hidden 
     */
    public static final <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> create(UjoProperty<UJO, VALUE> property) {
        return new PathProperty<UJO, VALUE>(property);
    }

    /** Create new instance
     * @hidden 
     */
    public static final <UJO1 extends Ujo, UJO2 extends Ujo, VALUE> PathProperty<UJO1, VALUE> create
        ( UjoProperty<UJO1, UJO2> property1
        , UjoProperty<UJO2, VALUE> property2
        ) {
        return new PathProperty<UJO1, VALUE>(property1, property2);
    }

    /** Create new instance
     * @hidden 
     */
    public static final <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> PathProperty<UJO1, VALUE> create
        ( UjoProperty<UJO1, UJO2> property1
        , UjoProperty<UJO2, UJO3> property2
        , UjoProperty<UJO3, VALUE> property3
        ) {
        return new PathProperty<UJO1, VALUE>(property1, property2, property3);
    }

    /** Create new instance
     * @hidden 
     */
    public static final <UJO1 extends Ujo, UJO2 extends Ujo, UJO3 extends Ujo, UJO4 extends Ujo, VALUE> PathProperty<UJO1, VALUE> create
        ( UjoProperty<UJO1, UJO2> property1
        , UjoProperty<UJO2, UJO3> property2
        , UjoProperty<UJO3, UJO4> property3
        , UjoProperty<UJO4, VALUE> property4
        ) {
        return new PathProperty<UJO1, VALUE>(property1, property2, property3, property4);
    }

    /** Create new instance
     * @hidden 
     */
    @SuppressWarnings("unchecked")
    public static final <UJO extends Ujo, VALUE> PathProperty<UJO, VALUE> createPro(UjoProperty<UJO, ? extends Object>... properties) {
        return new PathProperty(properties);
    }

}
