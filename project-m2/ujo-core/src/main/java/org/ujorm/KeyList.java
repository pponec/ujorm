/*
 * Copyright 2009-2017 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm;

import java.util.Iterator;

/**
 * The immutable list of the Keys.
 * The KeyList class is a subset of the methods from class List&lt;Key&gt;.
 * @param <UJO> Base Ujo implementation
 * @author Pavel Ponec
 * @composed 1 - N Key
 */
public interface KeyList<UJO extends Ujo> extends Iterable<Key<UJO,Object>> {

    /**
     * Find (both direct or indirect) key by key name from parameter.
     * @param name A key name by sample "user.address.street".
     * @return Key
     */
    public Key<UJO,?> find(String name) throws IllegalArgumentException;

    /**
     * Find (both direct or indirect) direct or indirect key by key name from parameter.
     *
     * @param name A key name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     */
    public Key<UJO,?> find
    ( final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    /**
     * Find a key by key name from parameter.
     * @param ujo An Ujo object
     * @param name A key name.
     * @param action Action type UjoAction.ACTION_* .
     * @param result Required result of action.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @hidden
     */
    @SuppressWarnings("deprecation")
    public Key<UJO,?> findDirectKey
    ( final Ujo ujo
    , final String name
    , final UjoAction action
    , final boolean result
    , final boolean throwException
    ) throws IllegalArgumentException;

    /**
     * Find a key by key name from parameter.
     *
     * @param name A key name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return Key
     */
    public Key<UJO,?> findDirectKey
    ( final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    /** Find Key by name */
    public Key<UJO,?> findDirectKey
    ( final Ujo ujo
    , final String name
    , final boolean throwException
    ) throws IllegalArgumentException;

    /** Returns a copy of internal array */
    public Key[] toArray();

    /** Get the first Property */
    public Key<UJO,?> getFirstKey();

    /** Get the last Property */
    public Key<UJO,?> getLastKey();

    /** The the domain class of related Keys.
     * The value can be {@code null} if the Key array is empty. */
    public Class<UJO> getType();

    /** Returns a base class name of the related UJO */
    public String getTypeName();

    /** Create new Instance */
    public UJO newBaseUjo() throws IllegalStateException;


    // ----------------- LIST IMPLEMENTATION ------------------------

    /** Get key on requered index */
    public Key<UJO,?> get(final int index);

    /** Returns a total count of its keys */
    public int size();

    /** Is the collection empty? */
    public boolean isEmpty();

    /** Returns true if list contains key from the parameter. */
    public boolean contains(final Key<?,?> key);

    /** Create an iterator for all keys.
     * The return UjoProperty item type is used for a back compatibility only,
     * in feature it will be replaced by the Key interface.
     */
    @Override
    public Iterator<Key<UJO,Object>> iterator();

    /** Returns or create UjoManager.
     * In your own implementation keep in a mind a simple serialization feature of the current object.
     */
    // public UjoManager getUjoManager();
}
