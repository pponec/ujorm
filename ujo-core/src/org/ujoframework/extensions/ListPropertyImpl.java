/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.extensions;

import java.util.ArrayList;
import org.ujoframework.implementation.array.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoComparator;

/**
 * The main implementation of the interface ListProperty.
 * @see AbstractUjo
 * @author Paul Ponec
 */
public class ListPropertyImpl<UJO extends Ujo, ITEM>
    extends Property<UJO,List<ITEM>>
    implements ListProperty<UJO,ITEM> {

    /** Class of the list item. */
    final private Class<ITEM> itemType;

    /** Protected constructor */
    protected ListPropertyImpl(Class<ITEM> itemType) {
        this.itemType = itemType;
    }

    /**
     * List property initialization.
     * @param name Replace the Name of property if the one is NULL.
     * @param index Replace index allways, the value -1 invoke a next number from the internal sequencer.
     * @param type Replace the Type of property if the one is NULL.
     * @param defaultValue Replace the Optional default value if the one is NULL.
     * @param lock Lock the property.
     * @return
     */
    @SuppressWarnings("unchecked")
    final protected ListPropertyImpl<UJO,ITEM> initList
    ( final String name
    , final int index
    , final Boolean lock
    ) {
       init(name, (Class) List.class, null, index, lock);
       return this;
    }


    /** Returns a count of Items. If a property value is null, method returns 0. */
    public int getItemCount(final UJO ujo) {
        List<ITEM> list = getValue(ujo);
        return list!=null ? list.size() : 0 ;
    }

    /** Return a Class of the Item. */
    public Class<ITEM> getItemType() {
        return itemType;
    }

    /** Returns true if the item type is a type or subtype of the parameter class. */
    @SuppressWarnings("unchecked")
    public boolean isItemTypeOf(final Class type) {
        return type.isAssignableFrom(itemType);
    }

    /**
     * Returns a value of property. The result is the same, like Ujo#getValue(UjoPropertyList) .get(index) .
     */
    public ITEM getItem(final UJO ujo, final int index) {
        return getValue(ujo).get(index);
    }

    /** Set a property item value. The action is the same, like Ujo#getValue(UjoPropertyList) .set(indexm, value).
     * @return the element previously at the specified position.
     */
    public ITEM setItem(final UJO ujo, final int index, final ITEM value) {
        return getValue(ujo).set(index, value);
    }

    /** Add Item Value. If List is null, the method creates an instance.
     * @return true (as per the general contract of Collection.add).
     */
    public boolean addItem(final UJO ujo, final ITEM value) {
        List<ITEM> result = getList(ujo);
        return result.add(value);
    }

    /** Removes the first occurrence in this list of the specified element.
     * @return true if this list is not null and contains the specified element, otherwise returns false.
     * @since 0.81
     */
    public boolean removeItem(UJO ujo, ITEM value) {
        List<ITEM> list = getValue(ujo);
        return list!=null && list.remove(value);
    }

    /**
     * Returns a not null List. If original list value is null, then a new List is created by a property type.
     * If the property type is an interface then the ArrayList instance is used.
     * @see #getItem(Ujo,int)
     */
    @SuppressWarnings("unchecked")
    public List<ITEM> getList(final UJO ujo) {
        List<ITEM> result = getValue(ujo);
        if (result==null) {
            try {
                result = getType().isInterface()
                    ? (List<ITEM>) new ArrayList()
                    : getType().newInstance()
                    ;
                setValue(ujo, result);
            } catch (InstantiationException ex) { throw new IllegalStateException("Can't create an empty list: " + getType(), ex);
            } catch (IllegalAccessException ex) { throw new IllegalStateException("Can't create an empty list: " + getType(), ex);
            }
        }
        return result;
    }

    /** Sort a list by its properties. */
    @SuppressWarnings("unchecked")
    public void sort(UJO ujo, UjoProperty ... properties) {
        List<ITEM> list = getValue(ujo);
        if ( list!=null) {
            Comparator comp = new UjoComparator(properties);
            Collections.sort(list, comp);
        }
    }


    /** Indicates whether a list of items is null or empty. */
    @Override
    public boolean isDefault(UJO ujo) {
        List<ITEM> list = getValue(ujo);
        return list==null || list.isEmpty();
    }

    // --------- STATIC METHODS -------------------

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, ITEM> ListPropertyImpl<UJO,ITEM> newListProperty
    ( final String name
    , final Class<ITEM> itemType
    , final int index
    , final boolean lock
    ) {
        final ListPropertyImpl<UJO,ITEM> result = new ListPropertyImpl<UJO,ITEM>(itemType);
        result.init(name, (Class) List.class, null, index, lock);
        return result;
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo, ITEM> ListPropertyImpl<UJO,ITEM> newListProperty
    ( final String name
    , final Class<ITEM> itemType
    , final int index
    ) {
        return newListProperty(name, itemType, index, true);
    }


    /** A ListProperty Factory
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo, ITEM> ListPropertyImpl<UJO,ITEM> newListProperty
    ( final String name
    , final Class<ITEM> itemType
    ) {
        return newListProperty(name, itemType, -1);
    }
    
}
