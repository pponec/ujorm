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
 * ResultSet iterator
 * @author pavel
 */
public class ResultSetIterator<T extends TableUjo> extends UjoIterator<T> {

    final Query query;
    final ResultSet rs;

    private boolean rowReady = false;
    private boolean hasNext = true;
    /** The end of ResultSet */
    private boolean eor = false;

    public ResultSetIterator(Query query, ResultSet rs) {
        this.query = query;
        this.rs = rs;
    }

    /**
     * Returns true if the recored has next record
     * @return
     * @throws java.lang.IllegalStateException
     */
    @Override
    public boolean hasNext() throws IllegalStateException {
        try {
            return rs.next();
        } catch (SQLException e) {
            throw new IllegalStateException("hasNext excepton", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T next() throws NoSuchElementException {

        if (eor) {
            throw new NoSuchElementException("Query: " + query.toString());
        }
        try {

            T row = (T) query.getTableModel().createBO();
            int colCount = query.getColumns().size();
            for (int i = 0; i<colCount; i++) {
                final OrmColumn column = query.getColumn(i);
                Class type = column.getType();
                Object value = rs.getObject(i + 1);

                final DbType dbType = OrmColumn.DB_TYPE.of(column);
//                switch (dbType) {
//                    case (DATE):
//                        if (java.util.Date.class.equals(type)) {
//                            java.util.Date d = (java.util.Date) value;
//                            value = new java.util.Date(d.getTime());
//                        }
//                        break;
//                    default:
//                }

                column.setValue(row, value);

            }

            return row;
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Query: " + query, e);
        }

    }
}
