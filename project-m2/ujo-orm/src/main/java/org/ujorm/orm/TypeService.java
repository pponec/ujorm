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

import java.awt.Color;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.ujorm.extensions.StringWrapper;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * A type service for popular Java types and more.
 * @author Ponec
 */
public class TypeService {

    // Type book:
    public static final char UNDEFINED = (char) 0;
    public static final char BOOLEAN = 1;
    public static final char BYTE = 2;
    public static final char CHAR = 3;
    public static final char SHORT = 4;
    public static final char INT = 5;
    public static final char LONG = 6;
    public static final char FLOAT = 7;
    public static final char DOUBLE = 8;
    public static final char BIG_DECI = 9;
    public static final char BIG_INTE = 10;
    public static final char STRING = 11;
    public static final char BYTES = 12;
    public static final char DATE_UTIL = 13;
    public static final char DATE_SQL = 14;
    public static final char TIME_SQL = 15;
    public static final char TIMESTAMP = 16;
    public static final char BLOB = 17;
    public static final char CLOB = 18;
    public static final char EXPORT_ENUM = 19;
    public static final char STRING_WRAP = 20;
    public static final char BYTES_WRAP = 21;
    public static final char ENUM = 22;
    public static final char COLOR = 23;

    /** Constructor for the String argument type */
    private static final Class[] STR_ARGS = new Class[] {String.class};

    /** Constructor for the byte[] argument type */
    private static final Class[] BYTES_ARGS = new Class[] {byte[].class};

    /** The method returns a data type code include relation */
    public char getTypeCode(final MetaColumn column) {

        final Class type = column.getType();

        if (StringWrapper.class.isAssignableFrom(type)) return type.isEnum()
                ? EXPORT_ENUM
                : STRING_WRAP;
        if (BytesWrapper.class.isAssignableFrom(type)) return BYTES_WRAP;
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
        if (type==java.sql.Blob.class) return BLOB;
        if (type==java.sql.Clob.class) return CLOB;
        if (type.isEnum()) return ENUM;
        if (type==Color.class) return COLOR;

        if (column.isForeignKey()) {
            List<MetaColumn> columns = column.getForeignColumns();
            if (columns.size()==1) {
                return getTypeCode(columns.get(0));
            }
        }
        return UNDEFINED;
    }

    /** GetValue from the result set by position */
    public Object getValue(final MetaColumn mColumn, final ResultSet rs) throws SQLException {
        Object r;
        String column = MetaColumn.NAME.of(mColumn);
        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : r = rs.getBoolean(column); break;
            case BYTE     : r = rs.getByte(column); break;
            case CHAR     : r = (char) rs.getInt(column); break;
            case SHORT    : r = rs.getShort(column); break;
            case INT      : r = rs.getInt(column); break;
            case LONG     : r = rs.getLong(column); break;
            case FLOAT    : r = rs.getFloat(column); break;
            case DOUBLE   : r = rs.getDouble(column); break;
            case BIG_DECI : return rs.getBigDecimal(column);
            case BIG_INTE : BigDecimal d = rs.getBigDecimal(column);
                            return d!=null ? d.toBigInteger() : null;
            case STRING   : return rs.getString(column);
            case BYTES    : return rs.getBytes(column);
            case DATE_UTIL: java.sql.Timestamp t = rs.getTimestamp(column);
                            return t!=null ? new java.util.Date(t.getTime()) : null;
            case DATE_SQL : return rs.getDate(column);
            case TIME_SQL : return rs.getTime(column);
            case TIMESTAMP: return rs.getTimestamp(column);
            case BLOB     : return rs.getBlob(column);
            case CLOB     : return rs.getClob(column);
            case ENUM     : int i = rs.getInt(column);
                            return i==0 && rs.wasNull()
                            ? null
                            : mColumn.getType().getEnumConstants()[i] ;
            case COLOR    : int c = rs.getInt(column);
                            return c==0 && rs.wasNull()
                            ? null
                            : new Color(c);
            case STRING_WRAP: return createStringWrapper(rs.getString(column), mColumn);
            case BYTES_WRAP : return createBytesWrapper(rs.getBytes(column), mColumn);
            case EXPORT_ENUM: return findEnum(rs.getString(column), mColumn);
            default       : return rs.getObject(column);
        }
        return rs.wasNull() ? null : r;
    }

    /** GetValue from the result set by position */
    public Object getValue(final MetaColumn mColumn, final ResultSet rs, final int column) throws SQLException {
        Object r;
        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : r = rs.getBoolean(column); break;
            case BYTE     : r = rs.getByte(column); break;
            case CHAR     : r = (char) rs.getInt(column); break;
            case SHORT    : r = rs.getShort(column); break;
            case INT      : r = rs.getInt(column); break;
            case LONG     : r = rs.getLong(column); break;
            case FLOAT    : r = rs.getFloat(column); break;
            case DOUBLE   : r = rs.getDouble(column); break;
            case BIG_DECI : return rs.getBigDecimal(column);
            case BIG_INTE : BigDecimal d = rs.getBigDecimal(column);
                            return d!=null ? d.toBigInteger() : null;
            case STRING   : return rs.getString(column);
            case BYTES    : return rs.getBytes(column);
            case DATE_UTIL: java.sql.Timestamp t = rs.getTimestamp(column);
                            return t!=null ? new java.util.Date(t.getTime()) : null;
            case DATE_SQL : return rs.getDate(column);
            case TIME_SQL : return rs.getTime(column);
            case TIMESTAMP: return rs.getTimestamp(column);
            case BLOB     : return rs.getBlob(column);
            case CLOB     : return rs.getClob(column);
            case ENUM     : int i = rs.getInt(column);
                            return i==0 && rs.wasNull()
                            ? null
                            : mColumn.getType().getEnumConstants()[i] ;
            case COLOR    : int c = rs.getInt(column);
                            return c==0 && rs.wasNull()
                            ? null
                            : new Color(c);
            case STRING_WRAP: return createStringWrapper(rs.getString(column), mColumn);
            case BYTES_WRAP : return createBytesWrapper(rs.getBytes(column), mColumn);
            case EXPORT_ENUM: return findEnum(rs.getString(column), mColumn);
            default       : return rs.getObject(column);
        }
        return rs.wasNull() ? null : r;
    }

    /** GetValue from the stored precedure by position */
    public Object getValue(final MetaColumn mColumn, final CallableStatement rs, final int column) throws SQLException {
        Object r;
        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : r = rs.getBoolean(column); break;
            case BYTE     : r = rs.getByte(column); break;
            case CHAR     : r = (char) rs.getInt(column); break;
            case SHORT    : r = rs.getShort(column); break;
            case INT      : r = rs.getInt(column); break;
            case LONG     : r = rs.getLong(column); break;
            case FLOAT    : r = rs.getFloat(column); break;
            case DOUBLE   : r = rs.getDouble(column); break;
            case BIG_DECI : return rs.getBigDecimal(column);
            case BIG_INTE : BigDecimal d = rs.getBigDecimal(column);
                            return d!=null ? d.toBigInteger() : null;
            case STRING   : return rs.getString(column);
            case BYTES    : return rs.getBytes(column);
            case DATE_UTIL: java.sql.Timestamp t = rs.getTimestamp(column);
                            return t!=null ? new java.util.Date(t.getTime()) : null;
            case DATE_SQL : return rs.getDate(column);
            case TIME_SQL : return rs.getTime(column);
            case TIMESTAMP: return rs.getTimestamp(column);
            case BLOB     : return rs.getBlob(column);
            case CLOB     : return rs.getClob(column);
            case ENUM     : int i = rs.getInt(column);
                            return i==0 && rs.wasNull()
                            ? null
                            : mColumn.getType().getEnumConstants()[i] ;
            case COLOR    : int c = rs.getInt(column);
                            return c==0 && rs.wasNull()
                            ? null
                            : new Color(c);
            case STRING_WRAP: return createStringWrapper(rs.getString(column), mColumn);
            case BYTES_WRAP : return createBytesWrapper(rs.getBytes(column), mColumn);
            case EXPORT_ENUM: return findEnum(rs.getString(column), mColumn);
            default       : return rs.getObject(column);
        }
        return rs.wasNull() ? null : r;
    }

    /** GetValue from the result set by position */
    public void setValue
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
            case DATE_UTIL: rs.setTimestamp(i, new java.sql.Timestamp(((java.util.Date)value).getTime()) ); break;
            case DATE_SQL : rs.setDate(i, (java.sql.Date) value); break;
            case TIME_SQL : rs.setTime(i, (java.sql.Time)value); break;
            case TIMESTAMP: rs.setTimestamp(i, (java.sql.Timestamp)value); break;
            case BLOB     : rs.setBlob(i, (Blob)value); break;
            case CLOB     : rs.setClob(i, (Clob)value); break;
            case ENUM     : rs.setInt(i, ((Enum)value).ordinal()); break;
            case COLOR    : rs.setInt(i, ((Color)value).getRGB()); break;
            case EXPORT_ENUM:
            case STRING_WRAP:rs.setString(i, value!=null ? ((StringWrapper)value).exportToString() : null ); break;
            case BYTES_WRAP :rs.setBytes(i, value!=null ? ((BytesWrapper)value).exportToBytes() : null ); break;
            default       : rs.setObject(i, value);  break;
        }
    }

    /** Find enum by KEY. */
    private Object findEnum(final String key, final MetaColumn mColumn) throws IllegalArgumentException {
        if (key==null || key.length()==0) {
            return null;
        }
        for (Object o : mColumn.getType().getEnumConstants()) {
            if (key.equals(((StringWrapper)o).exportToString())) {
                return o;
            }
        }
        throw new IllegalArgumentException("No enum key " + mColumn.getType() + "." + key);
    }

    /** Create the new StringWrapper by the KEY. */
    @SuppressWarnings("unchecked")
    private Object createStringWrapper(final String key, final MetaColumn mColumn) throws IllegalArgumentException {
        if (key==null || key.length()==0) {
            return null;
        }
        try {
            final Object result = mColumn.getType().getConstructor(STR_ARGS).newInstance(key);
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad value export " + mColumn.getType() + "." + key, e);
        }
    }

    /** Create the new BytesWrapper by the KEY. */
    @SuppressWarnings("unchecked")
    private Object createBytesWrapper(final byte[] key, final MetaColumn mColumn) throws IllegalArgumentException {
        if (key==null || key.length==0) {
            return null;
        }
        try {
            final Object result = mColumn.getType().getConstructor(BYTES_ARGS).newInstance(key);
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad value export " + mColumn.getType() + "." + key, e);
        }
    }

}
