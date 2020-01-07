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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.orm.metaModel.*;
import org.ujorm.tools.Check;

/** Database meta index model builder with a user ordered columns support.
 * Two columns of a database composite index can be ordered according a number
 * at the end of a index name separated by the character {@code '#'}.
 * Two index columns with the same order value are sorted according
 * to the natural order of the Keys in the {@link Ujo} class.
 * The default order value (with no number in the index name) is <strong>10</strong>.
 * <br>
 * See the next example to create a composite index with two columns in a reverted order:
 * <pre class="pre">
 *   private static final String IDX_STATE_COUNT = "idx_state_count";
 *
 *   &#64;Column(index = IDX_STATE_COUNT + <strong>"#30"</strong>)
 *   public static final Key&lt;XOrder, State&gt; STATE = newKey();
 *
 *   &#64;Column(index = IDX_STATE_COUNT + <strong>"#20"</strong>)
 *   public static final Key&lt;XOrder, Integer&gt; COUNT = newKey();
 * </pre>
 *
 * The builder class can be changed by the parameter {@link MetaParams#INDEX_MODEL_BUILDER}
 * in time of building a meta-model according the next example:
 *
 * <pre class="pre">
 *   MetaParams params = new MetaParams();
 *   params.set(MetaParams.INDEX_MODEL_BUILDER, IndexModelOrderedBuilder.class);
 *   ormHandler.config(params);
 * </pre>
 *
 * @see MetaParams#INDEX_MODEL_BUILDER
 */
public class IndexModelOrderedBuilder extends IndexModelBuilder {

    /** Order separator in the column name */
    @PackagePrivate static final char ORDER_SEPARATOR = '#';
    /** The default index column value is 10 */
    protected static final Integer DEFAULT_ORDER = 10;

    /** Index columns */
    private final Map<String, List<OrderedColumn>> idxMap = new HashMap<>();

    /** Returns a separator of the column order from an index name.
     * @return The default value is {@code '#'}
     * @see #DEFAULT_ORDER
     */
    protected char getOrderSerparator() {
        return ORDER_SEPARATOR;
    }

    /** Add the column model to the column model from the IndexMap
     * according the column name (case insensitive)
     * @param indexName Case sensitive column name
     * @param column Column model
     * @param unique Unique column request */
    @Override
    protected void addIndex
        ( String indexName
        , final MetaColumn column
        , final boolean unique) {

        if (Check.isEmpty(indexName)) {
            return;
        }
        if (MetaColumn.AUTO_INDEX_NAME.equals(indexName)) {
            indexName = unique
                    ? nameProvider.getUniqueConstraintName(column)
                    : nameProvider.getIndexName(column);
        }
        final int iSep = indexName.lastIndexOf(getOrderSerparator());
        final String indexNameShort = iSep > 0 ? indexName.substring(0, iSep) : indexName;
        final String indexKey = indexNameShort.toUpperCase();
        final Integer columnOrder = iSep > 0 ? Integer.parseInt(indexName.substring(1 + iSep)) : DEFAULT_ORDER;
        //
        List<OrderedColumn> columns = idxMap.get(indexKey);
        if (columns == null) {
            columns = new ArrayList<>();
            idxMap.put(indexKey, columns);
        }
        final MetaColumn lastCol = columns.isEmpty()
                ? null
                : columns.get(columns.size() - 1).column;
        if (column != lastCol) {
            columns.add(new OrderedColumn
                 ( indexNameShort
                 , column
                 , columnOrder
                 , unique));
        }
    }

    /** Returns all indexes of the current table
     * @return Collection of the Index model */
    @Override
    public Collection<MetaIndex> getIndexModels() {
        super.addColumnsToIndex();

        final List<MetaIndex> result = new ArrayList<>();
        for (List<OrderedColumn> columns : idxMap.values()) {
            addToResult(columns, result);
        }
        return result;
    }

    /** Add the column to the result index list */
    protected void addToResult(List<OrderedColumn> columns, final List<MetaIndex> result) {
        Collections.sort(columns);
        MetaIndex mIndex = new MetaIndex(columns.get(0).indexName, metaTable);
        result.add(mIndex);
        for (OrderedColumn column : columns) {
            MetaIndex.COLUMNS.addItem(mIndex, column.column);
            if (!column.unique) {
               MetaIndex.UNIQUE.setValue(mIndex, column.unique);
            }
        }
    }

    /** Container for the Index model with an order request */
    protected static class OrderedColumn implements Comparable<OrderedColumn>{
        private final String indexName;
        private final MetaColumn column;
        private final Integer order;
        private final Boolean unique;

        public OrderedColumn(String indexName, MetaColumn column, Integer order, Boolean unique) {
            this.indexName = indexName;
            this.column = column;
            this.order = order;
            this.unique = unique;
        }

        @Override
        public int compareTo(final OrderedColumn o) {
            return order.compareTo(o.order);
        }
    }
}
