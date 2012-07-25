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

/**
 * The immutable list of KeyProperties.
 * The UjoPropertyList class is a subset of the methods from class List&lt;Key&gt;.
 * Use the interface {@link KeyList} rather.
 * @param <UJO> Base Ujo implementation
 * @author Pavel Ponec
 * @composed 1 - N Key
 */
//@Deprecated
public interface UjoPropertyList<UJO extends Ujo> extends KeyList<UJO> {

//    /**
//     * Find a property by property name from parameter.
//     * @param ujo An Ujo object
//     * @param name A property name.
//     * @param action Action type UjoAction.ACTION_* .
//     * @param result Required result of action.
//     * @param throwException If result not found an Exception is throwed, or a null can be returned.
//     * @hidden
//     */
//    @SuppressWarnings("deprecation")
//    public Key<UJO,?> findDirectProperty
//    ( final Ujo ujo
//    , final String name
//    , final UjoAction action
//    , final boolean result
//    , final boolean throwException
//    ) throws IllegalArgumentException;
//
//    /**
//     * Find a property by property name from parameter.
//     *
//     * @param name A property name.
//     * @param throwException If result not found an Exception is throwed, or a null can be returned.
//     * @return Key
//     */
//    public Key<UJO,?> findDirectProperty
//    ( final String name
//    , final boolean throwException
//    ) throws IllegalArgumentException;
//
//    /** Find Key by name */
//    public Key<UJO,?> findDirectProperty
//    ( final Ujo ujo
//    , final String name
//    , final boolean throwException
//    ) throws IllegalArgumentException;
//
//    /** Get the first Property */
//    public Key<UJO,?> getFirstProperty();
//
//    /** Get the last Property */
//    public Key<UJO,?> getLastProperty();



}
