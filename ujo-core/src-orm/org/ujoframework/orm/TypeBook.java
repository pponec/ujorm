/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.ujoframework.orm.metaModel.MetaColumn;

/**
 * A book of popular Java types.
 * @author pavel
 */
final public class TypeBook {

    public static final char UNDEFINED = (char) -1;
    public static final char BOOLEAN = 0;
    public static final char BYTE = 1;
    public static final char CHAR = 2;
    public static final char SHORT = 3;
    public static final char INT = 4;
    public static final char LONG = 5;
    public static final char FLOAT = 6;
    public static final char DOUBLE = 7;
    public static final char BIG_DECI = 8;
    public static final char BIG_INTE = 9;
    public static final char STRING = 10;
    public static final char BYTES = 11;
    public static final char DATE_UTIL = 12;
    public static final char DATE_SQL = 13;
    public static final char TIME_SQL = 14;
    public static final char TIMESTAMP = 15;
    public static final char BLOB = 16;

    /** The method returns a data type code include relation */
    public static char getTypeCode(final MetaColumn column) {

        final Class type = column.getType();

        if (type==String.class) return STRING;
        if (type==Boolean.class) return BOOLEAN;
        if (type==Byte.class) return BYTE;
        if (type==Character.class) return CHAR;
        if (type==Short.class) return SHORT;
        if (type==Integer.class) return INT;
        if (type==Long.class) return LONG;
        if (type==Float.class) return FLOAT;
        if (type==Double.class) return DOUBLE;
        if (type==BigDecimal.class) return BIG_DECI;
        if (type==BigInteger.class) return BIG_INTE;
        if (type==byte[].class) return BYTES;
        if (type==java.util.Date.class) return DATE_UTIL;
        if (type==java.sql.Date.class) return DATE_SQL;
        if (type==java.sql.Time.class) return TIME_SQL;
        if (type==java.sql.Timestamp.class) return TIMESTAMP;
        if (type==Blob.class) return BLOB;

        if (column.isForeignKey()) {
            List<MetaColumn> columns = column.getForeignColumns();
            if (columns.size()==1) {
                return getTypeCode(columns.get(0));
            }
        }
        return UNDEFINED;
    }

    /** GetValue from the result set by position */
    public static Object getValue(final MetaColumn mColumn, final ResultSet rs) throws SQLException {

        String column = MetaColumn.NAME.of(mColumn);
        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : return rs.getBoolean(column);
            case BYTE     : return rs.getByte(column);
            case CHAR     : return (char) rs.getInt(column);
            case SHORT    : return rs.getShort(column);
            case INT      : return rs.getInt(column);
            case LONG     : return rs.getLong(column);
            case FLOAT    : return rs.getFloat(column);
            case DOUBLE   : return rs.getDouble(column);
            case BIG_DECI : return rs.getBigDecimal(column);
            case BIG_INTE : return rs.getBigDecimal(column).toBigInteger();
            case STRING   : return rs.getString(column);
            case BYTES    : return rs.getBytes(column);
            case DATE_UTIL: return new java.util.Date(rs.getTimestamp(column).getTime());
            case DATE_SQL : return rs.getDate(column);
            case TIME_SQL : return rs.getTime(column);
            case TIMESTAMP: return rs.getTimestamp(column);
            default       : return rs.getObject(column);
        }
    }

    /** GetValue from the result set by position */
    public static Object getValue(final MetaColumn mColumn, final ResultSet rs, int column) throws SQLException {
        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : return rs.getBoolean(column);
            case BYTE     : return rs.getByte(column);
            case CHAR     : return (char) rs.getInt(column);
            case SHORT    : return rs.getShort(column);
            case INT      : return rs.getInt(column);
            case LONG     : return rs.getLong(column);
            case FLOAT    : return rs.getFloat(column);
            case DOUBLE   : return rs.getDouble(column);
            case BIG_DECI : return rs.getBigDecimal(column);
            case BIG_INTE : return rs.getBigDecimal(column).toBigInteger();
            case STRING   : return rs.getString(column);
            case BYTES    : return rs.getBytes(column);
            case DATE_UTIL: return new java.util.Date(rs.getTimestamp(column).getTime());
            case DATE_SQL : return rs.getDate(column);
            case TIME_SQL : return rs.getTime(column);
            case TIMESTAMP: return rs.getTimestamp(column);
            default       : return rs.getObject(column);
        }
    }

    /** GetValue from the result set by position */
    public static void setValue
        ( final MetaColumn mColumn
        , final PreparedStatement rs
        , final Object value
        , final int i
        ) throws SQLException {

        if (value==null) {
           final int sqlType = MetaColumn.DB_TYPE.of(mColumn).getSqlType();
           rs.setNull(i, sqlType);
           return;
        }

        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : rs.setBoolean(i, (Boolean)value); break;
            case BYTE     : rs.setByte(i, (Byte)value); break;
            case CHAR     : rs.setInt(i, ((Character)value).charValue()); break;
            case SHORT    : rs.setShort(i, (Short)value); break;
            case INT      : rs.setInt(i, (Integer)value); break;
            case LONG     : rs.setLong(i, (Long)value); break;
            case FLOAT    : rs.setFloat(i, (Float)value); break;
            case DOUBLE   : rs.setDouble(i, (Double)value); break;
            case BIG_DECI : rs.setBigDecimal(i, (BigDecimal) value); break;
            case BIG_INTE : rs.setBigDecimal(i, new BigDecimal((BigInteger)value)); break;
            case STRING   : rs.setString(i, (String)value); break;
            case BYTES    : rs.setBytes(i, (byte[]) value); break;
            case DATE_UTIL: rs.setTimestamp(i, new java.sql.Timestamp(((java.util.Date)value).getTime())); break;
            case DATE_SQL : rs.setDate(i, (java.sql.Date) value); break;
            case TIME_SQL : rs.setTime(i, (java.sql.Time)value); break;
            case TIMESTAMP: rs.setTimestamp(i, (java.sql.Timestamp)value); break;
            default       : rs.setObject(i, value);  break;
        }
    }



}
