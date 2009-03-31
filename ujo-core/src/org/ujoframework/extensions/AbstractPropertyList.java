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
 * Abstract Property List implementation.
 * @see ArrayUjo
 * @author Paul Ponec
 */
abstract public class AbstractPropertyList<UJO extends Ujo,LIST extends List<ITEM>, ITEM>
    extends AbstractProperty<UJO, LIST>
    implements UjoPropertyList<UJO,LIST,ITEM> {

    /** Class of the list item. */
    private final Class<ITEM> itemType;
    
    /** Constructor */
    public AbstractPropertyList(String name, Class<LIST> type, Class<ITEM> itemType, int index) {
        super(name, type, index);
        this.itemType = itemType;
    }
    
    /** Returns a count of Items. If a property value is null, method returns 0. */
    public int getItemCount(final UJO ujo) {
        LIST list = getValue(ujo);
        return list!=null ? list.size() : 0 ;
    }
    
    /** Return a Class of the Item. */
    public Class<ITEM> getItemType() {
        return itemType;
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
        LIST result = getList(ujo);
        return result.add(value);
    }
    
    /** Removes the first occurrence in this list of the specified element. 
     * @return true if this list is not null and contains the specified element, otherwise returns false.
     * @since 0.81
     */
    public boolean removeItem(UJO ujo, ITEM value) {
        LIST list = getValue(ujo);
        return list!=null && list.remove(value); 
    }
    
    /**
     * Return a not null List. If original list value is empty, the new List is created.
     * @see #getItem(Ujo,int)
     */
    @SuppressWarnings("unchecked")
    public LIST getList(final UJO ujo) {
        LIST result = getValue(ujo);
        if (result==null) {
            try {
                result = getType().isInterface()
                    ? (LIST) new ArrayList()
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
    public void sort(UJO ujo, boolean asc, UjoProperty ... properties) {
        LIST list = getValue(ujo);
        if ( list!=null) {
            Comparator comp = new UjoComparator(asc, properties);
            Collections.sort(list, comp);
        }
    }

    /** The default list may have a <code>NULL</code> value only.
     * @deprecated Method throws the UnsupportedOperationException
     */
    @Override
    @Deprecated 
    public <PROPERTY extends AbstractProperty> PROPERTY setDefault(LIST value) {
        throw new UnsupportedOperationException("Property list can't have a non null value.");
    }

    /** Indicates whether a list of items is null or empty. */
    @Override
    public boolean isDefault(UJO ujo) {
        LIST list = getValue(ujo);
        return list==null || list.isEmpty();
    }
    
}
