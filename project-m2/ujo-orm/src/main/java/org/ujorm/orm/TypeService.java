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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.extensions.StringWrapper;
import org.ujorm.orm.ao.UjoStatement;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * A type service for popular Java types and more.
 * @author Ponec
 */
public class TypeService implements ITypeService<Object,Object> {

    // --- Java type book: ---

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
    public static final char UUID = 24; // An object type
    public static final char LOCAL_DATE = 25; 
    public static final char LOCAL_TIME = 26;
    public static final char LOCAL_DATE_TIME = 27;
    public static final char OFFSET_DATE_TIME = 28;
    
    /** Constructor for the String argument type */
    private static final Class[] STR_ARGS = new Class[] {String.class};

    /** Constructor for the byte[] argument type */
    private static final Class[] BYTES_ARGS = new Class[] {byte[].class};

    /** The method returns a <b>Java data type code</b>.
     * @param column Colum provides a Type, there is supported a relation types too.
     * @return Java type code for frequently used types.
     */
    public static char getTypeCode(@Nonnull final MetaColumn column) {
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
        if (type==java.time.LocalDate.class) return LOCAL_DATE;
        if (type==java.time.LocalTime.class) return LOCAL_TIME;
        if (type==java.time.LocalDateTime.class) return LOCAL_DATE_TIME;
        if (type==java.time.OffsetDateTime.class) return OFFSET_DATE_TIME;
        if (type==java.sql.Blob.class) return BLOB;
        if (type==java.sql.Clob.class) return CLOB;
        if (type.isEnum()) return ENUM;
        if (type==Color.class) return COLOR;
        if (type==java.util.UUID.class) return UUID;

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
     * It must be the same implementation as {@link #of(org.ujorm.orm.metaModel.MetaColumn, java.sql.CallableStatement, int)}.
     * @param mColumn Meta-model column, where the {@link MetaColumn#getTypeCode() typeCode} must be assigned before.
     * @param rs The ResultSet instance
     * @param c Catabase column index starting at #1
     * @return Value form the result set.
     * @throws SQLException
     */
    @Override
    public Object getValue
        ( @Nonnull final MetaColumn mColumn
        , @Nonnull final ResultSet rs
        , final int c) throws SQLException {
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
            case LOCAL_DATE:
            case LOCAL_TIME:
            case LOCAL_DATE_TIME:
            case OFFSET_DATE_TIME: return rs.getObject(c, mColumn.getType());
            case BLOB     : return rs.getBlob(c);
            case CLOB     : return rs.getClob(c);
            case ENUM     : int i = rs.getInt(c);
                            return i==0 && rs.wasNull()
                            ? null
                            : mColumn.getType().getEnumConstants()[i];
            case COLOR    : i = rs.getInt(c);
                            return i==0 && rs.wasNull()
                            ? null
                            : new Color(i);
            case STRING_WRAP: return createStringWrapper(rs.getString(c), mColumn);
            case BYTES_WRAP : return createBytesWrapper(rs.getBytes(c), mColumn);
            case EXPORT_ENUM: return findEnum(rs.getString(c), mColumn);
            case UUID:
            default         : return rs.getObject(c);
        }
        return rs.wasNull() ? null : r;
    }

    /**
     * GetValue from the <b>stored precedure</b> by position.
     * It must be the same implementation as {@link #of(org.ujorm.orm.metaModel.MetaColumn, java.sql.ResultSet, int)}.
     * @param mColumn Meta-model column, where the {@link MetaColumn#getTypeCode() typeCode} must be assigned before.
     * @param rs The CallableStatement instance
     * @param c Database column index starting at #1
     * @return Value form the result set.
     * @throws SQLException
     */
    @Override
    public Object getValue
        ( @Nonnull final MetaColumn mColumn
        , @Nonnull final CallableStatement rs
        , final int c) throws SQLException {
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
            case LOCAL_DATE:
            case LOCAL_TIME:
            case LOCAL_DATE_TIME:
            case OFFSET_DATE_TIME: return rs.getObject(c, mColumn.getType());
            case BLOB     : return rs.getBlob(c);
            case CLOB     : return rs.getClob(c);
            case ENUM     : int i = rs.getInt(c);
                            return i==0 && rs.wasNull()
                            ? null
                            : mColumn.getType().getEnumConstants()[i];
            case COLOR    : i = rs.getInt(c);
                            return i==0 && rs.wasNull()
                            ? null
                            : new Color(i);
            case STRING_WRAP: return createStringWrapper(rs.getString(c), mColumn);
            case BYTES_WRAP : return createBytesWrapper(rs.getBytes(c), mColumn);
            case EXPORT_ENUM: return findEnum(rs.getString(c), mColumn);
            case UUID:
            default         : return rs.getObject(c);
        }
        return rs.wasNull() ? null : r;
    }

    /** GetValue from the result set by position.
     * @param mColumn Meta-model column, where the {@link MetaColumn#getTypeCode() typeCode} must be assigned before.
     * @param rs PreparedStatement
     * @param value Value to assign
     * @param c The database column index starts at #1
     * @throws SQLException
     */
    @Override
    public void setValue
        ( @Nonnull final MetaColumn mColumn
        , @Nonnull final PreparedStatement rs
        , @Nullable final Object value
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
            case LOCAL_DATE:
            case LOCAL_TIME:
            case LOCAL_DATE_TIME:
            case OFFSET_DATE_TIME:
            case UUID:
            default: rs.setObject(c, value, MetaColumn.DB_TYPE.of(mColumn).getSqlType());
        }
    }

    /** Find an enumeration by the Key. */
    private Object findEnum
        ( @Nullable final String key
        , @Nonnull final MetaColumn mColumn) throws IllegalUjormException {
        if (key==null || key.isEmpty()) {
            return null;
        }
        for (Object o : mColumn.getType().getEnumConstants()) {
            if (key.equals(((StringWrapper)o).exportToString())) {
                return o;
            }
        }
        String msg = String.format("No enum was found for the key %s type of %s using the value: '%s'."
                , mColumn
                , mColumn.getType().getSimpleName()
                , key );
        throw new IllegalUjormException(msg);
    }

    /** Create the new StringWrapper by the KEY. */
    @SuppressWarnings("unchecked")
    private Object createStringWrapper(@Nullable final String key, @Nonnull final MetaColumn mColumn) throws IllegalUjormException {
        if (key==null || key.isEmpty()) {
            return null;
        }
        try {
            final Object result = mColumn.getType().getConstructor(STR_ARGS).newInstance(key);
            return result;
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("Bad value export " + mColumn.getType() + "." + key, e);
        }
    }

    /** Create the new BytesWrapper by the KEY. */
    @SuppressWarnings("unchecked")
    private Object createBytesWrapper(@Nullable final byte[] key, @Nonnull final MetaColumn mColumn) throws IllegalUjormException {
        if (key==null || key.length==0) {
            return null;
        }
        try {
            final Object result = mColumn.getType().getConstructor(BYTES_ARGS).newInstance(key);
            return result;
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("Bad value export " + mColumn.getType() + "." + key, e);
        }
    }

    /** Return an converted Java type to database <b>DDL statements</b> by a generic test. */
    @Override
    public Class getDbTypeClass(@Nonnull final MetaColumn column) {
        assert column.getConverter()==this : "Invalid column for this service: " + column;

        switch (column.getTypeCode()) {
            case BYTE: return column.getType();
            case CHAR: return column.getType();
        }

        final Class type = column.getType();
        Object testValue = column.getKey().getDefault();
        if (testValue != null) {
            // It is OK;
        } else if (type.isEnum()) {
            testValue = type.getEnumConstants()[0];
        } else if (false) {
            // TODO: to assign another sample value for next types (?)
        } else {
            return type;
        }

        // Column type code may not be intializad:
        if (!column.readOnly() && column.getTypeCode()==UNDEFINED) {
            column.initTypeCode();
        }

        // Convert the testValue using current TypeService implementation:
        Object dbValue = new UjoStatement().getDatabaseValue(column, testValue);

        // The default number of an Enum type is the Short type:
        if (dbValue instanceof Integer) {
            switch (column.getTypeCode()) {
                case ENUM: return Short.class;
            }
        }

        // Return a type of the dbValue:
        return dbValue != null
                ? dbValue.getClass()
                : type;
    }
}
