/*
 *  Copyright 2012 Pavel Ponec
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

import org.jetbrains.annotations.NotNull;
import org.ujorm.Key;
import org.ujorm.orm.impl.ColumnWrapperImpl;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * Wrapper for a MetaColumn
 * @author Pavel Ponec
 */
public interface ColumnWrapper {

    /** Returns an original meta-table model */
    MetaColumn getModel();

    /** Returns an original column name */
    String getName();

    /** Returns always the NonNull alias of the related database table */
    String getTableAlias();

    /** Build new table wrapper including an table alias */
    TableWrapper buildTableWrapper();

    /** Returns an original key */
    Key getKey();

    /** Returns if key is type of {@link org.ujorm.CompositeKey} */
    boolean isCompositeKey();

    /** Method returns the {@code true} value if two attributes Keys are the same */
    @Override
    boolean equals(Object column);

    // --- STATIC METHOD ----

    /** Create new instance for a required name */
    @NotNull
    static ColumnWrapper forName(@NotNull final MetaColumn column, @NotNull final String name) {
        return new ColumnWrapperImpl(column, column.getColumnAlias()) {
            @Override public String getName() {
                return name;
            }
        };
    }

    /** Create new instance for a required name */
    @NotNull
    static ColumnWrapper forAlias(@NotNull final MetaColumn column, @NotNull final String alias) {
        return new ColumnWrapperImpl(column, alias);
    }

}
