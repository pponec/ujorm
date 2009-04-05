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

package org.ujoframework.core;

import java.util.Comparator;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * Comparator of Ujo objects. A direction is the sorting is controlled by attribute UjoProperty.isAscending() .
 * @author Pavel Ponec
 * @see UjoProperty#isAscending()
 * @see UjoProperty#descending() 
 */
public class UjoComparator /* <Ujo extends Ujo>: The comparator can't have a generic type! */
    implements Comparator<Ujo> {
    
    final UjoProperty[] properties;
    
    /** Creates a new instance of UjoComparator
     * @param properties sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method UjoProperty#isAscending().
     * @see UjoProperty#isAscending()
     * @see UjoProperty#descending()
     */
    public UjoComparator(final UjoProperty ... properties) {
        this.properties = properties;
    }
    
    /**
     * Compare two Ujo objects.
     *
     * @param u1 Ujo Object 1
     * @param u2 Ujo Object 2
     * @return Result of comparation
     */
    @SuppressWarnings("unchecked")
    public int compare(Ujo u1, Ujo u2) {
        for (UjoProperty property : properties) {

            Comparable c1 = (Comparable) UjoManager.getValue(u1, property);
            Comparable c2 = (Comparable) UjoManager.getValue(u2, property);
            
            if (c1==c2  ) { continue;  }
            if (c1==null) { return +1; }
            if (c2==null) { return -1; }

            int result = property.isAscending()
            ? c1.compareTo(c2)
            : c2.compareTo(c1)
            ;
            if (result!=0) { return result; }
        }
        return 0;
    }
    
    /** A String reprezentation. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        for (UjoProperty property : properties) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(property.getName());
            sb.append("[");
            sb.append(property.isDirect() ? "ASC" : "DESC");
            sb.append("]");
        }
        return sb.toString();
    }
    
    /** An equals test */
    final public boolean equals(final Ujo u1, final Ujo u2) {
        final boolean result = compare(u1, u2)==0;
        return result;
    }
    
    // ------------ STATIC ------------
    
    /** Create new comparator */
    public static UjoComparator newInstance(UjoProperty ... properties) {
        return new UjoComparator(properties);
    }
    
}
