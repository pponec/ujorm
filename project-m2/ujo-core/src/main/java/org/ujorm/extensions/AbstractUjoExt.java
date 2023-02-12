/*
 *  Copyright 2007-2022 Pavel Ponec
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
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.UjoActionImpl;
import org.ujorm.core.UjoManager;
import org.ujorm.swing.UjoKeyRow;

/**
 * This is a simple abstract implementation of Ujo. <br>
 * For implementation define only a "public static final Key" constants in a child class.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: very simple implementation and a sufficient performance for common tasks. The architecture is useful for a rare assignment of values in object too.
 * @author Pavel Ponec
 */
public abstract class AbstractUjoExt<UJO extends AbstractUjoExt> extends SuperAbstractUjo implements UjoExt<UJO> {

    /** Getter based on one Key */
    @SuppressWarnings("unchecked")
    @Override
    public <VALUE> VALUE get
        ( final Key<? super UJO, VALUE> key
        ) {
        return key.of((UJO)this);
    }

    /** Getter based on two keys */
    @SuppressWarnings("unchecked")
    @Override
    public <UJO2 extends Ujo, VALUE> VALUE get
        ( final Key<? super UJO, UJO2 > property1
        , final Key<UJO2, VALUE> property2) {

        final Key<UJO, VALUE> path = PathProperty.of(property1, property2);
        return get(path);
    }

    /** Getter based on three keys */
    @SuppressWarnings("unchecked")
    @Override
    public <UJO2 extends Ujo, UJO3 extends Ujo, VALUE> VALUE get
        ( final Key<? super UJO, UJO2 > property1
        , final Key<UJO2, UJO3 > property2
        , final Key<UJO3, VALUE> property3
        ) {

        final Key<UJO, VALUE> path = PathProperty.of(property1, property2, property3);
        return get(path);
    }

    /** Setter  based on Key. Type of value is checked in the runtime. */
    @SuppressWarnings({"unchecked"})
    @Override
    public <VALUE> UJO set
        ( final Key<? super UJO, VALUE> key
        , final VALUE value
        ) {
        UjoManager.assertAssign(key, value);
        key.setValue((UJO)this, value);
        return (UJO) this;
    }

    /** Setter  based on two keys. Type of value is checked in the runtime. */
    @Override
    public <UJO2 extends Ujo, VALUE> void set
        ( final Key<? super UJO, UJO2> property1
        , final Key<UJO2, VALUE> property2
        , final VALUE value
        ) {

        final Key<UJO, VALUE> path = PathProperty.of(property1, property2);
        set(path, value);
    }

    /** Setter  based on three keys. Type of value is checked in the runtime. */
    @Override
    public <UJO2 extends Ujo, UJO3 extends Ujo, VALUE> void set
        ( final Key<? super UJO, UJO2 > property1
        , final Key<UJO2, UJO3 > property2
        , final Key<UJO3, VALUE> property3
        , final VALUE value
        ) {

        final Key<UJO, VALUE> path = PathProperty.of(property1, property2, property3);
        set(path, value);
    }

    @Override
    public KeyList<UJO> readKeyList() {
        return super.readKeys();
    }

    // ------ LIST ----------

    /** Returns a count of Items. If the key is null, method returns 0.
     * <br>Inside is called a method ListUjoPropertyCommon.getItemCount() internally.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <ITEM> int getItemCount
        ( final ListKey<? super UJO,ITEM> key
        ) {
        return key.getItemCount((UJO)this);
    }

    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method ListUjoPropertyCommon.addItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <ITEM> UJO add
        ( final ListKey<? super UJO,ITEM> key
        , final ITEM value
        ) {
        key.addItem((UJO) this, value);
        return (UJO) this;
    }

    /** Add Value, if the List is null then the list will be created.
     * <br>Inside is called a method ListUjoPropertyCommon.setItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <ITEM> UJO set
        ( final ListKey<? super UJO,ITEM> key
        , final int index
        , final ITEM value
        ) {
        key.setItem((UJO)this, index, value);
        return (UJO) this;
    }

    /** Get Value
     * <br>Inside is called a method ListUjoPropertyCommon.getItem(...) internally.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <ITEM> ITEM get
        ( final ListKey<? super UJO,ITEM> key
        , final int index
        ) {
        return key.getItem((UJO)this, index);
    }


    /**
     * Remove an item from the List by an index.
     * @param key
     * @param index
     * @return removed item
     */
    @SuppressWarnings("unchecked")
    @Override
    public <ITEM> ITEM remove
        ( final ListKey<? super UJO,ITEM> key
        , final int index
        ) {
        return key.getList((UJO)this).remove(index);
    }

    /**
     * Removes the first occurrence in this list of the specified element.
     * @param key ListUjoPropertyCommon
     * @param item Item to remove
     * @return true if the list is not null and contains the specified element
     */
    @SuppressWarnings("unchecked")
    public <ITEM> boolean remove
        ( final ListKey<? super UJO,ITEM> key
        , final ITEM item
        ) {
        return key.removeItem((UJO)this, item);
    }

    /** Returns a not null List. If original list value is empty, the new List is created.
     * <br>Inside is called a method ListUjoPropertyCommon.getList() internally.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <LIST extends List<ITEM>,ITEM> LIST list
        ( final ListKey<? super UJO,ITEM> key
        ) {
        return (LIST) key.getList( (UJO) this);
    }

    /** Returns a not null List. If original list value is empty, the new List is created.
     * <br>Inside is called a method ListUjoPropertyCommon.getList() internally.
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <ITEM> List<ITEM> getList(final ListKey<? super UJO,ITEM> key) {
        return key.getList( (UJO) this);
    }

    /** Indicates whether a parameter value "equal to" key default value. */
    @SuppressWarnings("unchecked")
    @Override
    public <VALUE> boolean isDefault
        ( final Key<? super UJO, VALUE> key) {
        final boolean result = key.isDefault((UJO)this);
        return result;
    }

    // ----------- TEXT --------------


    /**
     * Returns a String value by a NULL context.
     * otherwise method returns an instance of String.
     *
     * @param key A Property
     * @return If key type is "container" then result is null.
     */
    @Override
    public String getText(final Key key) {
        return readUjoManager().getText(this, key, null);
    }

    /**
     * Set value from a String format by a NULL context. Types Ujo, List, Object[] are not supported by default.
     * <br>The method is an alias for a method writeValueString(...)
     * @param key Property
     * @param value String value
     */
    @Override
    public void setText(final Key key, final String value) {
        readUjoManager().setText(this, key, value, null, null);
    }


    // ------- UTILITIES BUT NO INTERFACE SUPPORT -------

    /** Compare the key value with a parametrer value. The key value can be null.  */
    @SuppressWarnings("unchecked")
    public <VALUE> boolean equals(Key<? super UJO,VALUE> key, VALUE value) {
        return key.equals((UJO)this, value);
    }

    /**
     * Find a key by a "key name".
     * @param propertyName The name of key
     * @return The first Key with the same name.
     * @throws java.lang.IllegalArgumentException If key not found.
     */
    public Key findProperty(final String propertyName) throws IllegalArgumentException {
        final boolean throwException = true;
        return readKeys().findDirectKey(propertyName, throwException);
    }

    /** Create a list of Key. */
    public List<UjoKeyRow> createPropertyList() {
        return UjoManager.getInstance().createKeyRowList(this, new UjoActionImpl(this));
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
     * </ul>
     */
    @Override
    @SuppressWarnings("unchecked")
    public UJO clone(final int depth, final Object context) {
        return (UJO) super.clone(depth, context);
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
