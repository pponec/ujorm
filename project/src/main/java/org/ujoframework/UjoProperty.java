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
   
package org.ujoframework;

/**
 * <i>Ujo property</i> is a property of Ujo object. Every real Ujo implementation (ArrayUjo, MapUjo, BeanUjo)
 * have got its special implementation of an UjoProperty interface.
 * <br>See a <a href="package-summary.html#UJO">general information</a> about current framework or see some implementations.
 * 
 * @author Pavel Ponec
 * @see Ujo
 * @opt attributes
 * @opt operations
 */
public interface UjoProperty <UJO extends Ujo,VALUE> extends CharSequence /*, Comparable<UjoProperty>*/ {
    
    /** Returns a name of Property. */
    public String getName();

    /** Returns  a class of the current property. */
    public Class<VALUE> getType();
    
    /**
     * It is a basic method for setting an appropriate type safe value to an Ujo object. 
     * <br>The method calls a method 
     * {@link Ujo#writeValue(org.ujoframework.UjoProperty, java.lang.Object)}
     * always.
     * @see Ujo#writeValue(org.ujoframework.UjoProperty, java.lang.Object)
     */
    public void setValue(UJO ujo, VALUE value);

    
    /**
     * It is a basic method for getting an appropriate type safe value from an Ujo object. 
     * <br>The method calls a method
     * {@link Ujo#writeValue(org.ujoframework.UjoProperty, java.lang.Object)}
     * always.
     * <br>Note: this method replaces the value of <strong>null</strong> by default
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see Ujo#readValue(UjoProperty)
     */
    public VALUE getValue(UJO ujo);
    
    
    /**
     * A shortcut for the method getValue(Ujo) .
     * @see #getValue(Ujo)
     */
    public VALUE of(UJO ujo);
    

//    /**
//     * Similar function like getValue(UJO), however in case a null parameter is used so the result value is null and no NullPointerExeption is throwed.
//     * @param ujo If a null parameter is used then the null value is returned.
//     * @return Returns a type safe value from the ujo object.
//     * @see #getValue(Ujo)
//     */
//    public VALUE takeFrom(UJO ujo);

    /** Returns a property index or value -1 if the property index is not defined.
     * <br>The index is reasonable for an implementation an <code>ArrayUjo</code> class and the value is used is used 
     * <br>for a sorting of Properties in a method <code>UjoManager.readProperties(Class type)</code> .
     * @see org.ujoframework.implementation.array.ArrayUjo
     * @see org.ujoframework.core.UjoManager#readProperties(Class)
     */
    public int getIndex();
    
    /** Method returns a default value for substitution of the <code>null</code> value for the current property. 
     * The feature is purposeful only if the default value is not <code>null</code> and a propert value is <code>null</code> .
     * @see Ujo#readValue(UjoProperty)
     */
    public VALUE getDefault();


    /** Indicates whether a parameter value of the ujo "equal to" this property default value. */
    public boolean isDefault(UJO ujo);
    
    /**
     * Returns true, if the property value equals to a parameter value. The property value can be null.
     * 
     * @param ujo A basic Ujo.
     * @param value Null value is supported.
     * @return Accordance
     */
    public boolean equals(UJO ujo, VALUE value);
    
    /**
     * If the property is the direct property of the related UJO class then method returns the TRUE value.<br />
     * Note: Indirect (composite) properties are excluded from from function Ujo.readProperties() by default
     * and these properties should not be sent to methods Ujo.writeValue() and Ujo.readValue().
     * @since 0.81
     */
    public boolean isDirect();
    
    /** Returns true if the property type is a type or subtype of the parameter class. */
    public boolean isTypeOf(Class type);

    /** A flag for an ascending direction of sorting. It is recommended that the default result was true. 
     * @since 0.85
     * @see org.ujoframework.core.UjoComparator
     */
    public boolean isAscending();
    
    /** Create new instance of an <strong>indirect</strong> property with the descending direction of sorting.
     * @return returns a new instance of the indirect UjoProperty
     * @since 0.85
     * @see #isAscending()
     * @see org.ujoframework.core.UjoComparator
     */
    public UjoProperty<UJO,VALUE> descending();


    /** Create new composite (indirect) instance.
     * @since 0.92
     */
    public <VALUE_PAR> UjoProperty<UJO, VALUE_PAR> add(UjoProperty<? extends VALUE, VALUE_PAR> property);

    /** Copy a value from the first UJO object to second one. A null value is not replaced by the default. */
    public void copy(UJO from, UJO to);

    /** Returns the name of Property. */
    @Override
    public String toString();
    
}
