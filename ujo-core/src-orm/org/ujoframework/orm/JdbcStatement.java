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

package org.ujoframework.orm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.UjoAction;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmTable;

/**
 * JdbcStatement
 * @author Pavel Ponec
 */
public class JdbcStatement {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(JdbcStatement.class.toString());

    /** Prepared Statement */
    private final PreparedStatement ps;

    /** Parameter pointer */
    private int parameterPointer = 0;

    private StringBuilder values;

    private boolean logValues;

    public JdbcStatement(final Connection conn, final CharSequence sql) throws SQLException {
        this(conn.prepareStatement(sql.toString()));
    }

    public JdbcStatement(final PreparedStatement ps) {
        this.ps = ps;
        logValues = LOGGER.isLoggable(Level.INFO);
        if (logValues) {
            values = new StringBuilder();
        }
    }

    /** Return values in format: [1, "ABC", 2.55] */
    public String getAssignedValues() {
        if (values!=null
        &&  values.length()>0) {
            return values.toString() + "]";
        } else {
            return "NONE";
        }
    }

    public void close() throws SQLException {
        ps.close();
    }

    /** Run INSERT, UPDATE or DELETE. */
    public int executeUpdate() throws SQLException {
        return ps.executeUpdate();
    }

    public ResultSet executeQuery() throws SQLException {
        return ps.executeQuery();
    }

    /** Assign values into the prepared statement */
    @SuppressWarnings("unchecked")
    public void assignValues(OrmUjo ormUjo) throws SQLException {
        final OrmTable dbTable = ormUjo.readSession().getHandler().findTableModel((Class) ormUjo.getClass());
        final List<OrmColumn> columns = OrmTable.COLUMNS.getList(dbTable);
        assignValues(ormUjo, columns);
    }


    /** Assign values into the prepared statement */
    @SuppressWarnings("unchecked")
    public void assignValues(OrmUjo table, List<OrmColumn> columns) throws SQLException {
        for (OrmColumn column : columns) {

            if (column.isForeignKey()) {
                UjoProperty property = column.getProperty();
                Object value = table!=null ? property.of(table) : null ;
                assignValues((OrmUjo) value, column.getForeignColumns());
            } else if (column.isColumn()) {
                assignValue(table, column);
            }
        }
    }


    /** Assign values into the prepared statement */
    public void assignValues(CriterionDecoder decoder) throws SQLException {
        for (int i=0; i<decoder.getColumnCount(); ++i) {
            final OrmColumn column = decoder.getColumn(i);
            final Object value = decoder.getValueExtended(i);

            if (column.isForeignKey()) {
                List<OrmColumn> fc = column.getForeignColumns();
                OrmUjo ormUjo = (OrmUjo) value;
                for (OrmColumn rColumn : fc) {
                    Object rValue = rColumn.getValue(ormUjo);
                    assignValue(rColumn, rValue, ormUjo);
                }                
            } else {
               assignValue(column, value, null);
            }
        }
    }


    /** Add a next value to a SQL prepared statement. */
    @SuppressWarnings("unchecked")
    public void assignValue(final OrmUjo table, final OrmColumn column) throws SQLException {

        final UjoProperty property = column.getProperty();
        final Object value = table!=null ? property.of(table) : null ;

        assignValue(column, value, table);
    }


    /** Add a next value to a SQL prepared statement. */
    @SuppressWarnings("unchecked")
    public void assignValue
        ( final OrmColumn column
        , final Object value
        , final OrmUjo ormUjo
        ) throws SQLException {

        ++parameterPointer;

        UjoProperty property = column.getProperty();
        int sqlType = OrmColumn.DB_TYPE.of(column).getSqlType();

        if (ormUjo!=null) {
           logValue(ormUjo, property);
        } else if (logValues) {
           String textValue = UjoManager.getInstance().encodeValue(value, false);
           logValue(textValue, property);
        }

        try {
            if (value==null) {
                ps.setNull(parameterPointer, sqlType);
            } else switch (sqlType) {
                case Types.DATE:
                    final java.sql.Date sqlDate = new java.sql.Date(((java.util.Date) value).getTime());
                    ps.setDate(parameterPointer, sqlDate);
                    break;
                case Types.TIMESTAMP:
                    final java.sql.Timestamp sqlStamp = new java.sql.Timestamp(((java.util.Date) value).getTime());
                    ps.setTimestamp(parameterPointer, sqlStamp);
                    break;
                case Types.TIME:
                    final java.sql.Time sqlTime = new java.sql.Time(((java.util.Date) value).getTime());
                    ps.setTime(parameterPointer, sqlTime);
                    break;
                default:
                    ps.setObject(parameterPointer, value, sqlType);
                    break;
            }
        } catch (Throwable e) {
            String textValue = UjoManager.getInstance().getText(ormUjo, property, UjoAction.DUMMY);
            String msg = String.format
                ( "table: %s, column %s, columnOffset: %d, value: %s"
                , ormUjo.getClass().getSimpleName()
                , column
                , parameterPointer
                , textValue
                );
            throw new IllegalStateException(msg, e);
        }
    }

    /** Log a value value into a text format. */
    protected void logValue(final OrmUjo ormUjo, final UjoProperty property) {
        if (logValues) {
            String textValue = UjoManager.getInstance().getText(ormUjo, property, UjoAction.DUMMY);
            logValue(textValue, property);
        }
    }

    /** Log a value value into a text format. */
    protected void logValue(final String textValue, final UjoProperty property) {

        if (logValues) {
            String textSeparator = property.isTypeOf(CharSequence.class) ? "\"" : "";

            values.append(parameterPointer==1 ? "[" : ", " );
            values.append(textSeparator);
            values.append(textValue);
            values.append(textSeparator);
        }
    }

    /** Returns prepared statement - for internal use only */
    PreparedStatement getPreparedStatement() {
        return ps;
    }

    @Override
    public String toString() {
        if (ps!=null) {
            return ps.toString();
        } else {
            return super.toString();
        }
    }
    
}
