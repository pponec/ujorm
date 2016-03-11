/*
 *  Copyright 2014-2016 Pavel Ponec
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
package org.ujorm.orm;

import java.util.ArrayList;
import java.util.Collection;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.orm.impl.ColumnWrapperImpl;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * Relation from the one table to another including alias
 * @author ponec
 */
final public class AliasKey {

    /** The default relation alias contains an unsupported character(s) (e.g. space) */
    @PackagePrivate static final String DEFAULT_RELATION_ALIAS = new String("[RELATION ALIAS]");

    /** The Key is not null always */
    @PackagePrivate final Key<?,?> key;
    /** Nullable alias for the previous table */
    @PackagePrivate final String aliasFrom;
    /** Nullable alias for the next table */
    @PackagePrivate final String aliasTo;
    /** Hash */
    @PackagePrivate int hashCode;

    /**
     * Constructor
     * @param key Direct Key
     */
    public AliasKey(Key<?, ?> key) {
        this(key, CompositeKey.DEFAULT_ALIAS, CompositeKey.DEFAULT_ALIAS);
    }

    /**
     * Constructor
     * @param key direct Key
     * @param aliasFrom Alias for the previous table
     * @param aliasTo Alias for the next table
     */
    public AliasKey(Key<?, ?> key, String aliasFrom, String aliasTo) {
        this.key = key;
        this.aliasFrom = aliasFrom;
        this.aliasTo = aliasTo;
    }

    /** The direct Key */
    public Key<?, ?> getKey() {
        return key;
    }

    /** Alias previous table */
    public String getAliasFrom() {
        return aliasFrom;
    }

    /** Alias the next table */
    public String getAliasTo() {
        return aliasTo;
    }

    /** Create a new ColumnWrapper for a required alias */
    public ColumnWrapper getColumn(OrmHandler handler) {
        final MetaColumn column = handler.findColumnModel(key, true);
        return aliasFrom != null
             ? new ColumnWrapperImpl(column, aliasFrom)
             : column;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int hash = 5;
            hash = 79 * hash +  this.key.hashCode();
            hash = 79 * hash + (this.aliasFrom != null ? this.aliasFrom.hashCode() : 0);
            hash = 79 * hash + (this.aliasTo != null ? this.aliasTo.hashCode() : 0);
            hashCode = hash;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        final AliasKey other = (AliasKey) obj;
        if (this.key != other.key) {
            return false;
        }
        if (this.aliasFrom != other.aliasFrom
        && (this.aliasFrom == null || !this.aliasFrom.equals(other.aliasFrom))
        ){
            return false;
        }
        if (this.aliasTo != other.aliasTo
        && (this.aliasTo == null || !this.aliasTo.equals(other.aliasTo))
        ){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final String result = String.format("%s (%s, %s)"
                , key.getFullName()
                , aliasFrom
                , aliasTo);
        return result;
    }

    // ---------------------- STATIC METHODS ----------------------

    /** Add relations only
     * @param key The Ujorm composite Key
     * @param out Result
     */
    public static void addRelations(final Key<?,?> key, final Collection<AliasKey> out) {
        if (key.isComposite()) {
            final CompositeKey<?,?> cKey = (CompositeKey<?,?>) key;
            addConditions(cKey, 0, cKey.getCompositeCount() - 1, out);
        }
    }

    /** Add full range of composite key
     * @param key The Ujorm composite Key
     * @param out The result collection
     */
    public static void addKeys(final Key<?,?> key, final Collection<AliasKey> out) {
        if (key.isComposite()) {
            final CompositeKey<?,?> cKey = (CompositeKey<?,?>) key;
            addConditions(cKey, 0, cKey.getCompositeCount(), out);
        } else {
            out.add(new AliasKey(key));
        }
    }

    /** Add the last condition
     * @param key The Ujorm composite Key
     * @param out The result collection
     */
    public static void addLastKey(final Key<?,?> key, final Collection<AliasKey> out) {
        if (key.isComposite()) {
            final CompositeKey<?,?> cKey = (CompositeKey<?,?>) key;
            final int count = cKey.getCompositeCount();
            addConditions(cKey, count - 1, count, out);
        } else {
            out.add(new AliasKey(key));

        }
    }

    /** Add the last condition
     * @param compositeKey The Ujorm composite Key
     */
    public static AliasKey getLastKey(final Key<?,?> compositeKey) {
        final ArrayList<AliasKey> result = new ArrayList<AliasKey>(1);
        addLastKey(compositeKey, result);
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Add conditions for an internal use
     * @param cKey Composite Key.
     * @param beg Key start position
     * @param end Key max position
     * @param out The result collection
     */
    private static void addConditions(final CompositeKey<?,?> cKey, final int beg, final int end, final Collection<AliasKey> out) {
        String aliasFrom = beg > 0
                ? cKey.getAlias(beg - 1)
                : CompositeKey.DEFAULT_ALIAS;
        for (int i = beg; i < end; i++) {
            final String aliasTo = cKey.getAlias(i);
            final Key<?, ?> directKey = cKey.getDirectKey(i);
            out.add(new AliasKey(directKey, aliasFrom, aliasTo));
            aliasFrom = aliasTo;
        }
    }
}
