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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.DomainHandler;
import org.ujorm.Key;

import java.util.*;

/** Temporary Model */
public record TableModel<D>(
        DomainHandler<D> hander,
        ColumnModel<D,Object> pk,
        List<ColumnModel<D,Object>> columns,
        String tableName,
        /** Inserted columns without PK. */
        @NotNull
        List<ColumnModel<D, Object>> updatableColumns,
        Jdbc jdbc
) {

    /** Find a column model for the property name */
    @NotNull
    public ColumnModel<D,Object> getColumn(@NotNull CharSequence property) {
        var index = (property instanceof Key key)
                ? key.index()
                : hander.getKey(property.toString()).index();
        return columns.get(index);
    }

    /** Find a column model for the property name. If the model is empty, return all updatable columns. */
    @NotNull
    public List<ColumnModel<D,Object>> getColumns(@NotNull CharSequence... properties) {
        var result = properties.length > 0
                ? new ArrayList<ColumnModel<D,Object>>(properties.length)
                : updatableColumns();
        for (var prop : properties) {
            result.add(getColumn(prop));
        }
        return result;
    }

    /** Find a column model by the index */
    @NotNull
    public ColumnModel<D,Object> getColumn(@NotNull int index) {
        return columns.get(index);
    }

    /** Exclude PK according to the PK value. */
    @NotNull
    public List<ColumnModel<D,Object>> createInsertedColumns(@Nullable Object pkValue) {
        return pkValue == null
                ? updatableColumns()
                : columns();
    }

    /** Full database name */
    public String database() {
        return hander.getDatabaseTable();
    }

    /** Original key of Ujo API */
    public Key<D,?> pkRaw() {
        return pk.key();
    }

    /** Count of the properties */
    public int count() {
        return columns.size();
    }
}
