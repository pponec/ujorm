/*
 *  Copyright 2007-2010 Pavel Ponec
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

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * A generic comparator for the Ujo objects. A direction is the sorting is controlled by attribute UjoProperty.isAscending() .
 * @author Pavel Ponec
 * @see UjoProperty#isAscending()
 * @see UjoProperty#descending() 
 */
final public class UjoComparator /* <Ujo extends Ujo>: The comparator can't have a generic type! */
    implements Comparator<Ujo> {
    
    final UjoProperty[] properties;
    final private Locale collatorLocale;
    final private int collatorStrength;
    private Collator collator;
    
    /** Creates a new instance of UjoComparator. The String are compared as Collator.IDENTICAL by English locale by default.
     * @param properties sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method UjoProperty#isAscending().
     * @see UjoProperty#isAscending()
     * @see UjoProperty#descending()
     */
    public UjoComparator(final UjoProperty ... properties) {
        this(Locale.ENGLISH, Collator.IDENTICAL, properties);
    }

    /** Creates a new instance of UjoComparator
     * @param locale Locale for a String coparation
     * @param collatorStrength Cllator Strength for String comparations
     * @param properties sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method UjoProperty#isAscending().
     * @see UjoProperty#isAscending()
     * @see UjoProperty#descending()
     */
    public UjoComparator(Locale locale, int collatorStrength, final UjoProperty ... properties) {
        this.properties = properties;
        this.collatorLocale = locale;
        switch (collatorStrength) {
            case (Collator.PRIMARY):
            case (Collator.SECONDARY):
            case (Collator.TERTIARY):
            case (Collator.IDENTICAL):
                this.collatorStrength = collatorStrength;
                break;
            default:
                // Throw the IllegalArgumentException:
                Collator.getInstance(Locale.ENGLISH).setStrength(collatorStrength);
                this.collatorStrength = Integer.MIN_VALUE;
        }
    }

    /** Collator for String comparations.
     * Default collator have en English locale with the IDENTICAL strength (case sensitive);
     */
    public Collator getCollator() {
        if (collator==null) {
            collator = Collator.getInstance(collatorLocale);
            collator.setStrength(collatorStrength);
        }
        return collator;
    }

    /** Collator for String comparations */
    public void setCollator(Collator collator) {
        this.collator = collator;
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

            final Comparable c1 = (Comparable) property.of(u1);
            final Comparable c2 = (Comparable) property.of(u2);
            
            if (c1==c2  ) { continue;  }
            if (c1==null) { return +1; }
            if (c2==null) { return -1; }

            int result;
            if (property.isTypeOf(String.class)) {
                result = property.isAscending()
                ? getCollator().compare(c1, c2)
                : getCollator().compare(c2, c1)
                ;
            } else {
                result = property.isAscending()
                ? c1.compareTo(c2)
                : c2.compareTo(c1)
                ;
            }
            if (result != 0) {
                return result;
            }
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
    
    /** Creates a new instance of UjoComparator. The String are compared as Collator.IDENTICAL by English locale by default.
     * @param properties sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method UjoProperty#isAscending().
     * @see UjoProperty#isAscending()
     * @see UjoProperty#descending()
     */
    public static UjoComparator newInstance(UjoProperty ... properties) {
        return new UjoComparator(properties);
    }

    /** Creates a new instance of UjoComparator
     * @param locale Locale for a String coparation
     * @param collatorStrength Cllator Strength for String comparations
     * @param properties sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method UjoProperty#isAscending().
     * @see UjoProperty#isAscending()
     * @see UjoProperty#descending()
     */
    public static UjoComparator newInstance(Locale locale, int collatorStrength, final UjoProperty ... properties) {
        return new UjoComparator(properties);
    }

    
}
