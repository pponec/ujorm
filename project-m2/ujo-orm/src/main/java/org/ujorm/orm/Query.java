/*
 *  Copyright 2009-2010 Pavel Ponec
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

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import org.ujorm.UjoProperty;
import org.ujorm.core.UjoIterator;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaRelation2Many;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.criterion.Criterion;
import org.ujorm.CompositeProperty;
import org.ujorm.orm.utility.OrmTools;

/**
 * ORM query.
 * @author Pavel Ponec
 * @composed 1 - 1 Session
 * @composed 1 - 1 CriterionDecoder
 */
public class Query<UJO extends OrmUjo> implements Iterable<UJO> {

    final private MetaTable table;
    private List<MetaColumn> columns;
    private Session session;
    private Criterion<UJO> criterion;
    private boolean distinct;
    private CriterionDecoder decoder;
    private String statementInfo;

    /** A list of properties to sorting */
    private List<UjoProperty<UJO,?>> orderBy;
    /** Set the first row to retrieve. If not set, rows will be retrieved beginnning from row 0. */
    private int offset = 0;
    /** The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation. */
    private int limit = -1;
    /** Retrieves the number of result set rows that is the default fetch size of the Query objects */
    private int fetchSize = -1;
    /** Pessimistic lock request */
    private boolean lockRequest;

    /**
     * Create new ORM query. A one from argument is mandatory.
     * @param table Table model is mandatory
     * @param criterion If criterion is null, then a TRUE constant criterion is used.
     * @param session Session
     */
    public Query(final MetaTable table, final Criterion<UJO> criterion, final Session session) {
        this.table = table;
        this.columns = MetaTable.COLUMNS.getList(table);
        this.criterion = criterion;
        this.session = session;

        orderByMany(); // set an undefined ordering
    }

    /**
     * Create new ORM query without a session.
     * An open session must be {@see #setSession(org.ujorm.orm.Session) assigned} before executing a database request.
     * @param table Table model
     * @param criterion If criterion is null, then a TRUE constant criterion is used.
     * @see #setSession(org.ujorm.orm.Session)
     */
    public Query(final MetaTable table, final Criterion<UJO> criterion) {
        this(table, criterion, null);
    }

    /** Get Handler */
    private OrmHandler getHandler() {
        OrmHandler handler = null;
        if (table != null) {
            handler = table.getDatabase().getOrmHandler();
        } else if (session != null) {
            handler = session.getHandler();
        }
        if (handler == null) {
            throw new IllegalStateException("The base class must be assigned first!");
        }
        return handler;
    }

    /** An open session must be assigned before executing a database request. */
    public Query<UJO> setSession(Session session) {
        this.session = session;
        return this;
    }

    /** Returns a database row count along a current limit and offset attribues.
     * @see #getCount() 
     */
    public long getLimitedCount() {
        long result = getCount();

        // Recalculate the count by a limit and offset:
        if (isOffset()) {
            result -= offset;
            if (result<0) {
                result = 0L;
            }
        }
        if (limit>=0
        &&  limit<result
        ){
            result = limit;
        }

        return result;
    }

    /** Returns a count of the items 
     * @see #getLimitedCount() 
     */
    public long getCount() {
        final long result = session.getRowCount(this);
        return result;
    }

    public <ITEM> void setParameter(UjoProperty<UJO,ITEM> property, ITEM value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Add a new Criterion.
     * @param criterion Parameter is mandatory and must not be NULL.
     * @see Session#createQuery(org.ujorm.criterion.Criterion) createQuery(Criterion)
     * @see #setCriterion(org.ujorm.criterion.Criterion) setCriterion(..)
     */
    public void addCriterion(Criterion<UJO> criterion) throws IllegalArgumentException {
        if (criterion==null) {
            throw new IllegalArgumentException("Argument must not be null");
        }
        this.criterion = this.criterion!=null
            ? this.criterion.and(criterion)
            : criterion
            ;
        clearDecoder();
    }

    /** Set a new Criterion. There is recommended
     * @param criterion The value NULL is allowed because the value is replaced internally to expression <code>Criterion.where(true)</code>.
     *                  A MetaTable parameter must be specified in the constuctor for this case.
     * @see Session#createQuery(org.ujorm.criterion.Criterion) createQuery(Criteron)
     * @see #addCriterion(org.ujorm.criterion.Criterion) addCriterion(..)
     **/
    public Query<UJO> setCriterion(Criterion<UJO> criterion) {
        this.criterion = criterion != null
            ? criterion
            : ((Criterion<UJO>) (Object) Criterion.where(true))
            ;
        clearDecoder();
        return this;
    }

    /** Criterion */
    public Criterion<UJO> getCriterion() {
        return criterion;
    }

    /** Method builds and retuns a criterion decoder. 
     * The new decoder is cached to a next order by change.
     */
    @SuppressWarnings("unchecked")
    final public CriterionDecoder getDecoder() {
        if (decoder==null) {
            decoder = new CriterionDecoder(criterion, table.getDatabase(), (List)orderBy);
        }
        return decoder;
    }

    /** If the attribute 'order by' is changed so the decoder must be clearder. */
    private void clearDecoder() {
        decoder = null;
        statementInfo = null;
    }

    /** Session */
    public Session getSession() {
        return session;
    }

    /** Table Type */
    public MetaTable getTableModel() {
        return table;
    }

    /** Get Column List */
    public List<MetaColumn> getColumns() {
        return columns;
    }

    /** Get Column List */
    public MetaColumn getColumn(int index) {
        return columns.get(index);
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
        final UjoIterator<UJO> result = UjoIterator.getInstance(this);
        return result;
    }

    /** Create a new iterator by the query.
     * @deprecated Use {@link #iterator()} instead of.
     * @see #iterator()
     */
    @Deprecated
    final public UjoIterator<UJO> iterate() {
        return iterator();
    }

    /** There is recommended to use the method {@link #iterator()} rather.
     * The method calls internally the next statement:
     * <pre>iterator().toList()</pre>
     * @see #iterator()
     * @see OrmTools#loadLazyValues(java.lang.Iterable, int) 
     * @see OrmTools#loadLazyValuesAsBatch(org.ujorm.orm.Query) 
     */
    public List<UJO> list() {
        return iterator().toList();
    }

    /** Returns a unique result or null if no result item (database row) was found.
     * @throws NoSuchElementException Result is not unique.
     * @see #iterator()
     * @see #exists() 
     */
    public UJO uniqueResult() throws NoSuchElementException {
        final UjoIterator<UJO> iterator = iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        final UJO result = iterator.next();
        if (iterator.hasNext()) {
            iterator.close();
            throw new NoSuchElementException("Result is not unique for: " + criterion);
        }
        return result;
    }

    /** The method performs a new database request and returns result of the function <code>UjoIterator.hasNext()</code>.
     * The result TRUE means the query covers one item (database row) at least.
     * @see #iterator()
     * @see #uniqueResult()
     */
    public boolean exists() {
        int $limit = limit;
        limit = 1;
        final UjoIterator<UJO> iterator = iterator();
        final boolean result = iterator.hasNext();
        iterator.close();
        limit = $limit;
        return result;
    }

    /** Get the order item list. The method returns a not null result always. */
    final public List<UjoProperty<UJO,?>> getOrderBy() {
        return orderBy;
    }

    /** Get the order item array. The method returns a not null result always. */
    @SuppressWarnings("unchecked")
    final public UjoProperty<UJO,?>[] getOrderAsArray() {
        return orderBy.toArray(new UjoProperty[orderBy.size()]);
    }

    /** Set an order of the rows by a SQL ORDER BY phrase.
     * @deprecated Use the {@link #orderByMany(org.ujorm.UjoProperty[])} method instead of
     * @see #orderByMany(org.ujorm.UjoProperty[])
     */
    @Deprecated
    public Query<UJO> setOrder(UjoProperty... order) {
        return orderByMany(order);
    }

   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public Query<UJO> orderBy(UjoProperty<UJO,?> orderItem) {
        return orderByMany(new UjoProperty[]{orderItem});
    }

   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public Query<UJO> orderBy
        ( UjoProperty<UJO,?> orderItem1
        , UjoProperty<UJO,?> orderItem2
        ) {
        return orderByMany(new UjoProperty[]{orderItem1, orderItem2});
    }

   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public Query<UJO> orderBy
        ( UjoProperty<UJO,?> orderItem1
        , UjoProperty<UJO,?> orderItem2
        , UjoProperty<UJO,?> orderItem3
        ) {
        return orderByMany(new UjoProperty[]{orderItem1, orderItem2, orderItem3});
    }

   /** Set an order of the rows by a SQL ORDER BY phrase.
    * <br/>WARNING: the parameters are not type checked.
    */
    @SuppressWarnings("unchecked")
    public final Query<UJO> orderByMany(UjoProperty... orderItems) {
        clearDecoder();
        this.orderBy = new ArrayList(Math.max(orderItems.length, 4));
        for (final UjoProperty p : orderItems) {
            this.orderBy.add(p);
        }
        return this;
    }

   /** Set the one column to reading from database table.
    * Other columns will return a default value, no exception will be throwed.
    * @param column A Property to select. A composite Property is allowed however only the first item will be used.
    * @see #setColumn(org.ujorm.UjoProperty) setColumn(Property)
    */
    public Query<UJO> addColumn(UjoProperty<UJO,?> column) throws IllegalArgumentException {
        final MetaColumn mc = (MetaColumn) getHandler().findColumnModel(getDirectProperty(column));
        if (mc==null) {
            throw new IllegalArgumentException("Column " + column + " was not foud in the meta-model");
        }
        if (!columns.contains(mc)) {
           columns.add(mc);
        }
        return this;
    }

   /** Set the one column to reading from database table.
    * Other columns will return a default value, no exception will be throwed.
    * @param column A Property to select. A composite Property is allowed however only the first item will be used.
    * @see #addColumn(org.ujorm.UjoProperty) addColumn(Property)
    */
    @SuppressWarnings("unchecked")
    public Query<UJO> setColumn(UjoProperty<UJO,?> column) throws IllegalArgumentException {
        this.columns = new ArrayList<MetaColumn>();
        return addColumn(getDirectProperty(column));
    }

   /** Set an list of required columns to reading from database table.
    * Other columns (out of the list) will return a default value, no exception will be throwed.
    * <br/>WARNING: the parameters are not type checked in compile time, use setColumn(..) and addColumn() for this feature.
    * @param addPrimaryKey If the column list does not contains a primary key then the one can be included.
    * @param columns A Property list to select. A composite Property is allowed however only the first item will be used.
    * @see #setColumn(org.ujorm.UjoProperty) setColumn(Property)
    * @see #addColumn(org.ujorm.UjoProperty) addColumn(Property)
    */
    @SuppressWarnings("unchecked")
    public final Query<UJO> setColumns(boolean addPrimaryKey, UjoProperty... columns)  throws IllegalArgumentException {
        this.columns = new ArrayList<MetaColumn>(columns.length);
        final OrmHandler handler = getHandler();
        for (UjoProperty column : columns) {
            final MetaColumn mc = (MetaColumn) handler.findColumnModel(getDirectProperty(column));
            if (mc.getTable()!=table) {
                throw new IllegalArgumentException("Base class doesn't contains the column: " + column);
            } else {
                this.columns.add(mc);
            }
        }
        if (addPrimaryKey
        && !this.columns.contains(table.getFirstPK())) {
            this.columns.add(table.getFirstPK());
        }
        return this;
    }

    /** Only direct properties are supported */
    private UjoProperty getDirectProperty(UjoProperty p) {
        return p.isDirect()
            ?  p
            : ((CompositeProperty)p).getFirstProperty()
            ;
    }

   /** Set an order of the rows by a SQL ORDER BY phrase.
    * WARNING: the list items are not type checked. If you need an item chacking,
    * use the method {@link #addOrderBy(org.ujorm.UjoProperty)} rather.
    * @see #addOrderBy(org.ujorm.UjoProperty)
    */
    @SuppressWarnings("unchecked")
    public Query<UJO> orderBy(Collection<UjoProperty> orderItems) {
        clearDecoder();
        if (orderItems==null) {
            return orderByMany(); // empty sorting
        } else {
            this.orderBy.clear();
            this.orderBy.addAll( (Collection)orderItems );
        }
        return this;
    }

    /** Add an item to the end of order list. */
    public Query<UJO> addOrderBy(UjoProperty<UJO,?> property) {
        clearDecoder();
        orderBy.add(property);
        return this;
    }

    /** Returns an order column. A method for an internal use only.  */
    public MetaColumn readOrderColumn(int i) throws IllegalStateException {
        final UjoProperty property = orderBy.get(i);
        final MetaRelation2Many result = session.getHandler().findColumnModel(property);

        if (result instanceof MetaColumn) {
            return (MetaColumn) result;
        } else {
            String msg = "Property '" + table.getType().getSimpleName() + "." + property + "' is not a persistent table column";
            throw new IllegalStateException(msg);
        }        
    }

    /** Has this Query an offset? */
    public boolean isOffset() {
        return offset>0;
    }

    /** Get the first row to retrieve (offset). Default value is 0. */
    final public int getOffset() {
        return offset;
    }

    /** Get the first row to retrieve (offset). Default value is 0.
     * @see #setLimit(int, int)
     */
    public Query<UJO> setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    /** The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation.
     * @see #getLimit()
     */
    final public boolean isLimit() {
        return limit>0;
    }

    /** The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation.
     * @see #isLimit()
     */
    final public int getLimit() {
        return limit;
    }

    /** The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation.
     * @see java.sql.Statement#setMaxRows(int)
     */
    public Query<UJO> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Set a limit and offset.
     * @param limit The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation.
     * @param offset Get the first row to retrieve (offset). Default value is 0.
     * @see #setLimit(int) 
     * @see #setOffset(int)
     */
    public Query<UJO> setLimit(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
        return this;
    }

    /** Use the method {@link #setLimit(int)} rather.
     * @see #setLimit(int)
     */
    @Deprecated
    final public Query<UJO> setMaxRows(int limit) {
        return setLimit(limit);
    }

    /**
     * Gives the JDBC driver a hint as to the number of rows that should be fetched from the database when more rows are needed.
     * @see java.sql.Statement#getFetchSize()
     */
    public int getFetchSize() {
        return fetchSize;
    }

    /**
     * Retrieves the number of result set rows that is the default fetch size for ResultSet objects generated from this Statement object.
     * @see java.sql.Statement#setFetchSize(int)
     */
    public Query<UJO> setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }

    /** Create a PreparedStatement include assigned parameter values */
    public PreparedStatement getStatement() {
        return session.getStatement(this).getPreparedStatement();
    }

    /** Get the SQL statement or null, of statement is not known yet. */
    public String getStatementInfo() {
        return statementInfo;
    }

    /** Set the SQL statement */
    /*default*/ void setStatementInfo(String statementInfo) {
        this.statementInfo = statementInfo;
    }

    /** Pessimistic lock request */
    public boolean isLockRequest() {
        return lockRequest;
    }

    /** Pessimistic lock request. A default value is false. 
     * @see org.ujorm.orm.dialect.HsqldbDialect#printLockForSelect(org.ujorm.orm.Query, java.lang.Appendable) HsqldbDialect
     */
    public Query<UJO> setLockRequest(boolean lockRequest) {
        this.lockRequest = lockRequest;
        return this;
    }

    /** Set pessimistic lock request. A default value is false.
     * @see org.ujorm.orm.dialect.HsqldbDialect#printLockForSelect(org.ujorm.orm.Query, java.lang.Appendable) HsqldbDialect
     */
    public Query<UJO> setLockRequest() {
        return setLockRequest(true);
    }

    @Override
    public String toString() {

        if (statementInfo!=null) {
            return statementInfo;
        }

        StringBuilder result = new StringBuilder(64);

        if (table!=null) {
            result.append('(').append(table.getType().getSimpleName()).append(") ");
        }
        if (criterion!=null) {
            result.append(criterion);
        }

        return result.length()>0
            ? result.toString()
            : super.toString()
            ;
    }

    /** Select DISTINCT for a unique row result */
    public boolean isDistinct() {
        return distinct;
    }

    /** Select DISTINCT for a unique row result */
    public Query<UJO> setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /** Select DISTINCT for a unique row result */
    public Query<UJO> setDistinct() {
        return setDistinct(true);
    }

}
