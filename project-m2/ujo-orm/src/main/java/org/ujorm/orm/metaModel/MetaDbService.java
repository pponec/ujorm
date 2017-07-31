/*
 *  Copyright 2009-2015 Pavel Ponec
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

import java.io.IOException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.Session;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.SqlDialectEx;
import org.ujorm.orm.UjoSequencer;
import org.ujorm.orm.utility.OrmTools;
import static org.ujorm.logger.UjoLogger.*;
import org.ujorm.orm.ao.CommentPolicy;
import static org.ujorm.orm.metaModel.MetaDatabase.*;

/**
 * A service method for the MetaDatabase class.
 * The service class can be overriten
 * @author Pavel Ponec
 * @see MetaParams#META_DB_SERVICE
 */
public class MetaDbService {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(MetaDbService.class);
    /** Meta Database from constructor */
    protected MetaDatabase db;
    /** SQL Buffer */
    final protected StringBuilder sql = new StringBuilder(256);
    /** DB Statement for common use */
    protected Statement stat = null;
    /** There is a database change */
    protected boolean anyChange = false;

    /** Create DB */
    public void create(MetaDatabase metaDatabase, Session session) {
        this.db = metaDatabase;
        Connection conn = session.getConnection(db, true);
        List<MetaTable> tables = new ArrayList<>();
        List<MetaColumn> newColumns = new ArrayList<>();
        List<MetaColumn> foreignColumns = new ArrayList<>();
        List<MetaIndex> indexes = new ArrayList<>();
        int tableTotalCount = db.getTableTotalCount();

        try {
            final boolean createSequenceTable = initialize(conn);

            boolean ddlOnly = false;
            switch (ORM2DLL_POLICY.of(db)) {
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
            checkReportKeywords(conn, tables, newColumns, indexes);
            // 2. Create schemas:
            createSchema(tableTotalCount, tables, conn);
            // 3. Create tables:
            createTable(tables, foreignColumns);
            // 4. Create new columns:
            createNewColumn(newColumns, foreignColumns);
            // 5. Create Indexes:
            changeIndex(indexes);
            // 6. Create Foreign Keys:
            createForeignKey(foreignColumns);
            // 7. Create SEQUENCE table:
            createSequenceTable(createSequenceTable);
            // 8. Create table comment for the all tables:
            createTableComments(tables);
            // 9. Commit:
            conn.commit();

        } catch (SQLException | IOException | RuntimeException | OutOfMemoryError e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(WARN, "Can't rollback DB" + db.getId(), ex);
            }
            final String msg = Session.SQL_ILLEGAL + getSql();
            LOGGER.log(Level.SEVERE, msg, e);
            throw new IllegalUjormException(msg, e);
        }
    }

    /** SQL Buffer */
    public StringBuilder getSql() {
        return sql;
    }

    /** Find database table or columns to modify.
     * @param conn Database connection
     * @param newTables Output parameter
     * @param newColumns Output parameter
     */
    @SuppressWarnings("LoggerStringConcat")
    protected boolean isModelChanged(Connection conn
        , List<MetaTable>  newTables
        , List<MetaColumn> newColumns
        , List<MetaIndex>  newIndexes
        ) throws SQLException {
        newTables.clear();
        newColumns.clear();
        newIndexes.clear();

        final DatabaseMetaData dmd = conn.getMetaData();
        final boolean catalog = isCatalog();
        final String column = null;

        for (MetaTable table : TABLES.of(db)) {
            if (table.isTable()) {

                // ---------- CHECK TABLE COLUMNS ----------

                final Set<String> items = new HashSet<>(32);
                final String schema = dbIdentifier(MetaTable.SCHEMA.of(table),dmd);
                ResultSet rs = dmd.getColumns
                    ( catalog ? schema : null
                    , catalog ? null  : schema
                    , dbIdentifier(MetaTable.NAME.of(table),dmd)
                    , column
                    );
                while(rs.next()) {
                    items.add(rs.getString("COLUMN_NAME").toUpperCase());
                    if (false && LOGGER.isLoggable(INFO)) {
                        // Debug message:
                        String msg = "DB column: "
                                   + rs.getString("TABLE_CAT") + "."
                                   + rs.getString("TABLE_SCHEM") + "."
                                   + rs.getString("TABLE_NAME") + "."
                                   + rs.getString("COLUMN_NAME")
                                   ;
                        LOGGER.log(INFO, msg);
                    }
                }
                rs.close();

                boolean tableExists = items.size()>0;
                if (tableExists) {
                    // create columns:
                    for (MetaColumn mc : MetaTable.COLUMNS.of(table)) {

                        boolean exists = items.contains(mc.getName().toUpperCase());
                        if (!exists) {
                            LOGGER.log(INFO, "New DB column: {}", mc.getFullName());
                            newColumns.add(mc);
                        }
                    }
                } else {
                    LOGGER.log(INFO, "New DB table: {}", MetaTable.NAME.of(table));
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
                        LOGGER.log(INFO, "New DB index: {}", index);
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

    /** Returns a native database identifirer. */
    protected String dbIdentifier(final String name, final DatabaseMetaData dmd) throws SQLException {
        if (dmd.storesUpperCaseIdentifiers()) {
            return name.toUpperCase();
        }
        if (dmd.storesLowerCaseIdentifiers()) {
            return name.toLowerCase();
        }
        return name;
    }

    // =================================================

    /** 0. Initialization
     * @param conn
     * @return A createSequenceTable request
     * @throws java.sql.SQLException
     * @throws java.io.IOException */
    protected boolean initialize(Connection conn) throws SQLException, IOException, IllegalUjormException {
        this.stat = conn.createStatement();
        boolean createSequenceTable = false;
        if (db.isSequenceTableRequired()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Throwable exception = null;
            String logMsg = "";

            try {
                db.getDialect().printSequenceCurrentValue(findFirstSequencer(), sql);
                ps = conn.prepareStatement(sql.toString());
                ps.setString(1, "-");
                rs = ps.executeQuery();
            } catch (SQLException e) {
                exception = e;
            }

            if (exception!=null) {
                switch (ORM2DLL_POLICY.of(db)) {
                    case VALIDATE:
                    case WARNING:
                        throw new IllegalUjormException(logMsg, exception);
                    case CREATE_DDL:
                    case CREATE_OR_UPDATE_DDL:
                    case INHERITED:
                        createSequenceTable = true;
                }
            }

            if (LOGGER.isLoggable(INFO)) {
                logMsg = "Table ''{0}'' {1} available on the database ''{2}''.";
                logMsg = MessageFormat.format(logMsg
                       , db.getDialect().getSeqTableModel().getTableName()
                       , exception!=null ? "is not" : "is"
                       , db.getId()
                       );
                LOGGER.log(INFO, logMsg);
            }

            try {
                if (exception!=null) {
                    conn.rollback();
                }
            } finally {
               close(null, ps, rs, false);
            }
        }
        return createSequenceTable;
    }

    /** 1. CheckReport keywords: */
    protected void checkReportKeywords(Connection conn, List<MetaTable> tables, List<MetaColumn> newColumns, List<MetaIndex> indexes) throws SQLException {
        switch (MetaParams.CHECK_KEYWORDS.of(db.getParams())) {
            case WARNING:
            case EXCEPTION:
                Set<String> keywords = db.getDialect().getKeywordSet(conn);
                for (MetaTable table : tables) {
                    if (table.isTable()) {
                        checkKeyWord(MetaTable.NAME.of(table), table, keywords);
                        for (MetaColumn column : MetaTable.COLUMNS.of(table)) {
                            checkKeyWord(column.getName(), table, keywords);
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
    }

    /** 2. Create schemas: */
    protected void createSchema(int tableTotalCount, List<MetaTable> tables, Connection conn) throws SQLException, IOException {
        if (tableTotalCount == tables.size()) {
            for (String schema : db.getSchemas(tables)) {
                sql.setLength(0);
                db.getDialect().printCreateSchema(schema, sql);
                if (OrmTools.isFilled(sql)) {
                    try {
                        stat.executeUpdate(sql.toString());
                    } catch (SQLException e) {
                        LOGGER.log(INFO, "{}: {}; {}"
                                , e.getClass().getName()
                                , sql.toString()
                                , e.getMessage());
                        conn.rollback();
                    }
                }
            }
        }
    }

    /** 3. Create tables: */
    protected void createTable(List<MetaTable> tables, List<MetaColumn> foreignColumns) throws IOException, SQLException {
        for (MetaTable table : tables) {
            if (table.isTable()) {
                sql.setLength(0);
                db.getDialect().printTable(table, sql);
                executeUpdate(sql, table);
                foreignColumns.addAll(table.getForeignColumns());
                anyChange = true;
            }
        }
    }

    /** 4. Create new columns: */
    protected void createNewColumn(List<MetaColumn> newColumns, List<MetaColumn> foreignColumns) throws IOException, SQLException {
        for (MetaColumn column : newColumns) {
            sql.setLength(0);
            db.getDialect().printAlterTableAddColumn(column, sql);
            executeUpdate(sql, column.getTable());
            anyChange = true;

            // Pick up the foreignColumns:
            if (column.isForeignKey()) {
                foreignColumns.add(column);
            }
        }
    }

    /** 5. Create Indexes: */
    protected void changeIndex(List<MetaIndex> indexes) throws SQLException, IOException {
        for (MetaIndex index : indexes) {
            sql.setLength(0);
            db.getDialect().printIndex(index, sql);
            executeUpdate(sql, MetaIndex.TABLE.of(index));
            anyChange = true;
        }
    }

    /** 6. Create Foreign Keys: */
    protected void createForeignKey(List<MetaColumn> foreignColumns) throws IOException, SQLException {
        for (MetaColumn column : foreignColumns) {
            if (column.isForeignKey()) {
                sql.setLength(0);
                db.getDialect().printForeignKey(column, sql);
                executeUpdate(sql, column.getTable());
                anyChange = true;
            }
        }
    }

    /** 7. Create SEQUENCE table: */
    protected void createSequenceTable(boolean createSequenceTable) throws SQLException, IOException {
        if (createSequenceTable) {
            sql.setLength(0);
            db.getDialect().printSequenceTable(db, sql);
            final MetaTable table = new MetaTable();
            MetaTable.ORM2DLL_POLICY.setValue(table, MetaParams.ORM2DLL_POLICY.getDefault());
            executeUpdate(sql, table);

            // Write a table comment:
            sql.setLength(0);
            MetaTable.NAME.setValue(table, db.getDialect().getSeqTableModel().getTableName());
            MetaTable.SCHEMA.setValue(table, MetaDatabase.SCHEMA.of(db));
            MetaTable.COMMENT.setValue(table, db.getDialect().getSeqTableModel().getTableComment());
            db.getDialect().printComment(table, sql);
            executeUpdate(sql, table);
        }
    }

    /** 8. Create table comment for the all tables: */
    protected void createTableComments(List<MetaTable> tables) throws IllegalUjormException {
        @SuppressWarnings("unchecked")
        final List<MetaTable> cTables;
        final CommentPolicy policy = MetaParams.COMMENT_POLICY.of(db.getParams());
        switch (policy) {
            case FOR_NEW_OBJECT:
                cTables = tables;
                break;
            case ALWAYS:
                cTables = TABLES.getList(db);
                break;
            case ON_ANY_CHANGE:
                cTables = isAnyChange() ? TABLES.getList(db) : (List)Collections.emptyList();
                break;
            case NEVER:
                cTables = Collections.emptyList();
                break;
            default:
                throw new IllegalUjormException("Unsupported parameter: " + policy);
        }
        if (!cTables.isEmpty()) {
            createTableComments(cTables, sql);
        }
    }

    /** Create table and column comments. An error in this method does not affect the rest of all transaction.  */
    protected void createTableComments(List<MetaTable> cTables, StringBuilder out) {
        try {
            for (MetaTable table : cTables) {
                switch (MetaTable.ORM2DLL_POLICY.of(table)) {
                    case CREATE_DDL:
                    case CREATE_OR_UPDATE_DDL:
                        if (table.isTable()) {
                            if (table.isCommented()) {
                                out.setLength(0);
                                final Appendable sql = db.getDialect().printComment(table, out);
                                executeUpdate(sql, table);
                            }
                            for (MetaColumn column : MetaTable.COLUMNS.of(table)) {
                                if (column.isCommented()) {
                                    out.setLength(0);
                                    final Appendable sql = db.getDialect().printComment(column, out);
                                    executeUpdate(sql, table);
                                }
                            }
                        }
                    default:
                }
            }
        } catch (RuntimeException | IOException | SQLException e) {
            LOGGER.log(ERROR, "Error on table comment: {}", out);
        }
    }

    /** Check the keyword */
    protected void checkKeyWord(String word, MetaTable table, Set<String> keywords) throws IllegalUjormException {
        if (keywords.contains(word.toUpperCase())) {
            String msg = "The database table or column called '" + word
                + "' is a SQL keyword. See the class: "
                + table.getType().getName()
                + ".\nNOTE: the keyword checking can be disabled by the Ujorm parameter: " + MetaParams.CHECK_KEYWORDS.getFullName()
                ;
            switch (MetaParams.CHECK_KEYWORDS.of(db.getParams())) {
                case EXCEPTION:
                    throw new IllegalUjormException(msg);
                case WARNING:
                    LOGGER.log(WARN, msg);
            }
        }
    }

    /**
     * Check missing database table, index, or column
     * @param sqlAppendable Single SQL statement where the empty value is ignored
     * @param table Model of database table
     * @throws IllegalUjormException An runtime exception
     * @throws SQLException SQL exception
     */
    protected void executeUpdate(@Nonnull final Appendable sqlAppendable, @Nonnull final MetaTable table) throws IllegalUjormException, SQLException {
        final String sql = sqlAppendable.toString();
        if (sql.isEmpty()) {
            LOGGER.log(Level.FINEST, "Empty SQL statement");
            return;
        }

        boolean validateCase = false;
        switch (table.getOrm2ddlPolicy()) {
            case INHERITED:
                throw new IllegalUjormException("An internal error due the DDL policy: " + table.getOrm2ddlPolicy());
            case DO_NOTHING:
                return;
            case VALIDATE:
                validateCase = true;
            case WARNING:
                String msg = "A database validation (caused by the parameter "
                        + MetaTable.ORM2DLL_POLICY
                        + ") have found an inconsistency. "
                        + "There is required a database change: "
                        + sql;
                if (validateCase) {
                    throw new IllegalUjormException(msg);
                } else {
                    LOGGER.log(WARN, msg);
                }
            default:
                stat.executeUpdate(sql);
                LOGGER.log(INFO, sql);
        }
    }

    /** Find the first sequence of the database or returns null if no sequence was not found. */
    @Nullable
    protected UjoSequencer findFirstSequencer() {
        for (MetaTable table : TABLES.of(db)) {
            if (table.isTable()) {
                return table.getSequencer();
            }
        }
        return null;
    }

    /** Has the database any change? */
    protected boolean isAnyChange() {
        return anyChange;
    }

    protected SqlDialect getDialect() {
        return db.getDialect();
    }

    /** Returns an extended dialect */
    protected SqlDialectEx getDialectEx() {
        return db.getDialect().getExtentedDialect();
    }

    /** Does the database support a catalog?
     * The feature supports: MySqlDialect and MSSqlDialect.
     * @return Result value is provided from a SqlDialog class.
     */
    final protected boolean isCatalog() {
        return getDialect().isCatalog();
    }

}
