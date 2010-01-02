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

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.sql.DataSource;
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
import javax.naming.InitialContext;
import org.ujoframework.extensions.Property;
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
    private static final Class CLASS = MetaDatabase.class;

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(MetaDatabase.class.getName());
    /** Add a DB relation into table models */
    private static final boolean ADD_DB_MODEL = true;

    /** The meta-model id */
    @XmlAttribute
    public static final Property<MetaDatabase,String> ID = newProperty("id", "");
    /** MetaDatabase default schema */
    public static final Property<MetaDatabase,String> SCHEMA = newProperty("schema", "");
    /** SQL dialect type of Class&lt;SqlDialect&gt; */
    public static final Property<MetaDatabase,Class> DIALECT = newProperty("dialect", Class.class);
    /** List of tables */
    public static final ListProperty<MetaDatabase,MetaTable> TABLES = newListProperty("table", MetaTable.class);
    /** JDBC URL connection */
    public static final Property<MetaDatabase,String> JDBC_URL = newProperty("jdbcUrl", "");
    /** JDBC Driver */
    public static final Property<MetaDatabase,String> JDBC_DRIVER = newProperty("jdbcDriver", "");
    /** DB user */
    public static final Property<MetaDatabase,String> USER = newProperty("user", "");
    /** DB password */
    @Transient
    public static final Property<MetaDatabase,String> PASSWORD = newProperty("password", "");
    /** DB class root instance */
    @Transient
    public static final Property<MetaDatabase,OrmUjo> ROOT = newProperty("root", OrmUjo.class);
    /** <a href="http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface" target="_blank">JNDI</a>
     * (java naming and directory interface) connection string
     */
    public static final Property<MetaDatabase,String> JNDI = newProperty("jndi", "");
    /** The sequencer class for tables of the current database.
     * A value can be a subtype of 'org.ujoframework.orm.UjoSequencer' with one-parameter constructor type of MetaTable.
     * If the NULL value is specified the then a default sequencer 'UjoSequencer' will be used. */
    public static final Property<MetaDatabase,Class> SEQUENCER = newProperty("sequencer", Class.class).writeDefault(UjoSequencer.class);
    /** The property initialization */
    static{init(CLASS);}

    // --------------------

    private OrmHandler ormHandler;
    private SqlDialect dialect;
    private InitialContext initialContext;

    public MetaDatabase() {
    }

    public MetaDatabase(OrmHandler ormHandler, OrmUjo database, MetaDatabase param) {
        this.ormHandler = ormHandler;
        ROOT.setValue(this, database);

        if (param!=null) {
            changeDefault(this, SCHEMA  , SCHEMA.of(param));
            changeDefault(this, DIALECT , DIALECT.of(param));
            changeDefault(this, JDBC_URL, JDBC_URL.of(param));
            changeDefault(this, JDBC_DRIVER, JDBC_DRIVER.of(param));
            changeDefault(this, USER    , USER.of(param));
            changeDefault(this, PASSWORD, PASSWORD.of(param));
            changeDefault(this, JNDI    , JNDI.of(param));
            changeDefault(this, SEQUENCER, SEQUENCER.of(param));
        }

        Db annotDB = database.getClass().getAnnotation(Db.class);
        if (annotDB!=null) {
            changeDefault(this, SCHEMA  , annotDB.schema());
            changeDefault(this, DIALECT , annotDB.dialect());
            changeDefault(this, JDBC_URL, annotDB.jdbcUrl());
            changeDefault(this, JDBC_DRIVER, annotDB.jdbcDriver());
            changeDefault(this, USER    , annotDB.user());
            changeDefault(this, PASSWORD, annotDB.password());
            changeDefault(this, JNDI    , annotDB.jndi());
            changeDefault(this, SEQUENCER, annotDB.sequencer());
        }

        changeDefault(this, ID      , database.getClass().getSimpleName());
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
        else if (Short.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.SMALLINT);
        }
        else if (Long.class==type
        || BigInteger.class.isAssignableFrom(type)
        ){
            // Oracle: DbType.NUMBER
            MetaColumn.DB_TYPE.setValue(column, DbType.BIGINT);
        }
        else if (Double.class==type || BigDecimal.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.DECIMAL);
        }
        else if (java.sql.Date.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.DATE);
        }
        else if (java.util.Date.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.TIMESTAMP);
        }
        else if (Character.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.CHAR);
        }
        else if (Boolean.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.BOOLEAN);
        }
        else if (Enum.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.SMALLINT);
        }
        else if (OrmUjo.class.isAssignableFrom(type)) {
            // A later initialization !!!
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

    }


    /** Create DB */
    public void create(Session session) {
        Connection conn = session.getConnection(this);
        Statement stat = null;
        StringBuilder out = new StringBuilder(256);
        String sql = "";
        try {
            stat = conn.createStatement();

            // 0. Test for the presence of a SEQUENCE table:
            UjoSequencer seq = findFirstSequencer();
            if (seq!=null) {
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    sql = getDialect().printSequenceCurrentValue(seq, out).toString();
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, "-");
                    rs = ps.executeQuery();
                    LOGGER.info("Database structure is loaded: " + getId());
                    return; //
                } catch (SQLException e) {
                    LOGGER.info("Database structure is not loaded: " + getId());
                    conn.rollback();
                } finally {
                    close(null, ps, rs, false);
                }
            }

            // 1. Create schemas:
            for (String schema : getSchemas()) {
                out.setLength(0);
                sql = getDialect().printCreateSchema(schema, out).toString();
                if (isUsable(sql)) {
                    stat.executeUpdate(sql);
                    LOGGER.info(sql);
                }
            }

            // 2. Create tables:
            int tableCount = 0;
            for (MetaTable table : MetaDatabase.TABLES.getList(this)) {
                if (table.isTable()) {
                    tableCount++;
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
                    StringTokenizer st = new StringTokenizer(sql, ";");
                    while(st.hasMoreTokens()) {
                        sql = st.nextToken().trim();
                        if (isUsable(sql)) {
                            stat.executeUpdate(sql);
                            LOGGER.info(sql);
                        }
                    }
                }
            }

            // 4. Create SEQUENCE;
            if (tableCount>0) {
                out.setLength(0);
                sql = getDialect().printSequenceTable(this, out).toString();
                stat.executeUpdate(sql);
                LOGGER.info(sql);
            }

            conn.commit();

        } catch (Throwable e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Can't rollback DB" + getId(), ex);
            }
            throw new IllegalArgumentException(Session.SQL_ILLEGAL + sql, e);
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

    /** OrmHandler */
    public OrmHandler getOrmHandler() {
        return ormHandler;
    }

    /** Return the OrmHandler parameters */
    public MetaParams getParams() {
        return ormHandler.getParameters();
    }



    /** Returns an ID of the MetaDatabase. */
    public String getId() {
        return ID.of(this);
    }

    /** Create connection with auto-commit false. */
    public Connection createConnection() throws Exception {
        Connection result = dialect.createConnection(this);
        
        if (result==null) {
            String jndi = JNDI.of(this);
            if (isUsable(jndi)) {
                DataSource dataSource = (DataSource) getInitialContext().lookup(jndi);
                result = dataSource.getConnection();
            } else {
                Class.forName(JDBC_DRIVER.of(this));
                result = DriverManager.getConnection
                ( JDBC_URL.of(this)
                , USER.of(this)
                , PASSWORD.of(this)
                );
            }
        }
        result.setAutoCommit(false);
        return result;
    }

    /** Get or create an initial context */
    private InitialContext getInitialContext() throws NamingException {
        if (initialContext==null) {
            initialContext = new InitialContext();
        }
        return initialContext;
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

    /** Get all table schemas */
    public Set<String> getSchemas() {
        final Set<String> result = new HashSet<String>();
        for (MetaTable table : TABLES.of(this)) {
            if (table.isPersistent() && !table.isSelectModel()) {
                String schema = MetaTable.SCHEMA.of(table);
                if (isUsable(schema)) {
                   result.add(schema);
                }
            }
        }
        return result;
    }

    /** Finds the first table by ID or returns null.
     * The method is for internal use only.
     */
    MetaTable findTable(String id) {

        if (isUsable(id)) for (MetaTable table : TABLES.getList(this)) {
            if (MetaTable.ID.equals(table, id)) {
                return table;
            }
        }
        return null;
    }

    /** Find the first sequence of the database or returns null if no sequence was not found. */
    private UjoSequencer findFirstSequencer() {
        for (MetaTable table : TABLES.of(this)) {
            if (table.isTable()) {
                return table.getSequencer();
            }
        }
        return null;
    }

    /** Create a new sequencer for selected table */
    @SuppressWarnings("unchecked")
    protected UjoSequencer createSequencer(MetaTable table) {
        UjoSequencer result;
        Class seqClass = SEQUENCER.of(this);
        if (seqClass==UjoSequencer.class) {
            result = new UjoSequencer(table);
        } else try {
            Constructor<UjoSequencer> constr = seqClass.getConstructor(MetaTable.class);
            result = constr.newInstance(table);
        } catch (Exception e) {
            throw new IllegalStateException("Can't create sequencer for " + seqClass, e);
        }

        return result;
    }


}
