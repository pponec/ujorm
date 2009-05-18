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
import java.util.HashSet;
import java.util.Set;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.JdbcStatement;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.SqlRenderer;
import org.ujoframework.orm.UjoSequencer;
import org.ujoframework.orm.annot.Db;

/**
 * A logical database description.
 * @author Pavel Ponec
 * @composed 1 - * OrmTable
 */
public class OrmDatabase extends AbstractMetaModel {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(OrmDatabase.class.toString());
    /** Add a DB relation into table models */
    private static final boolean ADD_DB_MODEL = true;
    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    /** OrmDatabase default schema */
    @XmlAttribute
    public static final UjoProperty<OrmDatabase,String> SCHEMA = newProperty("schema", "", propertyCount++);
    /** SQL renderer type of SqlRenderer. */
    public static final UjoProperty<OrmDatabase,Class> RENDERER = newProperty("renderer", Class.class, propertyCount++);
    /** List of tables */
    public static final ListProperty<OrmDatabase,OrmTable> TABLES = newPropertyList("table", OrmTable.class, propertyCount++);
    /** JDBC URL connection */
    public static final UjoProperty<OrmDatabase,String> JDBC_URL = newProperty("jdbcUrl", "", propertyCount++);
    /** DB user */
    public static final UjoProperty<OrmDatabase,String> USER = newProperty("user", "", propertyCount++);
    /** DB password */
    @Transient
    public static final UjoProperty<OrmDatabase,String> PASSWORD = newProperty("password", "", propertyCount++);
    /** DB class root instance */
    @Transient
    public static final UjoProperty<OrmDatabase,TableUjo> ROOT = newProperty("root", TableUjo.class, propertyCount++);
    /** LDPA */
    public static final UjoProperty<OrmDatabase,String> LDAP = newProperty("ldap", "", propertyCount++);

    // --------------------

    private OrmHandler ormHandler;
    private SqlRenderer renderer;
    private UjoSequencer sequencer;

    public OrmDatabase() {
    }

    public OrmDatabase(OrmHandler ormHandler, TableUjo database, OrmDatabase param) {
        this.ormHandler = ormHandler;
        sequencer = new UjoSequencer(this);
        ROOT.setValue(this, database);

        if (param!=null) {
            changeDefault(this, SCHEMA  , SCHEMA.of(param));
            changeDefault(this, RENDERER, RENDERER.of(param));
            changeDefault(this, JDBC_URL, JDBC_URL.of(param));
            changeDefault(this, USER    , USER.of(param));
            changeDefault(this, PASSWORD, PASSWORD.of(param));
            changeDefault(this, LDAP    , LDAP.of(param));
        }

        Db annotDB = database.getClass().getAnnotation(Db.class);
        if (annotDB!=null) {
            changeDefault(this, SCHEMA  , annotDB.schema());
            changeDefault(this, RENDERER, annotDB.renderer());
            changeDefault(this, JDBC_URL, annotDB.jdbcUrl());
            changeDefault(this, USER    , annotDB.user());
            changeDefault(this, PASSWORD, annotDB.password());
            changeDefault(this, LDAP    , annotDB.ldap());
        }

        changeDefault(this, SCHEMA  , database.getClass().getSimpleName());
        changeDefault(this, JDBC_URL, getRenderer().getJdbcUrl());

        for (UjoProperty tableProperty : database.readProperties()) {

            if (tableProperty instanceof RelationToMany) {
                RelationToMany tProperty = (RelationToMany) tableProperty;
                OrmTable par   = param.findTable(tProperty.getName());
                OrmTable table = new OrmTable(this, tProperty, par);
                TABLES.addItem(this, table);
            }
        }
        if (ADD_DB_MODEL) {
            // Add database relations:
            @SuppressWarnings("unchecked")
            RelationToMany relation = new RelationToMany(SCHEMA.of(this), database.getClass());
            OrmTable table = new OrmTable(this, relation, null);
            table.setNotPersistent();
            TABLES.addItem(this, table);
        }
    }


    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    /** Returns a SQL renderer for the current database. */
    public SqlRenderer getRenderer() {
        if (renderer==null) try {
            renderer = (SqlRenderer) RENDERER.of(this).newInstance();
            renderer.setHandler(ormHandler);
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


    /** Create DB */
    public void create() {
        Connection conn = ormHandler.getSession().getConnection(this);
        Statement stat = null;
        String sql = "";
        try {
            sql = getRenderer().printCreateDatabase(this, new StringBuilder(256)).toString();
            conn = createConnection();
            stat = conn.createStatement();
            stat.executeUpdate(sql);
            conn.commit();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info(sql);
            }

            // Create UJO-ORM sequence;
            sequencer.createSequence(conn);

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

    /** Returns common sequencer. */
    public UjoSequencer getSequencer() {
        return sequencer;
    }

    /** OrmHandler */
    public OrmHandler getOrmHandler() {
        return ormHandler;
    }



    /** Returns a SCHEMA of the OrmDatabase. */
    @Override
    public String toString() {
        return SCHEMA.of(this);
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

            final String name1 = OrmDatabase.SCHEMA.of(this);
            final String name2 = OrmDatabase.SCHEMA.of(db);

            return name1.equals(name2);
        } else {
            return false;
        }
    }

    /** Hash code */
    @Override
    public int hashCode() {
        final String name = OrmDatabase.SCHEMA.of(this);
        return name.hashCode();
    }


    /** Returns a default handler session. */
    public Session getDefaultSession() {
        return ormHandler.getSession();
    }

    /** Get all table schemas. */
    public Set<String> getSchemas() {
        final Set<String> result = new HashSet<String>();
        for (OrmTable table : TABLES.of(this)) {
            if (table.isPersistent() && !table.isSelectModel()) {
                result.add(OrmTable.SCHEMA.of(table).toLowerCase());
            }
        }
        return result;
    }

    /** Finds the first table by ID or returns null.
     * The method is for internal use only.
     */
    OrmTable findTable(String id) {

        if (isValid(id)) for (OrmTable table : TABLES.getList(this)) {
            if (OrmTable.ID.equals(table, id)) {
                return table;
            }
        }
        return null;
    }
}
