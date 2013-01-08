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

import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.utility.OrmTools;
import static org.ujorm.orm.metaModel.MetaDatabase.*;

/**
 * S service method for the metaDatabase
 * @author Pavel Ponec
 */
public class MetaDbService {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(MetaDbService.class);
    protected MetaDatabase db;

    /** Set the meta Database */
    public MetaDatabase getMetaDatabase() {
        return db;
    }

    /** Get the meta Database */
    public void setMetaDatabase(MetaDatabase metaDatabase) {
        this.db = metaDatabase;
    }

    /** 1. CheckReport keywords: */
    public void checkReportKeywords(Connection conn, List<MetaTable> tables, List<MetaColumn> newColumns, List<MetaIndex> indexes) throws Exception {
        switch (MetaParams.CHECK_KEYWORDS.of(db.getParams())) {
            case WARNING:
            case EXCEPTION:
                Set<String> keywords = db.getDialect().getKeywordSet(conn);
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
    }

    /** 2. Create schemas: */
    public void createSchema(int tableTotalCount, List<MetaTable> tables, StringBuilder sql, Statement stat, Connection conn) throws SQLException, IOException {
        if (tableTotalCount == tables.size()) {
            for (String schema : db.getSchemas(tables)) {
                sql.setLength(0);
                db.getDialect().printCreateSchema(schema, sql);
                if (OrmTools.isFilled(sql)) {
                    try {
                        stat.executeUpdate(sql.toString());
                    } catch (SQLException e) {
                        LOGGER.log(Level.INFO, "{0}: {1}; {2}", new Object[]{e.getClass().getName(), sql.toString(), e.getMessage()});
                        conn.rollback();
                    }
                }
            }
        }
    }

    /** 3. Create tables: */
    public boolean createTable(List<MetaTable> tables, StringBuilder sql, Statement stat, List<MetaColumn> foreignColumns) throws Exception {
        boolean anyChange = false;
        for (MetaTable table : tables) {
            if (table.isTable()) {
                sql.setLength(0);
                db.getDialect().printTable(table, sql);
                executeUpdate(sql, stat, table);
                foreignColumns.addAll(table.getForeignColumns());
                anyChange = true;
            }
        }
        return anyChange;
    }

    /** 4. Create new columns: */
    public boolean createNewColumn(List<MetaColumn> newColumns, StringBuilder sql, Statement stat, List<MetaColumn> foreignColumns) throws Exception {
        boolean anyChange = false;
        for (MetaColumn column : newColumns) {
            sql.setLength(0);
            db.getDialect().printAlterTableAddColumn(column, sql);
            executeUpdate(sql, stat, column.getTable());
            anyChange = true;

            // Pick up the foreignColumns:
            if (column.isForeignKey()) {
                foreignColumns.add(column);
            }
        }
        return anyChange;
    }

    /** 5. Create Indexes: */
    public boolean changeIndex(List<MetaIndex> indexes, StringBuilder sql, Statement stat) throws Exception {
        boolean anyChange = false;
        for (MetaIndex index : indexes) {
            sql.setLength(0);
            db.getDialect().printIndex(index, sql);
            executeUpdate(sql, stat, MetaIndex.TABLE.of(index));
            anyChange = true;
        }
        return anyChange;
    }

    /** 6. Create Foreign Keys: */
    public boolean createForeignKey(List<MetaColumn> foreignColumns, StringBuilder sql, Statement stat) throws Exception {
        boolean anyChange = false;
        for (MetaColumn column : foreignColumns) {
            if (column.isForeignKey()) {
                sql.setLength(0);
                MetaTable table = MetaColumn.TABLE.of(column);
                db.getDialect().printForeignKey(column, table, sql);
                executeUpdate(sql, stat, column.getTable());
                anyChange = true;
            }
        }
        return anyChange;
    }

    /** 7. Create SEQUENCE table: */
    public void createSequenceTable(boolean createSequenceTable, StringBuilder sql, Statement stat) throws Exception {
        if (createSequenceTable) {
            sql.setLength(0);
            db.getDialect().printSequenceTable(db, sql);
            final MetaTable table = new MetaTable();
            MetaTable.ORM2DLL_POLICY.setValue(table, MetaParams.ORM2DLL_POLICY.getDefault());
            executeUpdate(sql, stat, table);
        }
    }

    /** 8. Create table comment for the all tables: */
    public void createTableComments(List<MetaTable> tables, boolean anyChange, Statement stat, StringBuilder sql) throws Exception {
        @SuppressWarnings("unchecked")
        final List<MetaTable> cTables;
        switch (MetaParams.COMMENT_POLICY.of(db.getParams())) {
            case FOR_NEW_OBJECT:
                cTables = tables;
                break;
            case ALWAYS:
                cTables = TABLES.getList(db);
                break;
            case ON_ANY_CHANGE:
                cTables = anyChange ? TABLES.getList(db) : (List)Collections.emptyList();
                break;
            case NEVER:
                cTables = Collections.emptyList();
                break;
            default:
                throw new IllegalStateException("Unsupported parameter");
        }
        if (!cTables.isEmpty()) {
            createTableComments(cTables, stat, sql);
        }
    }

    /** Create table and column comments. An error in this method does not affect the rest of all transaction.  */
    protected void createTableComments(List<MetaTable> cTables, Statement stat, StringBuilder out) {
        try {
            for (MetaTable table : cTables) {
                switch (MetaTable.ORM2DLL_POLICY.of(table)) {
                    case CREATE_DDL:
                    case CREATE_OR_UPDATE_DDL:
                        if (table.isTable()) {
                            if (table.isCommented()) {
                                out.setLength(0);
                                Appendable sql = db.getDialect().printComment(table, out);
                                if (sql.toString().length() > 0) {
                                    executeUpdate(sql, stat, table);
                                }
                            }
                            for (MetaColumn column : MetaTable.COLUMNS.of(table)) {
                                if (column.isCommented()) {
                                    out.setLength(0);
                                    Appendable sql = db.getDialect().printComment(column, out);
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

    /** Check the keyword */
    protected void checkKeyWord(String word, MetaTable table, Set<String> keywords) throws Exception {
        if (keywords.contains(word.toUpperCase())) {
            String msg = "The database table or column called '" + word
                + "' is a SQL keyword. See the class: "
                + table.getType().getName()
                + ".\nNOTE: the keyword checking can be disabled by the Ujorm parameter: " + MetaParams.CHECK_KEYWORDS
                ;
            switch (MetaParams.CHECK_KEYWORDS.of(db.getParams())) {
                case EXCEPTION:
                    throw new IllegalArgumentException(msg);
                case WARNING:
                    LOGGER.log(Level.WARNING, msg);
            }
        }
    }

    /** Check missing database table, index, or column */
    protected void executeUpdate(final Appendable sql, final Statement stat, final MetaTable table) throws IllegalStateException, SQLException {

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

}
