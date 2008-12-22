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
 * Comparator of Ujo objects.
 * <br>
 *
 * @author Pavel Ponec
 */
public class UjoComparator /* <Ujo extends Ujo>: The comparator can't have a generic type! */
    implements Comparator<Ujo> {
    
    final UjoProperty[] properties;
    final boolean asc;
    
    /** Creates a new instance of UjoComparator 
     * @param asc order of sorting
     * @param properties sorting criteria are ordered by importance to down.
     */
    public UjoComparator(boolean asc, UjoProperty ... properties) {
        this.properties = properties;
        this.asc = asc;
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
            int result = asc
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
                sb.append(',');
            }
            sb.append(property.getName());
        }
        sb.append(" (").append(asc ? "ASC)" : "DESC)");
        return sb.toString();
    }
    
    /** An equals test */
    final public boolean equals(final Ujo u1, final Ujo u2) {
        final boolean result = compare(u1, u2)==0;
        return result;
    }
    
    // ------------ STATIC ------------
    
    /** Create new comparator */
    public static UjoComparator create(boolean asc, UjoProperty ... properties) {
        return new UjoComparator(asc, properties);
    }
    
    /** Create new comparator */
    public static  UjoComparator create(UjoProperty ... properties) {
        return create(true, properties);
    }
    
}
