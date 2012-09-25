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
 * An extended Key implementation for only one generic type for VALUE type.
 * To create thek keys use an instance of LowKeyFaoctory.
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
