/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujoframework.orm.sample;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Abstraktn� p�edek pro JDBC operace (podporuje <tt>select, insert, update a delete</tt>), kter�
 * umo��uje efektivn� pracovat resultset. D�ky tomu se nekontaminuje klientsk� k�d
 * �asto se opakuj�c�m JDBC k�dem a nemus� se explicitn� starat o zpracov�n�
 * <code>{@link java.sql.SQLException}</code>.
 * <p>
 * Potomkov� mus� p�epsat metodu <code>{@link #handleRow(ResultSet)}</code> pokud
 * cht�j� zpracovat resultset (plat� pro <tt>select</tt>) a <code>{@link #getConnection()}</code>,
 * kterou budou poskytovat datab�zov� p�ipojen�.</p>
 *
 * @author    Roman "Dagi" Pichl�k
 */
public abstract class Xxx {

    /**
     * Konstanta ozna�uj�c� SQL p��kaz <tt>select</tt>
     */
    private static final int SELECT_QUERY = 0;
    /**
     * Konstanta ozna�uj�c� <tt>insert, update, delete</tt>
     */
    private static final int UPDATE_QUERY = 1;

    /**
     * Provede <tt>select</tt> SQL p��kaz. P�es v�sledn� resultset se proch�z� a ka�d�
     * ��dek se p�ed�v� k zpracov�n� metod� <code>{@link #handleRow(ResultSet)}</code>.
     * V p��pad�, �e vznikne {@link SQLException} je p�ed�na k zpracov�n� metod�
     * <code>{@link #handleSQLException(SQLException, String, Object[])}</code>
     *
     * @param sql SQL p��kaz (<em>nesm� b�t <code>null</code></em>)
     * @param params parametry pro SQL (<em>m��e b�t <code>null</code></em>)
     *
     * @throws NullPointerException pokud je SQL <code>null</code>
     *
     * @see #setParams(PreparedStatement, Object[])
     */
    public final void executeQuery(String sql, Object params[]) {
        try {
            executeQueryInternal(sql, params, SELECT_QUERY);
        } catch (SQLException e) {
            throw handleSQLException(e, sql, params);
        }
    }

    /**
     * Provede SQL p��kaz typu <tt>insert, update, delete</tt> a vr�t� po�et
     * modifikovan�ch ��dek. V p��pad�, �e vznikne {@link SQLException} je p�ed�na k zpracov�n� metod�
     * <code>{@link #handleSQLException(SQLException, String, Object[])}</code>.
     *
     * @param sql SQL p��kaz (<em>nesm� b�t <code>null</code></em>)
     * @param params parametry pro SQL (<em>m��e b�t <code>null</code></em>)
     *
     * @return po�et modifikovan�ch ��dek
     *
     * @throws NullPointerException pokud je SQL <code>null</code>
     */
    public final int executeUpdate(String sql, Object params[]) {
        try {
            return executeQueryInternal(sql, params, UPDATE_QUERY);
        } catch (SQLException e) {
            throw handleSQLException(e, sql, params);
        }
    }

    /**
     * Realizuje vlastn� vykon�n� p��kazu
     * @param sql SQL p��kaz
     * @param params parametr
     * @param queryType typ SQL  (insert, select, update ,delete)
     *
     * @return rows affected
     *
     * @throws SQLException v p��pad�, �e n�jak� vznikne
     */
    private int executeQueryInternal(String sql, Object params[], int queryType) throws SQLException {
        if (sql == null) {
            throw new NullPointerException();
        }
        int rowsAffected = -1;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            if (params != null) {
                setParams(ps, params);
            }

            if (queryType == SELECT_QUERY) {
                rs = ps.executeQuery();
                while (rs.next()) {
                    handleRow(rs);
                }
            } else {
                rowsAffected = ps.executeUpdate();
            }
        } finally {
            close(con, ps, rs);
        }
        return rowsAffected;
    }

    /**
     * Vrac� datab�zov� p�ipojen�, kter� se pou�ije pro vykon�n� SQL p��kazu.
     *
     * @return datab�zov� p�ipojen�
     */
    protected abstract Connection getConnection() throws SQLException;

    /**
     * Metoda, ve kter� se implementuje vlastn� zpracov�n� ��dku resultsetu.
     *
     * @param rs resultset
     * @throws SQLException v p��pad�, �e n�jak� vznikne
     */
    protected abstract void handleRow(ResultSet rs) throws SQLException;

    /**
     * Slou�� k nastaven� parametr� pro SQL p��kaz podle po�ad�. Nastaven� hodnot
     * <code>null</code> nen� podporov�no.
     *
     * <h4><a name="supported-types">Podporovan� datov� typy</a></h4>
     * <ul>
     *  <li><code>{@link BigDecimal}</code></li>
     *  <li><code>{@link Date}</code></li>
     *  <li><code>{@link String}</code></li>
     *  <li><code>{@link InputStream}</code></li>
     *  <li><code>{@link Timestamp}</code></li>
     *  <li><code>{@link java.lang.Double}</code></li>
     *  <li><code>{@link java.lang.Float}</code></li>
     *  <li><code>{@link Integer}</code></li>
     *  <li><code>{@link Long}</code></li>
     * </ul>
     *
     * @param ps statement
     * @param params parametry
     * @throws SQLException v p��pad�, �e n�jak� vznikne
     * @throws UnsupportedOperationException pokud se jedn� o parametr mimo <a href="#supported-types">podporovan�
     * datov� typy</a>.
     */
    protected void setParams(PreparedStatement ps, Object params[]) throws SQLException {
        Object paramValue = null;
        for (int i = 0; i < params.length;) {
            paramValue = params[i];
            if (paramValue == null) {
                throw new UnsupportedOperationException("Nastaven� hodnoty null nen� podporov�no");
            } else if (paramValue instanceof BigDecimal) {
                ps.setBigDecimal(++i, (BigDecimal) paramValue);
            } else if (paramValue instanceof Date) {
                ps.setDate(++i, (Date) paramValue);
            } else if (paramValue instanceof InputStream) {
                try {
                    InputStream is = (InputStream) paramValue;
                    ps.setBinaryStream(++i, is, is.available());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (paramValue instanceof String) {
                ps.setString(++i, (String) paramValue);
            } else if (paramValue instanceof Timestamp) {
                ps.setTimestamp(++i, (Timestamp) paramValue);
            } else if (paramValue instanceof Double) {
                ps.setDouble(++i, ((Double) paramValue).doubleValue());
            } else if (paramValue instanceof Float) {
                ps.setFloat(++i, ((Float) paramValue).floatValue());
            } else if (paramValue instanceof Integer) {
                ps.setInt(++i, ((Integer) paramValue).intValue());
            } else if (paramValue instanceof Long) {
                ps.setLong(++i, ((Long) paramValue).longValue());
            } else {
                throw new UnsupportedOperationException("Podpora datov�ho typu " + paramValue.getClass() + " nen� prozat�m implementov�na!");
            }
        }
    }

    /**
     * Umo��uje o�et�it vzniklou <code>{@link SQLException}</code>. Defaultn� je
     * implementov�na tak, �e vznikl� v�jimka je obalena do <code>{@link DatabaseException}</code>.
     * Potomkov� mohou toto chov�n� libovoln� p�epsat nap�. v�jimku zapsat do logu,
     * lokalizovat text v�j�mky apod.
     *
     * @param e vznikl� v�jimka
     * @param sql SQL p��kaz, p�i kter�m vznikla v�j�mka
     * @param params parametry pro SQL
     */
    protected DatabaseException handleSQLException(SQLException e, String sql, Object params[]) {
        String message = "Vznikla neo�ek�van� chyba b�hem datab�zov� operace!";
        return new DatabaseException(message, e, sql, params);
    }

    /**
     * Provede bezpe�n� uzav�en� statemntu, resultsetu a datab�zov�ho p�ipojen�.
     * @param con datab�zov� p�ipojen�
     * @param ps statemnt
     * @param rs resultset
     * @throws SQLException v p��pad�, �e n�jak� vznikne
     */
    private void close(Connection con, PreparedStatement ps, ResultSet rs) throws SQLException {
        try {
            if (rs != null) {
                rs.close();
            }
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } finally {
                if (con != null) {
                    con.close();
                }
            }
        }
    }
}