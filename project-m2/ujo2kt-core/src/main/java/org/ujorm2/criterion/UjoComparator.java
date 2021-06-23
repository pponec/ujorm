/*
 *  Copyright 2007-2014 Pavel Ponec
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

package org.ujorm2.criterion;

import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javax.annotation.*;
import org.ujorm2.Key;

/**
 * A generic comparator for the Ujo objects. A direction is the sorting is controlled by attribute Key.isAscending() .
 * @author Pavel Ponec
 * @see Key#isAscending()
 * @see Key#descending()
 */
final public class UjoComparator <D> implements Comparator<D> {

    @Nonnull
    final Key[] keys;
    final private Locale collatorLocale;
    final private int collatorStrength;
    @Nullable
    private Collator collator;

    /** Creates a new instance of UjoComparator. The String are compared as Collator.IDENTICAL by English locale by default.
     * @param keys sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method Key#isAscending().
     * @see Key#isAscending()
     * @see Key#descending()
     */
    public UjoComparator(@Nonnull final Key ... keys) {
        this(Locale.ENGLISH, Collator.IDENTICAL, keys);
    }

    /** Creates a new instance of UjoComparator
     * @param locale Locale for a String coparation
     * @param collatorStrength Cllator Strength for String comparations
     * @param keys sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method Key#isAscending().
     * @see Key#isAscending()
     * @see Key#descending()
     */
    public UjoComparator
        ( @Nullable final Locale locale
        , final int collatorStrength
        , @Nonnull final  Key ... keys) {
        this.keys = keys;
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
    @Nonnull
    public Collator getCollator() {
        if (collator==null) {
            collator = Collator.getInstance(collatorLocale);
            collator.setStrength(collatorStrength);
        }
        return collator;
    }

    /** Collator for String comparations */
    public void setCollator(@Nullable final Collator collator) {
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
    @Override
    public int compare(@Nullable final D u1, @Nullable final D u2) {
        if (u1==u2  ) { return  0; }
        if (u1==null) { return +1; }
        if (u2==null) { return -1; }

        for (Key key : keys) {

            final Comparable c1 = (Comparable) key.of(u1);
            final Comparable c2 = (Comparable) key.of(u2);

            if (c1==c2  ) { continue;  }
            if (c1==null) { return +1; }
            if (c2==null) { return -1; }

            int result;
            if (key.isTypeOf(String.class)) {
                result = key.isAscending()
                ? getCollator().compare(c1, c2)
                : getCollator().compare(c2, c1)
                ;
            } else {
                result = key.isAscending()
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

    /** Sort a list by this Comparator. */
    public List<D> sort(@Nonnull final List<D> list) {
        Collections.sort(list, this);
        return list;
    }

    /** Sort a list by this Comparator. */
    public D[] sort(@Nonnull final D[] array) {
        Arrays.sort(array, this);
        return array;
    }

    /** A String reprezentation. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        for (Key key : keys) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(key.getName());
            sb.append("[");
            sb.append(key.isAscending() ? "ASC" : "DESC");
            sb.append("]");
        }
        return sb.toString();
    }

    /** An equals test */
    public final boolean equals(@Nullable final D u1, @Nullable final D u2) {
        return compare(u1, u2) == 0;
    }

    // ------------ STATIC ------------

    /** Creates a new instance of UjoComparator. The String are compared as Collator.IDENTICAL by English locale by default.
     * Sample:
     * <pre class="pre">
     * List&lt;Person&gt; result = UjoComparator.&lt;Person&gt;of(Person.NAME).sort(persons);
     * </pre>
     * @param keys sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method Key#isAscending().
     * @see Key#isAscending()
     * @see Key#descending()
     */
    public static <U> UjoComparator<U> of(@Nonnull final Key<U,?> ... keys) {
        return new UjoComparator<>(keys);
    }

    /** @see #of(org.ujorm.KeyU[])  */
    public static <U> UjoComparator<U> of(@Nonnull final Key<U,?> p1) {
        return new UjoComparator<>(p1);
    }

    /** @see #of(org.ujorm.KeyU[])  */
    public static <U> UjoComparator<U> of
        ( @Nonnull final Key<U,?> p1
        , @Nonnull final Key<U,?> p2) {
        return new UjoComparator<>(p1, p2);
    }

    /** @see #of(org.ujorm.KeyU[])  */
    public static <U> UjoComparator<? extends U> of
        ( @Nonnull final Key<U,?> p1
        , @Nonnull final Key<U,?> p2
        , @Nonnull final Key<U,?> p3) {
        return new UjoComparator<>(p1, p2, p3);
    }

    /** Creates a new instance of UjoComparator
     * @param locale Locale for a String comparator
     * @param collatorStrength Cllator Strength for String comparations
     * @param keys sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method Key#isAscending().
     * @see Key#isAscending()
     * @see Key#descending()
     */
    public static <D> UjoComparator<D> of
        ( @Nonnull final Locale locale
        , final int collatorStrength
        , @Nonnull final  Key<D,?> ... keys) {
        return new UjoComparator<>(keys);
    }


    /** Creates a new instance of UjoComparator. The String are compared as Collator.IDENTICAL by English locale by default.
     * Sample:
     * <pre class="pre">
     * List&lt;Person&gt; result = UjoComparator.&lt;Person&gt;newInstance(Person.NAME).sort(persons);
     * </pre>
     * @param keys sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method Key#isAscending().
     * @see Key#isAscending()
     * @see Key#descending()
     * @deprecated Use the {@code of(..)} method instead of
     */
    public static <D> UjoComparator<D> newInstance(Key<D,?> ... keys) {
        return new UjoComparator<>(keys);
    }

    /** @see #newInstance(org.ujorm.Key<D,?>[])
     * @deprecated Use the {@code of(..)} method instead of
     */
    public static <D> UjoComparator<D> newInstance(Key<D,?> p1) {
        return new UjoComparator<>(p1);
    }

    /** @see #newInstance(org.ujorm.Key<D,?>[])
     * @deprecated Use the {@code of(..)} method instead of
     */
    public static <D> UjoComparator<D> newInstance(Key<D,?> p1, Key<D,?> p2) {
        return new UjoComparator<>(p1, p2);
    }

    /** @see #newInstance(org.ujorm.Key<D,?>[])
     * @deprecated Use the {@code of(..)} method instead of
     */
    public static <D> UjoComparator<D> newInstance(Key<D,?> p1, Key<D,?> p2, Key<D,?> p3) {
        return new UjoComparator<>(p1, p2, p3);
    }

    /** Creates a new instance of UjoComparator
     * @param locale Locale for a String coparation
     * @param collatorStrength Cllator Strength for String comparations
     * @param keys sorting criteria are ordered by importance to down.
     *        A direction of the sorting is used by a method Key#isAscending().
     * @see Key#isAscending()
     * @see Key#descending()
     * @deprecated Use the {@code of(..)} method instead of
     */
    public static <D> UjoComparator<D> newInstance(Locale locale, int collatorStrength, final Key<D,?> ... keys) {
        return new UjoComparator<>(keys);
    }

}
