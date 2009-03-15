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

import java.util.List;
import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmTable;
import org.ujoframework.tools.criteria.Expression;

/**
 * ORM query.
 * @author Ponec
 */
public class Query<UJO extends TableUjo> {

    final private OrmTable table;
    final private List<OrmColumn> columns;
    final private Expression<UJO> expression;
    final private Session session;

    /** There is required to know a count of selected items before reading a resultset */
    private boolean countRequest = false;
    /** Result is a readOnly, default value is false */
    private boolean readOnly = false;

    public Query(Class<UJO> tableType, Expression<UJO> expression, Session session) {
        this.table = OrmHandler.getInstance().findTableModel(tableType);
        this.columns = OrmTable.COLUMNS.getList(table);
        this.expression = expression;
        this.session = session;
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

    /** Expression */
    public Expression<UJO> getExpression() {
        return expression;
    }

    /** Session */
    public Session getSession() {
        return session;
    }

    /** Table Type */
    public OrmTable getTableModel() {
        return table;
    }

    /** Get Column List */
    public List<OrmColumn> getColumns() {
        return columns;
    }

    /** Get Column List */
    public OrmColumn getColumn(int index) {
        return columns.get(index);
    }

}
