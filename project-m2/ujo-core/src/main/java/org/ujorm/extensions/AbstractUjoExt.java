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

import org.ujorm.ListKey;
import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.UjoAction;
import org.ujorm.core.UjoActionImpl;
import org.ujorm.core.UjoManager;
import org.ujorm.swing.UjoPropertyRow;

/**
 * This is a simple abstract implementation of Ujo. <br>
 * For implementation define only a "public static final Key" constants in a child class.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: very simple implementaton and a sufficient performance for common tasks. The architecture is useful for a rare assignment of values in object too.
 * @author Pavel Ponec
 */
public abstract class AbstractUjoExt<UJO_IMPL extends UjoExt> extends AbstractUjo implements UjoExt<UJO_IMPL> {
    
    /** Getter based on one Key */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> VALUE get
        ( final Key<UJO, VALUE> property
        ) {
        return property.of((UJO)this);
    }

    /** Getter based on two keys */
    @SuppressWarnings("unchecked")
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, VALUE> VALUE get
        ( final Key<UJO1, UJO2 > property1
        , final Key<UJO2, VALUE> property2) {
        
        final PathProperty<UJO1, VALUE> path = PathProperty.newInstance(property1, property2);
        return get(path);    }

    /** Getter based on three keys */
    @SuppressWarnings("unchecked")
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> VALUE get
        ( final Key<UJO1, UJO2 > property1
        , final Key<UJO2, UJO3 > property2
        , final Key<UJO3, VALUE> property3
        ) {

        final PathProperty<UJO1, VALUE> path = PathProperty.newInstance(property1, property2, property3);
        return get(path);    }

    /** Setter  based on Key. Type of value is checked in the runtime. */
    @SuppressWarnings({"unchecked"})
    public <UJO extends UJO_IMPL, VALUE> UJO_IMPL set
        ( final Key<UJO, VALUE> property
        , final VALUE value
        ) {
        readUjoManager().assertAssign(property, value);
        property.setValue((UJO)this, value);
        return (UJO_IMPL) this;
    }

    /** Setter  based on two keys. Type of value is checked in the runtime. */
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, VALUE> void set
        ( final Key<UJO1, UJO2 > property1
        , final Key<UJO2, VALUE> property2
        , final VALUE value
        ) {
        
        final PathProperty<UJO1, VALUE> path = PathProperty.newInstance(property1, property2);
        set(path, value);
    }

    /** Setter  based on three keys. Type of value is checked in the runtime. */
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> void set
        ( final Key<UJO1, UJO2 > property1
        , final Key<UJO2, UJO3 > property2
        , final Key<UJO3, VALUE> property3
        , final VALUE value
        ) {

        final PathProperty<UJO1, VALUE> path = PathProperty.newInstance(property1, property2, property3);
        set(path, value);    
    }

    // ------ LIST ----------
    
    /** Returns a count of Items. If the property is null, method returns 0. 
     * <br>Inside is called a method ListUjoPropertyCommon.getItemCount() internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, ITEM> int getItemCount
        ( final ListKey<UJO,ITEM> property
        ) {
        return ((ListKey)property).getItemCount(this);
    }
    
    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method ListUjoPropertyCommon.addItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, ITEM> UJO_IMPL add
        ( final ListKey<UJO,ITEM> property
        , final ITEM value
        ) {
        property.addItem((UJO) this, value);
        return (UJO_IMPL) this;
    }

    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method ListUjoPropertyCommon.setItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, ITEM> UJO_IMPL set
        ( final ListKey<UJO,ITEM> property
        , final int index
        , final ITEM value
        ) {
        property.setItem((UJO)this, index, value);
        return (UJO_IMPL) this;
    }
    
    /** Get Value
     * <br>Inside is called a method ListUjoPropertyCommon.getItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, ITEM> ITEM get
        ( final ListKey<UJO,ITEM> property
        , final int index
        ) {
        return (ITEM) ((ListKey)property).getItem(this, index);
    }
    

    /**
     * Remove an item from the List by an index.
     * @param property
     * @param index
     * @return removed item
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, ITEM> ITEM remove
        ( final ListKey<UJO,ITEM> property
        , final int index
        ) {
        return (ITEM) ((ListKey)property).getList(this).remove(index);
    }

    /**
     * Removes the first occurrence in this list of the specified element. 
     * @param property ListUjoPropertyCommon
     * @param item Item to remove
     * @return true if the list is not null and contains the specified element
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, ITEM> boolean remove
        ( final ListKey<UJO,ITEM> property
        , final ITEM item
        ) {
        return ((ListKey)property).removeItem(this, item);
    }

    

    /** Returns a not null List. If original list value is empty, the new List is created.
     * <br>Inside is called a method ListUjoPropertyCommon.getList() internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> LIST list
        ( final ListKey<UJO,ITEM> property
        ) {
        return (LIST) ((ListKey)property).getList(this);
    }
    
    /** Indicates whether a parameter value "equal to" property default value. */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> boolean isDefault
        ( final Key<UJO, VALUE> property) {
        final boolean result = ((Key) property).isDefault(this);
        return result;
    }

    // ----------- TEXT --------------
    
    
    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     *
     * @param property A Property
     * @return If property type is "container" then result is null.
     */
    public String getText(final Key property) {
        return readUjoManager().getText(this, property, null);
    }
    
    /**
     * Set value from a String format by a NULL context. Types Ujo, List, Object[] are not supported by default.
     * <br>The method is an alias for a method writeValueString(...)
     * @param property Property
     * @param value String value
     */
    public void setText(final Key property, final String value) {
        readUjoManager().setText(this, property, value, null, null);
    }       
    
    
    // ------- UTILITIES BUT NO INTERFACE SUPPORT -------
    
    /** Compare the property value with a parametrer value. The property value can be null.  */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> boolean equals(Key<UJO,VALUE> property, VALUE value) {
        return property.equals((UJO)this, value);
    } 

    /**
     * Find a property by a "property name".
     * @param propertyName The name of property
     * @return The first Key with the same name.
     * @throws java.lang.IllegalArgumentException If property not found.
     */
    public Key findProperty(final String propertyName) throws IllegalArgumentException {
        final boolean throwException = true;
        return readKeys().findDirectKey(propertyName, throwException);
    }
        
    /** Create a list of Key. */
    public List<UjoPropertyRow> createPropertyList() {
        return UjoManager.getInstance().createPropertyList(this, new UjoActionImpl(this));
    }

    /**
     * Clone the UjoCloneable object. The Object and its items must have got a constructor with no parameters.
     * <br>Note: There are supported attributes
     * <ul>
     * <li>null value </li>
     * <li>Ujo</li>
     * <li>UjoCloneable</li>
     * <li>List</li>
     * <li>array of privitive values</li>
     * <ul>
     */
    @Override
    @SuppressWarnings("unchecked")
    public UJO_IMPL clone(final int depth, final Object context) {
        return (UJO_IMPL) super.clone(depth, context);
    }
    
    /** Copy all attributes to the target */
    public void copyTo(Ujo target, Object context) {
        UjoManager.getInstance().copy(this, target, new UjoActionImpl(UjoAction.ACTION_COPY, context), (Key[]) null);
    } 

    /** Copy selected attributes to the target */
    public void copyTo(Ujo target, Key... keys) {
        UjoManager.getInstance().copy(this, target, new UjoActionImpl(UjoAction.ACTION_COPY), keys);
    }

}
