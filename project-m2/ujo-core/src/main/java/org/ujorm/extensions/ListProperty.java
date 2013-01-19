/*
 *  Copyright 2007-2013 Pavel Ponec
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

import org.ujorm.ListKey;
import java.util.ArrayList;
import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.core.UjoComparator;

/**
 * The main implementation of the interface ListKey.
 * @see AbstractUjo
 * @author Pavel Ponec
 */
public class ListProperty<UJO extends Ujo, ITEM>
    extends AbstractCollectionProperty<UJO, List<ITEM>, ITEM>
    implements ListKey<UJO,ITEM> {

    /** Protected constructor */
    protected ListProperty(Class<ITEM> itemType) {
        this(null, itemType, UNDEFINED_INDEX);
    }

    /** Protected constructor */
    protected ListProperty(String name, Class<ITEM> itemType, int index) {
        super((Class<List<ITEM>>)(Object)List.class);
        initItemType(itemType);
        init(name, null, null, null, index, false);
    }

    /** Returns a count of Items. If a property value is null, method returns 0. */
    @Override
    public int getItemCount(final UJO ujo) {
        List<ITEM> list = of(ujo);
        return list!=null ? list.size() : 0 ;
    }

    /**
     * Returns a value of property. The result is the same, like Ujo#of(UjoPropertyList) .get(index) .
     */
    @Override
    public ITEM getItem(final UJO ujo, final int index) {
        return of(ujo).get(index);
    }

    /**
     * The alias for {@link #getItem(org.ujorm.Ujo, int) }.
     * @return Returns a value of property. The result is the same, like Ujo#of(UjoPropertyList) .get(index) .
     */
    @Override
    public ITEM of(final UJO ujo, final int index) {
        return of(ujo).get(index);
    }

    /** Set a property item value. The action is the same, like Ujo#of(UjoPropertyList) .set(indexm, value).
     * @return the element previously at the specified position.
     */
    @Override
    public ITEM setItem(final UJO ujo, final int index, final ITEM value) {
        return of(ujo).set(index, value);
    }

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
    @Override
    public boolean addItem(final UJO ujo, final ITEM value) {
        List<ITEM> result = getList(ujo);
        return result.add(value);
    }

    /** Removes the first occurrence in this list of the specified element.
     * @return true if this list is not null and contains the specified element, otherwise returns false.
     * @since 0.81
     */
    @Override
    public boolean removeItem(UJO ujo, ITEM value) {
        List<ITEM> list = of(ujo);
        return list!=null && list.remove(value);
    }

    /**
     * Returns a not null List. If original list value is null, then a new List is created by a property type.
     * If the property type is an interface then the ArrayList instance is used.
     * @see #getItem(Ujo,int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ITEM> getList(final UJO ujo) {
        List<ITEM> result = of(ujo);
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

    /** Sort a list by its keys. */
    @SuppressWarnings("unchecked")
    public void sort(UJO ujo, Key ... keys) {
        List<ITEM> list = of(ujo);
        if ( list!=null) {
            new UjoComparator(keys).sort(list);
        }
    }

    /** Indicates whether a list of items is null or empty. */
    @Override
    public boolean isDefault(UJO ujo) {
        List<ITEM> list = of(ujo);
        return list==null || list.isEmpty();
    }

    // --------- STATIC METHODS -------------------

    /** A ListKey Factory
     * Method assigns a next property index.
     * @hidden
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListProperty
    ( final String name
    , final Class<ITEM> itemType
    , final int index
    , final boolean lock
    ) {
        final ListProperty<UJO,ITEM> result = new ListProperty<UJO,ITEM>(itemType);
        result.init(name, null, null, null, index, lock);
        return result;
    }

    /** A ListKey Factory
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListProperty
    ( final String name
    , final Class<ITEM> itemType
    , final int index
    ) {
        return newListProperty(name, itemType, index, false);
    }


    /** A ListKey Factory
     * Method assigns a next property index.
     * @hidden
     */
    public static <UJO extends Ujo, ITEM> ListProperty<UJO,ITEM> newListProperty
    ( final String name
    , final Class<ITEM> itemType
    ) {
        return newListProperty(name, itemType, UNDEFINED_INDEX);
    }
    
}
