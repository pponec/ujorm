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
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.orm.SqlNameProvider;
import static org.ujorm.orm.metaModel.MetaTable.COLUMNS;
import static org.ujorm.orm.metaModel.MetaTable.DATABASE;

/** The database index model builder. The builder sorts columns of the composite index
 * according the natural order of Keys in the {@link Ujo} class
 * <br>
 * See the next example to create a composite index with two columns with a natural order:
 * <pre class="pre">
 *   private static final String IDX_STATE_COUNT = "idx_state_count";
 *   private static final String IDX_COUNT = "idx_count";
 *
 *   &#64;Column(index = IDX_STATE_COUNT)
 *   public static final Key&lt;XOrder, State&gt; STATE = newKey();
 *
 *   &#64;Column(index = {IDX_STATE_COUNT, IDX_COUNT})
 *   public static final Key&lt;XOrder, Integer&gt; COUNT = newKey();
 * </pre>
 * The builder class can be changed by the parameter {@link MetaParams#INDEX_MODEL_BUILDER}.
 * @see IndexModelOrderedBuilder
 * @see MetaParams#INDEX_MODEL_BUILDER
 */
public class IndexModelBuilder  {

    /** MetaTable */
    protected MetaTable metaTable;
    /** SQL name provider */
    protected SqlNameProvider nameProvider;
    /** Map a MetaIndex for an index name (case insensitive) */
    protected Map<String, MetaIndex> mapIndex;

    /** Initialize the object */
    public void init(MetaTable metaTable) throws IllegalUjormException {
        if (this.metaTable != null) {
            throw new IllegalUjormException("The class is initialized by " + metaTable);
        }
        this.metaTable = metaTable;
        this.nameProvider = metaTable.getDatabase().getDialect().getNameProvider();
        this.mapIndex = new HashMap<>();
    }

    /** Add the column model to the index model from the IndexMap according the index name (case insensitive)
     * @param indexName Case sensitive index name
     * @param column Column model
     * @param unique Unique index request */
    protected void addIndex
        ( String indexName
        , final MetaColumn column
        , final boolean unique) {
        if (indexName == null || indexName.isEmpty()) {
            return;
        }
        if (MetaColumn.AUTO_INDEX_NAME.equals(indexName)) {
            indexName = unique
                    ? nameProvider.getUniqueConstraintName(column)
                    : nameProvider.getIndexName(column);
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
        addColumnsToIndex();
        return mapIndex.values();
    }

    /** Add columns to index */
    protected void addColumnsToIndex() {
        final boolean extendedStrategy = isExtendedIndexStrategy();
        for (MetaColumn column : COLUMNS.getList(metaTable)) {
            for (String idx : MetaColumn.UNIQUE_INDEX.of(column)) {
                addIndex(idx, column, true);
            }
            for (String idx : MetaColumn.INDEX.of(column)) {
                addIndex(idx, column, false);
            }
            if (extendedStrategy && column.isForeignKey()) {
                addIndex(MetaColumn.AUTO_INDEX_NAME, column, false);
            }
        }
    }

    /** Is an extended index naming strategy
     * @see MoreParams#EXTENTED_INDEX_NAME_STRATEGY
     */
    protected Boolean isExtendedIndexStrategy() {
        return MetaParams.EXTENTED_INDEX_NAME_STRATEGY.of(DATABASE.of(metaTable).getOrmHandler().getParameters());
    }

}
