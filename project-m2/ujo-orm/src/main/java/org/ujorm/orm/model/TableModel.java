/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.orm.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.generator.TableIdentifier;

import java.util.*;

/** Temporary Model */
public record TableModel<D>(
        /** Gets the domain handler. */
        DomainHandler<D> handler,

        /** Gets the primary key. */
        ColumnModel<D, ?> pk,

        /** Gets the columns ordered by the {@link Key#index()} . */
        List<ColumnModel<D, ?>> columns,

        /** Gets the table name. */
        TableIdentifier tableName,

        /** Gets the inserted columns without PK. */
        @NotNull
        List<ColumnModel<D, ?>> updatableColumns,

        /** Gets the selected database parameters extracted from the JDBC model. */
        Jdbc jdbc
) {

    /** Find a column model for the property name */
    @NotNull
    public ColumnModel<D, ?> getColumn(@NotNull CharSequence property) {
        var index = (property instanceof Key key)
                ? key.index()
                : handler.getKey(property.toString()).index();
        return columns.get(index);
    }

    /** Find column model by the key */
    @NotNull
    @SuppressWarnings("unchecked")
    public <V> ColumnModel<D, V> getColumnOfKey(@NotNull Key<D, V> key) {
        return (ColumnModel<D, V>) columns.get(key.index());
    }

    /** Find a column model for the property name. If the model is empty, return all updatable columns. */
    @NotNull
    public List<ColumnModel<D, ?>> getColumns(@NotNull CharSequence... properties) {
        var result = properties.length > 0
                ? new ArrayList<ColumnModel<D, ?>>(properties.length)
                : updatableColumns();
        for (var prop : properties) {
            result.add(getColumn(prop));
        }
        return result;
    }

    /** Find a column model by the index */
    @NotNull
    public ColumnModel<D, ?> getColumn(@NotNull int index) {
        return columns.get(index);
    }

    /** Exclude PK according to the PK value. */
    @NotNull
    public List<ColumnModel<D, ?>> createInsertedColumns(@Nullable Object pkValue) {
        return pkValue == null
                ? updatableColumns()
                : columns();
    }

    /** Full database name */
    public String database() {
        return handler.getDatabaseTable();
    }

    /** Original key of Ujo API */
    public Key<D, ?> pkRaw() {
        return pk.key();
    }

    /** Count of the properties */
    public int count() {
        return columns.size();
    }
}