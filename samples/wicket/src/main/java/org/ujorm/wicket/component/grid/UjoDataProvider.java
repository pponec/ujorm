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
package org.ujorm.wicket.component.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.lang.Args;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.gui.WicketApplication;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;

/**
 * <p>This class called <strong>UjoDataProvider</strong> is an database
 * Wicket DataProvider. For a customization you can use a your own {@link IColumn} implementations
 * or you can owerwrite selected methods of this provider.
 * </p><p>
 * The implementation generates two database requests per a one rendering,
 * the first one get size and the second one get paged data. You can owerwrite the two data methods:
 * {@link #iterator(long, long) iterator()} and the {@link #size() size()}
 * for more optimization.
 * </p><p>
 * The current class uses a {@link WicketApplication} implementation, which must
 * implement the interface OrmHandlerProvider for an ORM support. See the example:
 * </p>
 * <h4>See the simple sample:</h4>
 * <pre class="pre"> {@code
 *  Criterion<Hotel> allActiveHotels = Hotel.ACTIVE.whereEq(true);
 *  UjoDataProvider<Hotel> dataProvider = UjoDataProvider.of(allActiveHotels);
 *
 *  dataProvider.addColumn(Hotel.NAME);
 *  dataProvider.addColumn(Hotel.CITY.add(City.NAME)); // An example of relations
 *  dataProvider.addColumn(Hotel.STREET);
 *  dataProvider.addColumn(Hotel.PRICE);
 *  dataProvider.addColumn(KeyColumn.of(Hotel.CURRENCY, SORTING_OFF));
 *  dataProvider.addColumn(Hotel.STARS);
 *  dataProvider.addColumn(Hotel.PHONE);
 *  dataProvider.setSort(Hotel.NAME);
 *
 *  panel.add(dataProvider.createDataTable("datatable", 10));
 * }
 * </pre>
 * @author Pavel Ponec
 */
public class UjoDataProvider<T extends OrmUjo> extends SortableDataProvider<T, Object> {
    private static final long serialVersionUID = 1L;
    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(UjoDataProvider.class);
    /** Default Datatable ID have got value {@code "datatable"}. */
    public static final String DEFAULT_DATATABLE_ID = "datatable";
    /** Data size */
    protected Long size;

    /** Data criterion */
    protected Criterion<T> criterion;
    /** Domain model */
    protected KeyRing<T> model;
    /** Visible table columns */
    private List<IColumn<T, ?>> columns = new ArrayList<IColumn<T, ?>>();
    /** Default column sorting for the method {@link #addColumn(org.ujorm.Key) }
     * where the feature is enabled by default
     */
    public boolean defaultColumnSorting = true;
    /** Fetch database columns for better SQL performance
     * where the feature is enabled by default
     */
    public boolean fetchDatabaseColumns = true;
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
        super.setSort((KeyRing)KeyRing.of(property), property.isAscending()
                ? SortOrder.ASCENDING
                : SortOrder.DESCENDING);
    }

    /** Vrací klíč pro řazení */
    public Key<T,?> getSortKey() {
        final SortParam<Object> sort = getSort();
        if (sort != null) {
            final Object property = getSort().getProperty();
            return property instanceof KeyRing
            ? ((KeyRing<T>)property).getFirstKey().descending(!sort.isAscending())
            : null ;
        } else {
            return null;
        }
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
        sortDatabaseQuery(query);
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
    public boolean addColumn(IColumn<T, ?> column) {
        return columns.add(column);
    }

    /** Add table column according to column type */
    public <V> boolean addColumn(Key<T,V> column) {
        if (column.isTypeOf(Boolean.class)) {
            return addColumn(KeyColumnBoolean.of(column, isSortingEnabled(column)));
        }
        if (column.isTypeOf(Number.class)) {
            return addColumn(KeyColumn.of(column, isSortingEnabled(column), "number"));
        }
        else {
            return addColumn(KeyColumn.of(column, isSortingEnabled(column), null));
        }
    }

    /** The sorting is enabled for a persistent Ujorm Key by default
     * @see #isDefaultColumnSorting()
     */
    protected boolean isSortingEnabled(final Key<T, ?> column) throws IllegalArgumentException {
        return defaultColumnSorting
            && getOrmSession().getHandler().findColumnModel(column, false) != null;
    }

    /** Create AJAX-based DataTable with a {@link #DEFAULT_DATATABLE_ID} */
    public <S> DataTable<T,S> createDataTable(final int rowsPerPage) {
        return createDataTable(DEFAULT_DATATABLE_ID, rowsPerPage);
    }

    /** Create AJAX-based DataTable */
    public <S> DataTable<T,S> createDataTable(final String id, final int rowsPerPage) {
        final DataTable<T,S> result = new DataTable<T,S>(id, (List)columns, this, rowsPerPage) {
            @Override protected Item<T> newRowItem
                    ( final String id
                    , final int index
                    , final IModel<T> model) {
                return new OddEvenItem<T>(id, index, model);
            }
        };

        result.addTopToolbar(new AjaxNavigationToolbar(result));
        result.addTopToolbar(new HeadersToolbar(result, this));
        result.addBottomToolbar(new NoRecordsToolbar(result));
        result.setOutputMarkupId(true);
        return result;
    }

    /**
     * The method reduces a lazy database requests from relational table columns.
     * The current implementation assigns all direct keys/columns from domain entity and
     * all required keys/columns from the IColumn object.
     *
     * <br/>Note: You can overwrite the method for a different behaviour.
     * <br/>Note: If the implementation will be empty, so all related attributes will be lazy loaded,
     * so it can be a performance problem in some cases.
     * @see #isFetchDatabaseColumns()
     */
    protected void fetchDatabaseColumns(Query<T> query) {
        if (!fetchDatabaseColumns) {
            return; // Fetching is disabled
        }
        if (columns.isEmpty()) {
            return; // Keep the default state
        }
        if (query.getTableModel().isView()) {
            return; // View is not supported for fetching
        }

        final OrmHandler handler = query.getSession().getHandler();
        final List<Key> keys = new ArrayList(query.getColumns().size() + 3);

        for (ColumnWrapper c : query.getColumns()) {
            keys.add(c.getKey());
        }

        for (IColumn<T, ?> iColumn : columns) {
            if (iColumn instanceof KeyColumn) {
                Key<T,?> key = ((KeyColumn) iColumn).getKey();
                if (key.isComposite()
                && ((CompositeKey)key).getCompositeCount() > 1
                && handler.findColumnModel(key) != null) {
                    keys.add(key);
                }
            }
        }
        query.setColumns(true, keys.toArray(new Key[keys.size()]));
    }

    /** Add sorting to a database Query,
     * an empty method causes a natural sorting from database */
    protected void sortDatabaseQuery(Query<T> query) {
        final Key sortKey = getSortKey();
        if (sortKey != null) {
            query.addOrderBy(sortKey);
        }
    }

    // ============= SETERS / GETTERS =============

    /**
     * Default column sorting for the method {@link #addColumn(org.ujorm.Key) }
     * where the feature is enabled by default
     * @return the defaultColumnSorting
     */
    public final boolean isDefaultColumnSorting() {
        return defaultColumnSorting;
    }

    /**
     * Default column sorting for the method {@link #addColumn(org.ujorm.Key) }
     * where the feature is enabled by default
     * @param defaultColumnSorting the defaultColumnSorting to set
     */
    public void setDefaultColumnSorting(boolean defaultColumnSorting) {
        this.defaultColumnSorting = defaultColumnSorting;
    }

    /**
     * Fetch database columns for better SQL performance
     * where the feature is enabled by default
     * @return the fetchDatabaseColumns
     */
    public final boolean isFetchDatabaseColumns() {
        return fetchDatabaseColumns;
    }

    /**
     * Fetch database columns for better SQL performance
     * where the feature is enabled by default
     * @param fetchDatabaseColumns the fetchDatabaseColumns to set
     */
    public void setFetchDatabaseColumns(boolean fetchDatabaseColumns) {
        this.fetchDatabaseColumns = fetchDatabaseColumns;
    }

    /** Transient table columns */
    public List<IColumn<T, ?>> getColumns() {
        return columns;
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