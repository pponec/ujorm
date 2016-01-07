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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.toolbar.InsertToolbar;
import org.ujorm.wicket.component.tools.DateTimes;

/**
 * <p>This class called <strong>UjoDataProvider</strong> is an common
 * Wicket DataProvider to create an AJAX DataTable component.
 * For a column customizations you can use your own {@link IColumn} implementations.
 * or you can overwrite selected methods of this provider.
 * </p><p>
 * The implementation generates two database requests per a one rendering,
 * the first one get size and the second one get paged data. You can overwrite the two data methods:
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
public abstract class AbstractDataProvider<U extends Ujo> extends SortableDataProvider<U, Object> {
    private static final long serialVersionUID = 1L;
    /** Default DataTable ID have got value {@code "datatable"}. */
    public static final String DEFAULT_DATATABLE_ID = "datatable";
    /** Default CSS style for a SELECTED row */
    protected static final String DEFAULT_CSS_SELECTED = "selected";
    /** Default CSS style for an ACTION COLUMN */
    public static final String DEFAULT_CSS_ACTION = "actionColumn";

    /** Data size */
    protected Long size;
    /** Data criterion model for filtering the data resource */
    protected IModel<Criterion<U>> filter;
    /** Data criterion model for highlighting data rows */
    protected IModel<Criterion<U>> highlighting;
    /** Visible table columns */
    private List<IColumn<U, ?>> columns = new ArrayList<IColumn<U, ?>>();
    /** Default column sorting for the method {@link #addColumn(org.ujorm.Key) }
     * where the feature is enabled by default
     */
    private boolean defaultColumnSorting = true;

    /** Constructor
     * @param criterion Condition to a database query
     */
    public AbstractDataProvider(@Nonnull IModel<Criterion<U>> criterion) {
        this(criterion, null);
    }

    /** Constructor
     * @param filter Model of a condition to a database query
     * @param defaultSort Default sorting can be assigned optionally
     */
    public AbstractDataProvider
            ( @Nonnull IModel<Criterion<U>> filter
            , @Nullable Key<? super U,?> defaultSort) {
        this.filter = (IModel) Args.notNull(filter, "The filter is required");

        if (defaultSort == null) {
            KeyRing<U> keys = KeyRing.of((Class<U>)filter.getObject().getDomain());
            defaultSort =  keys.getFirstKey();
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
    final public void setSort(Key<? super U, ?> key) {
        super.setSort((KeyRing)KeyRing.of(key), key.isAscending()
                ? SortOrder.ASCENDING
                : SortOrder.DESCENDING);
    }

    /** Returns a sorting Key */
    public Key<U,?> getSortKey() {
        final SortParam<Object> sort = getSort();
        if (sort != null) {
            final Object key = getSort().getProperty();
            return key instanceof KeyRing
            ? ((KeyRing<U>)key).getFirstKey().descending(!sort.isAscending())
            : null ;
        } else {
            return null;
        }
    }

    /** Returns a sorting Key */
    @SuppressWarnings({"unchecked"})
    public Key<U,?>[] getSortKeys() {
        final Key<U,?> columnKey = getSortKey();
        return columnKey != null
                ? new Key[]{columnKey}
                : new Key[0];
    }

    /** Build a JDBC ResultSet always.
     * Overwrite the method for an optimization.<br>
     */
    @Override
    public abstract Iterator<U> iterator(long first, long count);

    /** Method calculate the size using special SQL request.
     * Overwrite the method for an optimization.<br>
     * Original documentation: {@inheritDoc}
     */
    @Override
    public abstract long size();

    /** Commit and close transaction */
    @Override
    public abstract void detach();

    /** Get a domain class */
    public Class<U> getDomainClass() {
        return (Class<U>) filter.getObject().getDomain();
    }

    /** Create a model */
    @Override
    public IModel<U> model(U object) {
        return new Model((Serializable)object);
    }

    /** Add table column */
    public boolean add(IColumn<? super U, ?> column) {
        return columns.add((IColumn)column);
    }

    /** Add table columns according to column type including CSS class */
    public void add(KeyList<? super U> columns) {
        for (Key t : columns) {
            add(t);
        }
    }

    /** Add table column according to column type including CSS class */
    public <V> boolean add(Key<? super U,V> column, CssAppender cssClass) {
        final boolean result = add(column);
        ((KeyColumn)columns.get(columns.size()-1)).setCssClass(cssClass.getCssClass());
        return result;
    }

    /** Add table column according to column type */
    public <V> boolean add(Key<? super U,V> column) {
        if (column.isTypeOf(Boolean.class)) {
            return add(KeyColumnBoolean.of(column, isSortingEnabled((Key) column)));
        }
        if (column.isTypeOf(Number.class)) {
            return add(KeyColumn.of(column, isSortingEnabled((Key)column), "number"));
        }
        if (column.isTypeOf(java.sql.Date.class)) {
            return add(KeyColumnDate.of(column, isSortingEnabled((Key)column), KeyColumnDate.DEFAULT_CSS_CLASS));
        }
        if (column.isTypeOf(java.util.Date.class)) {
            return add(KeyColumnDate.of(column, isSortingEnabled((Key)column), "datetime", DateTimes.LOCALE_DATETIME_FORMAT_KEY));
        }
        if (column.isTypeOf(Enum.class)) {
            return add(KeyColumnEnum.of(column, isSortingEnabled((Key)column), "enum"));
        }

        // Default:
        return add(KeyColumn.of(column, isSortingEnabled((Key)column), null));
    }

    /** Create new instance of a Panel from the argument {@code panelClass}
     * and add the result to the grid as new column.
     * @param <V> Value type
     * @param column Key for the column, where the Key can't get data.
     * @param panelClass A panel with two constructor arguments:
     * <ul>
     *    <li>String - component identifier</li>
     *    <li>U - a row object type of {@link Key#getDomainType()}</li>
     * </ul>
     */
    public <V> void add(final Key<? super U,V> column, final Class<? extends WebMarkupContainer> panelClass) {
        final Class<? super U> domainType = column.getDomainType();
        add(new KeyColumn<U, Object>(KeyRing.<U>of(column), null) {
            @Override
            public void populateItem(final Item<ICellPopulator<U>> item, final String componentId, final IModel<U> model) {
                try {
                    final Constructor<? extends WebMarkupContainer> constr = panelClass.getConstructor(String.class, domainType);
                    item.add(constr.newInstance(componentId, model.getObject()));
                } catch (/*ReflectiveOperationException*/ Exception e) {
                    final String msg = String.format
                            ("The %s must have got two constructor arguments type of '%s' and '%s'."
                            , panelClass
                            , String.class.getName()
                            , domainType.getName());
                    throw new IllegalArgumentException(msg, e);
                }
            }
        });
    }

    /** Create new instance of an Action Panel using actions from the argument list.
     * @param <V> Value type
     * @param column Key for the column, where the Key can't get data.
     * @param actions Action array
     */
    public <V> void add(final Key<? super U,V> column, final CommonAction ... actions) {
        final KeyColumn<U, Object> col = new KeyColumn<U, Object>(KeyRing.<U>of(column), null) {
            @Override public void populateItem(final Item<ICellPopulator<U>> item, final String componentId, final IModel<U> model) {
                item.add(new CommonActionPanel(componentId, model.getObject(), actions));
            }
        };
        col.setCssClass(DEFAULT_CSS_ACTION);
        add(col);
    }

    /** Returns a CSS style for SELECTED row.
     * The default value is {@link #DEFAULT_CSS_SELECTED} */
    protected String getCssSelected() {
        return DEFAULT_CSS_SELECTED;
    }

    /** The sorting is enabled for a persistent Ujorm Key by default
     * @see #isDefaultColumnSorting()
     */
    protected boolean isSortingEnabled(final Key<U, ?> column) throws IllegalArgumentException {
        return defaultColumnSorting;
    }

    /** Create AJAX-based DataTable with a {@link #DEFAULT_DATATABLE_ID} */
    public final <S> DataTable<U,S> createDataTable(final int rowsPerPage) {
        return createDataTable(DEFAULT_DATATABLE_ID, rowsPerPage);
    }

    /** Create AJAX-based DataTable */
    public final <S> DataTable<U,S> createDataTable(final String id, final int rowsPerPage) {
        return createDataTable(id, rowsPerPage, false);
    }

    /** Create AJAX-based DataTable */
    public final <S> DataTable<U,S> createDataTable(final int rowsPerPage, boolean insertToolbar) {
        return createDataTable(DEFAULT_DATATABLE_ID, rowsPerPage, insertToolbar);
    }

    /** Create AJAX-based DataTable.
     * The attribute {@link DataTable#getOutputMarkupId() } is set to the {@code true} value.
     * @param id Component ID
     * @param rowsPerPage Row count per the one page
     * @param insertToolbar Append a generic toolbar for an insert action.
     * @return Create AJAX-based DataTable
     */
    public <S> DataTable<U,S> createDataTable(final String id, final int rowsPerPage, boolean insertToolbar) {
        final DataTable<U,S> result = new UjoDataTable<U,S>(id, this.<S>getColumns(), this, rowsPerPage) {
            @Override protected Item<U> newRowItem
                    ( final String id
                    , final int index
                    , final IModel<U> model) {
                final Item<U> result = new OddEvenItem<U>(id, index, model);

                // Mark a highlighting rows:
                if (highlighting != null) {
                    final Criterion<U> crn = highlighting.getObject();
                    if (crn!=null && crn.evaluate(model.getObject())) {
                       result.add(new CssAppender(getCssSelected()));
                    }
                }
                return result;
            }
        };

        result.setOutputMarkupId(true);
        result.setVersioned(false);

        createTopToolbars(result);
        createBottomToolbars(result);

        if (insertToolbar) {
            result.addBottomToolbar(new InsertToolbar(result, getDomainClass()));
        }

        return result;
    }

    /** Create default top table toolbars: AjaxNavigationToolbar and  AjaxFallbackHeadersToolbar */
    @SuppressWarnings("unchecked")
    protected <S> void createTopToolbars(final DataTable<U, S> result) {
        result.addTopToolbar(new AjaxNavigationToolbar(result));
        result.addTopToolbar(new AjaxFallbackHeadersToolbar(result, this));
    }

    /** Create default bottom table toolbars: NoRecordsToolbar */
    protected <S> void createBottomToolbars(final DataTable<U, S> result) {
        result.addBottomToolbar(new NoRecordsToolbar(result));
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

    /** Column table list */
    public <T> List<IColumn<U, T>> getColumns() {
        return (List) columns;
    }

    /** Assign a CSS class to a KeyColumn, if exists */
    public void setCssClass(final Key<? super U, ?> key, final String cssClass) {
        for (IColumn<U, ?> iColumn : columns) {
            if (iColumn instanceof KeyColumn
            && ((KeyColumn) iColumn).getKey().equals(key)) {
               ((KeyColumn) iColumn).setCssClass(cssClass);
               break;
            }
        }
    }

    /**
     * Data criterion model for select data rows
     * @return the highlighting
     */
    @Nullable
    public IModel<Criterion<U>> getHighlighting() {
        return highlighting;
    }

    /**
     * Data criterion model for highlighting data rows
     * @param criterionModel The highlighting criterion model to set
     */
    public void setHighlighting(@Nullable IModel<Criterion<U>> criterionModel) {
        this.highlighting = criterionModel;
    }

    /**
     * Data criterion model for select data rows
     * @param criterion The highlighting criterion to set
     */
    public void setHighlighting(@Nonnull Criterion<U> criterion) {
        setHighlighting(new Model(criterion));
    }

    /**
     * Data criterion model for select data rows
     * @param criterionModel The highlighting criterion model to set
     * @depreated Use the method {@link #setHighlighting(org.apache.wicket.model.IModel)}
     */
    @Deprecated
    public final void setSelected(@Nullable IModel<Criterion<U>> criterionModel) {
        setHighlighting(criterionModel);
    }

    /**
     * Data criterion model for highlighting data rows
     * @param criterion The highlighting criterion to set
     * @depreated Use the method {@link #setHighlighting(org.ujorm.criterion.Criterion) }
     */
    @Deprecated
    public final void setSelected(@Nonnull Criterion<U> criterion) {
        setHighlighting(criterion);
    }

    /** A common tool returns the first row of the selected dataTable or the {@code null} value if no row was found */
    @SuppressWarnings("unchecked")
    protected U getFirstTableRow(@Nonnull DataTable dataTable) {
        final Long firstRowIndex = dataTable.getCurrentPage() * dataTable.getItemsPerPage();
        final Iterator<U> iterator = dataTable.getDataProvider().iterator(firstRowIndex, 1);
        return iterator.hasNext()
               ? iterator.next()
               : null ;
    }

    // --------- CRUD support ---------

    /** Insert row to the data source.
     * The method is not implemented by default.
     * @param row Insert one table row
     */
    public boolean insertRow(U row) {
        throw new UnsupportedOperationException();
    }

    /** Delete rows from the data source
     * The method is not implemented by default.
     * @param deleteCondition Remove all row with a condition.
     */
    public long deleteRow(Criterion<? super U> deleteCondition) {
        throw new UnsupportedOperationException();
    }

    /** Update all rows with a condition using the row
     * The method is not implemented by default.
     * @param updateCondition Update condition
     * @param updatedRow Updated row
     */
    public long updateRow(Criterion<? super U> updateCondition, U updatedRow) {
        throw new UnsupportedOperationException();
    }

}