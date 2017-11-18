/*
 *  Copyright 2009-2017 Pavel Ponec
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.logger.UjoLogger;
import static org.ujorm.logger.UjoLogger.*;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.Session;
import org.ujorm.orm.SqlDialect;
import org.ujorm.orm.SqlDialectEx;
import org.ujorm.orm.UjoSequencer;
import org.ujorm.orm.ao.CommentPolicy;
import static org.ujorm.orm.metaModel.MetaDatabase.*;
import static org.ujorm.tools.Check.hasLength;

/**
 * A service method for the MetaDatabase class.
 * The service class can be overriten
 * @author Pavel Ponec
 * @see MetaParams#META_DB_SERVICE
 */
@Immutable
public class MetaDbService {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(MetaDbService.class);
    /** Debug mode */
    private static final boolean DEBUG_MODE = false;
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
        // New database entities:
        final int[] counts = db.getDbItemCount();
        final int tableTotalCount = counts[0];
        final int columnTotalCount = counts[1];
        final DbItems news = new DbItems(tableTotalCount, columnTotalCount);

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
                    boolean change = isModelChanged(conn, news);
                    if (change && ddlOnly) {
                        if (news.getTables().size()<tableTotalCount) {
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
            checkReportKeywords(conn, news);
            // 2. Create schemas:
            createSchema(news.getSchemas(), conn);
            // 3. Create tables:
            createTable(news);
            // 4. Create new columns:
            createNewColumn(news);
            // 5. Create Indexes:
            changeIndex(news.getIndexes());
            // 6. Create Foreign Keys:
            createForeignKey(news.getForeignColumns());
            // 7. Create SEQUENCE table:
            createSequenceTable(createSequenceTable);
            // 8. Create table comment for the all tables:
            createTableComments(news.getTables());
            // 9. Commit:
            conn.commit();

        } catch (SQLException | IOException | RuntimeException | OutOfMemoryError e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                LOGGER.log(WARN, "Can't rollback DB {}", db.getId(), ex);
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

    /**
     * Find database table or columns to modify.
     * @param conn Database connection
     * @return Output argumens constains list of new entities to create.
     * @throws SQLException
     */
    @SuppressWarnings("LoggerStringConcat")
    protected boolean isModelChanged(final Connection conn, final DbItems news) throws SQLException {
        final DatabaseMetaData dbModel = conn.getMetaData();
        final HashMap<String, String> requiredSchemas = new HashMap<>();
        final Boolean isCatalog = db.getDialect().isCatalog();

        for (MetaTable table : TABLES.of(db)) {
            if (table.isTable()) {
                // CHECK COLUMNS AND INDEXES OF THE TABLE:
                final boolean tableExists = addNewColumns
                          ( dbModel
                          , table
                          , news.getTables()
                          , news.getColumns());
                if (tableExists) {
                    addNewIndexes(dbModel, table, news.getIndexes());
                }
                switch (table.getOrm2ddlPolicy()) {
                    case CREATE_DDL:
                    case CREATE_OR_UPDATE_DDL: {
                        final String schemaUpper = toUpperCase(table.getSchema());
                        if (!requiredSchemas.containsKey(schemaUpper)) {
                            requiredSchemas.put(schemaUpper, table.getSchema());
                        }
                    }
                }
            }
        }

        // Check DB schemas:
        try (ResultSet schemas = dbModel.getSchemas()) {
            while(schemas.next()) {
                final String schemaUpper = toUpperCase(schemas.getString(isCatalog ? 2 : 1));
                final Level levelTrace = UjoLogger.TRACE;
                if (LOGGER.isLoggable(levelTrace)) {
                    LOGGER.log(levelTrace, "Schema: {}.{} Catalog: {}"
                            , schemas.getString(2)
                            , schemas.getString(1)
                            , isCatalog);
                }
                if (schemaUpper != null) {
                   requiredSchemas.remove(schemaUpper);
                }
            }
        }
        news.getSchemas().addAll(requiredSchemas.values());

        final boolean result = !news.getTables().isEmpty()
                            || !news.getColumns().isEmpty()
                            || !news.getIndexes().isEmpty();
        return result;
    }

    @Nullable
    private String toUpperCase(@Nullable final String text) {
        return text != null ? text.toUpperCase(Locale.ENGLISH) : text;
    }

    private void logColumn(final ResultSet columns) throws SQLException {
        final String msg = "DB column: "
                + columns.getString("TABLE_CAT") + "."
                + columns.getString("TABLE_SCHEM") + "."
                + columns.getString("TABLE_NAME") + "."
                + columns.getString("COLUMN_NAME")
                ;
        LOGGER.log(INFO, msg);
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
                LOGGER.log(INFO, "Table '{}' {} available on the database '{}'."
                       , db.getDialect().getSeqTableModel().getTableName()
                       , exception!=null ? "is not" : "is"
                       , db.getId());
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
    protected void checkReportKeywords(final Connection conn, final DbItems news) throws SQLException {
        switch (MetaParams.CHECK_KEYWORDS.of(db.getParams())) {
            case WARNING:
            case EXCEPTION:
                Set<String> keywords = db.getDialect().getKeywordSet(conn);
                for (MetaTable table : news.getTables()) {
                    if (table.isTable()) {
                        checkKeyWord(MetaTable.NAME.of(table), table, keywords);
                        for (MetaColumn column : MetaTable.COLUMNS.of(table)) {
                            checkKeyWord(column.getName(), table, keywords);
                        }
                    }
                }
                for (MetaColumn column : news.getColumns()) {
                    checkKeyWord(MetaColumn.NAME.of(column), column.getTable(), keywords);
                }
                for (MetaIndex index : news.getIndexes()) {
                    checkKeyWord(MetaIndex.NAME.of(index), MetaIndex.TABLE.of(index), keywords);
                }
        }
    }

    /** 2. Create schemas: */
    protected void createSchema(List<String> schemas, Connection conn) throws SQLException, IOException {
        for (String schema : schemas) {
            sql.setLength(0);
            db.getDialect().printCreateSchema(schema, sql);
            if (hasLength(sql)) {
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

    /** 3. Create tables: */
    protected void createTable(final DbItems news) throws IOException, SQLException {
        for (MetaTable table : news.getTables()) {
            if (table.isTable()) {
                sql.setLength(0);
                db.getDialect().printTable(table, sql);
                executeUpdate(sql, table);
                news.getForeignColumns().addAll(table.getForeignColumns());
                anyChange = true;
            }
        }
    }

    /** 4. Create new columns: */
    protected void createNewColumn(final DbItems news) throws IOException, SQLException {
        for (MetaColumn column : news.getColumns()) {
            sql.setLength(0);
            db.getDialect().printAlterTableAddColumn(column, sql);
            executeUpdate(sql, column.getTable());
            anyChange = true;

            // Pick up the foreignColumns:
            if (column.isForeignKey()) {
                news.getForeignColumns().add(column);
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
    protected final boolean isCatalog() {
        return getDialect().isCatalog();
    }

    /**
     *
     * @param dbModel
     * @param table required table
     * @param newTables Output parameter
     * @param newColumns Output parameter
     * @return
     * @throws SQLException
     */
    private boolean addNewColumns
        ( final DatabaseMetaData dbModel
        , final MetaTable table
        , final List<MetaTable> newTables
        , final List<MetaColumn> newColumns
        ) throws SQLException {

        final boolean catalog = isCatalog();
        final Set<String> existingColumns = new HashSet<>(32);
        final String schema = dbIdentifier(MetaTable.SCHEMA.of(table),dbModel);
        try (ResultSet columns = dbModel.getColumns
            ( catalog ? schema : null
            , catalog ? null  : schema
            , dbIdentifier(MetaTable.NAME.of(table),dbModel)
            , null // colmn patern
            )) {
            while(columns.next()) {
                existingColumns.add(columns.getString("COLUMN_NAME").toUpperCase());
                if (DEBUG_MODE && LOGGER.isLoggable(INFO)) {
                    logColumn(columns);
                }
            }
        }

        final boolean tableExists = existingColumns.size()>0;
        if (tableExists) {
            // create columns:
            for (MetaColumn mc : MetaTable.COLUMNS.of(table)) {
                final boolean exists = existingColumns.contains(mc.getName().toUpperCase());
                if (!exists) {
                    LOGGER.log(INFO, "New DB column: {}", mc.getFullName());
                    newColumns.add(mc);
                }
            }
        } else {
            LOGGER.log(INFO, "New DB table: {}", MetaTable.NAME.of(table));
            newTables.add(table);
        }
        return tableExists;
    }

    /**
     *
     * @param dbModel
     * @param table
     * @param newIndexes
     * @throws SQLException
     */
    protected void addNewIndexes(final DatabaseMetaData dbModel, final MetaTable table,  List<MetaIndex>  newIndexes) throws SQLException {
                    final boolean catalog = isCatalog();

        final String schema = dbIdentifier(MetaTable.SCHEMA.of(table),dbModel);
        final Set<String> existingIndexes = new HashSet<>();

        try (ResultSet indexes = dbModel.getIndexInfo
            ( catalog ? schema : null
            , catalog ? null : schema
            , dbIdentifier(MetaTable.NAME.of(table),dbModel)
            , false // unique
            , false // approximate
            )) {
            while(indexes.next()) {
                final String name = indexes.getString("INDEX_NAME");
                if (name!=null) {
                    existingIndexes.add(name.toUpperCase());
                }
            }
        }

        for (MetaIndex index : table.getIndexCollection()) {
            final boolean exists = existingIndexes.contains(MetaIndex.NAME.of(index).toUpperCase());
            if (!exists) {
                LOGGER.log(INFO, "New DB index: {}", index);
                newIndexes.add(index);
            }
        }
    }

}
