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
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.implementation.orm.RelationToMany;
import java.sql.*;
import java.util.List;
import org.ujoframework.orm.ExpressionDecoder;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.JdbcStatement;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.SqlRenderer;
import org.ujoframework.orm.annot.Db;

/**
 * A logical database description.
 * @author pavel
 */
public class OrmDatabase extends AbstractMetaModel {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(OrmDatabase.class.toString());
    /** Add a DB relation into table models */
    private static final boolean ADD_DB_MODEL = true;


    /** OrmDatabase name */
    @XmlAttribute
    public static final UjoProperty<OrmDatabase,String> NAME = newProperty("name", "");
    /**  SQL renderer type of SqlRenderer. */
    public static final UjoProperty<OrmDatabase,Class> RENDERER = newProperty("renderer", Class.class);
    /** List of tables */
    public static final ListProperty<OrmDatabase,OrmTable> TABLES = newPropertyList("table", OrmTable.class);
    /** JDBC URL connection */
    public static final UjoProperty<OrmDatabase,String> JDBC_URL = newProperty("jdbcUrl", "");
    /** DB user */
    public static final UjoProperty<OrmDatabase,String> USER = newProperty("user", "");
    /** DB password */
    @Transient
    public static final UjoProperty<OrmDatabase,String> PASSWORD = newProperty("password", "");
    /** DB class root instance */
    @Transient
    public static final UjoProperty<OrmDatabase,TableUjo> ROOT = newProperty("root", TableUjo.class);
    /** LDPA */
    public static final UjoProperty<OrmDatabase,String> LDAP = newProperty("ldap", "");

    // --------------------

    private SqlRenderer renderer;

    public OrmDatabase(TableUjo database) {
        ROOT.setValue(this, database);

        Db annotDB = database.getClass().getAnnotation(Db.class);
        if (annotDB!=null) {
            NAME.setValue(this, annotDB.name());
            RENDERER.setValue(this, annotDB.renderer());
            JDBC_URL.setValue(this, annotDB.jdbcUrl());
            USER.setValue(this, annotDB.user());
            PASSWORD.setValue(this, annotDB.password());
            LDAP.setValue(this, annotDB.ldap());
        }
        if (NAME.isDefault(this)) {
            NAME.setValue(this, database.getClass().getSimpleName());
        }
        if (JDBC_URL.isDefault(this)) {
            JDBC_URL.setValue(this, getRenderer().getJdbcUrl());
        }


        for (UjoProperty tableProperty : database.readProperties()) {

            if (tableProperty instanceof RelationToMany) {
                RelationToMany tProperty = (RelationToMany) tableProperty;

                OrmTable table = new OrmTable(this, tProperty);
                TABLES.addItem(this, table);
            }
        }
        if (ADD_DB_MODEL) {
            // Add database relations:
            @SuppressWarnings("unchecked")
            RelationToMany relation = new RelationToMany(NAME.of(this), database.getClass());
            OrmTable table = new OrmTable(this, relation);
            table.setNotPersistent();
            TABLES.addItem(this, table);
        }
    }

    /** Create an SQL insert */
    public String createInsert(TableUjo ujo) {
        SqlRenderer renderer = getRenderer();
        StringBuilder result = new StringBuilder();
        try {
            renderer.printInsert(ujo, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }


    /** Create an SQL update */
    public String createUpdate(TableUjo ujo, List<OrmColumn> changedColumns) {




        SqlRenderer renderer = getRenderer();
        StringBuilder result = new StringBuilder();
        try {
            renderer.printUpdate(ujo, changedColumns, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    /** Create an SQL select */
    public ExpressionDecoder createSelect(Query query, Appendable result ) {
        SqlRenderer renderer = getRenderer();
        try {
            final ExpressionDecoder decoder = renderer.printSelect(query, result);
            return decoder;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns an SQL renderer. */
    public SqlRenderer getRenderer() {
        if (renderer==null) try {
            renderer = (SqlRenderer) RENDERER.of(this).newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Can't create an instance of " + renderer, e);
        }
        return renderer;
    }



    /** Change DbType by a Java property */
    public void changeDbType(OrmColumn column) {
       UjoProperty property = column.getProperty();

       Class type = property.getType();

        if (String.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.VARCHAR);
        }
        else if (Integer.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.INT);
        }
        else if (Long.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.BIGINT);
        }
        else if (BigInteger.class.isAssignableFrom(type)) {
            OrmColumn.DB_TYPE.setValue(column, DbType.BIGINT);
        }
        else if (Double.class==type || BigDecimal.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            OrmColumn.DB_TYPE.setValue(column, DbType.DATE);
        }
        else if (Date.class.isAssignableFrom(type)) {
            OrmColumn.DB_TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (TableUjo.class.isAssignableFrom(type)) {
            OrmColumn.DB_TYPE.setValue(column, DbType.INT);
        }
    }

    /** Change DbType by a Java property */
    public void changeDbLength(OrmColumn column) {

        switch (OrmColumn.DB_TYPE.of(column)) {
            case DECIMAL:
                changeDefault(column, OrmColumn.MAX_LENGTH, 8);
                changeDefault(column, OrmColumn.PRECISION, 2);
                break;
            case VARCHAR:
            case VARCHAR_IGNORECASE:
                changeDefault(column, OrmColumn.MAX_LENGTH, 128);
                break;
            default:
        }


       UjoProperty property = column.getProperty();

       Class type = property.getType();

        if (String.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.VARCHAR);
            changeDefault(column, OrmColumn.MAX_LENGTH, 128);
        }
        else if (Integer.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.INT);
            changeDefault(column, OrmColumn.MAX_LENGTH, 8);
        }
        else if (Long.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.BIGINT);
            //changeDefault(column, OrmColumn.MAX_LENGTH, 16);
        }
        else if (BigInteger.class.isAssignableFrom(type)) {
            OrmColumn.DB_TYPE.setValue(column, DbType.BIGINT);
            changeDefault(column, OrmColumn.MAX_LENGTH, 16);
        }
        else if (Double.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, OrmColumn.MAX_LENGTH, 8);
            changeDefault(column, OrmColumn.PRECISION, 2);
        }
        else if (BigDecimal.class==type) {
            OrmColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, OrmColumn.MAX_LENGTH, 8);
            changeDefault(column, OrmColumn.PRECISION, 2);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            OrmColumn.DB_TYPE.setValue(column, DbType.DATE);
        }
        else if (Date.class.isAssignableFrom(type)) {
            OrmColumn.DB_TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (TableUjo.class.isAssignableFrom(type)) {
            OrmColumn.DB_TYPE.setValue(column, DbType.INT);
        }
    }


    /** Vytvoøí DB */
    public void create() {
        Connection conn = OrmHandler.getInstance().getSession().getConnection(this);
        Statement stat = null;
        StringBuilder sql = new StringBuilder(256);
        try {
            getRenderer().printCreateDatabase(this, sql);
            conn = createConnection();
            stat = conn.createStatement();
            stat.executeUpdate(sql.toString());
            conn.commit();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(sql.toString());
            }

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
                close(conn, stat, null, true);
            } catch (IllegalStateException ex) {
                LOGGER.log(Level.WARNING, "Can't rollback DB" + toString(), ex);
            }
        }
    }

    /** Close a connection, statement and a result set. */
    public static void close(Connection connection, JdbcStatement statement, ResultSet rs, boolean throwExcepton) throws IllegalStateException {

        try {

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
        } catch (Throwable e) {
            String msg = "Can't close a SQL object";
            if (throwExcepton) {
                throw new IllegalStateException(msg, e);
            } else {
                LOGGER.log(Level.SEVERE, msg, e);
            }
        }
    }

    /** Close a connection, statement and a result set. */
    public static void close(Connection connection, Statement statement, ResultSet rs, boolean throwExcepton) throws IllegalStateException {

        try {
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
        } catch (Throwable e) {
            String msg = "Can't close a SQL object";
            if (throwExcepton) {
                throw new IllegalStateException(msg, e);
            } else {
                LOGGER.log(Level.SEVERE, msg, e);
            }
        }
    }


    /** Returns a NAME of the OrmDatabase. */
    @Override
    public String toString() {
        return NAME.of(this);
    }

    /** Create connection */
    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName(getRenderer().getJdbcDriver());
        final Connection result = DriverManager.getConnection
            ( JDBC_URL.of(this)
            , USER.of(this)
            , PASSWORD.of(this)
            );
        return result;
    }

    /** Equals */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OrmDatabase) {
            OrmDatabase db = (OrmDatabase) obj;

            final String name1 = OrmDatabase.NAME.of(this);
            final String name2 = OrmDatabase.NAME.of(db);

            return name1.equals(name2);
        } else {
            return false;
        }
    }

    /** Hash code */
    @Override
    public int hashCode() {
        final String name = OrmDatabase.NAME.of(this);
        return name.hashCode();
    }



}
