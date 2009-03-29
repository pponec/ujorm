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
import java.util.NoSuchElementException;

/**
 * Ujo iterator have got some extended functions:
 * <ul>
 *    <li>iterator can provide optionally a count of items by a size() method</li>
 *    <li>iterator can be used in generic loop syntax for( : ) </li>
 *    <li>iterator can create a List object</li>
 * </ul>

 * @author Ponec
 */
public class UjoIteratorImpl<T> extends UjoIterator<T> {

    final private Enumeration<T> e;
    final private int size;

    public UjoIteratorImpl(final Enumeration<T> enumeration, int size ) {
        this.e = enumeration;
        this.size = size;
    }

    public UjoIteratorImpl(final Enumeration<T> enumeration) {
        this(enumeration, -1);
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


}
