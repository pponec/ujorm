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

import org.ujoframework.core.*;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import static org.ujoframework.extensions.UjoAction.*;

/**
 * This is a simple abstract implementation of Ujo. <br>
 * For implementation define only a "public static final MapProperty" constants in a child class.
 * The code syntax is Java 1.5 complied.<br>
 * <br>Features: very simple implementaton and a sufficient performance for common tasks. The architecture is useful for a rare assignment of values in object too.

 * @author Pavel Ponec
 */
public abstract class AbstractUjo implements Ujo, UjoTextable, UjoCloneable {

    /** A property order index. The field is used in a static method newProperty(..). 
     * @see #_nextPropertyIndex()
     */
    protected static int _propertyIndex = 0;

    /** Returns a next property index.
     * The UJO property indexed by this method may not be in continuous series
     * however numbers have the <strong>upward direction</strong> always.
     */
    protected static final synchronized int _nextPropertyIndex() {
        return _propertyIndex++;
    }
    
    /** Returns an UjoManager */
    protected UjoManager readUjoManager() {
        return UjoManager.getInstance();
    }
    
    /** Read all defined properties. <br>
     * An order of properties can be orderd by definition in code, but by a Java sepcification the feature is not guaranteed.
     */
    public UjoProperty[] readProperties() {
        final UjoProperty[] result = readUjoManager().readProperties(getClass());
        return result;
    }
    
    /**
     * Get an authorization of the property for different actions.
     * <br>A Default value is TRUE for all actions, properties and values.
     *
     *
     * @param action Type of request. See constant(s) ACTION_* for more information.
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY .
     * @param property A property of the Ujo
     * @param value A value
     * @return Returns TRUE, if property is authorized.
     * @see org.ujoframework.extensions.UjoAction Action Constants
     */
    public boolean readAuthorization(final UjoAction action, final UjoProperty property, final Object value) {
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
     * @param property A direct property only. See a method UjoProperty.isDirect().
     * @param action A context of the action.
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY .
     * @return If property type is "container" then result is null.
     */
    @SuppressWarnings("unchecked")
    public String readValueString(final UjoProperty property, final UjoAction action) {
        final Object value  = readValue(property);
        final String result = readUjoManager().encodeValue(value, false);
        return result;
    }

    
    /**
     * Set value from a String format. Property must be an direct type.
     *
     * @param property A direct property only. See a method UjoProperty.isDirect().
     * @param value String value
     * @param type Type can be a subtype of a Property.type. If type is null, then a property.type is used.
     * @param action A context of the action.
     *        The action must not be null, however there is allowed to use a dummy constant UjoAction.DUMMY .
     */
    public void writeValueString(final UjoProperty property, final String value, final Class type, final UjoAction action) {
        final Object valueObj = readUjoManager().decodeValue(type!=null ? type : property.getType(), value);
        writeValue(property, valueObj);
    }
    
}
