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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.Query;

/**
 * Ujo iterator have got some extended functions:
 * <ul>
 *    <li>iterator can provide optionally a count of items by a size() method</li>
 *    <li>iterator can be used in generic loop syntax for( : ) </li>
 *    <li>iterator can create a List object</li>
 * </ul>

 * @author Ponec
 */
abstract public class UjoIterator<T> implements Iterable<T>, Iterator<T> {


    /** Tests if this enumeration contains more elements. */
    abstract public boolean hasNext();

    /**
     * Returns the next element if exists.
     * @return     the next element
     * @exception  NoSuchElementException no more elements exist.
     */
    abstract public T next() throws NoSuchElementException;

    /** Returns a count of items or value -1 if the count is not known. */
    public int size() {
        return -1;
    }

    /** Returns the same instance */
    public Iterator<T> iterator() {
        return this;
    }

    /** An unsupported method.
     * @deprecated The method is not implemented.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /** Copy items to a new List */
    public List<T> toList() {
        final List<T> result = new ArrayList<T>(size()>=0 ? size() : 10);
        for (T item : this) {
            result.add(item);
        }
        return result;
    }

    /** Returns a count of items. */
    @Override
    public String toString() {
        return "size: " + size();
    }

    // --- STATIC FACTORY ---------

    @SuppressWarnings("unchecked")
    final public static <T> UjoIteratorImpl<T> getIntance(final Enumeration<T> enumeration) {
        return new UjoIteratorImpl(enumeration);
    }

    @SuppressWarnings("unchecked")
    final public static <T extends TableUjo> ResultSetIterator<T> getIntance(Query<T> query, ResultSet rs) {
        return new ResultSetIterator(query, rs);
    }


}
