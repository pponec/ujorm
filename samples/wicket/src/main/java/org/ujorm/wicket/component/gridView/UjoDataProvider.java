/*
 *  Copyright 2013 Pavel Ponec
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
package org.ujorm.wicket.component.gridView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Args;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;

/**
 * SortableDataProvider extended form the Ujorm
 * @author Pavel Ponec
 */
public class UjoDataProvider<T extends OrmUjo> extends SortableDataProvider<T, String> {
    private static final long serialVersionUID = 1L;

    /** Data criterion */
    protected Criterion<T> criterion;
    /** Domain model */
    protected KeyRing<T> model;
    /** OrmSession */
    transient private Session ormSession;
    /** Transient table columns */
    transient private List<IColumn<T, Key<T,?>>> columns = new ArrayList<IColumn<T, Key<T,?>>>();

    /** Constructor
     * @param criterion Condition to a database query
     */
    public UjoDataProvider(Criterion<T> criterion) {
        this(criterion, null);
    }

    /** Constructor
     * @param criterion Condition to a database query
     * @param defaultSort Default sorting can be assigned optionally
     */
    public UjoDataProvider(Criterion<T> criterion, Key<T,?> defaultSort) {
        this.criterion = Args.notNull(criterion, "Criterion is mandatory");
        model = KeyRing.of((Class<T>)criterion.getDomain());
        if (defaultSort == null) {
            defaultSort = model.getFirstKey();
        }
        setSort(defaultSort);
    }

    /**
     * Sets the current sort state and assign the BaseClass
     *
     * @param property
     * sort property
     * @param order
     * sort order
     */
    final public void setSort(Key<T, ?> property) {
        super.setSort(property.getName(), property.isAscending() 
                ? SortOrder.ASCENDING
                : SortOrder.DESCENDING);
    }

    /** Vrací klíč pro řazení */
    public Key<T,?> getSortKey() {
        final Key<T,?> result = model.find(super.getSort().getProperty());
        return result.descending(!super.getSort().isAscending());
    }

    @Override
    public Iterator<T> iterator(long first, long count) {
        return createQuery(criterion).setLimit((int)(first + count), (int)first).addOrderBy(getSortKey()).iterator();
    }

    @Override
    public long size() {
        return createQuery(criterion).getCount();
    }

    /** Returns orm Session */
    protected Session getOrmSession() {
        if (ormSession == null) {
            WebApplication application = WebApplication.get();
            if (application instanceof OrmHandlerProvider) {
                ormSession = ((OrmHandlerProvider) application).getOrmHandler().createSession();
            } else {
                throw new IllegalStateException("The WebApplication must to implement " + OrmHandlerProvider.class);
            }
        }
        return ormSession;
    }

    /** Commit and close transaction */
    @Override
    public void detach() {
        if (ormSession != null) {
            ormSession.close();
            ormSession = null;
        }
    }

    /** Create default Query */
    protected Query<T> createQuery(Criterion<T> criterion) {
        return getOrmSession().createQuery(criterion);
    }

    /** Get a bean Model */
    public KeyRing<T> getModel() {
        return model;
    }

    /** Create a model */
    @Override
    public IModel<T> model(T object) {
        return new Model((Serializable)object);
    }

    /** Add table column */
    public <V> boolean addColumn(IColumn<T, V> column) {
        return columns.add((IColumn)column);
    }

    /** Add table column */
    public <V> boolean addColumn(Key<T,V> column) {
        final IColumn<T, Key<T,V>> c = KeyColumn.of(column);
        return addColumn(c);
    }

    /** Transient table columns */
    public  List<IColumn<T, Key<T,?>>> getColumns() {
        return columns;
    }

    /** Create AJAX-based DataTable */
    public DataTable createDataTable( final String id, final int rowsPerPage) {
        return new UjoDataTable(id, getColumns(), this, rowsPerPage);
    }

    // ============= STATIC METHOD =============

    /** Factory for the class */
    public static <T extends OrmUjo> UjoDataProvider<T> of(Criterion<T> criterion, Key<T,?> defaultSort) {
        return new UjoDataProvider<T>(criterion, defaultSort);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> UjoDataProvider<T> of(Criterion<T> criterion) {
        return new UjoDataProvider<T>(criterion, null);
    }

}
