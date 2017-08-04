/*
 *  Copyright 2017-2017 Effectiva Solutions company
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
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;

/**
 * Metamodel initialization context
 */
public class DbItems {
    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(DbItems.class);

    // Missing database entities:
    private final List<String> schemas = new ArrayList<>();
    private final List<MetaTable> tables = new ArrayList<>();
    private final List<MetaColumn> columns = new ArrayList<>();
    private final List<MetaColumn> foreignColumns = new ArrayList<>();
    private final List<MetaIndex> indexes = new ArrayList<>();

    public List<String> getSchemas() {
        return schemas;
    }

    public List<MetaTable> getTables() {
        return tables;
    }

    public List<MetaColumn> getColumns() {
        return columns;
    }

    public List<MetaColumn> getForeignColumns() {
        return foreignColumns;
    }

    public List<MetaIndex> getIndexes() {
        return indexes;
    }

}
