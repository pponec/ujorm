/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.metaModel.MetaColumn;

/**
 * ResultSet iterator. It is not a thread safe implementation.
 * @author Pavel Ponec
 */
public class ResultSetIterator<T extends OrmUjo> extends UjoIterator<T> {

    final Query query;
    final ResultSet rs;
    /** Is the query a view? */
    final boolean view;
    /** A count of the item count, the negative value means the undefined value. */
    private long count = -1L;
    /** A state before the first reading a BO. An auxiliary variable.*/
    private boolean initState = true;

    /** It the cursor ready for reading? After a row reading the value will be set to false. */
    private boolean cursorReady = false;
    /** Has a resultset a next row? */
    private boolean hasNext = true;

    public ResultSetIterator(Query query, ResultSet rs) {
        this.query = query;
        this.rs   = rs;
        this.view = query.getTable().isSelectModel();
    }

    /**
     * Returns true if the recored has next record
     * @throws java.lang.IllegalStateException
     */
    @Override
    public boolean hasNext() throws IllegalStateException {
        
        if (!cursorReady) try {
            cursorReady = true;
            hasNext = rs.next();
            if (!hasNext) {
                rs.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("A hasNext() reading exception", e);
        }
        return hasNext;

    }

    /** Returns a next table row. */
    @Override
    @SuppressWarnings("fallthrough")
    public T next() throws NoSuchElementException {

        if (!hasNext()) {
            throw new NoSuchElementException("Query: " + query.toString());
        }
        try {
            cursorReady = false; // switch off the cursor flag.
            @SuppressWarnings("unchecked")
            T row = (T) query.getTableModel().createBO();
            int colCount = query.getColumns().size();

            for (int i=0; i<colCount; i++) {
                final MetaColumn column = query.getColumn(i);
                Class type = column.getType();
                Object value = view
                    ? rs.getObject(MetaColumn.NAME.of(column))
                    : rs.getObject(i + 1)
                    ;

                switch (MetaColumn.DB_TYPE.of(column)) {
                    case DATE: {
                        if (value==null
                        ||  type==value.getClass()) {
                            // OK
                        } else if (java.util.Date.class==type){
                            value = new java.util.Date( ((java.util.Date) value).getTime() );
                        }
                    }
                    default:
                }
                column.setValue(row, value);
            }
            row.writeSession(query.getSession());
            if (initState) { initState=false; }
            return row;
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Query: " + query, e);
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
}
