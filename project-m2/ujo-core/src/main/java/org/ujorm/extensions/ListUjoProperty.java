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

package org.ujorm.extensions;

import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;

/**
 * A property metadata interface for value type of {@code List<ITEM>}.
 * @author Pavel Ponec
 */
public interface ListUjoProperty<UJO extends Ujo, ITEM> extends UjoProperty<UJO,List<ITEM>> {

    /** Returns a class of the property. */
    public Class<ITEM> getItemType();

    /** Returns a count of Items. If the property is null, method returns 0. */
    public int getItemCount(UJO ujo);

    /** Returns true if the item type is a type or subtype of the parameter class. */
    public boolean isItemTypeOf(Class type);

    /**
     * Returns a value of property. The result is the same, like Ujo#readValue(ListUjoPropertyCommon).
     */
    public ITEM getItem(UJO ujo, int index);

    /**
     * An alias for {@link #getItem(org.ujorm.Ujo, int)}.
     * @return Returns a value of property. The result is the same, like Ujo#readValue(ListUjoPropertyCommon).
     */
    public ITEM of(UJO ujo, int index);

    /**
     * Return a not null List. If original list value is empty, the new List is created.
     * @see #getItem(Ujo, int)
     */
    public List<ITEM> getList(UJO ujo);

    /** Set a property item value.
     * @return the element previously at the specified position.
     */
    public ITEM setItem(UJO ujo, int index, ITEM value);

    /** Add an Item value to a List property. If the list is {@code null}, than the method create a new instance of List (for exact behaviour see an implementation).
     * The method works like a simolar code:
     * <pre class="pre">
     * if (ujo.get(VALUE_LIST)==null) {
     *    ujo.set(VALUE_LIST, new ArrayList());
     * }
     * ujo.get(VALUE_LIST).add(itemValue);
     * <pre class="pre">
     * @return Value {@code true} as per the general contract of Collection.add.
     */
    public boolean addItem(UJO ujo, ITEM value);

    /** Removes the first occurrence in this list of the specified element.
     * @return true if this list is not null and contains the specified element, otherwise returns false.
     * @since 0.81
     */
    public boolean removeItem(UJO ujo, ITEM value);

    /** Indicates whether a list of items is null or empty. */
    public boolean isDefault(UJO ujo);

}
