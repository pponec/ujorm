/*
 *  Copyright 2009 Paul Ponec
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.core;

import java.util.Iterator;
import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.UjoPropertyList;
import org.ujoframework.UjoAction;

/**
 * The immutable list of UjoProperties.
 * The UjoPropertyList class is a subset of the methods from class List&lt;UjoProperty&gt;.
 * @author Pavel Ponec
 * @composed 1 - N UjoProperty
 */
final public class UjoPropertyListImpl implements UjoPropertyList {

    /** An empty array of the UJO properties */
    final static public UjoProperty[] EMPTY = new UjoProperty[0];
    final private UjoProperty[] props;
    /** Contains the total count of its properties */
    final public int length;
    /** The Ujo type */
    final private Class type;


    public UjoPropertyListImpl(Class type, List<UjoProperty> props) {
        this(type, props.toArray(new UjoProperty[props.size()]));
    }

    public UjoPropertyListImpl(Class type, UjoProperty[] props) {
        type.hashCode(); // The not null test

        this.type  = type;
        this.props = props;
        this.length = props.length;
    }

    /** Create the empty list */
    public UjoPropertyListImpl(Class type) {
        this(type, EMPTY);
    }

    /**
     * Find a property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return .
     */
    public UjoProperty find
    ( final String name
    , final boolean throwException
    ) throws IllegalArgumentException
    {
        int nameHash = name.hashCode();

        for (UjoProperty prop : props) {
            if (prop.getName().hashCode()==nameHash  // speed up
            &&  prop.getName().equals(name)) {
                return prop;
            }
        }

        if (throwException) {
            throw new IllegalArgumentException("A property name \"" + name + "\" was not found in the " + type);
        } else {
            return null;
        }
    }

    final public UjoProperty find
    ( final Ujo ujo
    , final String name
    , final boolean throwException
    ) throws IllegalArgumentException
    {
        return find(ujo, name, UjoAction.DUMMY, true, throwException);
    }

    /**
     * Find a property by property name from parameter.
     * @param ujo An Ujo object
     * @param name A property name.
     * @param action Action type UjoAction.ACTION_* .
     * @param result Required result of action.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     */
    @SuppressWarnings("deprecation")
    public UjoProperty find
    ( final Ujo ujo
    , final String name
    , final UjoAction action
    , final boolean result
    , final boolean throwException
    ) throws IllegalArgumentException
    {
        if (ujo==null) { return null; }
        int nameHash = name.hashCode();
        UjoManager ujoManager = UjoManager.getInstance();

        for (final UjoProperty prop : props) {
            if (prop.getName().hashCode()==nameHash  // speed up
            &&  prop.getName().equals(name)
            && (action.getType()==UjoAction.ACTION_XML_ELEMENT
                ? !ujoManager.isXmlAttribute(prop)
                : ujo.readAuthorization(action, prop, null)
               )==result
            ){
                return prop;
            }
        }

        if (throwException) {
            throw new IllegalArgumentException("A name \"" + name + "\" was not found in a " + ujo.getClass());
        } else {
            return null;
        }
    }

    /** Returns a copy of internal array */
    public UjoProperty[] toArray() {
        final UjoProperty[] result = new UjoProperty[length];
        System.arraycopy(props, 0, result, 0, length);
        return result;
    }

    /** Get last property */
    public UjoProperty last() {
        return props[length-1];
    }

    /** Returns a class of the related UJO */
    public Class getType() {
        return type;
    }

    /** Returns a class name of the related UJO */
    public String getTypeName() {
        return getClass().getName();
    }

    // ----------------- LIST IMPLEMENTATION ------------------------

    /** Get property on requered index */
    public UjoProperty get(final int index) {
        return props[index];
    }

    /** Returns a total count of its properties */
    public int size() {
        return length;
    }

    /** Is the collection empty? */
    public boolean isEmpty() {
        return length==0;
    }

    /** Returns true if list contains property from the parameter. */
    public boolean contains(final UjoProperty o) {
        for (UjoProperty p : props) {
            if (p==o) return true;
        }
        return false;
    }

    /** Create an interator for all properties. */
    @Override
    public Iterator<UjoProperty> iterator() {
        final Iterator<UjoProperty> result = new Iterator<UjoProperty>() {
            private int i = 0;

            final public boolean hasNext() {
                return i<length;
            }
            final public UjoProperty next() {
                return props[i++];
            }
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        return result;
    }

}
