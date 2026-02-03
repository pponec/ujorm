/*
 *  Copyright 2010 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ujorm.orm.ao;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import org.ujorm.orm.ITypeService;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * Special UJO PreparedStatement to get an assigned value.
 * <br>The class was designed to get an key value due a bug pro PostgreSQL:
 * There is an error when I create/alter a table using PreparedStatement with some parameters "?".
 * <br>See the <a href="http://archives.postgresql.org/pgsql-jdbc/2006-09/msg00049.php">link</a> for more information.;
 *
 * @author Pavel Ponec
 */
final public class UjoStatement extends UnsupportedOperationException implements PreparedStatement {

    /** The converted value */
    private Object v;

    public UjoStatement() {
        super("Method is not implemented.");
    }

    /** Returns a <b>default value</b> in a JDBC friendly type.
     * The real result type depends in an implementatin a ITypeService.
     * For example a Java Enumerator default value can return either the Integer or String type too.
     * @see ITypeService
     */
    @SuppressWarnings("unchecked")
    public Object getDefaultValue(final MetaColumn column) {
        return getDatabaseValue(column, column.getKey().getDefault());
    }

    /** Returns a <b>any value</b> in a JDBC friendly type.
     * The real result type depends in an implementation a ITypeService.
     * For example a Java Enumerator default value can return either the Integer or String type too.
     * @see ITypeService
     */
    @SuppressWarnings("unchecked")
    public Object getDatabaseValue(final MetaColumn column, Object value) {
        assert value==null || column.getType().isInstance(value) : "Wrong value type: " + value;
        try {
            if (value instanceof OrmUjo tableValue) {
                final MetaTable mt = column.getHandler().findTableModel(tableValue.getClass());
                value = mt.getFirstPK().getKey().of(tableValue);
            }
            column.getConverter().setValue(column, this, value, 1);
            return v;
        } catch (SQLException e) {
            throw this;
        }
    }

    /** Return the last database value assigned by a JDBC API, */
    public Object getValue() {
        return v;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        throw this;
    }

    @Override
    public int executeUpdate() throws SQLException {
        throw this;
    }

    @Override
    public void setNull(int i, int sqlType) throws SQLException {
        v = null;
    }

    @Override
    public void setBoolean(int i, boolean x) throws SQLException {
        v = x;
    }

    @Override
    public void setByte(int i, byte x) throws SQLException {
        v = x;
    }

    @Override
    public void setShort(int i, short x) throws SQLException {
        v = x;
    }

    @Override
    public void setInt(int i, int x) throws SQLException {
        v = x;
    }

    @Override
    public void setLong(int i, long x) throws SQLException {
        v = x;
    }

    @Override
    public void setFloat(int i, float x) throws SQLException {
        v = x;
    }

    @Override
    public void setDouble(int i, double x) throws SQLException {
        v = x;
    }

    @Override
    public void setBigDecimal(int i, BigDecimal x) throws SQLException {
        v = x;
    }

    @Override
    public void setString(int i, String x) throws SQLException {
        v = x;
    }

    @Override
    public void setBytes(int i, byte[] x) throws SQLException {
        v = x;
    }

    @Override
    public void setDate(int i, Date x) throws SQLException {
        v = x;
    }

    @Override
    public void setTime(int i, Time x) throws SQLException {
        v = x;
    }

    @Override
    public void setTimestamp(int i, Timestamp x) throws SQLException {
        v = x;
    }

    @Override
    public void setAsciiStream(int i, InputStream x, int length) throws SQLException {
        v = x;
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int i, InputStream x, int length) throws SQLException {
        v = x;
    }

    @Override
    public void setBinaryStream(int i, InputStream x, int length) throws SQLException {
        v = x;
    }

    @Override
    public void setObject(int i, Object x, int targetSqlType) throws SQLException {
        v = x;
    }

    @Override
    public void setObject(int i, Object x) throws SQLException {
        v = x;
    }

    @Override
    public void setCharacterStream(int i, Reader x, int length) throws SQLException {
        v = x;
    }

    @Override
    public void setRef(int i, Ref x) throws SQLException {
        v = x;
    }

    @Override
    public void setBlob(int i, Blob x) throws SQLException {
        v = x;
    }

    @Override
    public void setClob(int i, Clob x) throws SQLException {
        v = x;
    }

    @Override
    public void setArray(int i, Array x) throws SQLException {
        v = x;
    }

    @Override
    public void setDate(int i, Date x, Calendar cal) throws SQLException {
        v = x;
    }

    @Override
    public void setTime(int i, Time x, Calendar cal) throws SQLException {
        v = x;
    }

    @Override
    public void setTimestamp(int i, Timestamp x, Calendar cal) throws SQLException {
        v = x;
    }

    @Override
    public void setNull(int i, int sqlType, String typeName) throws SQLException {
        v = null;
    }

    @Override
    public void setURL(int i, URL x) throws SQLException {
        v = x;
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw this;
    }

    @Override
    public void setRowId(int i, RowId x) throws SQLException {
        v = x;
    }

    @Override
    public void setNString(int i, String x) throws SQLException {
        v = x;
    }

    @Override
    public void setNCharacterStream(int i, Reader x, long length) throws SQLException {
        v = x;
    }

    @Override
    public void setNClob(int i, NClob x) throws SQLException {
        v = x;
    }

    @Override
    public void setClob(int i, Reader x, long length) throws SQLException {
        v = x;
    }

    @Override
    public void setBlob(int i, InputStream x, long length) throws SQLException {
        v = x;
    }

    @Override
    public void setNClob(int i, Reader x, long length) throws SQLException {
        v = x;
    }

    @Override
    public void setSQLXML(int i, SQLXML x) throws SQLException {
        v = x;
    }

    @Override
    public void setObject(int i, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        v = x;
    }

    @Override
    public void setAsciiStream(int i, InputStream x, long length) throws SQLException {
        v = x;
    }

    @Override
    public void setBinaryStream(int i, InputStream x, long length) throws SQLException {
        v = x;
    }

    @Override
    public void setCharacterStream(int i, Reader x, long length) throws SQLException {
        v = x;
    }

    @Override
    public void setAsciiStream(int i, InputStream x) throws SQLException {
        v = x;
    }

    @Override
    public void setBinaryStream(int i, InputStream x) throws SQLException {
        v = x;
    }

    @Override
    public void setCharacterStream(int i, Reader x) throws SQLException {
    }

    @Override
    public void setNCharacterStream(int i, Reader x) throws SQLException {
        v = x;
    }

    @Override
    public void setClob(int i, Reader x) throws SQLException {
        v = x;
    }

    @Override
    public void setBlob(int i, InputStream x) throws SQLException {
        v = x;
    }

    @Override
    public void setNClob(int i, Reader x) throws SQLException {
        v = x;
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throw this;
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw this;
    }

    @Override
    public void close() throws SQLException {
        throw this;
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        throw this;
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
    }

    @Override
    public int getMaxRows() throws SQLException {
        throw this;
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        throw this;
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
    }

    @Override
    public void cancel() throws SQLException {
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw this;
    }

    @Override
    public void clearWarnings() throws SQLException {
    }

    @Override
    public void setCursorName(String name) throws SQLException {
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        throw this;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throw this;
    }

    @Override
    public int getUpdateCount() throws SQLException {
        throw this;
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        throw this;
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw this;
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
    }

    @Override
    public int getFetchSize() throws SQLException {
        throw this;
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        throw this;
    }

    @Override
    public int getResultSetType() throws SQLException {
        throw this;
    }

    @Override
    public void addBatch(String sql) throws SQLException {
    }

    @Override
    public void clearBatch() throws SQLException {
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw this;
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw this;
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw this;
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw this;
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw this;
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw this;
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw this;
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw this;
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw this;
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw this;
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw this;
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw this;
    }

    @Override
    public boolean isPoolable() throws SQLException {
        throw this;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw this;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw this;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw this;
    }

    @Override
    public void clearParameters() throws SQLException {
    }

    @Override
    public boolean execute() throws SQLException {
        throw this;
    }

    @Override
    public void addBatch() throws SQLException {
    }

    /** Java 7 Required */
    public void closeOnCompletion() throws SQLException {
        close();
    }

    /** Java 7 Required */
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }


}
