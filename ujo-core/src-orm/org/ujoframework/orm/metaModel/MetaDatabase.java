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
import org.ujoframework.implementation.orm.RelationToMany;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.JdbcStatement;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.SqlDialect;
import org.ujoframework.orm.UjoSequencer;
import org.ujoframework.orm.annot.Db;

/**
 * A logical database description.
 * @author Pavel Ponec
 * @composed 1 - * MetaTable
 */
public class MetaDatabase extends AbstractMetaModel {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(MetaDatabase.class.toString());
    /** Add a DB relation into table models */
    private static final boolean ADD_DB_MODEL = true;
    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    /** The meta-model id */
    @XmlAttribute
    public static final UjoProperty<MetaDatabase,String> ID = newProperty("id", "", propertyCount++);
    /** MetaDatabase default schema */
    public static final UjoProperty<MetaDatabase,String> SCHEMA = newProperty("schema", "", propertyCount++);
    /** SQL dialect type of Class&lt;SqlDialect&gt; */
    public static final UjoProperty<MetaDatabase,Class> DIALECT = newProperty("dialect", Class.class, propertyCount++);
    /** List of tables */
    public static final ListProperty<MetaDatabase,MetaTable> TABLES = newPropertyList("table", MetaTable.class, propertyCount++);
    /** JDBC URL connection */
    public static final UjoProperty<MetaDatabase,String> JDBC_URL = newProperty("jdbcUrl", "", propertyCount++);
    /** JDBC Driver */
    public static final UjoProperty<MetaDatabase,String> JDBC_DRIVER = newProperty("jdbcDriver", "", propertyCount++);
    /** DB user */
    public static final UjoProperty<MetaDatabase,String> USER = newProperty("user", "", propertyCount++);
    /** DB password */
    @Transient
    public static final UjoProperty<MetaDatabase,String> PASSWORD = newProperty("password", "", propertyCount++);
    /** DB class root instance */
    @Transient
    public static final UjoProperty<MetaDatabase,OrmUjo> ROOT = newProperty("root", OrmUjo.class, propertyCount++);
    /** LDPA */
    public static final UjoProperty<MetaDatabase,String> LDAP = newProperty("ldap", "", propertyCount++);

    // --------------------

    private OrmHandler ormHandler;
    private SqlDialect dialect;
    private UjoSequencer sequencer;

    public MetaDatabase() {
    }

    public MetaDatabase(OrmHandler ormHandler, OrmUjo database, MetaDatabase param) {
        this.ormHandler = ormHandler;
        sequencer = new UjoSequencer(this);
        ROOT.setValue(this, database);

        if (param!=null) {
            changeDefault(this, SCHEMA  , SCHEMA.of(param));
            changeDefault(this, DIALECT , DIALECT.of(param));
            changeDefault(this, JDBC_URL, JDBC_URL.of(param));
            changeDefault(this, JDBC_DRIVER, JDBC_DRIVER.of(param));
            changeDefault(this, USER    , USER.of(param));
            changeDefault(this, PASSWORD, PASSWORD.of(param));
            changeDefault(this, LDAP    , LDAP.of(param));
        }

        Db annotDB = database.getClass().getAnnotation(Db.class);
        if (annotDB!=null) {
            changeDefault(this, SCHEMA  , annotDB.schema());
            changeDefault(this, DIALECT , annotDB.dialect());
            changeDefault(this, JDBC_URL, annotDB.jdbcUrl());
            changeDefault(this, JDBC_DRIVER, annotDB.jdbcDriver());
            changeDefault(this, USER    , annotDB.user());
            changeDefault(this, PASSWORD, annotDB.password());
            changeDefault(this, LDAP    , annotDB.ldap());
        }

        changeDefault(this, ID      , database.getClass().getSimpleName());
        changeDefault(this, SCHEMA  , database.getClass().getSimpleName());
        changeDefault(this, JDBC_URL, getDialect().getJdbcUrl());
        changeDefault(this, JDBC_DRIVER, getDialect().getJdbcDriver());

        for (UjoProperty tableProperty : database.readProperties()) {

            if (tableProperty instanceof RelationToMany) {
                RelationToMany tProperty = (RelationToMany) tableProperty;
                MetaTable par   = param!=null ? param.findTable(tProperty.getName()) : null;
                MetaTable table = new MetaTable(this, tProperty, par);
                TABLES.addItem(this, table);
            }
        }
        if (ADD_DB_MODEL) {
            // Add database relations:
            @SuppressWarnings("unchecked")
            RelationToMany relation = new RelationToMany(SCHEMA.of(this), database.getClass());
            MetaTable table = new MetaTable(this, relation, null);
            table.setNotPersistent();
            TABLES.addItem(this, table);
        }
    }


    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    /** Returns a SQL dialect for the current database. */
    public SqlDialect getDialect() {
        if (dialect==null) try {
            dialect = (SqlDialect) DIALECT.of(this).newInstance();
            dialect.setHandler(ormHandler);
        } catch (Exception e) {
            throw new IllegalStateException("Can't create an instance of " + dialect, e);
        }
        return dialect;
    }



    /** Change DbType by a Java property */
    public void changeDbType(MetaColumn column) {
       UjoProperty property = column.getProperty();

       Class type = property.getType();

        if (String.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.VARCHAR);
        }
        else if (Integer.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.INT);
        }
        else if (Long.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.BIGINT);
        }
        else if (BigInteger.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.BIGINT);
        }
        else if (Double.class==type || BigDecimal.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.DATE);
        }
        else if (Date.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (OrmUjo.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.INT);
        }
    }

    /** Change DbType by a Java property */
    public void changeDbLength(MetaColumn column) {

        switch (MetaColumn.DB_TYPE.of(column)) {
            case DECIMAL:
                changeDefault(column, MetaColumn.MAX_LENGTH, 8);
                changeDefault(column, MetaColumn.PRECISION, 2);
                break;
            case VARCHAR:
            case VARCHAR_IGNORECASE:
                changeDefault(column, MetaColumn.MAX_LENGTH, 128);
                break;
            default:
        }


       UjoProperty property = column.getProperty();

       Class type = property.getType();

        if (String.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.VARCHAR);
            changeDefault(column, MetaColumn.MAX_LENGTH, 128);
        }
        else if (Integer.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.INT);
            //changeDefault(column, MetaColumn.MAX_LENGTH, 8);
        }
        else if (Long.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.BIGINT);
            //changeDefault(column, MetaColumn.MAX_LENGTH, 16);
        }
        else if (BigInteger.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.BIGINT);
            changeDefault(column, MetaColumn.MAX_LENGTH, 16);
        }
        else if (Double.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, MetaColumn.MAX_LENGTH, 8);
            changeDefault(column, MetaColumn.PRECISION, 2);
        }
        else if (BigDecimal.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
            changeDefault(column, MetaColumn.MAX_LENGTH, 8);
            changeDefault(column, MetaColumn.PRECISION, 2);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.DATE);
        }
        else if (Date.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (OrmUjo.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.INT);
        }
    }


    /** Create DB */
    public void create() {
        Connection conn = ormHandler.getSession().getConnection(this);
        Statement stat = null;
        StringBuilder out = new StringBuilder(128);
        String sql = "";
        try {
            conn = createConnection();
            stat = conn.createStatement();

            // 1. Create schemas:
            for (String schema : getSchemas()) {
                out.setLength(0);
                sql = getDialect().printCreateSchema(schema, out).toString();
                if (isValid(sql)) {
                    stat.executeUpdate(sql);
                    LOGGER.info(sql);
                }
            }

            // 2. Create tables:
            for (MetaTable table : MetaDatabase.TABLES.getList(this)) {
                if (table.isTable()) {
                    out.setLength(0);
                    sql = getDialect().printTable(table, out).toString();
                    stat.executeUpdate(sql);
                    LOGGER.info(sql);
                }
            }

            // 3. Create Foreign Keys:
            for (MetaTable table : MetaDatabase.TABLES.getList(this)) {
                if (table.isTable()){
                    out.setLength(0);
                    sql = getDialect().printForeignKey(table, out).toString();
                    if (isValid(sql)) {
                        stat.executeUpdate(sql);
                        LOGGER.info(sql);
                    }
                }
            }

            // 4. Create SEQUENCE;
            if (true) {
                out.setLength(0);
                sql = getDialect().printCreateSequence(sequencer, out).toString();
                StringTokenizer st = new StringTokenizer(sql, ";");
                while (st.hasMoreTokens()) {
                    sql = st.nextToken().trim();
                    if (isValid(sql)) {
                        stat.executeUpdate(sql);
                        LOGGER.info(sql);
                    }
                }
            }
            // 5. ALTER Sequence INCREMENT;
            if (true) {
                out.setLength(0);
                sql = getDialect().printAlterSequenceIncrement(sequencer, out).toString();
                stat.executeUpdate(sql);
            }
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

    /** Return the OrmHandler parameters */
    public MetaParams getParams() {
        return ormHandler.getParameters();
    }



    /** Returns a SCHEMA of the MetaDatabase. */
    @Override
    public String toString() {
        return SCHEMA.of(this);
    }

    /** Create connection with auto-commit false. */
    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER.of(this));
        final Connection result = DriverManager.getConnection
            ( JDBC_URL.of(this)
            , USER.of(this)
            , PASSWORD.of(this)
            );
        result.setAutoCommit(false);
        return result;
    }

    /** Equals */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaDatabase) {
            MetaDatabase db = (MetaDatabase) obj;

            final String name1 = MetaDatabase.SCHEMA.of(this);
            final String name2 = MetaDatabase.SCHEMA.of(db);

            return name1.equals(name2);
        } else {
            return false;
        }
    }

    /** Hash code */
    @Override
    public int hashCode() {
        final String name = MetaDatabase.SCHEMA.of(this);
        return name.hashCode();
    }


    /** Returns a default handler session. */
    public Session getDefaultSession() {
        return ormHandler.getSession();
    }

    /** Get all table schemas. */
    public Set<String> getSchemas() {
        final Set<String> result = new HashSet<String>();
        for (MetaTable table : TABLES.of(this)) {
            if (table.isPersistent() && !table.isSelectModel()) {
                result.add(MetaTable.SCHEMA.of(table).toLowerCase());
            }
        }
        return result;
    }

    /** Finds the first table by ID or returns null.
     * The method is for internal use only.
     */
    MetaTable findTable(String id) {

        if (isValid(id)) for (MetaTable table : TABLES.getList(this)) {
            if (MetaTable.ID.equals(table, id)) {
                return table;
            }
        }
        return null;
    }
}
