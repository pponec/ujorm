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

import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoActionImpl;
import org.ujoframework.core.UjoManager;
import org.ujoframework.swing.UjoPropertyRow;

/**
 * This is a simple abstract implementation of Ujo. <br>
 * For implementation define only a "public static final MapProperty" constants in a child class.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: very simple implementaton and a sufficient performance for common tasks. The architecture is useful for a rare assignment of values in object too.
 * @author Pavel Ponec
 */
public abstract class SuperUjoExt<UJO_IMPL extends UjoExt> extends SuperUjo implements UjoExt<UJO_IMPL> {
    
    /** Getter based on one UjoProperty */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> VALUE get
        ( UjoProperty<UJO, VALUE> property) {
        return (VALUE) UjoManager.getValue(this, property);
    }

    /** Getter based on two properties */
    @SuppressWarnings("unchecked")
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, VALUE> VALUE get
        ( UjoProperty<UJO1, UJO2 > property1
        , UjoProperty<UJO2, VALUE> property2) {
        
        final PathProperty<UJO1, VALUE> path = PathProperty.create(property1, property2);
        return get(path);    }

    /** Getter based on three properties */
    @SuppressWarnings("unchecked")
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> VALUE get
        ( UjoProperty<UJO1, UJO2 > property1
        , UjoProperty<UJO2, UJO3 > property2
        , UjoProperty<UJO3, VALUE> property3 ) {

        final PathProperty<UJO1, VALUE> path = PathProperty.create(property1, property2, property3);
        return get(path);    }

    /** Setter  based on UjoProperty. Type of value is checked in the runtime. */
    @SuppressWarnings({"unchecked"})
    public <UJO extends UJO_IMPL, VALUE> UJO_IMPL set
        ( UjoProperty<UJO, VALUE> property
        , VALUE value) {
        readUjoManager().assertAssign(property, value);
        UjoManager.setValue(this, property, value);
        return (UJO_IMPL) this;
    }

    /** Setter  based on two properties. Type of value is checked in the runtime. */
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, VALUE> void set
        ( UjoProperty<UJO1, UJO2 > property1
        , UjoProperty<UJO2, VALUE> property2
        , VALUE value) {
        
        final PathProperty<UJO1, VALUE> path = PathProperty.create(property1, property2);
        set(path, value);    }

    /** Setter  based on three properties. Type of value is checked in the runtime. */
    public <UJO1 extends UJO_IMPL, UJO2 extends Ujo, UJO3 extends Ujo, VALUE> void set
        ( UjoProperty<UJO1, UJO2 > property1
        , UjoProperty<UJO2, UJO3 > property2
        , UjoProperty<UJO3, VALUE> property3
        , VALUE value) {

        final PathProperty<UJO1, VALUE> path = PathProperty.create(property1, property2, property3);
        set(path, value);    
    }

    // ------ LIST ----------
    
    /** Returns a count of Items. If the property is null, method returns 0. 
     * <br>Inside is called a method UjoPropertyList.getItemCount() internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> int getItemCount
        (UjoPropertyList<UJO,LIST,ITEM> property) {
        return ((UjoPropertyList)property).getItemCount(this);
    }
    
    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method UjoPropertyList.addItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> UJO_IMPL add
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , ITEM value 
        ) {
        property.addItem((UJO) this, value);
        return (UJO_IMPL) this;
    }

    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method UjoPropertyList.setItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> UJO_IMPL set
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , int index, ITEM value
        ) {
        property.setItem((UJO)this, index, value);
        return (UJO_IMPL) this;
    }
    
    /** Get Value
     * <br>Inside is called a method UjoPropertyList.getItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> ITEM get
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , int index
        ) {
        return (ITEM) ((UjoPropertyList)property).getItem(this, index);
    }
    

    /**
     * Remove an item from the List by an index.
     * @param property
     * @param index
     * @return removed item
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> ITEM remove
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , int index
        ) {
        return (ITEM) ((UjoPropertyList)property).getList(this).remove(index);
    }

    /**
     * Removes the first occurrence in this list of the specified element. 
     * @param property UjoPropertyList
     * @param item Item to remove
     * @return true if the list is not null and contains the specified element
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> boolean remove
        ( UjoPropertyList<UJO,LIST,ITEM> property
        , ITEM item
        ) {
        return ((UjoPropertyList)property).removeItem(this, item);
    }

    

    /** Returns a not null List. If original list value is empty, the new List is created.
     * <br>Inside is called a method UjoPropertyList.getList() internally.
     */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, LIST extends List<ITEM>,ITEM> LIST list
        (UjoPropertyList<UJO,LIST,ITEM> property
        ) {
        return (LIST) ((UjoPropertyList)property).getList(this);
    }
    
    /** Indicates whether a parameter value "equal to" property default value. */
    public <UJO extends UJO_IMPL, VALUE> boolean isDefault(UjoProperty<UJO, VALUE> property) {
        return property.isDefault(this);
    }

    // ----------- TEXT --------------
    
    
    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     * <br>The method is an alias for a method readValueString(...)
     *
     * @param property A Property
     * @return If property type is "container" then result is null.
     */
    public String getText(final UjoProperty property) {
        return readValueString(property, null);
    }
    
    
    
    /**
     * Set value from a String format by a NULL context. Property can't be an "container" type (Ujo, List, Object[]).
     * <br>The method is an alias for a method writeValueString(...)
     * @param property Property
     * @param value String value
     */
    public void setText(final UjoProperty property, final String value) {
        writeValueString(property, value, null, null);
    }       
    
    
    // ------- UTILITIES BUT NO INTERFACE SUPPORT -------
    
    /** Compare the property value with a parametrer value. The property value can be null.  */
    @SuppressWarnings("unchecked")
    public <UJO extends UJO_IMPL, VALUE> boolean equals(UjoProperty<UJO,VALUE> property, VALUE value) {
        return property.equals((UJO)this, value);
    } 

    /**
     * Find a property by a "property name".
     * @param propertyName The name of property
     * @return The first UjoProperty with the same name.
     * @throws java.lang.IllegalArgumentException If property not found.
     */
    public UjoProperty findProperty(String propertyName) throws IllegalArgumentException {
        
        final boolean throwException = true;
        return UjoManager.getInstance().findProperty(this, propertyName, throwException);
    }
        
    /** Create a list of UjoProperty. */
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
        UjoManager.getInstance().copy(this, target, new UjoActionImpl(UjoAction.ACTION_COPY, context), (UjoProperty[]) null);
    } 

    /** Copy selected attributes to the target */
    public void copyTo(Ujo target, UjoProperty... properties) {
        UjoManager.getInstance().copy(this, target, new UjoActionImpl(UjoAction.ACTION_COPY), properties);
    }

}
