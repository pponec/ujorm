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
 * Abstraktní pøedek pro JDBC operace (podporuje <tt>select, insert, update a delete</tt>), který
 * umožòuje efektivnì pracovat resultset. Díky tomu se nekontaminuje klientský kód
 * èasto se opakujícím JDBC kódem a nemusí se explicitnì starat o zpracování
 * <code>{@link java.sql.SQLException}</code>.
 * <p>
 * Potomkové musí pøepsat metodu <code>{@link #handleRow(ResultSet)}</code> pokud
 * chtìjí zpracovat resultset (platí pro <tt>select</tt>) a <code>{@link #getConnection()}</code>,
 * kterou budou poskytovat databázové pøipojení.</p>
 *
 * @author    Roman "Dagi" Pichlík
 */
public abstract class Xxx {

    /**
     * Konstanta oznaèující SQL pøíkaz <tt>select</tt>
     */
    private static final int SELECT_QUERY = 0;
    /**
     * Konstanta oznaèující <tt>insert, update, delete</tt>
     */
    private static final int UPDATE_QUERY = 1;

    /**
     * Provede <tt>select</tt> SQL pøíkaz. Pøes výsledný resultset se prochází a každý
     * øádek se pøedává k zpracování metodì <code>{@link #handleRow(ResultSet)}</code>.
     * V pøípadì, že vznikne {@link SQLException} je pøedána k zpracování metodì
     * <code>{@link #handleSQLException(SQLException, String, Object[])}</code>
     *
     * @param sql SQL pøíkaz (<em>nesmí být <code>null</code></em>)
     * @param params parametry pro SQL (<em>mùže být <code>null</code></em>)
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
     * Provede SQL pøíkaz typu <tt>insert, update, delete</tt> a vrátí poèet
     * modifikovaných øádek. V pøípadì, že vznikne {@link SQLException} je pøedána k zpracování metodì
     * <code>{@link #handleSQLException(SQLException, String, Object[])}</code>.
     *
     * @param sql SQL pøíkaz (<em>nesmí být <code>null</code></em>)
     * @param params parametry pro SQL (<em>mùže být <code>null</code></em>)
     *
     * @return poèet modifikovaných øádek
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
     * Realizuje vlastní vykonání pøíkazu
     * @param sql SQL pøíkaz
     * @param params parametr
     * @param queryType typ SQL  (insert, select, update ,delete)
     *
     * @return rows affected
     *
     * @throws SQLException v pøípadì, že nìjaká vznikne
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
     * Vrací databázové pøipojení, které se použije pro vykonání SQL pøíkazu.
     *
     * @return databázové pøipojení
     */
    protected abstract Connection getConnection() throws SQLException;

    /**
     * Metoda, ve které se implementuje vlastní zpracování øádku resultsetu.
     *
     * @param rs resultset
     * @throws SQLException v pøípadì, že nìjaká vznikne
     */
    protected abstract void handleRow(ResultSet rs) throws SQLException;

    /**
     * Slouží k nastavení parametrù pro SQL pøíkaz podle poøadí. Nastavení hodnot
     * <code>null</code> není podporováno.
     *
     * <h4><a name="supported-types">Podporované datové typy</a></h4>
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
     * @throws SQLException v pøípadì, že nìjaká vznikne
     * @throws UnsupportedOperationException pokud se jedná o parametr mimo <a href="#supported-types">podporované
     * datové typy</a>.
     */
    protected void setParams(PreparedStatement ps, Object params[]) throws SQLException {
        Object paramValue = null;
        for (int i = 0; i < params.length;) {
            paramValue = params[i];
            if (paramValue == null) {
                throw new UnsupportedOperationException("Nastavení hodnoty null není podporováno");
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
                throw new UnsupportedOperationException("Podpora datového typu " + paramValue.getClass() + " není prozatím implementována!");
            }
        }
    }

    /**
     * Umožòuje ošetøit vzniklou <code>{@link SQLException}</code>. Defaultnì je
     * implementována tak, že vzniklá výjimka je obalena do <code>{@link DatabaseException}</code>.
     * Potomkové mohou toto chování libovolnì pøepsat napø. výjimku zapsat do logu,
     * lokalizovat text výjímky apod.
     *
     * @param e vzniklá výjimka
     * @param sql SQL pøíkaz, pøi kterém vznikla výjímka
     * @param params parametry pro SQL
     */
    protected DatabaseException handleSQLException(SQLException e, String sql, Object params[]) {
        String message = "Vznikla neoèekávaná chyba bìhem databázové operace!";
        return new DatabaseException(message, e, sql, params);
    }

    /**
     * Provede bezpeèné uzavøení statemntu, resultsetu a databázového pøipojení.
     * @param con databázové pøipojení
     * @param ps statemnt
     * @param rs resultset
     * @throws SQLException v pøípadì, že nìjaká vznikne
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