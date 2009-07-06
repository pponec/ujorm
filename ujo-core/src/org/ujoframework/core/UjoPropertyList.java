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
import org.ujoframework.UjoProperty;

/**
 * The immutable set of UjoPropertySet.
 * The UjoProperty class is a subset of the methods from class List&lt;UjoProperty&gt;.
 * @author Pavel Ponec
 */
final public class UjoPropertyList implements Iterable<UjoProperty> {

    /** An empty array of the UJO properties */
    final static public UjoProperty[] EMPTY = new UjoProperty[0];
    final private UjoProperty[] props;
    final public int length;


    public UjoPropertyList(List<UjoProperty> props) {
        this(props.toArray(new UjoProperty[props.size()]));
    }

    public UjoPropertyList(UjoProperty[] props) {
        this.props = props;
        this.length = props.length;
    }

    /** Create the empty list */
    public UjoPropertyList() {
        this(EMPTY);
    }

    /**
     * Find a property by property name from parameter.
     *
     * @param ujo An Ujo object
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return .
     */
    public UjoProperty findProperty
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
            throw new IllegalArgumentException("A name \"" + name + "\" was not found");
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

    // ----------------- LIST IMPLEMENTATION ------------------------

    /** Get property on requered index */
    public UjoProperty get(final int index) {
        return props[index];
    }

    /** Returns a size of properties */
    public int size() {
        return length;
    }

    /** Is the collection empty? */
    public boolean isEmpty() {
        return length==0;
    }

    public boolean contains(Object o) {
        for (UjoProperty p : props) {
            if (p==o) return true;
        }
        return false;
    }

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