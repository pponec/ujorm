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
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.wicket.component.gridView.columns.*;

/**
 * SortableDataProvider extended form the Ujorm
 * @author Pavel Ponec
 */
public class UjoDataProvider<T extends OrmUjo> extends SortableDataProvider<T, String> {
    private static final long serialVersionUID = 1L;
    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(UjoDataProvider.class);
    /** Data size */
    protected Long size;

    /** Data criterion */
    protected Criterion<T> criterion;
    /** Domain model */
    protected KeyRing<T> model;
    /** Visible table columns */
    private List<IColumn<T, Key<T,?>>> columns = new ArrayList<IColumn<T, Key<T,?>>>();
    /** OrmSession */
    transient private Session ormSession;

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

    /** Build a JDBC ResultSet allways.
     * Overwrite the method for an optimization.<br>
     */
    @Override
    public Iterator<T> iterator(long first, long count) {
        Args.isTrue(count <= Integer.MAX_VALUE
                , "The argument 'count' have got limit %s but the current value is %s"
                , Integer.MAX_VALUE
                , count);
        Query<T> query = createQuery(criterion)
                .setLimit((int) count, first)
                .addOrderBy(getSortKey());
        fetchDatabaseColumns(query);
        return query.iterator();
    }

    /** Method calculate the size using special SQL requst.
     * Overwrite the method for an optimization.<br>
     * Original documentation: {@inheritDoc}
     */
    @Override
    public long size() {
       if (size == null) {
           size = createQuery(criterion).getCount();
       }
       return size;
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
        size = null;
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

    /** Add table column according to column type */
    public <V> boolean addColumn(Key<T,V> column) {
        if (column.isTypeOf(Boolean.class)) {
            return addColumn(new KeyColumnBoolean<T>((Key)column));
        } 
        if (column.isTypeOf(Number.class)) {
            return addColumn(KeyColumn.of(column, "number"));
        }
        else {
            final IColumn<T, Key<T,V>> c = KeyColumn.of(column);
            return addColumn(c);
        }
    }

    /** Transient table columns */
    public  List<IColumn<T, Key<T,?>>> getColumns() {
        return columns;
    }

    /** Create AJAX-based DataTable */
    public DataTable createDataTable( final String id, final int rowsPerPage) {
        return new UjoDataTable(id, getColumns(), this, rowsPerPage);
    }

    /**
     * The method reduces a lazy database requests from relational table columns.
     * The current implementation assigns all direct keys/columns from domain entity and
     * all required keys/columns from the IColumn object.
     *
     * <br/>Note: You can overwrite the method for a different behaviour.
     */
    protected void fetchDatabaseColumns(Query<T> query) {
        if (columns.isEmpty()) {
            return ; // Save the default state;
        }

        if (query.getTableModel().isView()) {
            return ; // View is not supported;
        }

        final OrmHandler handler = query.getSession().getHandler();
        final List<Key> keys = new ArrayList(query.getColumns().size() + 3);

        for (ColumnWrapper c : query.getColumns()) {
            keys.add(c.getKey());
        }

        for (IColumn<T, Key<T, ?>> iColumn : columns) {
            if (iColumn instanceof KeyColumn) {
                Key<T,?> key = ((KeyColumn) iColumn).getKey();
                if (key.isComposite()
                && ((CompositeKey)key).getDirectKeyCount() > 1
                && handler.findColumnModel(key) != null) {
                    keys.add(key);
                }
            }
        }
        query.setColumns(true, keys.toArray(new Key[keys.size()]));
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
