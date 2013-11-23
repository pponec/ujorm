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

import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.*;

/**
 * This is a simple abstract implementation of Ujo. <br>
 * For implementation define only a "public static final Key" constants in a child class.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: very simple implementaton and a sufficient performance for common tasks. The architecture is useful for a rare assignment of values in object too.

 * @author Pavel Ponec
 */
public abstract class SuperAbstractUjo implements Ujo, UjoTextable, UjoCloneable {

    /**
     * Initializa all keys. If the keys are unlocked than recalculate index
     * and set an undefined property name by its static field.
     * @param ujoClass Ujo class
     */
    @SuppressWarnings("unchecked")
    protected static KeyList init(Class ujoClass) throws IllegalStateException {
        return init(ujoClass, false);
    }


    /**
     * Initializa all keys. If the keys are unlocked than recalculate index
     * and set an undefined property name by its static field.
     * @param ujoClass Ujo class
     * @param checkUniqueProperties Check unique keys
     */
    @SuppressWarnings("unchecked")
    protected static KeyList init(Class ujoClass, boolean checkUniqueProperties) throws IllegalStateException {
        KeyList result = UjoManager.getInstance().readKeys(ujoClass);
        if (checkUniqueProperties) {
            UjoManager.getInstance().checkUniqueProperties(ujoClass);
        }
       return result;
    }

    /** Returns an UjoManager */
    protected UjoManager readUjoManager() {
        return UjoManager.getInstance();
    }
    
    /** Returns all direct keys.
     * <br>Note 1: An order of keys is sorted by a value of the index attribute.
     * <br>Note 2: The implemetation returns the original property array so it is possible to change some original property in the array from an extefnal code.
     *            Overwrite the method to return a copy array in case you need an assurance of immutable!
     * @see Key#isDirect()
     */
    @SuppressWarnings("unchecked")
    public <T extends Ujo> KeyList<T> readKeys() {
        return (KeyList<T>) readUjoManager().readKeys(getClass());
    }

    /**
     * Get an authorization of the property for different actions.
     * <br>A Default value is TRUE for all actions, keys and values.
     *
     *
     * @param action Type of request. See constant(s) ACTION_* for more information.
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY .
     * @param property A property of the Ujo
     * @param value A value
     * @return Returns TRUE, if property is authorized.
     * @see UjoAction Action Constants
     */
    public boolean readAuthorization(final UjoAction action, final Key property, final Object value) {
        return true;
    }
    
    /**
     * Is the object equals to a parameter Ujo?
     */
    @Override
    public boolean equals(final Object obj) {
        final boolean result = obj instanceof Ujo
        ? readUjoManager().equalsUjo(this, (Ujo) obj )
        : false
        ;
        return result;
    }
    
    
    /** A String representation. */
    @Override
    public String toString() {
        final String result = readUjoManager().toString(this);
        return result;
    }
    
    /**
     * Object is Cloneable
     * <br>Note: There are supported attributes
     * <ul>
     * <li>null value </li>
     * <li>Ujo</li>
     * <li>UjoCloneable</li>
     * <li>List</li>
     * <li>array of privitive values</li>
     * <ul>
     * 
     * @param depth Depth of clone.
     * @param context A context of the action.
     * <br>Sample: value "0" returns the same object, value "1" returns the same attribute values, etc.
     * @return A clone of current class
     */
    public Object clone(final int depth, final Object context) {
        return readUjoManager().clone(this, depth, context);
    }
    
    // ---- An UjoTextable implementation -----
    
    /**
     * Get an original value in a String format. Property must be an direct type.
     * otherwise method returns an instance of String.
     *
     * @param property A direct property only. See a method Key.isDirect().
     * @param action A context of the action.
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY .
     * @return If property type is "container" then result is null.
     */
    @SuppressWarnings("unchecked")
    public String readValueString(final Key property, final UjoAction action) {
        final Object value  = property.of(this);
        final String result = readUjoManager().encodeValue(value, false);
        return result;
    }
    
    /**
     * Set value from a String format. Property must be an direct type.
     *
     * @param property A direct property only. See a method Key.isDirect().
     * @param value String value
     * @param type Type can be a subtype of a Property.type. If type is null, then a property.type is used.
     * @param action A context of the action.
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY .
     */
    public void writeValueString(final Key property, final String value, final Class type, final UjoAction action) {
        final Object valueObj = readUjoManager().decodeValue(property, value, type);
        writeValue(property, valueObj);
    }

}
