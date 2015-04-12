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

import java.util.ArrayList;
import org.ujorm.orm.metaModel.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.core.annot.PackagePrivate;

/** Database meta model index builder with a support of ordered columns */
public class IndexModelOrderedBuilder extends IndexModelBuilder {

    /** Order separator in the column name */
    @PackagePrivate static final char ORDER_SEPARATOR = '#';
    /** Order separator in the column name */
    @PackagePrivate static final Integer DEFAULT_ORDER = 1;

    private final Map<String, List<OrderedColumn>> indexes = new HashMap<String, List<OrderedColumn>>();
    private final Map<String, List<OrderedColumn>> uniqueIndexes = new HashMap<String, List<OrderedColumn>>();

    /** Add the column model to the column model from the IndexMap according the column name (case insensitive)
     * @param indexName Case sensitive column name
     * @param column Column model
     * @param unique Unique column request */
    @Override
    public void addIndex
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
        final String indexKey = indexName.toUpperCase();
        final Map<String, List<OrderedColumn>> idxMap = unique ? uniqueIndexes : indexes;
        List<OrderedColumn> columns = idxMap.get(indexKey);
        if (columns == null) {
            columns = new ArrayList<OrderedColumn>();
            idxMap.put(indexKey, columns);
        }
        MetaColumn lastCol = columns.isEmpty() ? null
                : columns.get(columns.size() - 1).column;
        if (column != lastCol) {
            columns.add(new OrderedColumn(indexName, column, DEFAULT_ORDER));
        }
    }

    /** Returns all indexes of the current table
     * @return Collection of the Index model */
    @Override
    public Collection<MetaIndex> getIndexModels() {
        final List<MetaIndex> result = new ArrayList<MetaIndex>();
        for (List<OrderedColumn> columns : indexes.values()) {
            addToResult(columns, false, result);
        }
        for (List<OrderedColumn> columns : uniqueIndexes.values()) {
            addToResult(columns, true, result);
        }
        return result;
    }

    /** Add the column to the result index list */
    protected void addToResult(List<OrderedColumn> columns, boolean unique, final List<MetaIndex> result) {
        Collections.sort(columns);
        MetaIndex mIndex = new MetaIndex(columns.get(0).indexName, metaTable);
        result.add(mIndex);
        for (OrderedColumn column : columns) {
            MetaIndex.COLUMNS.addItem(mIndex, column.column);
            if (!unique) {
               MetaIndex.UNIQUE.setValue(mIndex, unique);
            }
        }
    }

    /** Container for the Index model with an order request */
    protected static class OrderedColumn implements Comparable<OrderedColumn>{
        private final String indexName;
        private final MetaColumn column;
        private final Integer order;

        public OrderedColumn(String indexName, MetaColumn column, Integer order) {
            this.indexName = indexName;
            this.column = column;
            this.order = order;
        }

        @Override
        public int compareTo(final OrderedColumn o) {
            return order.compareTo(o.order);
        }
    }
}
