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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm;

import java.util.Iterator;

/**
 * The immutable list of UjoProperties.
 * The UjoPropertyList class is a subset of the methods from class List&lt;UjoProperty&gt;.
 * @param <UJO> Base Ujo implementation
 * @author Pavel Ponec
 * @composed 1 - N UjoProperty
 */
public interface UjoPropertyList<UJO extends Ujo> extends Iterable<UjoProperty<UJO,?>> {

    /**
     * Find a property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return .
     */
    public UjoProperty<UJO,?> find
    ( final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    /**
     * Find direct or indirect property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return .
     */
    public UjoProperty<UJO,?> findIndirect
    ( final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    
    /** Find UjoProperty by name */
    public UjoProperty<UJO,?> find
    ( final Ujo ujo
    , final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    /**
     * Find a property by property name from parameter.
     * @param ujo An Ujo object
     * @param name A property name.
     * @param action Action type UjoAction.ACTION_* .
     * @param result Required result of action.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @hidden 
     */
    @SuppressWarnings("deprecation")
    public UjoProperty<UJO,?> find
    ( final Ujo ujo
    , final String name
    , final UjoAction action
    , final boolean result
    , final boolean throwException
    ) throws IllegalArgumentException;


    /** Returns a copy of internal array */
    public UjoProperty[] toArray();

    /** Get last property */
    public UjoProperty<UJO,?> last();

    /** Returns a class of the related UJO */
    public Class<UJO> getType();

    /** Create new Instance */
    public UJO newInstance() throws IllegalStateException;

    /** Returns a class name of the related UJO */
    public String getTypeName();

    // ----------------- LIST IMPLEMENTATION ------------------------

    /** Get property on requered index */
    public UjoProperty get(final int index);

    /** Returns a total count of its properties */
    public int size();

    /** Is the collection empty? */
    public boolean isEmpty();

    /** Returns true if list contains property from the parameter. */
    public boolean contains(final UjoProperty<UJO,?> o);

    /** Create an interator for all properties. */
    @Override
    public Iterator<UjoProperty<UJO,?>> iterator();

}
