/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.metaModel.DbColumn;
import org.ujoframework.orm.metaModel.DbTable;

/**
 * JdbcStatement
 * @author pavel
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

    /** Add parameter */
    @SuppressWarnings("unchecked")
    public void addParameter(TableUjo table, DbColumn column) throws SQLException {

        UjoProperty property = DbColumn.TABLE_PROPERTY.of(column);
        Object value = property.of(table);

        ps.setObject(++parameterPointer, value, DbColumn.DB_TYPE.of(column).getSqlType());

        if (logValues) {
            String textSeparator = property.isTypeOf(CharSequence.class) ? "\"" : "";
            values.append(parameterPointer==1 ? "[" : ", " );
            values.append(textSeparator);
            String textValue = UjoManager.getInstance().getText(table, property, UjoAction.DUMMY);
            values.append(textSeparator);
            values.append(textValue);
        }
    }

    /** Return values in format: [1, "ABC", 2.55] */
    public String getTextParameters() {
        if (values!=null
        &&  values.length()>0) {
            return values.toString() + "]";
        } else {
            return "?";
        }
    }

    public void close() throws SQLException {
        ps.close();
    }

    public int executeUpdate() throws SQLException {
        return ps.executeUpdate();
    }

    public ResultSet executeQuery() throws SQLException {
        return ps.executeQuery();
    }

    /** Assign values into the prepared statement */
    @SuppressWarnings("unchecked")
    public int assignValues(TableUjo table) throws SQLException {
        final DbTable dbTable = DbHandler.getInstance().findTableModel((Class) table.getClass());
        final List<DbColumn> columns = DbTable.COLUMNS.getList(dbTable);
        return assignValues(table, columns, 0);
    }


    /** Assign values into the prepared statement */
    @SuppressWarnings("unchecked")
    protected int assignValues(TableUjo table, List<DbColumn> columns, int columnOffset) throws SQLException {
        for (DbColumn column : columns) {
            UjoProperty property = DbColumn.TABLE_PROPERTY.of(column);
            Object value = table!=null ? property.of(table) : null ;

            if (column.isForeignKey()) {
                columnOffset += assignValues((TableUjo) value, column.getForeignColumns(), columnOffset);
            }
            else if (column.isColumn()) {
                ++columnOffset;
                Class type = property.getType();
                int sqlType = DbColumn.DB_TYPE.of(column).getSqlType();


                try {
                    if (value==null) {
                        ps.setNull(columnOffset, sqlType);
                    } else switch (sqlType) {
                        case Types.DATE:
                            final java.sql.Date sqlDate = new java.sql.Date(((java.util.Date) value).getTime());
                            ps.setDate(columnOffset, sqlDate);
                            break;
                        case Types.TIMESTAMP:
                            final java.sql.Timestamp sqlStamp = new java.sql.Timestamp(((java.util.Date) value).getTime());
                            ps.setTimestamp(columnOffset, sqlStamp);
                            break;
                        case Types.TIME:
                            final java.sql.Time sqlTime = new java.sql.Time(((java.util.Date) value).getTime());
                            ps.setTime(columnOffset, sqlTime);
                            break;
                        default:
                            ps.setObject(columnOffset, value, sqlType);
                            break;
                    }
                } catch (Throwable e) {
                    String msg = String.format("table: %s, column %s, columnOffset: %d, value: %s", table.getClass().getSimpleName(), column, columnOffset, value);
                    throw new IllegalStateException(msg, e);
                }
            }
        }
        return columnOffset;
    }



}
