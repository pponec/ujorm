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
 * Abstraktní předek pro JDBC operace (podporuje <tt>select, insert, update a delete</tt>), který
 * umožňuje efektivně pracovat resultset. Díky tomu se nekontaminuje klientský kód
 * často se opakujícím JDBC kódem a nemusí se explicitně starat o zpracování
 * <code>{@link java.sql.SQLException}</code>.
 * <p>
 * Potomkové musí přepsat metodu <code>{@link #handleRow(ResultSet)}</code> pokud
 * chtějí zpracovat resultset (platí pro <tt>select</tt>) a <code>{@link #getConnection()}</code>,
 * kterou budou poskytovat databázové připojení.</p>
 *
 * @author    Roman "Dagi" Pichlík
 */
public abstract class Z$_XXX {

    /**
     * Konstanta označující SQL příkaz <tt>select</tt>
     */
    private static final int SELECT_QUERY = 0;
    /**
     * Konstanta označující <tt>insert, update, delete</tt>
     */
    private static final int UPDATE_QUERY = 1;

    /**
     * Provede <tt>select</tt> SQL příkaz. Přes výsledný resultset se prochází a každý
     * řádek se předává k zpracování metodě <code>{@link #handleRow(ResultSet)}</code>.
     * V případě, že vznikne {@link SQLException} je předána k zpracování metodě
     * <code>{@link #handleSQLException(SQLException, String, Object[])}</code>
     *
     * @param sql SQL příkaz (<em>nesmí být <code>null</code></em>)
     * @param params parametry pro SQL (<em>může být <code>null</code></em>)
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
     * Provede SQL příkaz typu <tt>insert, update, delete</tt> a vrátí počet
     * modifikovaných řádek. V případě, že vznikne {@link SQLException} je předána k zpracování metodě
     * <code>{@link #handleSQLException(SQLException, String, Object[])}</code>.
     *
     * @param sql SQL příkaz (<em>nesmí být <code>null</code></em>)
     * @param params parametry pro SQL (<em>může být <code>null</code></em>)
     *
     * @return počet modifikovaných řádek
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
     * Realizuje vlastní vykonání příkazu
     * @param sql SQL příkaz
     * @param params parametr
     * @param queryType typ SQL  (insert, select, update ,delete)
     *
     * @return rows affected
     *
     * @throws SQLException v případě, že nějaká vznikne
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
     * Vrací databázové připojení, které se použije pro vykonání SQL příkazu.
     *
     * @return databázové připojení
     */
    protected abstract Connection getConnection() throws SQLException;

    /**
     * Metoda, ve které se implementuje vlastní zpracování řádku resultsetu.
     *
     * @param rs resultset
     * @throws SQLException v případě, že nějaká vznikne
     */
    protected abstract void handleRow(ResultSet rs) throws SQLException;

    /**
     * Slouží k nastavení parametrů pro SQL příkaz podle pořadí. Nastavení hodnot
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
     * @throws SQLException v případě, že nějaká vznikne
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
     * Umožňuje ošetřit vzniklou <code>{@link SQLException}</code>. Defaultně je 
     * implementována tak, že vzniklá výjimka je obalena do <code>{@link DatabaseException}</code>.
     * Potomkové mohou toto chování libovolně přepsat např. výjimku zapsat do logu,
     * lokalizovat text výjímky apod.
     *
     * @param e vzniklá výjimka
     * @param sql SQL příkaz, při kterém vznikla výjímka
     * @param params parametry pro SQL
     */
    protected DatabaseException handleSQLException(SQLException e, String sql, Object params[]) {
        String message = "Vznikla neočekávaná chyba během databázové operace!";
        return new DatabaseException(message, e, sql, params);
    }

    /**
     * Provede bezpečné uzavření statemntu, resultsetu a databázového připojení.
     * @param con databázové připojení
     * @param ps statemnt
     * @param rs resultset
     * @throws SQLException v případě, že nějaká vznikne
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