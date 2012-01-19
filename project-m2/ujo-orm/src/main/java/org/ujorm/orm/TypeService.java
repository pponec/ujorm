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
    public final char getTypeCode(final MetaColumn column) {

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

    /**
     * GetValue from the result set by position
     * It must be the same implementation as {@link #getValue(org.ujorm.orm.metaModel.MetaColumn, java.sql.CallableStatement, int)}.
     * @param mColumn Meta-model column
     * @param rs The ResultSet instance
     * @param c Catabase column index starting at #1
     * @return Value form the result set.
     * @throws SQLException
     */
    public Object getValue(final MetaColumn mColumn, final ResultSet rs, final int c) throws SQLException {
        final Object r;
        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : r = rs.getBoolean(c); break;
            case BYTE     : r = rs.getByte(c); break;
            case CHAR     : String s = rs.getString(c); return (s != null && s.length() > 0) ? s.charAt(0) : null;
            case SHORT    : r = rs.getShort(c); break;
            case INT      : r = rs.getInt(c); break;
            case LONG     : r = rs.getLong(c); break;
            case FLOAT    : r = rs.getFloat(c); break;
            case DOUBLE   : r = rs.getDouble(c); break;
            case BIG_DECI : return rs.getBigDecimal(c);
            case BIG_INTE : BigDecimal d = rs.getBigDecimal(c);
                            return d!=null ? d.toBigInteger() : null;
            case STRING   : return rs.getString(c);
            case BYTES    : return rs.getBytes(c);
            case DATE_UTIL: java.sql.Timestamp t = rs.getTimestamp(c);
                            return t!=null ? new java.util.Date(t.getTime()) : null;
            case DATE_SQL : return rs.getDate(c);
            case TIME_SQL : return rs.getTime(c);
            case TIMESTAMP: return rs.getTimestamp(c);
            case BLOB     : return rs.getBlob(c);
            case CLOB     : return rs.getClob(c);
            case ENUM     : int i = rs.getInt(c);
                            return i==0 && rs.wasNull()
                            ? null
                            : mColumn.getType().getEnumConstants()[i] ;
            case COLOR    : i = rs.getInt(c);
                            return i==0 && rs.wasNull()
                            ? null
                            : new Color(i);
            case STRING_WRAP: return createStringWrapper(rs.getString(c), mColumn);
            case BYTES_WRAP : return createBytesWrapper(rs.getBytes(c), mColumn);
            case EXPORT_ENUM: return findEnum(rs.getString(c), mColumn);
            default       : return rs.getObject(c);
        }
        return rs.wasNull() ? null : r;
    }

    /**
     * GetValue from the <b>stored precedure</b> by position.
     * It must be the same implementation as {@link #getValue(org.ujorm.orm.metaModel.MetaColumn, java.sql.ResultSet, int)}.
     * @param mColumn Meta-model column
     * @param rs The CallableStatement instance
     * @param c Catabase column index starting at #1
     * @return Value form the result set.
     * @throws SQLException
     */
    public Object getValue(final MetaColumn mColumn, final CallableStatement rs, final int c) throws SQLException {
        final Object r;
        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : r = rs.getBoolean(c); break;
            case BYTE     : r = rs.getByte(c); break;
            case CHAR     : String s = rs.getString(c); return (s != null && s.length() > 0) ? s.charAt(0) : null;
            case SHORT    : r = rs.getShort(c); break;
            case INT      : r = rs.getInt(c); break;
            case LONG     : r = rs.getLong(c); break;
            case FLOAT    : r = rs.getFloat(c); break;
            case DOUBLE   : r = rs.getDouble(c); break;
            case BIG_DECI : return rs.getBigDecimal(c);
            case BIG_INTE : BigDecimal d = rs.getBigDecimal(c);
                            return d!=null ? d.toBigInteger() : null;
            case STRING   : return rs.getString(c);
            case BYTES    : return rs.getBytes(c);
            case DATE_UTIL: java.sql.Timestamp t = rs.getTimestamp(c);
                            return t!=null ? new java.util.Date(t.getTime()) : null;
            case DATE_SQL : return rs.getDate(c);
            case TIME_SQL : return rs.getTime(c);
            case TIMESTAMP: return rs.getTimestamp(c);
            case BLOB     : return rs.getBlob(c);
            case CLOB     : return rs.getClob(c);
            case ENUM     : int i = rs.getInt(c);
                            return i==0 && rs.wasNull()
                            ? null
                            : mColumn.getType().getEnumConstants()[i] ;
            case COLOR    : i = rs.getInt(c);
                            return i==0 && rs.wasNull()
                            ? null
                            : new Color(i);
            case STRING_WRAP: return createStringWrapper(rs.getString(c), mColumn);
            case BYTES_WRAP : return createBytesWrapper(rs.getBytes(c), mColumn);
            case EXPORT_ENUM: return findEnum(rs.getString(c), mColumn);
            default       : return rs.getObject(c);
        }
        return rs.wasNull() ? null : r;
    }

    /** GetValue from the result set by position.
     * @param mColumn the Column Model
     * @param rs PreparedStatement
     * @param value Value to assign
     * @param c The database column index starts at #1
     * @throws SQLException
     */
    public void setValue
        ( final MetaColumn mColumn
        , final PreparedStatement rs
        , final Object value
        , final int c
        ) throws SQLException {

        if (value==null) {
           final int sqlType = MetaColumn.DB_TYPE.of(mColumn).getSqlType();
           rs.setNull(c, sqlType);
           return;
        }

        switch (mColumn.getTypeCode()) {
            case BOOLEAN  : rs.setBoolean(c, (Boolean)value); break;
            case BYTE     : rs.setByte(c, (Byte)value); break;
            case CHAR     : rs.setString(c, String.valueOf(value)); break;
            case SHORT    : rs.setShort(c, (Short)value); break;
            case INT      : rs.setInt(c, (Integer)value); break;
            case LONG     : rs.setLong(c, (Long)value); break;
            case FLOAT    : rs.setFloat(c, (Float)value); break;
            case DOUBLE   : rs.setDouble(c, (Double)value); break;
            case BIG_DECI : rs.setBigDecimal(c, (BigDecimal) value); break;
            case BIG_INTE : rs.setBigDecimal(c, new BigDecimal((BigInteger)value)); break;
            case STRING   : rs.setString(c, (String)value); break;
            case BYTES    : rs.setBytes(c, (byte[]) value); break;
            case DATE_UTIL: rs.setTimestamp(c, new java.sql.Timestamp(((java.util.Date)value).getTime()) ); break;
            case DATE_SQL : rs.setDate(c, (java.sql.Date) value); break;
            case TIME_SQL : rs.setTime(c, (java.sql.Time)value); break;
            case TIMESTAMP: rs.setTimestamp(c, (java.sql.Timestamp)value); break;
            case BLOB     : rs.setBlob(c, (Blob)value); break;
            case CLOB     : rs.setClob(c, (Clob)value); break;
            case ENUM     : rs.setInt(c, ((Enum)value).ordinal()); break;
            case COLOR    : rs.setInt(c, ((Color)value).getRGB()); break;
            case EXPORT_ENUM:
            case STRING_WRAP:rs.setString(c, value!=null ? ((StringWrapper)value).exportToString() : null ); break;
            case BYTES_WRAP :rs.setBytes(c, value!=null ? ((BytesWrapper)value).exportToBytes() : null ); break;
            default       : rs.setObject(c, value);  break;
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
