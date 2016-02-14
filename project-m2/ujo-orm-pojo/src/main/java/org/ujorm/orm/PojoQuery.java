/*
 *  Copyright 2009-2016 Pavel Ponec
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

package org.ujorm.orm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.ujorm.Key;
import org.ujorm.core.UjoIterator;
import org.ujorm.orm.utility.OrmTools;

/**
 * ORM query.
 * @author Pavel Ponec
 * @param <UJO>
 * @composed 1 - 1 Session
 * @composed 1 - 1 CriterionDecoder
 */
public class PojoQuery<UJO extends OrmUjo> implements Iterable<UJO> {

    private final Query<UJO> query;

    public PojoQuery(Query<UJO> query) {
        this.query = query;
    }

    public Query<?> getQuery() {
        return query;
    }

    /** Create a new iterator by the query. The result iterator can be used
     *  in the Java statement <code>for(...)</code> directly.
     * <br>NOTE: The items can be iterated inside a database transaction only,
     * in other case call the next expression:
     * <pre>iterator().toList()</pre>
     * @see #uniqueResult()
     * @see #exists()
     */
    @Override
    public UjoIterator<UJO> iterator() {
        return query.iterator();
    }

    /** There is recommended to use the method {@link #iterator()} rather.
     * The method calls internally the next statement:
     * <pre>iterator().toList()</pre>
     * @return
     */
    public List<? super UJO> list() {
        return query.list();
    }

    /** Returns an instance of the Map where the key is DB table primary key and a value is the row.
     * The method calls internally the next statement:
     * <pre>iterator()</pre>
     * @see #iterator()
     */
    @SuppressWarnings("unchecked")
    public <T> Map<T, ? super UJO> map() {
        return query.map();
    }

    /** Create list and Load all lazy values for the current parameter
     * recursively until optional depth.
     *
     * <br>Performance note: all lazy values are loaded using the one more SQL statement per one relation Key.
     * The method can consume a lot of memory in dependence on the database row count and content of the Criterion.
     *
     * @param depth The object resursion depth where value 0 means: do not any lazy loading.
     * level. The current release supports only values: 0 and 1.
     * @see #iterator()
     * @see OrmTools#loadLazyValues(java.lang.Iterable, int)
     * @see OrmTools#loadLazyValuesAsBatch(org.ujorm.orm.Query)
     */
    public List<? super UJO> list(int depth) {
        return query.list(depth);
    }

    /** Returns a unique result or null if no result item (database row) was found.
     * @throws NoSuchElementException Result is not unique.
     * @see #iterator()
     * @see #exists()
     */
    public UJO uniqueResult() throws NoSuchElementException {
        return query.uniqueResult();
    }

    /** The method performs a new database request and returns result of the function <code>UjoIterator.hasNext()</code>.
     * The result TRUE means the query covers one item (database row) at least.
     * @see #iterator()
     * @see #uniqueResult()
     */
    public boolean exists() {
        return query.exists();
    }


   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public PojoQuery<UJO> orderBy(Key<UJO,?> orderItem) {
        query.orderBy((Key)orderItem);
        return this;
    }

   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public PojoQuery<UJO> orderBy
        ( Key<UJO,?> orderItem1
        , Key<UJO,?> orderItem2
        ) {
        query.orderByMany(new Key[]{orderItem1, orderItem2});
        return this;
    }

   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public PojoQuery<UJO> orderBy
        ( Key<UJO,?> orderItem1
        , Key<UJO,?> orderItem2
        , Key<UJO,?> orderItem3
        ) {
        query.orderByMany(new Key[]{orderItem1, orderItem2, orderItem3});
        return this;
    }

   /** Set an order of the rows by a SQL ORDER BY phrase.
    * <br/>WARNING: the parameters are not type checked.
    */
    @SuppressWarnings("unchecked")
    public final PojoQuery<UJO> orderByMany(Key... orderItems) {
        query.orderByMany(orderItems);
        return this;
    }

   /** Set the one column to reading from database table.
    * Other columns will return a default value, no exception will be throwed.
    * <br/>WARNING 1: assigning an column from a view is forbidden.
    * <br/>WARNING 2: the parameters are not type checked in compile time, use setColumn(..) and addColumn() for this feature.
    * <br/>WARNING 3: assigning an column from a view is forbidden.
    * @param column A Property to select. A composite Property is allowed however only the first item will be used.
    * @see #setColumn(org.ujorm.Key) setColumn(Property)
    */
    public PojoQuery<UJO> addColumn(Key<UJO,?> column) throws IllegalArgumentException {
        query.addColumn((Key)column);
        return this;
    }

   /** Set the one column to reading from database table.
    * Other columns will return a default value, no exception will be throwed.
    * <br/>WARNING: assigning an column from a view is forbidden.
    * @param column A Property to select. A composite Property is allowed however only the first item will be used.
    * @see #addColumn(org.ujorm.Key) addColumn(Property)
    */
    @SuppressWarnings("unchecked")
    public PojoQuery<UJO> setColumn(Key<UJO, ?> column) throws IllegalArgumentException {
        query.setColumns(false, column);
        return this;
    }

   /** Set an list of required columns to reading from database table.
    * Other columns (out of the list) will return a default value, no exception will be throwed.
    * @param addPrimaryKey If the column list does not contains a primary key then the one can be included.
    * @param columns A Key list including a compositer one to database select. The method does not check column duplicities.
    * @see #setColumn(org.ujorm.Key) setColumn(Property)
    * @see #addColumn(org.ujorm.Key) addColumn(Property)
    */
    @SuppressWarnings("unchecked")
    public final PojoQuery<UJO> setColumns(boolean addPrimaryKey, Key... columns)  throws IllegalArgumentException {
        query.setColumns(addPrimaryKey, columns);
        return this;
    }

   /** Set an list of required columns to reading from database table.
    * Other columns (out of the list) will return a default value, no exception will be throwed.
    * <br/>WARNING 1: the parameters are not type checked in compile time, use setColumn(..) and addColumn() for this feature.
    * <br/>WARNING 2: assigning an column from a view is forbidden.
    * @param addPrimaryKey If the column list doesn't contain a primary key of the base Entity then the one will be included.
    * @param addChilds Add all children of the all <strong>foreign keys</strong>.
    * @param columns A Key list including a compositer one to database select. The method does not check column duplicities.
    * @see #setColumn(org.ujorm.Key) setColumn(Property)
    * @see #addColumn(org.ujorm.Key) addColumn(Property)
    */
    public final PojoQuery<UJO> setColumns(boolean addPrimaryKey, boolean addChilds, Key... columns) throws IllegalArgumentException {
        query.setColumns(addPrimaryKey, addChilds, columns);
        return this;
    }

   /** Set an order of the rows by a SQL ORDER BY phrase.
    * WARNING: the list items are not type checked. If you need an item chacking,
    * use the method {@link #addOrderBy(org.ujorm.Key)} rather.
    * @see #addOrderBy(org.ujorm.Key)
    */
    @SuppressWarnings("unchecked")
    public PojoQuery<UJO> orderBy(Collection<Key> orderItems) {
        query.orderBy((Collection) orderItems);
        return this;
    }

    /** Add an item to the end of order list. */
    public PojoQuery<UJO> addOrderBy(final Key<UJO,?> ... keys) {
        query.addOrderBy((Key[])keys);
        return this;
    }

    /** Add an item to the end of order list. */
    public PojoQuery<UJO> addOrderBy(Key<UJO,?> key) {
        query.addOrderBy((Key)key);
        return this;
    }

    /** Has this Query an offset? */
    public boolean isOffset() {
        return query.isOffset();
    }

    /** Get the first row to retrieve (offset). Default value is 0. */
    final public long getOffset() {
        return query.getOffset();
    }

    /** Get the first row to retrieve (offset). Default value is 0.
     * @see #setLimit(int, int)
     */
    public PojoQuery<UJO> setOffset(int offset) {
        query.setOffset(offset);
        return this;
    }

    /** The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation.
     * @see #getLimit()
     */
    final public boolean isLimit() {
        return query.isLimit();
    }

    /** The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation.
     * @see #isLimit()
     */
    final public int getLimit() {
        return query.getLimit();
    }

    /** The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation.
     * @see java.sql.Statement#setMaxRows(int)
     */
    public PojoQuery<UJO> setLimit(int limit) {
        query.setLimit(limit);
        return this;
    }

    /**
     * Set a limit and offset.
     * @param limit The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation.
     * @param offset Get the first row to retrieve (offset). Default value is 0.
     * @see #setLimit(int)
     * @see #setOffset(int)
     */
    public PojoQuery<UJO> setLimit(int limit, long offset) {
        query.setLimit(limit, offset);
        return this;
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should be fetched from the database when more rows are needed.
     * @see java.sql.Statement#getFetchSize()
     */
    public int getFetchSize() {
        return query.getFetchSize();
    }

    /**
     * Retrieves the number of result set rows that is the default fetch size for ResultSet objects generated from this Statement object.
     * @see java.sql.Statement#setFetchSize(int)
     */
    public PojoQuery<UJO> setFetchSize(int fetchSize) {
        query.setFetchSize(fetchSize);
        return this;
    }

    /** Pessimistic lock request */
    public boolean isLockRequest() {
        return query.isLockRequest();
    }

    /** Pessimistic lock request. A default value is false.
     * @see org.ujorm.orm.dialect.HsqldbDialect#printLockForSelect(org.ujorm.orm.Query, java.lang.Appendable) HsqldbDialect
     */
    public PojoQuery<UJO> setLockRequest(boolean lockRequest) {
        query.setLockRequest(lockRequest);
        return this;
    }

    /** Set pessimistic lock request. A default value is false.
     * @see org.ujorm.orm.dialect.HsqldbDialect#printLockForSelect(org.ujorm.orm.Query, java.lang.Appendable) HsqldbDialect
     */
    public PojoQuery<UJO> setLockRequest() {
        query.setLockRequest();
        return this;
    }

    @Override
    public String toString() {
        return query.toString();
    }

    /** Select DISTINCT for a unique row result */
    public boolean isDistinct() {
        return query.isDistinct();
    }

    /** Select DISTINCT for a unique row result */
    public PojoQuery<UJO> setDistinct(boolean distinct) {
        query.setDistinct();
        return this;
    }

    /** Select DISTINCT for a unique row result */
    public PojoQuery<UJO> setDistinct() {
        query.setDistinct();
        return this;
    }

}
