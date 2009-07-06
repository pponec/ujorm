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
final public class UjoPropertySet implements Iterable<UjoProperty> {

    /** An empty array of the UJO properties */
    final static public UjoProperty[] EMPTY = new UjoProperty[0];
    final private UjoProperty[] props;
    final public int length;


    public UjoPropertySet(List<UjoProperty> props) {
        this(props.toArray(new UjoProperty[props.size()]));
    }

    public UjoPropertySet(UjoProperty[] props) {
        this.props = props;
        this.length = props.length;
    }

    /** Create the empty list */
    public UjoPropertySet() {
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
