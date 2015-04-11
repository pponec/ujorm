/*
 *  Copyright 2015 Pavel Ponec
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
package org.ujorm.orm.ao;

import org.ujorm.orm.metaModel.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.ujorm.orm.SqlNameProvider;

/** Database meta model index builder with a support of ordered columns */
public class ModelIndexBuilder  {

    /** MetaTable */
    protected MetaTable metaTable;
    /** SQL name provider */
    protected SqlNameProvider nameProvider;
    /** Map a MetaIndex for an index name (case insensitive) */
    protected Map<String, MetaIndex> mapIndex;

    /** Initialize the object */
    public void init(MetaTable metaTable) throws IllegalStateException {
        if (this.metaTable != null) {
            throw new IllegalStateException("The class is initialized by " + this.metaTable);
        }
        this.metaTable = metaTable;
        this.nameProvider = metaTable.getDatabase().getDialect().getNameProvider();
        this.mapIndex = new HashMap<String, MetaIndex>();
    }

    /** Add the column model to the index model from the IndexMap according the index name (case insensitive)
     * @param indexName Case sensitive index name
     * @param column Column model
     * @param unique Unique index request */
    public void addIndex
        ( String indexName
        , final MetaColumn column
        , final boolean unique) {
        if (MetaColumn.AUTO_INDEX_NAME.equals(indexName)) {
            indexName = unique
                    ? nameProvider.getUniqueConstraintName(column)
                    : nameProvider.getIndexName(column);
        }
        if (indexName == null || indexName.isEmpty()) {
            return;
        }

        final String index = indexName.toUpperCase();
        MetaIndex mi = mapIndex.get(index);
        if (mi == null) {
            mi = new MetaIndex(indexName, metaTable);
            mapIndex.put(index, mi);
        }
        if (!unique) {
            MetaIndex.UNIQUE.setValue(mi, false);
        }
        if (column != MetaIndex.COLUMNS.getLastItem(mi)) {
            MetaIndex.COLUMNS.addItem(mi, column);
        }
    }

    /** Returns all indexes of the current table
     * @return Collection of the Index model */
    public Collection<MetaIndex> getIndexModels() {
        return mapIndex.values();
    }

}
