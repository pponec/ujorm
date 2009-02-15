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

package org.ujoframework.orm.metaModel;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.annot.Database;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.implementation.orm.RelationToMany;
import java.sql.*;

/**
 * A logical database description.
 * @author pavel
 */
public class Db extends AbstractMetaModel {
    
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(Db.class.toString());



    /** Database name */
    @XmlAttribute
    public static final UjoProperty<Db,String> NAME = newProperty("name", "");
    /** List of tables */
    public static final ListProperty<Db,DbTable> TABLES = newPropertyList("table", DbTable.class);
    /** JDBC URL connection */
    public static final UjoProperty<Db,String> JDBC_URL = newProperty("jdbcUrl", "");
    /** JDBC Class */
    public static final UjoProperty<Db,String> JDBC_CLASS = newProperty("jdbcClass", "");
    /** DB user */
    public static final UjoProperty<Db,String> USER = newProperty("user", "");
    /** DB password */
    @Transient
    public static final UjoProperty<Db,String> PASSWORD = newProperty("password", "");
    /** DB class root instance */
    @Transient
    public static final UjoProperty<Db,TableUjo> ROOT = newProperty("root", TableUjo.class);
    /** LDPA */
    public static final UjoProperty<Db,String> LDAP = newProperty("ldap", "");

    public Db(TableUjo database) {
        ROOT.setValue(this, database);

        Database annotDB = database.getClass().getAnnotation(Database.class);
        if (annotDB!=null) {
            NAME.setValue(this, annotDB.name());
            JDBC_URL.setValue(this, annotDB.jdbcUrl());
            JDBC_CLASS.setValue(this, annotDB.jdbcClass());
            USER.setValue(this, annotDB.user());
            PASSWORD.setValue(this, annotDB.password());
            LDAP.setValue(this, annotDB.ldap());
        }
        if (NAME.isDefault(this)) {
            NAME.setValue(this, database.getClass().getSimpleName());
        }


        for (UjoProperty tableProperty : database.readProperties()) {

            if (tableProperty instanceof RelationToMany) {
                RelationToMany tProperty = (RelationToMany) tableProperty;

                DbTable table = new DbTable(this, tProperty);
                TABLES.addItem(this, table);
            }
        }

    }

    /** Change DbType by a Java property */
    public void changeDbType(DbColumn column) {
       UjoProperty property = DbColumn.PROPERTY.of(column);

       Class type = property.getType();

        if (String.class==type) {
            DbColumn.TYPE.setValue(column, DbType.VARCHAR);
            changeDefault(column, DbColumn.MAX_LENGTH, 128);
        }
        else if (Integer.class==type) {
            DbColumn.TYPE.setValue(column, DbType.INT);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
        }
        else if (BigInteger.class.isAssignableFrom(type)) {
            DbColumn.TYPE.setValue(column, DbType.BIGINT);
            changeDefault(column, DbColumn.MAX_LENGTH, 16);
        }
        else if (Double.class==type) {
            DbColumn.TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
            changeDefault(column, DbColumn.PRECISION, 2);
        }
        else if (BigDecimal.class==type) {
            DbColumn.TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, DbColumn.MAX_LENGTH, 8);
            changeDefault(column, DbColumn.PRECISION, 2);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            DbColumn.TYPE.setValue(column, DbType.DATE);
        }
        else if (Date.class.isAssignableFrom(type)) {
            DbColumn.TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (TableUjo.class.isAssignableFrom(type)) {
            DbColumn.TYPE.setValue(column, DbType.INT);
        }
    }

    /** Vytvoøí DB */
    public void create() {
        Connection conn = null;
        Statement stat = null;
        String sql = null;
        try {
            sql  = createSql();
            conn = createConnection();
            stat = conn.createStatement();
            stat.executeUpdate(sql);
            conn.commit();

        } catch (Throwable e) {
            if (conn!=null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Can't rollback DB" + toString(), ex);
                }
            }
            throw new IllegalArgumentException("Statement error:\n" + sql, e);
        } finally {
            try {
                close(conn, stat, null);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Can't rollback DB" + toString(), ex);
            }
        }
    }

    /** Private SQL statement */
    public String createSql() throws IOException {
        StringBuilder result = new StringBuilder(256);

        for (DbTable table : TABLES.getList(this)) {
            result.append("CREATE TABLE ");
            result.append(DbTable.NAME.of(table));
            String separator = "\n( ";
            for (DbColumn column : DbTable.COLUMNS.getList(table)) {
                result.append(separator);
                separator = "\n, ";
                column.printColumn(result);
            }
            result.append("}\n");
        }
        result.toString();
        return result.toString();
    }

    /** Close a connection, statement and a result set. */
    private void close(Connection connection, Statement statement, ResultSet rs) throws SQLException {
        try {
            if (rs != null) {
                rs.close();
            }
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }

    /** Name of Database. */
    @Override
    public String toString() {
        return NAME.of(this);
    }

    /** Create connection */
    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_CLASS.of(this));
        final Connection result = DriverManager.getConnection
            ( JDBC_URL.of(this)
            , USER.of(this)
            , PASSWORD.of(this)
            );
        return result;
    }


}
