/*
 *  Copyright 2007-2026 Pavel Ponec
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

import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;

/**
 * The interface is reasonable for a text serialization and deserializaton of non UJO keys of a UJO object.
 * <br>If you can use an UJO persistence (XML, CSV, ResourceBundle) then the all affected UJO classes must have got next features:
 * <ul>
 *   <li>no parameter constructor and</li>
 *   <li>non UJO keys must have got a features of ValueTextable *</li>
 *   <li>key type of List must be implemented by a UjoPropertyList implementation (only XML persistence supports the one)</li>
 * </ul>
 *
 * @see ValueTextable See ValueTextable for suppored data types.
 * @author Pavel Ponec
 */
public interface UjoTextable extends Ujo {

    /**
     * Set value from a String format. Property can't be an "container" type (Ujo, List, Object[]).
     *
     * @param key A direct key only. See a method Key.isDirect().
     * @param value String value
     * @param type Type can be a subtype of a Property.type. If type is null, then a key.type is used.
     * @param action A context of the action.
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY .
     */
    void writeValueString(Key<?,?> key, String value, Class<?> type, UjoAction action);

    /**
     * Get an original value in a String format. If key type is a "container" type (Ujo, List, Object[]), method returns null,
     * otherwise method returns an instance of String.
     *
     * @param key A direct key only. See a method Key.isDirect().
     * @param action A context of the action.
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY .
     * @return If key type is "container" result is null.
     */
    String readValueString(Key<?,?> key, UjoAction action);

}
