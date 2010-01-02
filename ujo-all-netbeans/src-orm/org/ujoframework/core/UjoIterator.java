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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Query;

/**
 * Ujo iterator have got some extended functions:
 * <ul>
 *    <li>iterator can provide optionally a count of items by a count() method</li>
 *    <li>iterator can be used in generic loop syntax for( : ) </li>
 *    <li>iterator can create a List object</li>
 * </ul>
 * @author Pavel Ponec
 */
abstract public class UjoIterator<T> implements Iterable<T>, Iterator<T> {


    /** Tests if this enumeration contains more elements. */
    @Override
    abstract public boolean hasNext();

    /**
     * Returns the next element if exists.
     * @return     the next element
     * @exception  NoSuchElementException no more elements exist.
     */
    @Override
    abstract public T next() throws NoSuchElementException;

    /** Returns a count of items or value -1 if the count is not known. */
    public long count() {
        return -1L;
    }

    /** Returns the same instance */
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    /** An unsupported method.
     * @deprecated The method is not implemented.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /** Skip some items by the parameter.
     * @param count A count of item to skip.
     * @return Returns a true value if the skip count was no limited.
     */
    public boolean skip(int count) {
        for (; count>0 && hasNext(); --count) {
            next();
        }
        return count==0;
    }


    /** Copy items to a new List */
    public List<T> toList() throws IllegalStateException {
        final List<T> result = new ArrayList<T>(32);
        for (T item : this) {
            result.add(item);
        }
        return result;
    }

    /** Close all resources, if any.
     * You may call this method if a data source was not read until the end.
     */
    public void close() {
    }

    /** Returns a count of items. */
    @Override
    public String toString() {
        return "size: " + count();
    }

    // --- STATIC FACTORY ---------

    @SuppressWarnings("unchecked")
    final public static <T> UjoIterator<T> getInstance(final Iterator<T> iterator) {
        return new UjoIteratorImpl(iterator);
    }

    @SuppressWarnings("unchecked")
    final public static <T> UjoIterator<T> getInstance(final Collection<T> collection) {
        return new UjoIteratorImpl(collection.iterator(), collection.size());
    }

    @SuppressWarnings("unchecked")
    final public static <T extends OrmUjo> UjoIterator<T> getInstance(Query<T> query) {
        return new ResultSetIterator(query);
    }


}
