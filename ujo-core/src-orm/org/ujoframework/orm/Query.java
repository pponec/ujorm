/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.orm;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaRelation2Many;
import org.ujoframework.orm.metaModel.MetaTable;
import org.ujoframework.criterion.Criterion;

/**
 * ORM query.
 * @author Pavel Ponec
 * @composed 1 - 1 Session
 * @composed 1 - 1 CriterionDecoder
 */
public class Query<UJO extends OrmUjo> {

    final private MetaTable table;
    final private List<MetaColumn> columns;
    final private Criterion<UJO> criterion;
    final private CriterionDecoder decoder;
    final private Session session;

    /** A list of properties to sorting */
    private List<UjoProperty> order;
    /** There is required to know a count of selected items before reading a resultset */
    private boolean countRequest = false;
    /** Result is a readOnly, default value is false */
    private boolean readOnly = false;
    /** The max row for the resulset, default value 0 means an unlimited value */
    private int maxRow = 0;
    /** Pessimistic lock request */
    private boolean lockRequest;

    /**
     * Create new ORM query.
     * @param tableClass Table can be null if the criterion parameter is not null and contains a table Property.
     * @param criterion If criterion is null, then a TRUE constant criterion is used.
     * @param session Session
     */
    public Query(Class<UJO> tableClass, Criterion<UJO> criterion, Session session) {
        this( session.getHandler().findTableModel(tableClass)
            , criterion
            , session
            );
    }

    /**
     * Create new ORM query.
     * @param table Table model
     * @param criterion If criterion is null, then a TRUE constant criterion is used.
     * @param session Session
     */
    public Query(MetaTable table, Criterion<UJO> criterion, Session session) {
        this.table = table;
        this.columns = MetaTable.COLUMNS.getList(table);
        this.criterion = criterion;
        this.session = session;
        this.decoder = new CriterionDecoder(criterion, table);

        setOrder(); // set an undefined ordering
    }

    /** Returns a count of the items */
    public long getCount() {
        final long result = session.getRowCount(this);
        return result;
    }

    public <ITEM> void setParameter(UjoProperty<UJO,ITEM> property, ITEM value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Result is a readOnly, default value is false */
    public boolean isReadOnly() {
        return readOnly;
    }

    /** Result is a readOnly, default value is false */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** There is required to know a count of selected items before reading a resultset */
    public boolean isCountRequest() {
        return countRequest;
    }

    /** There is required to know a count of selected items before reading a resultset */
    public void setCountRequest(boolean countRequest) {
        this.countRequest = countRequest;
    }

    /** Criterion */
    public Criterion<UJO> getCriterion() {
        return criterion;
    }

    /** Retuns a Criterion Decoder */
    final public CriterionDecoder getDecoder() {
        return decoder;
    }

    /** Session */
    public Session getSession() {
        return session;
    }

    /** Table Type */
    public MetaTable getTableModel() {
        return table;
    }

    /** Get Column List */
    public List<MetaColumn> getColumns() {
        return columns;
    }

    /** Get Column List */
    public MetaColumn getColumn(int index) {
        return columns.get(index);
    }

    /** Create a new iterator by the query. */
    public UjoIterator<UJO> iterate() {
        final UjoIterator<UJO> result = UjoIterator.getInstance(this);
        return result;
    }

    /** Get the order item list. The method returns a not null result allways. */
    final public List<UjoProperty> getOrder() {
        return order;
    }

    /** Returns table model */
    public MetaTable getTable() {
        return table;
    }

    /** Set the order item list to an SQL ORDER BY phrase. */
    public Query<UJO> setOrder(UjoProperty... order) {
        this.order = new ArrayList<UjoProperty>(Math.max(order.length, 4));
        for (final UjoProperty p : order) {
            this.order.add(p);
        }
        return this;
    }

    /** Add an item to the end of order list. */
    public Query<UJO> addOrder(UjoProperty property) {
        order.add(property);
        return this;
    }

    /** Returns an order column. A method for an internal use only. */
    public MetaColumn readOrderColumn(int i) {
        UjoProperty property = order.get(i);
        MetaRelation2Many result = session.getHandler().findColumnModel(property);
        return (MetaColumn) result;
    }

    /** The max row for the resulset, default value 0 means an unlimited value */
    public int getMaxRow() {
        return maxRow;
    }

    /** The max row for the resulset, default value 0 means an unlimited value */
    public void setMaxRow(int maxRow) {
        this.maxRow = maxRow;
    }

    /** Create a PreparedStatement include assigned parameter values */
    public PreparedStatement getStatement() {
        return session.getStatement(this).getPreparedStatement();
    }

    /** Pessimistic lock request */
    public boolean isLockRequest() {
        return lockRequest;
    }

    /** Pessimistic lock request */
    public void setLockRequest(boolean lockRequest) {
        this.lockRequest = lockRequest;
    }



}
