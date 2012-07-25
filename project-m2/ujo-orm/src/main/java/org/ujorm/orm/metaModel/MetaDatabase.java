/*
 *  Copyright 2009-2010 Pavel Ponec
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

package org.ujorm.orm.metaModel;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.logging.Level;
import org.ujorm.logger.UjoLogger;
import javax.sql.DataSource;
import org.ujorm.UjoAction;
import org.ujorm.Key;
import org.ujorm.core.annot.Transient;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.DbType;
import org.ujorm.implementation.orm.RelationToMany;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.InitialContext;
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.Immutable;
import org.ujorm.extensions.StringWrapper;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.DbProcedure;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.JdbcStatement;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Session;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.BytesWrapper;
import org.ujorm.orm.UjoSequencer;
import org.ujorm.orm.annot.Db;
import org.ujorm.orm.ao.Orm2ddlPolicy;
import org.ujorm.orm.ao.UjoStatement;
import org.ujorm.orm.dialect.MySqlDialect;

/**
 * A logical database description.
 * @author Pavel Ponec
 * @composed 1 - * MetaTable
 * @composed 1 - * MetaProcedure
 */
@Immutable
final public class MetaDatabase extends AbstractMetaModel implements Comparable<MetaDatabase> {
    private static final Class CLASS = MetaDatabase.class;

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(MetaDatabase.class);
    /** Add a DB relation into table models. The {@code true} value allows to use a property RelationToMany of Database model 
     * by the same way as a RelationToMany on any persistent table. A sample of the use:
     * <pre class="pre">
     * for (Order order: database.get(Database.ORDER)) {
     *     // use the order instance
     * }
     * </pre>
     */
    private static final boolean ADD_DB_MODEL = true;

    /** Property Factory */
    private static final KeyFactory<MetaDatabase> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** The meta-model id */
    @XmlAttribute
    public static final Key<MetaDatabase,String> ID = fa.newKey("id", "");
    /** MetaDatabase default schema */
    public static final Key<MetaDatabase,String> SCHEMA = fa.newKey("schema", "");
    /** The default state read-only for the database. */
    public static final Key<MetaDatabase,Boolean> READ_ONLY = fa.newKey("readOnly", false);
    /** SQL dialect type of Class&lt;SqlDialect&gt; */
    public static final Key<MetaDatabase,Class<? extends SqlDialect>> DIALECT = fa.newKey("dialect");
    /** JDBC URL connection */
    public static final Key<MetaDatabase,String> JDBC_URL = fa.newKey("jdbcUrl", "");
    /** JDBC Driver */
    public static final Key<MetaDatabase,String> JDBC_DRIVER = fa.newKey("jdbcDriver", "");
    /** DB user */
    public static final Key<MetaDatabase,String> USER = fa.newKey("user", "");
    /** DB password of the user */
    public static final Key<MetaDatabase,String> PASSWORD = fa.newKey("password", "");
    /** <a href="http://en.wikipedia.org/wiki/Java_Naming_and_Directory_Interface" target="_blank">JNDI</a>
     * (java naming and directory interface) connection string
     */
    public static final ListKey<MetaDatabase,String> JNDI = fa.newListProperty("jndi");
    /** The sequencer class for tables of the current database.
     * A value can be a subtype of 'org.ujorm.orm.UjoSequencer' with one-parameter constructor type of MetaTable.
     * If the NULL value is specified the then a default sequencer 'UjoSequencer' will be used. */
    public static final Key<MetaDatabase,Class<? extends UjoSequencer>> SEQUENCER = fa.newClassKey("sequencer", UjoSequencer.class);
    /** A policy to defining the database structure by a DDL.
     * @see Orm2ddlPolicy Parameter values
     */
    public static final Key<MetaDatabase,Orm2ddlPolicy> ORM2DLL_POLICY = fa.newKey("orm2ddlPolicy", Orm2ddlPolicy.INHERITED);
    /** List of tables */
    public static final ListKey<MetaDatabase,MetaTable> TABLES = fa.newListProperty("table");
    /** List of procedures */
    public static final ListKey<MetaDatabase,MetaProcedure> PROCEDURES = fa.newListProperty("procedure");
    /** Database order number */
    @Transient
    public static final Key<MetaDatabase,Integer> ORDER = fa.newKey("order", 0);
    /** An instance of the DB class. */
    @Transient
    public static final Key<MetaDatabase,OrmUjo> ROOT = fa.newKey("root");

    /** The property initialization */
    static{fa.lock();}

    // --------------------

    private OrmHandler ormHandler;
    private SqlDialect dialect;

    public MetaDatabase() {
    }

    /**
     * Create a new Database.
     * @param ormHandler ORM handler
     * @param database Database instance
     * @param param Configuration data from a XML file
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MetaDatabase(OrmHandler ormHandler, OrmUjo database, MetaDatabase param, Integer order) {
        this.ormHandler = ormHandler;
        ROOT.setValue(this, database);
        ORDER.setValue(this, order);

        if (param!=null) {
            changeDefault(this, SCHEMA  , SCHEMA.of(param));
            changeDefault(this, READ_ONLY, READ_ONLY.of(param));
            changeDefault(this, ORM2DLL_POLICY, ORM2DLL_POLICY.of(param));
            changeDefault(this, DIALECT , DIALECT.of(param));
            changeDefault(this, JDBC_URL, JDBC_URL.of(param));
            changeDefault(this, JDBC_DRIVER, JDBC_DRIVER.of(param));
            changeDefault(this, USER    , USER.of(param));
            changeDefault(this, PASSWORD, PASSWORD.of(param));
            changeDefault(this, JNDI    , JNDI.of(param));
            changeDefault(this, SEQUENCER,SEQUENCER.of(param));
        }

        Db annotDB = database.getClass().getAnnotation(Db.class);
        if (annotDB!=null) {
            changeDefault(this, SCHEMA  , annotDB.schema());
            changeDefault(this, READ_ONLY, annotDB.readOnly());
            changeDefault(this, ORM2DLL_POLICY, annotDB.Orm2ddlPolicy());
            changeDefault(this, DIALECT , annotDB.dialect());
            changeDefault(this, JDBC_URL, annotDB.jdbcUrl());
            changeDefault(this, JDBC_DRIVER, annotDB.jdbcDriver());
            changeDefault(this, USER    , annotDB.user());
            changeDefault(this, PASSWORD, annotDB.password());
            changeDefault(this, JNDI    , Arrays.asList(annotDB.jndi()));
            changeDefault(this, SEQUENCER, annotDB.sequencer());
        }

        changeDefault(this, ID      , database.getClass().getSimpleName());
        changeDefault(this, JDBC_URL, getDialect().getJdbcUrl());
        changeDefault(this, JDBC_DRIVER, getDialect().getJdbcDriver());
        changeDefault(this, ORM2DLL_POLICY, MetaParams.ORM2DLL_POLICY.of(getParams()));
        changeDefault(this, ORM2DLL_POLICY, MetaParams.ORM2DLL_POLICY.getDefault());

        for (Key tableProperty : database.readProperties()) {

            if (tableProperty instanceof RelationToMany) {
                RelationToMany tProperty = (RelationToMany) tableProperty;
                MetaTable par   = param!=null ? param.findTable(tProperty.getName()) : null;
                MetaTable table = new MetaTable(this, tProperty, par);
                TABLES.addItem(this, table);
                ormHandler.addTableModel(table);
            }
            else if (tableProperty.isTypeOf(DbProcedure.class)) {
                Key tProcedure = tableProperty;
                MetaProcedure par = param!=null ? param.findProcedure(tProcedure.getName()) : null;
                MetaProcedure procedure = new MetaProcedure(this, tProcedure, par);
                PROCEDURES.addItem(this, procedure);
                ormHandler.addProcedureModel(procedure);
            }
        }
        if (ADD_DB_MODEL) {
            // Add database relations:
            @SuppressWarnings("unchecked")
            RelationToMany relation = new RelationToMany(SCHEMA.of(this), database.getClass());
            MetaTable table = new MetaTable(this, relation, null);
            table.setNotPersistent();
            TABLES.addItem(this, table);
            ormHandler.addTableModel(table);
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
        final Class type = column.getDbTypeClass();

        if (Void.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.NULL);
        }
        else if (String.class==type
              || StringWrapper.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.VARCHAR);
        }
        else if (Integer.class==type || Color.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.INT);
        }
        else if (Short.class==type) {
            MetaColumn.DB_TYPE.setValue(column, DbType.SMALLINT);
        }
        else if (Long.class==type
        || BigInteger.class.isAssignableFrom(type)
        ){
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
        else if (Byte.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.CHAR);
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
        else if (Blob.class.isAssignableFrom(type)
        || BytesWrapper.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.BLOB);
        }
        else if (Clob.class.isAssignableFrom(type)) {
            MetaColumn.DB_TYPE.setValue(column, DbType.CLOB);
        }
        else if (OrmUjo.class.isAssignableFrom(type)) {
            // Make a later initialization!
        }
    }

    /** Change DbType by a Java property */
    public void changeDbLength(final MetaColumn column) {

        switch (MetaColumn.DB_TYPE.of(column)) {
            case DECIMAL:
                changeDefault(column, MetaColumn.MAX_LENGTH, 8);
                changeDefault(column, MetaColumn.PRECISION, 2);
                break;
            case VARCHAR:
            case VARCHAR_IGNORECASE:
                if (MetaColumn.MAX_LENGTH.isDefault(column)) {
                    final boolean isEnum = column.getType().isEnum();
                    MetaColumn.MAX_LENGTH.setValue(column, isEnum ? maxEnumLenght4Db(column) : 128);
                }
                break;
            default:
        }
    }

    /** Calculate database VARCHAR lenght for required column. Minimal lenght is 1 character */
    private int maxEnumLenght4Db(final MetaColumn column) throws IllegalArgumentException {
        try {
            int maxLenght = 1;
            final UjoStatement statement = new UjoStatement();
            for (Object enumValue : column.getType().getEnumConstants()) {
                column.getConverter().setValue(column, statement, enumValue, 1);
                final Object value = statement.getValue();
                if (value instanceof String) {
                    maxLenght = Math.max(maxLenght, ((String)value).length());
                }
            }
            return maxLenght;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Create table and column comments. An error in this method does not affect the rest of all transaction.  */
    private void createTableComments(List<MetaTable> cTables, Statement stat, StringBuilder out) {
        try {
            for (MetaTable table : cTables) {
                switch (MetaTable.ORM2DLL_POLICY.of(table)) {
                    case CREATE_DDL:
                    case CREATE_OR_UPDATE_DDL:
                        if (table.isTable()) {
                            if (table.isCommented()) {
                                out.setLength(0);
                                Appendable sql = getDialect().printComment(table, out);
                                if (sql.toString().length() > 0) {
                                    executeUpdate(sql, stat, table);
                                }
                            }
                            for (MetaColumn column : MetaTable.COLUMNS.of(table)) {
                                if (column.isCommented()) {
                                    out.setLength(0);
                                    Appendable sql = getDialect().printComment(column, out);
                                    if (sql.toString().length() > 0) {
                                        executeUpdate(sql, stat, table);
                                    }
                                }
                            }
                        }
                    default:
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error on table comment: {0}", out);
        }
    }

    /** Returns a native database identifirer. */
    private String dbIdentifier(final String name, final DatabaseMetaData dmd) throws SQLException {
        if (dmd.storesUpperCaseIdentifiers()) {
            return name.toUpperCase();
        }
        if (dmd.storesLowerCaseIdentifiers()) {
            return name.toLowerCase();
        }
        return name;
    }

    /** Returns a full count of the database tables (views are excluded) */
    private int getTableTotalCount() {
        int tableCount = 0;
        for (MetaTable metaTable : TABLES.getList(this)) {
            if (metaTable.isTable()) {
                ++tableCount;
            }
        }
        return tableCount;
    }

    /** Find database table or columns to modify.
     * @param conn Database connection
     * @param newTables Output parameter
     * @param newColumns Output parameter
     */
    @SuppressWarnings("LoggerStringConcat")
    private boolean isModelChanged(Connection conn
        , List<MetaTable>  newTables
        , List<MetaColumn> newColumns
        , List<MetaIndex>  newIndexes
        ) throws SQLException {
        newTables.clear();
        newColumns.clear();
        newIndexes.clear();

        final DatabaseMetaData dmd = conn.getMetaData();
        final boolean catalog = getDialect() instanceof MySqlDialect;
        final String column = null;

        for (MetaTable table : TABLES.of(this)) {
            if (table.isTable()) {

                // ---------- CHECK TABLE COLUMNS ----------

                final Set<String> items = new HashSet<String>(32);
                final String schema = dbIdentifier(MetaTable.SCHEMA.of(table),dmd);
                ResultSet rs = dmd.getColumns
                    ( catalog ? schema : null
                    , catalog ? null  : schema
                    , dbIdentifier(MetaTable.NAME.of(table),dmd)
                    , column
                    );
                while(rs.next()) {
                    items.add(rs.getString("COLUMN_NAME").toUpperCase());
                    if (false && LOGGER.isLoggable(Level.INFO)) {
                        // Debug message:
                        String msg = "DB column: "
                                   + rs.getString("TABLE_CAT") + "."
                                   + rs.getString("TABLE_SCHEM") + "."
                                   + rs.getString("TABLE_NAME") + "."
                                   + rs.getString("COLUMN_NAME")
                                   ;
                        LOGGER.log(Level.INFO, msg);
                    }
                }
                rs.close();

                boolean tableExists = items.size()>0;
                if (tableExists) {
                    // create columns:
                    for (MetaColumn mc : MetaTable.COLUMNS.of(table)) {

                        boolean exists = items.contains(MetaColumn.NAME.of(mc).toUpperCase());
                        if (!exists) {
                            LOGGER.log(Level.INFO, "New DB column: " + mc.getFullName());
                            newColumns.add(mc);
                        }
                    }
                } else {
                    LOGGER.log(Level.INFO, "New DB table: " + MetaTable.NAME.of(table));
                    newTables.add(table);
                }

                // ---------- CHECK INDEXES ----------

                items.clear();
                if (tableExists) {
                    rs = dmd.getIndexInfo
                    ( catalog ? schema : null
                    , catalog ? null : schema
                    , dbIdentifier(MetaTable.NAME.of(table),dmd)
                    , false // unique
                    , false // approximate
                    );
                    while(rs.next()) {
                        String name = rs.getString("INDEX_NAME");
                        if (name!=null) {
                           items.add(name.toUpperCase());
                        }
                    }
                    rs.close();
                }
                for (MetaIndex index : table.getIndexCollection()) {
                    boolean exists = items.contains(MetaIndex.NAME.of(index).toUpperCase());
                    if (!exists) {
                        LOGGER.log(Level.INFO, "New DB index: " + index);
                        newIndexes.add(index);
                    }
                }
            }
        }

        boolean result = !newTables.isEmpty()
                      || !newColumns.isEmpty()
                      || !newIndexes.isEmpty()
                       ;
        return result;
    }

    /** Create DB */
    public void create(Session session) {
        Connection conn = session.getConnection(this, true);
        Statement stat = null;
        StringBuilder out = new StringBuilder(256);
        Appendable sql = out;
        List<MetaTable> tables = new ArrayList<MetaTable>();
        List<MetaColumn> newColumns = new ArrayList<MetaColumn>();
        List<MetaColumn> foreignColumns = new ArrayList<MetaColumn>();
        List<MetaIndex> indexes = new ArrayList<MetaIndex>();
        boolean createSequenceTable = false;
        int tableTotalCount = getTableTotalCount();
        boolean anyChange = false;

        try {
            stat = conn.createStatement();

            if (isSequenceTableRequired()) {
                PreparedStatement ps = null;
                ResultSet rs = null;
                Throwable exception = null;
                String logMsg = "";

                try {
                    sql = getDialect().printSequenceCurrentValue(findFirstSequencer(), out);
                    ps = conn.prepareStatement(sql.toString());
                    ps.setString(1, "-");
                    rs = ps.executeQuery();
                } catch (Throwable e) {
                    exception = e;
                }

                if (exception!=null) {
                    switch (ORM2DLL_POLICY.of(this)) {
                        case VALIDATE:
                        case WARNING:
                            throw new IllegalStateException(logMsg, exception);
                        case CREATE_DDL:
                        case CREATE_OR_UPDATE_DDL:
                        case INHERITED:
                            createSequenceTable = true;
                    }
                }

                if (LOGGER.isLoggable(Level.INFO)) {
                    logMsg = "Table ''{0}'' {1} available on the database ''{2}''.";
                    logMsg = MessageFormat.format(logMsg
                           , getDialect().getSeqTableModel().getTableName()
                           , exception!=null ? "is not" : "is"
                           , getId()
                           );
                    LOGGER.log(Level.INFO, logMsg);
                }

                try {
                    if (exception!=null) {
                        conn.rollback();
                    }
                } finally {
                   close(null, ps, rs, false);
                }
            }

            boolean ddlOnly = false;
            switch (ORM2DLL_POLICY.of(this)) {
                case CREATE_DDL:
                    ddlOnly = true;
                case CREATE_OR_UPDATE_DDL:
                case VALIDATE:
                case WARNING:
                case INHERITED:
                    boolean change = isModelChanged(conn, tables, newColumns, indexes);
                    if (change && ddlOnly) {
                        if (tables.size()<tableTotalCount) {
                            // This is a case of the PARTIAL DDL
                            return;
                        }
                    }
                    break;
                case DO_NOTHING:
                default:
                    return;
            }

            // ================================================

            // 1. CheckReport keywords:
            switch (MetaParams.CHECK_KEYWORDS.of(getParams())) {
                case WARNING:
                case EXCEPTION:
                    Set<String> keywords = getDialect().getKeywordSet(conn);
                    for (MetaTable table : tables) {
                        if (table.isTable()) {
                            checkKeyWord(MetaTable.NAME.of(table), table, keywords);
                            for (MetaColumn column : MetaTable.COLUMNS.of(table)) {
                                checkKeyWord(MetaColumn.NAME.of(column), table, keywords);
                            }
                        }
                    }
                    for (MetaColumn column : newColumns) {
                        checkKeyWord(MetaColumn.NAME.of(column), column.getTable(), keywords);
                    }
                    for (MetaIndex index : indexes) {
                        checkKeyWord(MetaIndex.NAME.of(index), MetaIndex.TABLE.of(index), keywords);
                    }
            }

            // 2. Create schemas:
            if (tableTotalCount==tables.size()) for (String schema : getSchemas(tables)) { // TODO
                out.setLength(0);
                sql = getDialect().printCreateSchema(schema, out);
                if (isFilled(sql)) {
                    try {
                       stat.executeUpdate(sql.toString());
                    } catch (SQLException e) {
                       LOGGER.log(Level.INFO, "{0}: {1}; {2}", new Object[]{e.getClass().getName(), sql.toString(), e.getMessage()});
                       conn.rollback();
                    }
                }
            }

            // 3. Create tables:
            int tableCount = 0;
            for (MetaTable table : tables) {
                if (table.isTable()) {
                    tableCount++;
                    out.setLength(0);
                    sql = getDialect().printTable(table, out);
                    executeUpdate(sql, stat, table);
                    foreignColumns.addAll(table.getForeignColumns());
                    anyChange = true;
                }
            }

            // 4. Create new columns:
            for (MetaColumn column : newColumns) {
                out.setLength(0);
                sql = getDialect().printAlterTableAddColumn(column, out);
                executeUpdate(sql, stat, column.getTable());
                anyChange = true;

                // Pick up the foreignColumns:
                if (column.isForeignKey()) {
                    foreignColumns.add(column);
                }
            }

            // 5. Create Indexes:
            for (MetaIndex index : indexes) {
                out.setLength(0);
                sql = getDialect().printIndex(index, out);
                executeUpdate(sql, stat, MetaIndex.TABLE.of(index));
                anyChange = true;
            }

            // 6. Create Foreign Keys:
            for (MetaColumn column : foreignColumns) {
                if (column.isForeignKey()) {
                    out.setLength(0);
                    MetaTable table = MetaColumn.TABLE.of(column);
                    sql = getDialect().printForeignKey(column, table, out);
                    executeUpdate(sql, stat, column.getTable());
                    anyChange = true;
                }
            }

            // 7. Create SEQUENCE table;
            if (createSequenceTable) {
                out.setLength(0);
                sql = getDialect().printSequenceTable(this, out);
                final MetaTable table = new MetaTable();
                MetaTable.ORM2DLL_POLICY.setValue(table, MetaParams.ORM2DLL_POLICY.getDefault());
                executeUpdate(sql, stat, table);
            }

            // 8. Create table comment for the all tables:
            @SuppressWarnings("unchecked")
            final List<MetaTable> cTables;
            switch (MetaParams.COMMENT_POLICY.of(ormHandler.getParameters())) {
                case FOR_NEW_OBJECT:
                    cTables = tables;
                    break;
                case ALWAYS:
                    cTables = TABLES.getList(this);
                    break;
                case ON_ANY_CHANGE:
                    cTables = anyChange ? TABLES.getList(this) : (List)Collections.emptyList();
                    break;
                case NEVER:
                    cTables = Collections.emptyList();
                    break;
                default:
                    throw new IllegalStateException("Unsupported parameter");
            }
            if (!cTables.isEmpty()) {
                sql = out;
                createTableComments(cTables, stat, out);
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

    /** Check missing database table, index, or column */
    private void executeUpdate(final Appendable sql, final Statement stat, final MetaTable table) throws IllegalStateException, SQLException {

       boolean validateCase = false;
       switch (table.getOrm2ddlPolicy()) {
           case INHERITED:
               throw new IllegalStateException("An internal error due the DDL policy: " + table.getOrm2ddlPolicy());
           case DO_NOTHING:
               return;
           case VALIDATE:
               validateCase = true;
           case WARNING:
               String msg = "A database validation (caused by the parameter "
                          + MetaTable.ORM2DLL_POLICY
                          + ") have found an inconsistency. "
                          + "There is required a database change: "
                          + sql
                          ;
               if (validateCase) {
                   throw new IllegalStateException(msg);
               } else {
                   LOGGER.log(Level.WARNING, msg);
               }

           default:
               stat.executeUpdate(sql.toString());
               LOGGER.log(Level.INFO, sql.toString());
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

    /** Check the keyword */
    protected void checkKeyWord(String word, MetaTable table, Set<String> keywords) throws Exception {
        if (keywords.contains(word.toUpperCase())) {
            String msg = "The database table or column called '" + word
                + "' is a SQL keyword. See the class: "
                + table.getType().getName()
                + ".\nNOTE: the keyword checking can be disabled by the Ujorm parameter: " + MetaParams.CHECK_KEYWORDS
                ;
            switch (MetaParams.CHECK_KEYWORDS.of(getParams())) {
                case EXCEPTION:
                    throw new IllegalArgumentException(msg);
                case WARNING:
                    LOGGER.log(Level.WARNING, msg);
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
        final Connection result = dialect.createConnection(this);
        result.setAutoCommit(false);

        return result;
    }

    /** Call the method from SqlDialect only. Connection is set to autocommit to false. */
    public Connection createInternalConnection() throws Exception {
        Connection result;

        final List<String> jndi = JNDI.of(this);
        if (!jndi.isEmpty()) {

            LOGGER.log(Level.FINE, "JNDI: {0}", jndi);

            InitialContext initContext = dialect.createJndiInitialContext(this);
            final int lastItem = jndi.size()-1;
            for (int i=0; i<lastItem; i++) {
                initContext = (InitialContext) initContext.lookup(jndi.get(i));
                if (initContext==null) {
                    throw new IllegalStateException("JNDI problem: InitialContext was not found for the: " + jndi.get(i));
                }
            }
            DataSource dataSource = (DataSource) initContext.lookup(jndi.get(lastItem));
            if (dataSource==null) {
                throw new IllegalStateException("JNDI problem: database connection was not found for the: " + jndi);
            }
            result = dataSource.getConnection();
        } else {
            Class.forName(JDBC_DRIVER.of(this));
            result = DriverManager.getConnection(JDBC_URL.of(this), USER.of(this), PASSWORD.of(this));
        }

        return result;
    }

    /** Equals */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaDatabase) {
            MetaDatabase db = (MetaDatabase) obj;

            final Integer i1 = MetaDatabase.ORDER.of(this);
            final Integer i2 = MetaDatabase.ORDER.of(db);

            return i1.equals(i2);
        } else {
            return false;
        }
    }

    /** Hash code */
    @Override
    public int hashCode() {
        final Integer ir = MetaDatabase.ORDER.of(this);
        return ir.hashCode();
    }


    /** Returns a default handler session. It is a session of the first database. */
    public Session getDefaultSession() {
        return ormHandler.getSession();
    }

    /** Get all table schemas */
    private Set<String> getSchemas(List<MetaTable> tables) {
        final Set<String> result = new HashSet<String>();
        for (MetaTable table : tables) {
            if (table.isTable()) {
                String schema = MetaTable.SCHEMA.of(table);
                if (isFilled(schema)) {
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

        if (isFilled(id)) for (MetaTable table : TABLES.getList(this)) {
            if (MetaTable.ID.equals(table, id)) {
                return table;
            }
        }
        return null;
    }

    /** Finds the first procedure by ID or returns null.
     * The method is for internal use only.
     */
    MetaProcedure findProcedure(String id) {

        if (isFilled(id)) for (MetaProcedure procedure : PROCEDURES.getList(this)) {
            if (MetaProcedure.ID.equals(procedure, id)) {
                return procedure;
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

    /** Method returns true in case any table requires the internal table 'ujorm_pk_support' to get a next sequence value. */
    public boolean isSequenceTableRequired() {
        for (MetaTable table : TABLES.of(this)) {
            if (table.isTable()
            &&  table.getSequencer().isSequenceTableRequired()) {
                return true;
            }
        }
        return false;
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

    /** Returns all database indexes */
    public List<MetaIndex> getIndexList() {
        final List<MetaIndex> result = new ArrayList<MetaIndex>(32);

        for (MetaTable table : TABLES.of(this)) {
            result.addAll(table.getIndexCollection());
        }
        return result;
    }

    @Override
    public String toString() {
        final String msg = ID.of(this)
            + '['
            + MetaDatabase.TABLES.getItemCount(this)
            + ']'
            ;
	     return msg;
    }

    /** Compare the object by ORDER. */
    public int compareTo(MetaDatabase o) {
        final Integer i1 = ORDER.of(this);
        final Integer i2 = ORDER.of(o);
        return i1.compareTo(i2);
    }

    /** The PASSWORD property is not exported to XML for a better security. */
    @Override
    public boolean readAuthorization(final UjoAction action, final Key property, final Object value) {
        switch (action.getType()) {
            case UjoAction.ACTION_XML_EXPORT:
                return property != PASSWORD;
            default:
                return super.readAuthorization(action, property, value);
        }
    }

}
