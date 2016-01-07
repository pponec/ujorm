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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoComparator;
import org.ujorm.core.UjoIterator;
import org.ujorm.criterion.Criterion;

/**
 * <p>This class called <strong>ListDataProvider</strong> is a {@link List} based DataProvider.
 * For a customization you can use a your own {@link IColumn} implementations
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
public class ListDataProvider<U extends Ujo> extends AbstractDataProvider<U> {
    private static final long serialVersionUID = 1L;
    /** Original row list */
    private List<U> dataRows;
    /** Filtered list rows */
    private List<U> filteredRows;
    /** Sort request */
    private boolean sortRequest;

    /** Constructor
     * @param criterion Condition to a database query
     */
    public ListDataProvider(IModel<Criterion<U>> criterion) {
        this(criterion, null);
    }

    /** Constructor
     * @param filter Model of a condition to a database query
     * @param defaultSort Default sorting can be assigned optionally
     */
    public ListDataProvider(IModel<Criterion<U>> filter, Key<? super U,?> defaultSort) {
        super(filter, defaultSort);
        this.sortRequest = defaultSort != null;
    }

    /** Assign data resource */
    public void setRows(List<? super U> dataRows) {
        this.dataRows = (List) dataRows;
        clearBuffer();
    }

    /** Returns original data rows */
    @Nonnull
    public List<U> getRows() {
        return dataRows != null
             ? dataRows
             : Collections.<U>emptyList();
    }

    /** Returns a filtered rows and cach the result */
    @Nonnull
    protected List<U> getFileredRows() {
        if (filteredRows == null) {
            filteredRows = filter.getObject().evaluate(getRows());
        }
        return filteredRows;
    }

    /** Build a JDBC ResultSet always.
     * Overwrite the method for an optimization.<br>
     */
    @Override
    public Iterator<U> iterator(long first, long count) {
        Args.isTrue(count <= Integer.MAX_VALUE
                , "The argument '%s' have got limit %s but the current value is %s"
                , "first"
                , Integer.MAX_VALUE
                , first);
        Args.isTrue(count <= Integer.MAX_VALUE
                , "The argument '%s' have got limit %s but the current value is %s"
                , "count"
                , Integer.MAX_VALUE
                , count);

        final List<U> rows = getFileredRows();

        // Sort:
        if (sortRequest) {
            sortRows(rows);
        }

        // The sublist:
        final int last = (int) Math.min(first + count, rows.size());
        final int frst = (int) Math.min(first, last);
        return UjoIterator.of(rows.subList(frst, last));
    }

    /** Sort the rows */
    protected void sortRows(final List<U> rows) {
        UjoComparator.of(getSortKeys()).sort(rows);
    }

    /** Method calculate the size of filtered rows */
    @Override
    public long size() {
       if (size == null) {
           size = (long) getFileredRows().size();
       }
       return size;
    }

    /**
     * Detaches model after use. This is generally used to null out transient references that can be
     * re-attached later.
     */
    @Override
    public void detach() {
        clearBuffer();
    }

    /** Clear a filter rows and size */
    protected void clearBuffer() {
        this.filteredRows = null;
        this.size = null;
    }

    // --------- CRUD support ---------

    /** Insert row to the data source
     * @param row Insert the one table row
     */
    @Override
    public boolean insertRow(@Nonnull final U row) {
        clearBuffer();
        return getRows().add(row);
    }

    /** Delete rows from the data source
     * @param deleteCondition Remove all row with a condition.
     */
    @Override
    public long deleteRow(@Nonnull final Criterion<? super U> deleteCondition) {
        long result = 0;
        final List<U> rows = getRows();
        for (int i = rows.size() - 1; i >= 0; i--) {
            final U row = rows.get(i);
            if (deleteCondition.evaluate(row)) {
                rows.remove(i);
                ++result;
            }
        }
        clearBuffer();
        return result;
    }

    /** Update all rows with a condition using the row
     * @param updateCondition Update condition
     * @param updatedRow Updated row
     */
    @Override
    public long updateRow(@Nonnull final Criterion<? super U> updateCondition, @Nonnull final U updatedRow) {
        long result = 0;
        final List<U> rows = getRows();
        for (int i = rows.size() - 1; i >= 0; i--) {
            final U row = rows.get(i);
            if (updateCondition.evaluate(row)) {
                rows.set(i, updatedRow);
                ++result;
            }
        }
        clearBuffer();
        return result;
    }

    // ============= STATIC METHOD =============

    /** Factory for the class */
    public static <T extends Ujo> ListDataProvider<T> of(IModel<Criterion<T>> criterion, Key<? super T,?> defaultSort) {
        return new ListDataProvider<T>(criterion, defaultSort);
    }

    /** Factory for the class */
    public static <T extends Ujo> ListDataProvider<T> of(IModel<Criterion<T>> criterion) {
        return new ListDataProvider<T>(criterion, null);
    }

    /** Factory for the class */
    public static <T extends Ujo> ListDataProvider<T> of(Criterion<T> criterion, Key<? super T,?> defaultSort) {
        return new ListDataProvider<T>(new Model(criterion), defaultSort);
    }

    /** Factory for the class */
    public static <T extends Ujo> ListDataProvider<T> of(Criterion<? super T> criterion) {
        return new ListDataProvider<T>(new Model(criterion), null);
    }
}