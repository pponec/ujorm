/*
 *  Copyright 2012-2022 Pavel Ponec
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

package org.ujorm.orm.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.TableWrapper;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.tools.Assert;

/**
 * Wrapper for a MetaColumn
 * @author Pavel Ponec
 */
public class ColumnWrapperImpl implements ColumnWrapper {

    /** The base column */
    private final MetaColumn column;
    /** SQL table alias */
    private final String tableAlias;
    /** The base key */
    private final Key key;

    public ColumnWrapperImpl(@NotNull final MetaColumn column, @Nullable final String tableAlias) {
        this(column, tableAlias, column.getKey());
    }

    public ColumnWrapperImpl(@NotNull final MetaColumn column, @Nullable final Key key) {
        this(column, key.isComposite() ? getAlias((CompositeKey)key) : null, key);
    }

    /**
     * Basic constructor
     * @param column Required column
     * @param tableAlias Optional table
     * @param key Optional Key
     */
    public ColumnWrapperImpl(@NotNull final MetaColumn column, @Nullable final String tableAlias, @Nullable final Key key) {
        this.column = Assert.notNull(column, "column");
        this.tableAlias = tableAlias != null ? tableAlias : column.getTableAlias();
        this.key = key != null ? key : column.getKey();
    }

    /** Returns an alias of the key or the {@code nul} value */
    private static String getAlias(@Nullable final CompositeKey key) {
        final int count = key != null ? key.getKeyCount() : 0;
        return count > 1 ? key.getAlias(count - 2) : null;
    }

    /** Returns an original column model */
    @Override
    public MetaColumn getModel() {
        return column;
    }

    /** Returns an original colum name */
    @Override
    public String getName() {
        return column.getName();
    }

    /** Returns always the NonNull alias of the related database table */
    @Override
    //@javax.annotation.Nonnull
    public String getTableAlias() {
        return tableAlias;
    }

    /** Build new table wrapper */
    @Override
    public TableWrapper buildTableWrapper() {
        return column.getTable().addAlias(tableAlias);
    }

    /** Returns an original Key */
    @Override
    public Key getKey() {
        return key;
    }

    /** Is it a composite Key? */
    @Override
    public boolean isCompositeKey() {
        return key.isComposite();
    }

    /** Two models are the same if its key names have
     * the same for the same domain type
     * with the same table alias. */
    @Override
    public boolean equals(@Nullable final Object relation) {
        if (relation instanceof ColumnWrapper) {
            final ColumnWrapper relColumn = (ColumnWrapper) relation;
            final Key argKey = relColumn.getKey();
            final Key localKey = getKey();
            final boolean result = localKey.getName().equals(argKey.getName())
                && localKey.getDomainType() == argKey.getDomainType()
                && getTableAlias().equals(relColumn.getTableAlias());
            return result;
        } else {
            return false;
        }
    }

    /** The hashCode form the Key name */
    @Override
    public int hashCode() {
        return getKey().getName().hashCode();
    }

    /** Returns the Key */
    @Override
    public String toString() {
        return key.getFullName();
    }

}
