/*
 *  Copyright 2012-2012 Pavel Ponec
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

package org.ujorm.extensions;

import java.util.List;
import java.util.Map;
import org.ujorm.Key;
import org.ujorm.Ujo;


/**
 * The WeakKey is a simplified Key interface without the generic parameter for the domain type.
 * The default implementation does not have the full support of several methods:
 * <ul>
 *   <li>{@link Key#getDomainType()} - method return the result {@code Ujo.class} always </li>
 *   <li>{@link Key#add(org.ujorm.Key)} - method is not supported by the default implementation</li>
 *   <li>{@link Key#descending() } - result is not type of WeakKey</li>
 * </ul>
 * , and some more methods are not fully type-safe.
 * On the other side the interface supports some operations with the {@link List} and {@link Map} interfaces.
 * To create new keys use an instance of the factory class {@link WeakKeyFactory}.
 * @author Pavel Ponec
 */
public interface WeakKey<VALUE> extends Key<Ujo, VALUE> {

    /**
     * It is an extended method for setting an appropriate type safe value to a common Map object.
     */
    public void setValue(Map<String,? super VALUE> map, VALUE value);

   /**
     * It is an extended method for getting an appropriate type safe value from a common Map object.
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the map object.
     */
    public VALUE getValue(Map<String,? super VALUE> map);


    /**
     * A shortcut for the method {@link #getValue(java.util.Map) } .
     * @see #getValue(java.util.Map)
     */
    public VALUE of(Map<String,? super VALUE> map);

    /**
     * It is an extended method for setting an appropriate type safe value to a common Map object.
     * In case the size of the list is insufficient, the list is filled by the {@code null} values.
     */
    public void setValue(List<? super VALUE> list, VALUE value);

   /**
     * It is an extended method for getting an appropriate type safe value from a common Map object.
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the map object.
     */
    public VALUE getValue(List<? super VALUE> list);


    /**
     * A shortcut for the method {@link #getValue(java.util.List) } .
     * @see #getValue(java.util.List)
     */
    public VALUE of(List<? super VALUE> list);



    
}
