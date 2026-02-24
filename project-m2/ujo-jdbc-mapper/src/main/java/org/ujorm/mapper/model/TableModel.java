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
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.generator.DatabaseModel;

import java.util.*;

/** Temporary Model */
public record TableModel<D>(
        DomainHandler<D> hander,
        ColumnModel<D,Object> pk,
        List<ColumnModel<D,Object>> columns,
        Map<String, ColumnModel<D,Object>> propertyMap,
        DatabaseModel databaseModel,
        /** Inserted columns without PK. */
        List<ColumnModel<D, Object>> insertedColumns
) {

    /** Find a column model for the property name */
    @NotNull
    public ColumnModel<D,Object> getColumn(@NotNull String property) {
        var result = propertyMap.get(property);
        if (result == null) {
            var msg = "Property not found: %s.%s".formatted(hander.getDomainClass().getSimpleName(), property);
            throw new NoSuchElementException(msg);
        }
        return result;
    }

    /** Exclude PK according to the PK value. */
    @NotNull
    public List<ColumnModel<D,Object>> createInsertedColumns(@Nullable Object pkValue) {
        return pkValue == null
                ? insertedColumns()
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
