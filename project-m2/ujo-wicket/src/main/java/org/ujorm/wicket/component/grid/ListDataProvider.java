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
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoIterator;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.orm.OrmUjo;

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
public class ListDataProvider<T extends Ujo> extends AbstractDataProvider<T> {
    private static final long serialVersionUID = 1L;
    /** Original row list */
    private List<T> dataRows;
    /** Filtered list rows */
    private List<T> filteredRows;
    
    /** Constructor
     * @param criterion Condition to a database query
     */
    public ListDataProvider(IModel<Criterion<T>> criterion) {
        this(criterion, null);
    }

    /** Constructor
     * @param criterion Model of a condition to a database query
     * @param defaultSort Default sorting can be assigned optionally
     */
    public ListDataProvider(IModel<Criterion<T>> criterion, Key<T,?> defaultSort) {
        super(criterion, defaultSort);
    }

    public void setRows(List<T> dataRows) {
        this.dataRows = dataRows;
        this.filteredRows = null;
        this.size = null;
    }

    /** Returns a filtered rows and cach the result */
    public List<T> getRows(Criterion<T> crn) {
        body:
        if (filteredRows == null) {
            if (crn instanceof ValueCriterion) {
                final ValueCriterion<T> valCrn = (ValueCriterion) crn;
                if (valCrn.getOperator() == Operator.XFIXED) {
                    filteredRows = Boolean.TRUE.equals(valCrn.getRightNode())
                            ? dataRows
                            : new ArrayList();
                    break body;
                }
            }
            filteredRows = new ArrayList<T>();
            for (T t : dataRows) {
                if (crn.evaluate(t)) {
                    filteredRows.add(t);
                }
            }
        }
        return filteredRows;
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

        List<T> rows = getRows(criterion.getObject());
        return UjoIterator.of(rows);

        // TODO :
//        List<T> rows = createQuery(criterion.getObject())
//                .setLimit((int) count, first)
//                .addOrderBy(getSortKey());
//        fetchDatabaseColumns(query);
//        sortDatabaseQuery(query);
//        return query.iterator();
    }

    /** Method calculate the size using special SQL requst.
     * Overwrite the method for an optimization.<br>
     * Original documentation: {@inheritDoc}
     */
    @Override
    public long size() {
       if (size == null) {
           size = (long) getRows(criterion.getObject()).size();
       }
       return size;
    }

    /** Commit and close transaction */
    @Override
    public void detach() {
        // TODO ...
    }

    // ============= STATIC METHOD =============

    /** Factory for the class */
    public static <T extends OrmUjo> ListDataProvider<T> of(IModel<Criterion<T>> criterion, Key<T,?> defaultSort) {
        return new ListDataProvider<T>(criterion, defaultSort);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> ListDataProvider<T> of(IModel<Criterion<T>> criterion) {
        return new ListDataProvider<T>(criterion, null);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> ListDataProvider<T> of(Criterion<T> criterion, Key<T,?> defaultSort) {
        return new ListDataProvider<T>(Model.of(criterion), defaultSort);
    }

    /** Factory for the class */
    public static <T extends OrmUjo> ListDataProvider<T> of(Criterion<T> criterion) {
        return new ListDataProvider<T>(Model.of(criterion), null);
    }

}