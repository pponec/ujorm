/*
 *  Copyright 2009-2014 Pavel Ponec
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.core.UjoIterator;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.impl.ColumnWrapperImpl;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaRelation2Many;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.orm.utility.OrmTools;
import static org.ujorm.core.UjoTools.SPACE;

/**
 * ORM query.
 * @author Pavel Ponec
 * @composed 1 - 1 Session
 * @composed 1 - 1 CriterionDecoder
 */
public class Query<UJO extends OrmUjo> implements Iterable<UJO> {

    /** The base table */
    final private MetaTable table;
    /** Modified columns, the default value is the {@code null}.
     * @see #getDefaultColumns()
     */
    private ArrayList<ColumnWrapper> columns;
    /** Database session */
    private Session session;
    /** Data constraint */
    private Criterion<UJO> criterion;
    /** Select distinct */
    private boolean distinct;
    /** An internal decoder */
    private CriterionDecoder decoder;
    /** An internal info */
    private String statementInfo;

    /** A list of keys to sorting */
    private List<Key<UJO,?>> orderBy;
    /** Set the first row to retrieve. If not set, rows will be retrieved beginning from row 0. */
    private long offset = 0;
    /** The max row count for the resultset. The value -1 means no change, value 0 means no limit (or a default value by the JDBC driver implementation. */
    private int limit = -1;
    /** Retrieves the number of result set rows that is the default fetch size of the Query objects */
    private int fetchSize = -1;
    /** Pessimistic lock request */
    private boolean lockRequest;
    /** SQL parameters for a Native view */
    private SqlParameters sqlParameters;

    /**
     * Create new ORM query. A one from argument is mandatory.
     * @param table Table model is mandatory
     * @param criterion If criterion is null, then the ForAll criterion is used.
     * @param session Session
     */
    public Query(final MetaTable table, final Criterion<UJO> criterion, final Session session) {
        this.table = table;
        this.columns = null;
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

    public <ITEM> void setParameter(Key<UJO,ITEM> key, ITEM value) {
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
     *                  A MetaTable parameter must be specified in the constructor for this case.
     * @see Session#createQuery(org.ujorm.criterion.Criterion) createQuery(Criterion)
     * @see #addCriterion(org.ujorm.criterion.Criterion) addCriterion(..)
     **/
    public Query<UJO> setCriterion(Criterion<UJO> criterion) {
        this.criterion = criterion != null
            ? criterion
            : ((Criterion<UJO>) (Criterion) Criterion.where(true))
            ;
        clearDecoder();
        return this;
    }

    /** Criterion */
    public Criterion<UJO> getCriterion() {
        return criterion;
    }

    /** Method builds and returns a criterion decoder.
     * The new decoder is cached to a next order by change.
     */
    @SuppressWarnings("unchecked")
    final public CriterionDecoder getDecoder() {
        if (decoder==null) {
            final List<Key> relations = new ArrayList<Key>(16);
            for (Key key : orderBy) {
                if (key.isComposite()) {
                    relations.add(key);
                }
            }
            for (ColumnWrapper column : getColumns()) {
                if (column.isCompositeKey()) {
                    relations.add(column.getKey());
                }
            }
            decoder = new CriterionDecoder(criterion, table, relations);
        }
        return decoder;
    }

    /** If the attribute 'order by' is changed so the decoder must be clearder. */
    private void clearDecoder() {
        setDecoder(null);
    }

    /** If the attribute 'order by' is changed so the decoder must be clearder. */
    @PackagePrivate void setDecoder(CriterionDecoder decoder) {
        this.decoder = decoder;
        this.statementInfo = null;
    }

    /** Session */
    public Session getSession() {
        return session;
    }

    /** Table Type */
    public MetaTable getTableModel() {
        return table;
    }

    /** Get Column Collection */
    public List<ColumnWrapper> getColumns() {
        return columns!=null
                ? columns
                : getDefaultColumns();
    }

    /** Returns all direct columns of the base table. */
    protected List<ColumnWrapper> getDefaultColumns() {
        return (List<ColumnWrapper>) (List) MetaTable.COLUMNS.getList(table);
    }

    /** Create a new column List. */
    @SuppressWarnings("empty-statement")
    public ColumnWrapper[] getColumnArray() {
        final Collection<ColumnWrapper> resColumns = getColumns();
        final ColumnWrapper[] result = resColumns.toArray(new ColumnWrapper[resColumns.size()]);
        return result;
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
        final UjoIterator<UJO> result = UjoIterator.of(this);
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

    /** Returns an instance of the Map where the key is DB table primary key and a value is the row.
     * The method calls internally the next statement:
     * <pre>iterator()</pre>
     * @see #iterator()
     */
    @SuppressWarnings("unchecked")
    public <T> Map<T,UJO> map() {
        return map(table.getFirstPK().getKey(), new HashMap<T,UJO>(128));
    }

    /** Returns an instance of the Map where the key is DB table primary key and a value is the row.
     * The method calls internally the next statement:
     * <pre>iterator()</pre>
     * <br/>Note, the last row of the the same ID wins the instance in the map.
     * @param mapKey Ujo Key for the mapKey
     * @see #iterator()
     */
    public <T> Map<T,UJO> map(Key<UJO,T> mapKey, Map<T,UJO> result) {
        for (UJO ujo : iterator()) {
            result.put(mapKey.of(ujo), ujo);
        }
        return result;
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
    public List<UJO> list(int depth) {
        switch (depth) {
            case 0: return list();
            case 1: return OrmTools.loadLazyValuesAsBatch((Query) this);
            default: throw new IllegalArgumentException("The method supports only two values 0 and 1 in the current release");
        }
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
    final public List<Key<UJO,?>> getOrderBy() {
        return orderBy;
    }

    /** Get the order item array. The method returns a not null result always. */
    @SuppressWarnings("unchecked")
    final public Key<UJO,?>[] getOrderAsArray() {
        return orderBy.toArray(new Key[orderBy.size()]);
    }

    /** Set an order of the rows by a SQL ORDER BY phrase.
     * @deprecated Use the {@link #orderByMany(org.ujorm.Key[])} method instead of
     * @see #orderByMany(org.ujorm.Key[])
     */
    @Deprecated
    public Query<UJO> setOrder(Key... order) {
        return orderByMany(order);
    }

   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public Query<UJO> orderBy(Key<UJO,?> orderItem) {
        return orderByMany(new Key[]{orderItem});
    }

   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public Query<UJO> orderBy
        ( Key<UJO,?> orderItem1
        , Key<UJO,?> orderItem2
        ) {
        return orderByMany(new Key[]{orderItem1, orderItem2});
    }

   /** Set an order of the rows by a SQL ORDER BY phrase. */
    public Query<UJO> orderBy
        ( Key<UJO,?> orderItem1
        , Key<UJO,?> orderItem2
        , Key<UJO,?> orderItem3
        ) {
        return orderByMany(new Key[]{orderItem1, orderItem2, orderItem3});
    }

   /** Set an order of the rows by a SQL ORDER BY phrase.
    * <br/>WARNING: the parameters are not type checked.
    */
    @SuppressWarnings("unchecked")
    public final Query<UJO> orderByMany(Key... orderItems) {
        clearDecoder();
        this.orderBy = new ArrayList(Math.max(orderItems.length, 4));
        for (final Key p : orderItems) {
            this.orderBy.add(p);
        }
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
    public Query<UJO> addColumn(Key<UJO,?> column) throws IllegalArgumentException {
        clearDecoder();
        final MetaColumn mc = getHandler().findColumnModel(getLastProperty(column));
        if (mc==null) {
            throw new IllegalArgumentException("Column " + column.getFullName() + " was not foud in the meta-model");
        }
        final ColumnWrapper wColumn = column.isComposite()
                ? new ColumnWrapperImpl(mc, column)
                : mc;
        if (columns==null) {
            columns = new ArrayList<ColumnWrapper>(getDefaultColumns());
        }
        addMissingColumn(wColumn, true, true);
        return this;
    }

   /** Set the one column to reading from database table.
    * Other columns will return a default value, no exception will be throwed.
    * <br/>WARNING: assigning an column from a view is forbidden.
    * @param column A Property to select. A composite Property is allowed however only the first item will be used.
    * @see #addColumn(org.ujorm.Key) addColumn(Property)
    */
    @SuppressWarnings("unchecked")
    public Query<UJO> setColumn(Key<UJO, ?> column) throws IllegalArgumentException {
        return setColumns(false, column);
    }

   /** Set an list of required columns to reading from database table.
    * Other columns (out of the list) will return a default value, no exception will be throwed.
    * @param addPrimaryKey If the column list does not contains a primary key then the one can be included.
    * @param columns A Key list including a compositer one to database select. The method does not check column duplicities.
    * @see #setColumn(org.ujorm.Key) setColumn(Property)
    * @see #addColumn(org.ujorm.Key) addColumn(Property)
    */
    @SuppressWarnings("unchecked")
    public final Query<UJO> setColumns(boolean addPrimaryKey, Key... columns)  throws IllegalArgumentException {
        return setColumns(addPrimaryKey, true, columns);
    }

  /** Set an list of required columns to reading from database table.
    * Other columns (out of the list) will return a default value, no exception will be throwed.
    * <br/>WARNING 1: the parameters are not type checked in compile time, use setColumn(..) and addColumn() for this feature.
    * <br/>WARNING 2: assigning an column from a view is forbidden.
    * @param columns A Key list including a compositer one to database select. The method does not check column duplicities.
    * @see #setColumn(org.ujorm.Key) setColumn(Property)
    * @see #addColumn(org.ujorm.Key) addColumn(Property)
    */
    public final Query<UJO> setColumns(Collection<ColumnWrapper> columns) throws IllegalArgumentException {
        clearDecoder();
        this.columns = new ArrayList<ColumnWrapper>(columns);
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
    public final Query<UJO> setColumns(boolean addPrimaryKey, boolean addChilds, Key<UJO,?>... columns) throws IllegalArgumentException {
        clearDecoder();
        if (columns.length > 1) {
            // There is strongly preferred to sort the keys from direct to the relations (along a count of the relations in the key) due
            // an entity relation column cleans related foreign key.
            // For exmample use the set Item[order.id, order.date] instead of Item[order, order.date]
            Arrays.sort(columns, INNER_KEY_COMPARATOR);
        }
        this.columns = new ArrayList<ColumnWrapper>(columns.length + 3);
        final OrmHandler handler = getHandler();
        for (Key key : columns) {
            final MetaColumn mc = (MetaColumn) handler.findColumnModel(getLastProperty(key), true);
            final ColumnWrapper column = key.isComposite()
                    ? new ColumnWrapperImpl(mc, key)
                    : mc;
            addMissingColumn(column, addChilds, false);
        }
        if (addPrimaryKey) {
            addMissingColumn(table.getFirstPK(), false, true);
        }
        return this;
    }

    /** Add a missing column. The method is for an internal use.
     * @param column Add the column for case it is missing in the column list
     * @param addChilds Add all children of the <strong>foreign key</strong>.
     * @param checkDuplicities Check a duplicity column
     */
    protected void addMissingColumn(final ColumnWrapper column, final boolean addChilds, final boolean checkDuplicities) {
        final int hashCode = column.hashCode();
        final MetaColumn model = column.getModel();

        if (checkDuplicities && !model.isForeignKey()) {
            for (final ColumnWrapper c : columns) {
                if (c.hashCode()==hashCode && column.equals(c)) {
                    return; // The same column is assigned
                }
            }
        }
        if (addChilds) {
            if (model.isForeignKey()) {
                for (ColumnWrapper columnWrapper : model.getForeignTable().getColumns()) {
                    final Key myKey = column.getKey().add(columnWrapper.getKey());
                    final ColumnWrapper cw = new ColumnWrapperImpl(columnWrapper.getModel(), myKey);
                    addMissingColumn(cw, false, true);
                }
            } else {
               columns.add(column);
            }
        } else {
            columns.add(column);
        }
    }

    /** Only direct keys are supported */
    private Key getLastProperty(Key<UJO,?> p) {
        return p.isComposite()
            ? ((CompositeKey)p).getLastKey()
            : p ;
    }

   /** Set an order of the rows by a SQL ORDER BY phrase.
    * WARNING: the list items are not type checked. If you need an item chacking,
    * use the method {@link #addOrderBy(org.ujorm.Key)} rather.
    * @see #addOrderBy(org.ujorm.Key)
    */
    @SuppressWarnings("unchecked")
    public Query<UJO> orderBy(Collection<Key<UJO,?>> orderItems) {
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
    public Query<UJO> addOrderBy(final Key<UJO,?> ... keys) {
        clearDecoder();
        for (Key<UJO, ?> key : keys) {
           orderBy.add(key);
        }
        return this;
    }

    /** Add an item to the end of order list. */
    public Query<UJO> addOrderBy(Key<UJO,?> key) {
        clearDecoder();
        orderBy.add(key);
        return this;
    }

    /** Returns an order column. A method is for an internal use only.
     * @param i Column index
     * @return ColumnWrapper */
    public ColumnWrapper readOrderColumn(int i) throws IllegalStateException {
        final Key key = orderBy.get(i);
        final MetaRelation2Many result = session.getHandler().findColumnModel(key);

        if (result instanceof MetaColumn) {
            return key.isComposite()
                 ? new ColumnWrapperImpl((MetaColumn) result, key)
                 : (MetaColumn) result;
        } else {
            final String msg = String.format
                 ( "The key '%s.%s' is not persistent table column"
                 , table.getType().getSimpleName()
                 , key);
            throw new IllegalStateException(msg);
        }
    }

    /** Has this Query an offset? */
    public boolean isOffset() {
        return offset > 0;
    }

    /** Get the first row to retrieve (offset). Default value is 0. */
    final public long getOffset() {
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
    public Query<UJO> setLimit(int limit, long offset) {
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

    /** Create a PreparedStatement including assigned parameter values */
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
            result.append(" criterion: ");
            result.append(criterion);
        }

        if (orderBy!=null && orderBy.size() > 0) {
            result.append(" | ordered by: ");
            for (Key<UJO, ?> ujoProperty : orderBy) {
                result.append(ujoProperty)
                      .append(SPACE)
                      .append(ujoProperty.isAscending() ? "ASC" : "DESC")
                      .append(", ")
                      ;
            }
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

    /** Get a SQL parameters of the Native view */
    public SqlParameters getSqlParameters() {
        return sqlParameters;
    }

    /** Set a SQL parameters of the <strong>Native View</strong>
     * @see org.ujorm.orm.annot.View
     * @throws IllegalArgumentException The SQL parameters can be used for the VIEW only
     */
    public Query<UJO> setSqlParameters(SqlParameters sqlParameters) throws IllegalArgumentException {
        this.sqlParameters = sqlParameters;
        return this;
    }

    /** Set a SQL parameters of the Native View
     * @see org.ujorm.orm.annot.View
     * @throws IllegalArgumentException The SQL parameters can be used for the VIEW only
     */
    public Query<UJO> setSqlParameters(Object ... parameters) throws IllegalArgumentException {
        return setSqlParameters(new SqlParameters(parameters));
    }

    // --------------- INNER CLASS ---------------

    /** Compare two keys according to count of the KeyCount on sequence */
    @PackagePrivate static final Comparator<Key> INNER_KEY_COMPARATOR = new Comparator<Key>() {
            public int compare(final Key k1, final Key k2) {
                if (!k1.isComposite()) {
                    return k2.isComposite() ? -1 : 0;
                }
                else if (k2.isComposite()) {
                    final int c1 = ((CompositeKey)k1).getCompositeCount();
                    final int c2 = ((CompositeKey)k2).getCompositeCount();
                    return c1 == c2 ? 0
                         : c1 < c2 ? -1 : 1;
                } else {
                    return 1;
                }
            }
        };
}
