/*
 *  Copyright 2009-2026 Pavel Ponec
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

package org.ujorm.core;

import java.io.Closeable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.CompositeKey;
import org.ujorm.Ujo;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * ResultSet iterator. It is not a thread safe implementation.
 * @author Pavel Ponec
 */
final class ResultSetIterator<T extends OrmUjo> extends UjoIterator<T> implements Closeable {
    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(ResultSetIterator.class);

    /** Base query */
    @NotNull
    private final Query query;
    /** Query columns */
    @NotNull
    private final ColumnWrapper[] queryColumns;
    /** Result set */
    @Nullable
    private final ResultSet rs;
    /** If the statemtnt is null then is a sign that it is closed. */
    @Nullable
    private PreparedStatement statement;
    /** Is the query a view? */
    private final boolean view;
    /** A count of the item count, the negative value means the undefined value. */
    private long count = -1L;
    /** A state before the first reading a BO. An auxiliary variable.*/
    private boolean initState = true;
    /** It the cursor ready for reading? After a row reading the value will be set to false. */
    private boolean cursorReady = false;
    /** Has a resultset a next row? */
    private boolean hasNext = true;

    public ResultSetIterator(@NotNull Query query) throws IllegalUjormException {
        try {
            this.query = query;
            this.queryColumns = query.getColumnArray();
            this.statement = query.getStatement();
            this.rs = statement.executeQuery();
            this.view = query.getTableModel().isSelectModel();
        } catch (SQLException e) {
            throw newException(e);
        }
    }

    /** Close all resources and create new exception class */
    @NotNull
    private RuntimeException newException(@Nullable final Throwable e) {
        close(false);
        final boolean noSuchElement = (e == null);
        final String msg = "Error for SQL: " + query;
        return noSuchElement
            ? new NoSuchElementException(msg)
            : new IllegalUjormException(msg, e);
    }

   /** Close all resources and create new NoSuchElementException class */
    @NotNull
    private RuntimeException newNoSuchElementException() {
        return newException(null);
    }

    /**
     * Returns true if the recored has next record
     * @throws java.lang.IllegalStateException
     */
    @Override
    public boolean hasNext() throws IllegalUjormException {

        if (!cursorReady) try {
            cursorReady = true;
            hasNext = rs.next();
            if (!hasNext) {
                close();
            }
        } catch (SQLException e) {
            throw newException(e);
        }
        return hasNext;
    }

    /** Close all resources.
     * If the current iterator moves after the last entry then this method is called automatically.
     */
    @Override
    public void close() throws IllegalUjormException {
        close(true);
    }

    /** Close all resources.
     * If the current iterator moves after the last entry then this method is called automatically.
     * @param throwException Throws the IllegalUjormException or log an event as WARNING.
     */
    private void close(final boolean throwException) throws IllegalUjormException {
        if (statement != null) try {
            if (rs != null) {
                rs.close();
            }
            statement.close();
            statement = null;
        } catch (SQLException e) {
            statement = null; // Forced closure to prevent recursion
            if (throwException) {
               throw newException(e);
            } else {
                LOGGER.log(UjoLogger.WARN, "", e);
            }
        }
    }

    /** Is the instance closed? */
    public boolean isClosed() {
        return statement == null;
    }

    /** Returns a next table row. */
    @Override
    @SuppressWarnings("fallthrough")
    public T next() throws NoSuchElementException, IllegalUjormException {

        if (!hasNext()) {
            throw newNoSuchElementException();
        }
        try {
            cursorReady = false; // switch off the cursor flag.
            @SuppressWarnings("unchecked")
            final T row = (T) query.getTableModel().createBO();

            for (int i=0, max=queryColumns.length; i<max; i++) {
                final ColumnWrapper colWrap = queryColumns[i];
                final MetaColumn column = colWrap.getModel();
                final int iCol = view ? rs.findColumn(MetaColumn.NAME.of(column)) : (i+1);
                final Object value = column.getConverter().getValue(column, rs, iCol);

                if (colWrap.isCompositeKey()) {
                    final Ujo semiRow = ((CompositeKey)colWrap.getKey()).getSemiValue(row, true);
                    column.setValueRaw(semiRow, value);
                    // A session of the related object will be assigned using the OrmProperty later.
                } else {
                    column.setValueRaw(row, value);
                }
            }
            row.writeSession(query.getSession());
            if (initState) {
                initState = false;
            }
            return row;
        } catch (RuntimeException | SQLException | ReflectiveOperationException | OutOfMemoryError e) {
             throw newException(e);
        }
    }

    /** Returns the count if items.
     * The fist call can perform a new SQL statement.
     * This additional SQL calling is skipped if the result is zero.
     */
    @Override
    public long count() {
        if (count<0L) {
            count = !hasNext() && initState
            ? 0L
            : query.getCount()
            ;
        }
        return count;
    }

    /** Skip some rows by the parameter without reading date from a ResultSet.
     * @param count A count of item to skip.
     * @return Returns a true value if the skip count was no limited.
     */
    @Override
    public boolean skip(int count) {
        for (; count>0 && hasNext(); --count) {
            cursorReady=false;
        }
        return count==0;
    }

//    /** This solution is unsupported for ResultSet FORWARD_ONLY by the JDBC specification! <br>
//     * Skip some rows by the parameter without reading date from a ResultSet.
//     * @param count A count of item to skip.
//     * @return Returns a true value if the skip count was no limited.
//     */
//    @Override
//    public boolean skip_UNSUPPORTED(int count) {
//        if (!hasNext()) {
//            return count==0;
//        }
//        try {
//            cursorReady = true;
//            hasNext = rs.relative(--count);
//            return hasNext;
//
//        } catch (RuntimeException | OutOfMemoryError e) {
//            throw newException(e);
//        }
//    }
}

