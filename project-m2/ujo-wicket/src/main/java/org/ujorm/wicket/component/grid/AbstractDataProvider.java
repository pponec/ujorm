/*
 *  Copyright 2013-2014 Pavel Ponec
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
import org.apache.wicket.util.lang.Args;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.Query;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.toolbar.InsertToolbar;

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
public abstract class AbstractDataProvider<T extends Ujo> extends SortableDataProvider<T, Object> {
    private static final long serialVersionUID = 1L;
    /** Default Datatable ID have got value {@code "datatable"}. */
    public static final String DEFAULT_DATATABLE_ID = "datatable";
    /** Data size */
    protected Long size;

    /** Data criterion model */
    protected IModel<Criterion<T>> criterion;
    /** Domain model */
    protected KeyRing<T> model;
    /** Visible table columns */
    private List<IColumn<T, ?>> columns = new ArrayList<IColumn<T, ?>>();
    /** Default column sorting for the method {@link #addColumn(org.ujorm.Key) }
     * where the feature is enabled by default
     */
    private boolean defaultColumnSorting = true;

    /** Constructor
     * @param criterion Condition to a database query
     */
    public AbstractDataProvider(IModel<Criterion<T>> criterion) {
        this(criterion, null);
    }

    /** Constructor
     * @param criterion Model of a condition to a database query
     * @param defaultSort Default sorting can be assigned optionally
     */
    public AbstractDataProvider(IModel<Criterion<T>> criterion, Key<T,?> defaultSort) {        
        this.criterion = Args.notNull(criterion, "Criterion is mandatory");
        this.model = KeyRing.of((Class<T>)criterion.getObject().getDomain());

        if (defaultSort == null) {
            defaultSort = model.getFirstKey();
        }
        setSort(defaultSort);
    }

    /**
     * Sets the current sort state and assign the BaseClass
     *
     * @param key
     * sort key
     * @param order
     * sort order
     */
    final public void setSort(Key<T, ?> key) {
        super.setSort((KeyRing)KeyRing.of(key), key.isAscending()
                ? SortOrder.ASCENDING
                : SortOrder.DESCENDING);
    }

    /** Vrací klíč pro řazení */
    public Key<T,?> getSortKey() {
        final SortParam<Object> sort = getSort();
        if (sort != null) {
            final Object key = getSort().getProperty();
            return key instanceof KeyRing
            ? ((KeyRing<T>)key).getFirstKey().descending(!sort.isAscending())
            : null ;
        } else {
            return null;
        }
    }

    /** Build a JDBC ResultSet allways.
     * Overwrite the method for an optimization.<br>
     */

    public abstract Iterator<T> iterator(long first, long count);

    /** Method calculate the size using special SQL requst.
     * Overwrite the method for an optimization.<br>
     * Original documentation: {@inheritDoc}
     */
    public abstract long size();

    /** Commit and close transaction */
    public abstract void detach();

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
    public boolean add(IColumn<T, ?> column) {
        return columns.add(column);
    }

    /** Add table column according to column type including CSS class */
    public <V> boolean add(Key<T,V> column, CssAppender cssClass) {
        final boolean result = add(column);
        ((KeyColumn)columns.get(columns.size()-1)).setCssClass(cssClass.getCssClass());
        return result;
    }

    /** Add table column according to column type */
    public <V> boolean add(Key<T,V> column) {
        if (column.isTypeOf(Boolean.class)) {
            return add(KeyColumnBoolean.of(column, isSortingEnabled(column)));
        }
        if (column.isTypeOf(Number.class)) {
            return add(KeyColumn.of(column, isSortingEnabled(column), "number"));
        }
        else {
            return add(KeyColumn.of(column, isSortingEnabled(column), null));
        }
    }

    /** The sorting is enabled for a persistent Ujorm Key by default
     * @see #isDefaultColumnSorting()
     */
    protected boolean isSortingEnabled(final Key<T, ?> column) throws IllegalArgumentException {
        return defaultColumnSorting;
    }

    /** Create AJAX-based DataTable with a {@link #DEFAULT_DATATABLE_ID} */
    public final <S> DataTable<T,S> createDataTable(final int rowsPerPage) {
        return createDataTable(DEFAULT_DATATABLE_ID, rowsPerPage);
    }

    /** Create AJAX-based DataTable */
    public final <S> DataTable<T,S> createDataTable(final String id, final int rowsPerPage) {
        return createDataTable(id, rowsPerPage, false);
    }

    /** Create AJAX-based DataTable */
    public final <S> DataTable<T,S> createDataTable(final int rowsPerPage, boolean insertToolbar) {
        return createDataTable(DEFAULT_DATATABLE_ID, rowsPerPage, insertToolbar);
    }

    /** Create AJAX-based DataTable
     * @param id Component ID
     * @param rowsPerPage Row count per the one page
     * @param insertToolbar Append a generic toolbar for an insert action.
     * @return Create AJAX-based DataTable
     */
    public <S> DataTable<T,S> createDataTable(final String id, final int rowsPerPage, boolean insertToolbar) {
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

        if (insertToolbar) {
            result.addBottomToolbar(new InsertToolbar(result, getModel().getType()));
        }

        return result;
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

    /** Transient tableOf columns */
    public List<IColumn<T, ?>> getColumns() {
        return columns;
    }

    /** Assign a CSS class to a KeyColumn, if exists */
    public void setCssClass(final Key<T, ?> key, final String cssClass) {
        for (IColumn<T, ?> iColumn : columns) {
            if (iColumn instanceof KeyColumn
            && ((KeyColumn) iColumn).getKey().equals(key)) {
               ((KeyColumn) iColumn).setCssClass(cssClass);
               break;
            }
        }
    }
}