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

package org.ujorm.core;

import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.UjoAction;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;


/**
 * The immutable list of KeyProperties.
 * The UjoPropertyList class is a subset of the methods from class List&lt;Key&gt;.
 * @author Pavel Ponec
 * @deprecated Use the KeyRing indead of this.
 */
@Deprecated
final public class UjoPropertyListImpl extends KeyRing<Ujo> implements UjoPropertyList  {

    /** An empty array of the UJO keys */
    final static public Key[] EMPTY = new Key[0];

    /** Constructor */
    public UjoPropertyListImpl(KeyRing keyRing) {
        super(keyRing.getType(), keyRing.keys);
    }

    /** Constructor */
    public UjoPropertyListImpl(KeyList keyList) {
        super(keyList.getType(), ((KeyRing)keyList).keys);
    }

    /**
     * Find (both direct or indirect) property by property name from parameter.
     * @param name A property name by sample "user.address.street".
     * @return Key
     */
    @Override
    public UjoProperty find(String name) throws IllegalArgumentException{
        return (UjoProperty) super.find(name);
    }

    /**
     * Find (both direct or indirect) direct or indirect property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     */
    @Override
    public UjoProperty find
    ( final String name
    , final boolean throwException
    ) throws IllegalArgumentException{
        return (UjoProperty) super.find(name, throwException);
    }

    /** Returns a copy of internal array */
    @Override
    public UjoProperty[] toArray(){
        Key[] keys = super.toArray();
        UjoProperty[] result = new UjoProperty[keys.length];
        System.arraycopy(keys, 0, result, 0, result.length);
        return result;
    }

    // ======================= DEPRECATED ===========================

    public UjoPropertyListImpl(Class baseClass, Key[] keys) {
        super(baseClass, keys);
    }

    public UjoProperty findDirectProperty(Ujo ujo, String name, UjoAction action, boolean result, boolean throwException) throws IllegalArgumentException {
        return (UjoProperty) findDirectKey(ujo, name, action, result, throwException);
    }

    public UjoProperty findDirectProperty(String name, boolean throwException) throws IllegalArgumentException {
        return (UjoProperty) findDirectKey(name, throwException);
    }

    public UjoProperty findDirectProperty(Ujo ujo, String name, boolean throwException) throws IllegalArgumentException {
        return (UjoProperty) findDirectKey(ujo, name, throwException);
    }

    public UjoProperty getFirstProperty() {
        return (UjoProperty) getFirstKey();
    }

    public UjoProperty getLastProperty() {
        return (UjoProperty) getLastKey();
    }

    /** Get The Last Properties
     * @deprecated Use the method {@link #getLastKey()} rather.
     */
   
    @Override
    public UjoProperty last() {
        return (UjoProperty) getLastKey();
    }


    @Override
    public UjoProperty findIndirect(String name, boolean throwException) throws IllegalArgumentException {
        return (UjoProperty) find(name);
    }

    @Override
    public UjoProperty get(int index) {
        return (UjoProperty) super.keys[index];
    }

    @Override
    public boolean contains(Key o) {
        return super.contains(o);
    }


}
