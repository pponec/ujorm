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
 * The immutable list of KeyProperties.
 * The KeyList class is a subset of the methods from class List&lt;Key&gt;.
 * @param <UJO> Base Ujo implementation
 * @author Pavel Ponec
 * @composed 1 - N Key
 */
public interface KeyList<UJO extends Ujo> extends Iterable<Key<UJO,?>> {

    /**
     * Find (both direct or indirect) property by property name from parameter.
     * @param name A property name by sample "user.address.street".
     * @return Key
     */
    public Key<UJO,?> find(String name) throws IllegalArgumentException;

    /**
     * Find (both direct or indirect) direct or indirect property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     */
    public Key<UJO,?> find
    ( final String name
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
    public Key<UJO,?> findDirectProperty
    ( final Ujo ujo
    , final String name
    , final UjoAction action
    , final boolean result
    , final boolean throwException
    ) throws IllegalArgumentException;

    /**
     * Find a property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return Key
     */
    public Key<UJO,?> findDirectProperty
    ( final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    /** Find Key by name */
    public Key<UJO,?> findDirectProperty
    ( final Ujo ujo
    , final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    /**
     * Find direct or indirect property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @deprecated Uset the method {@link #find(java.lang.String, boolean)}
     */
    public Key<UJO,?> findIndirect
    ( final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    /** Returns a copy of internal array */
    public Key<UJO,?>[] toArray();

    /** Get the first Property */
    public Key<UJO,?> getFirstProperty();

    /** Get the last Property */
    public Key<UJO,?> getLastProperty();

    /** Get last property 
     * @deprecated Use the method {@link #getLastProperty()} rather.
     */
    @Deprecated
    public Key<UJO,?> last();

    /** Returns a base class of the related UJO */
    public Class<UJO> getType();

    /** Returns a base class name of the related UJO */
    public String getTypeName();

    /** Create new Instance */
    public UJO newBaseUjo() throws IllegalStateException;


    // ----------------- LIST IMPLEMENTATION ------------------------

    /** Get property on requered index */
    public Key get(final int index);

    /** Returns a total count of its properties */
    public int size();

    /** Is the collection empty? */
    public boolean isEmpty();

    /** Returns true if list contains property from the parameter. */
    public boolean contains(final Key<UJO,?> o);

    /** Create an interator for all properties. */
    @Override
    public Iterator<Key<UJO,?>> iterator();

    /** Returns or create UjoManager.
     * In your own implementation keep in a mind a simple serialization freature of the current object.
     */
    // public UjoManager getUjoManager();
}
