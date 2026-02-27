/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.mapper.model;

import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;

import java.sql.JDBCType;

public record ColumnModel<D,V>(
        Key<D,V> key,
        JDBCType jdbcType,
        /** Is foreign key to a relation */
        @Nullable
        Key<V,?> foreignKey
) {

    /** Column Name */
    public String name() {
        return key.columnName();
    }

    /** Java Property Name */
    public String property() {
        return key.name();
    }

    /** Column index */
    public int index() {
        return key.index();
    }

    /** Column value */
    @SuppressWarnings("unchecked")
    public <T> T valueOf(D domain) {
        return (T) key.getValue(domain);
    }

    /** Is it a Primary Key? */
    public boolean pk() {
        return key.primaryKey();
    }

    /** Return a domain Key for a common value type. */
    @SuppressWarnings("unchecked")
    public Key<D,Object> keyObject() {
        return (Key<D,Object>) key;
    }

    public boolean relation() {
        return foreignKey != null;
    }

    /** Returns a full name */
    @Override
    public String toString() {
        return key.fullName();
    }

}
