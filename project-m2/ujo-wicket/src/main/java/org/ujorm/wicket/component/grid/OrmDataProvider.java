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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.lang.Args;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.wicket.OrmSessionProvider;

/**
 * <p>This class called <strong>OrmDataProvider</strong> is an ORM based
 * Wicket DataProvider. For a customization you can use a your own {@link IColumn} implementations
 * or you can overwrite highlighting methods of this provider.
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
 *  OrmDataProvider<Hotel> dataProvider = OrmDataProvider.of(allActiveHotels);
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
public class OrmDataProvider<U extends OrmUjo> extends AbstractDataProvider<U> {
    private static final long serialVersionUID = 1L;

    /** Hidden table columns for DB fetching */
    List<KeyRing<U>> hiddenColumns = null;
    /** OrmSession */
    private OrmSessionProvider ormSession;
    /** Fetch database columns for better SQL performance
     * where the feature is enabled by default
     */
    private boolean fetchDatabaseColumns = true;

    /** Constructor
     * @param criterion Condition to a database query
     */
    public OrmDataProvider(IModel<Criterion<U>> criterion) {
        this(criterion, null);
    }

    /** Constructor
     * @param criterion Model of a condition to a database query
     * @param defaultSort Default sorting can be assigned optionally
     */
    public OrmDataProvider(IModel<Criterion<U>> criterion, Key<? super U,?> defaultSort) {
        super(criterion, defaultSort);
        this.ormSession = new OrmSessionProvider();
    }

    /** Add the hidden column for a fetch loading from database */
    public void addHidden(Key<? super U, ?> column) {
        if (hiddenColumns == null) {
             hiddenColumns = new ArrayList<KeyRing<U>>();
        }
        hiddenColumns.add(KeyRing.<U>of(column));
    }

    /** Build a JDBC ResultSet always.
     * Overwrite the method for an optimization.<br>
     */
    @Override
    public Iterator<U> iterator(long first, long count) {
        Args.isTrue(count <= Integer.MAX_VALUE
                , "The argument '%s' have got limit %s but the current value is %s"
                , "count"
                , Integer.MAX_VALUE
                , count);
        Query<U> query = createQuery(filter.getObject())
                .setLimit((int) count, first)
                .addOrderBy(getSortKeys());
        fetchDatabaseColumns(query);
        sortDatabaseQuery(query);
        return query.iterator();
    }

    /** Method calculate the size using special SQL request.
     * Overwrite the method for an optimization.<br>
     * Original documentation: {@inheritDoc}
     */
    @Override
    public long size() {
       if (size == null) {
           size = createQuery(filter.getObject()).getCount();
       }
       return size;
    }

    /** Returns ORM Session */
    protected Session getOrmSession() {
        return ormSession.getSession();
    }

    /** Commit and close transaction */
    @Override
    public void detach() {
        ormSession.closeSession();
        size = null;
    }

    /** Create default Query */
    protected Query<U> createQuery(Criterion<U> criterion) {
        return getOrmSession().createQuery(criterion);
    }


    /** The sorting is enabled for a persistent Ujorm Key by default
     * @see #isDefaultColumnSorting()
     */
    @Override
    protected boolean isSortingEnabled(final Key<U, ?> column) throws IllegalArgumentException {
        return super.isSortingEnabled(column)
            && getOrmSession().getHandler().findColumnModel(column, false) != null;
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
    protected void fetchDatabaseColumns(Query<U> query) {
        if (getColumns().isEmpty()) {
            return; // Keep the default state
        }
        if (query.getTableModel().isView()) {
            return; // View is not supported for fetching
        }
        if (hiddenColumns != null) {
            for (KeyRing<U> hiddenKey : hiddenColumns) {
                query.addColumn(hiddenKey.getFirstKey());
            }
        }
        if (!fetchDatabaseColumns) {
            return; // Auto fetching is disabled
        }

        final OrmHandler handler = query.getSession().getHandler();

        for (IColumn<U, ?> iColumn : getColumns()) {
            if (iColumn instanceof KeyColumn) {
                Key<U,?> key = ((KeyColumn) iColumn).getKey();
                if (key.isComposite()
                && ((CompositeKey)key).getCompositeCount() > 1
                && handler.findColumnModel(key) != null) {
                    query.addColumn(key);
                }
            }
        }
    }

    /** Hidden column table list */
    public List<KeyRing<U>> getHiddenColumns() {
        return hiddenColumns;
    }

    /** Add sorting to a database Query,
     * an empty method causes a natural sorting from database */
    protected void sortDatabaseQuery(Query<U> query) {
        final Key sortKey = getSortKey();
        if (sortKey != null) {
            query.addOrderBy(sortKey);
        }
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


    // ============= STATIC METHOD =============

    /** Factory for the class */
    public static <T extends OrmUjo> OrmDataProvider<T> of(IModel<Criterion<T>> criterion, Key<? super T,?> defaultSort) {
        return new OrmDataProvider<T>(criterion, defaultSort);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> OrmDataProvider<T> of(IModel<Criterion<T>> criterion) {
        return new OrmDataProvider<T>(criterion, null);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> OrmDataProvider<T> of(Criterion<T> criterion, Key<? super T,?> defaultSort) {
        return new OrmDataProvider<T>(new Model(criterion), defaultSort);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> OrmDataProvider<T> of(Criterion<T> criterion) {
        return new OrmDataProvider<T>(new Model(criterion), null);
    }

}