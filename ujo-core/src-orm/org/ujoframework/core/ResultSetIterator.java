/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujoframework.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.metaModel.OrmColumn;

/**
 * ResultSet iterator. It is not thread safe implementation.
 * @author pavel
 */
public class ResultSetIterator<T extends TableUjo> extends UjoIterator<T> {

    final Query query;
    final ResultSet rs;

    /** It the cursor ready for reading? After a recored reading the value will be false. */
    private boolean cursorReady = false;
    /** Has a resultset a next row? */
    private boolean hasNext = true;

    public ResultSetIterator(Query query, ResultSet rs) {
        this.query = query;
        this.rs = rs;
    }

    /**
     * Returns true if the recored has next record
     * @throws java.lang.IllegalStateException
     */
    @Override
    public boolean hasNext() throws IllegalStateException {
        try {
            if (!cursorReady) {
                cursorReady = true;
                hasNext = rs.next();
                if (!hasNext) {
                    rs.close();
                }
            }
            return hasNext;

        } catch (SQLException e) {
            throw new IllegalStateException("A hasNext() reading exception", e);
        }
    }

    /** Returns a next table row. */
    @SuppressWarnings("unchecked")
    @Override
    public T next() throws NoSuchElementException {

        if (!hasNext()) {
            throw new NoSuchElementException("Query: " + query.toString());
        }
        try {
            cursorReady = false; // switch off the cursor flag.
            T row = (T) query.getTableModel().createBO();
            int colCount = query.getColumns().size();

            for (int i=0; i<colCount; i++) {
                final OrmColumn column = query.getColumn(i);
                Class type = column.getType();
                Object value = rs.getObject(i + 1);

                switch (OrmColumn.DB_TYPE.of(column)) {
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
            return row;
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Query: " + query, e);
        }
    }
    
}
