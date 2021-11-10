/*
 *  Copyright 2017-2017 Pavel Ponec
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
package org.ujorm.orm.metaModel;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Database items tu create
 */
@Unmodifiable
public class DbItems {

    // --- New database entities ---
    private final List<String> schemas;
    private final List<MetaTable> tables;
    private final List<MetaColumn> columns;
    private final List<MetaColumn> foreignColumns;
    private final List<MetaIndex> indexes;

    /** Constructor */
    public DbItems(int tableCount, int columnCount) {
        schemas = new ArrayList<>();
        tables = new ArrayList<>(tableCount);
        columns = new ArrayList<>(columnCount);
        foreignColumns = new ArrayList<>();
        indexes = new ArrayList<>();
    }

    /** Get list of the schema names */
    public final List<String> getSchemas() {
        return schemas;
    }

    /** Get list of the table models */
    public final List<MetaTable> getTables() {
        return tables;
    }

    /** Get list of the column models */
    public final List<MetaColumn> getColumns() {
        return columns;
    }

    /** Get list of the column models */
    public final List<MetaColumn> getForeignColumns() {
        return foreignColumns;
    }

    /** Get list of the Index models */
    public List<MetaIndex> getIndexes() {
        return indexes;
    }

}
