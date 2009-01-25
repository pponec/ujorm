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


package org.ujoframework.core;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Ujo Iterator
 * @author pavel
 */
public class UjoIterator<T> implements Iterable<T>, Iterator<T> {

    final private Enumeration<T> e;
    final private int size;

    public UjoIterator(final Enumeration<T> enumeration, int size ) {
        this.e = enumeration;
        this.size = size;
    }

    public UjoIterator(final Enumeration<T> enumeration) {
        this(enumeration, -1);
    }

    /** Returns the same instance */
    public Iterator<T> iterator() {
        return this;
    }

    /** Tests if this enumeration contains more elements. */
    public boolean hasNext() {
        return e.hasMoreElements();
    }

    /**
     * Returns the next element if exists.
     * @return     the next element
     * @exception  NoSuchElementException no more elements exist.
     */
    public T next() throws NoSuchElementException {
        return e.nextElement();
    }

    /** Returns a count of items or value -1 if the count is not defined. */
    public int size() {
        return size;
    }

    /** An unsupported method.
     * @deprecated The method is not implemented.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }


    // --- STATIC FACTORY ---------

    @SuppressWarnings("unchecked")
    final public static <T> UjoIterator<T> getIntance(final Enumeration<T> enumeration) {
        return new UjoIterator(enumeration);
    }

}
