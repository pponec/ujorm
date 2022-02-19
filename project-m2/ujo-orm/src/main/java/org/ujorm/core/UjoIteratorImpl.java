/*
 *  Copyright 2020-2022 Pavel Ponec
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

package org.ujorm.core;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Ujo iterator have got some extended functions:
 * <ul>
 *    <li>iterator can provide optionally a count of items by a count() method</li>
 *    <li>iterator can be used in generic loop syntax for( : ) </li>
 *    <li>iterator can create a List object</li>
 * </ul>

 * @author Pavel Ponec
 */
class UjoIteratorImpl<T> extends UjoIterator<T> {

    final private Iterator<T> e;
    final private long count;

    public UjoIteratorImpl(final Iterator<T> iterator, long count) {
        this.e = iterator;
        this.count = count;
    }

    public UjoIteratorImpl(final Iterator<T> iterator) {
        this(iterator, -1);
    }

    /** Tests if this enumeration contains more elements. */
    @Override
    public boolean hasNext() {
        return e.hasNext();
    }

    /**
     * Returns the next element if exists.
     * @return     the next element
     * @exception  NoSuchElementException no more elements exist.
     */
    @Override
    public T next() throws NoSuchElementException {
        return e.next();
    }

    /** Returns a count of items or value -1 if the count is not defined. */
    @Override
    public long count() {
        return count;
    }

}
