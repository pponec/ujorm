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

/**
 * Database items tu create
 */
public class DbItems {

    /** Init size of the lists */
    private static final int INIT_SIZE = 220;

    // --- New database entities ---
    private final List<String> schemas = new ArrayList<>();
    private final List<MetaTable> tables = new ArrayList<>(INIT_SIZE);
    private final List<MetaColumn> columns = new ArrayList<>(INIT_SIZE);
    private final List<MetaColumn> foreignColumns = new ArrayList<>(INIT_SIZE);
    private final List<MetaIndex> indexes = new ArrayList<>();

    public final List<String> getSchemas() {
        return schemas;
    }

    public final List<MetaTable> getTables() {
        return tables;
    }

    public final List<MetaColumn> getColumns() {
        return columns;
    }

    public final List<MetaColumn> getForeignColumns() {
        return foreignColumns;
    }

    public List<MetaIndex> getIndexes() {
        return indexes;
    }

}
