/*
 *  Copyright 2013-2014 Effectiva Solutions company
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

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.Session;
import org.ujorm.orm.SqlNameProvider;
import org.ujorm.orm.UjoSequencer;
import org.ujorm.orm.ao.Orm2ddlPolicy;
import org.ujorm.orm.dialect.MSSqlDialect;
import org.ujorm.orm.dialect.MySqlDialect;
import static org.ujorm.orm.ao.CheckReport.EXCEPTION;
import static org.ujorm.logger.UjoLogger.*;
import static org.ujorm.orm.metaModel.MetaDatabase.*;

/**
 * A service method for the MetaDatabase class.
 * The service class can be overwritten
 * @author Effectiva Solutions company
 * @see MetaParams#META_DB_SERVICE
 */
@SuppressWarnings("unchecked")
public class MetaDbServiceEx extends MetaDbService {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(MetaDbServiceEx.class);
    /**
     * Specifies if default values in DB are allowed. There are many problems
     * with default value constraints in MSSQL, therefore it's recommended to
     * not use them. Ujorm provides own mechanism to deal with default values
     * and INSERTs and UPDATEs contains default values explicitly.
     */
    public static boolean DEFAULT_VALUES_IN_DB_ALLOWED = false;
    public static final String COLUMN_DEF_DEFAULT_VALUE = "COLUMN_DEF";
    public static final String COLUMN_DEF_NAME = "COLUMN_NAME";
    public static final String COLUMN_DEF_NULLABLE = "NULLABLE";
    public static final String COLUMN_DEF_CHAR_LENGTH = "COLUMN_SIZE";
    public static final String COLUMN_DEF_VALUE_CONSTRAINTS = "COLUMN_DEF_VALUE_CONSTRAINTS";
    public static final String PKEY_DEF_NAME = "PK_NAME";
    public static final String PKEY_DEF_COLUMN_NAME = "COLUMN_NAME";
    public static final String FKEY_DEF_NAME = "FK_NAME";
    //
    private static final String MYSQL_PRIMARY_KEY_NAME = "PRIMARY";
    private static final String ID_COLUMN_NAME = "ID";

    public void checkDBStructure(Session session, boolean repairDB) throws Exception {
        LOGGER.log(INFO, "UJORM checking db structure...");
        Connection conn = session.getConnection(db, repairDB);
        try {
            List<String> messages = new ArrayList<>();
            List<MetaTable> mappedTables = new ArrayList<>(db.get(TABLES));
            // filter only persistent tables
            for (Iterator<MetaTable> it = mappedTables.iterator(); it.hasNext();) {
                MetaTable metaTable = it.next();
                if (!metaTable.isPersistent()) {
                    it.remove();
                }
            }
            List<MetaIndex> mappedIndexes = db.getIndexList();
            // kontrola na klíčová slova
            switch (MetaParams.CHECK_KEYWORDS.of(db.getParams())) {
                case EXCEPTION:
                    LOGGER.log(INFO, "Checking keywords (tables=" + mappedTables.size() + ", indexes=" + mappedIndexes.size() + ") ...");
                    checkKeywords(conn, mappedTables, null, mappedIndexes);
            }
            // kontrola tabulek (a jejich sloupcu)
            LOGGER.log(INFO, "Checking tables (tables=" + mappedTables.size() + ") ...");
            messages.addAll(checkTables(conn, mappedTables, repairDB));
            // kontrola ujorm_pk_support
            LOGGER.log(INFO, "Checking ujorm_pk_support sequences (tables=" + mappedTables.size() + ") ...");
            messages.addAll(checkUjormPKSupport(conn, mappedTables, repairDB));
            if (repairDB) {
                conn.commit();
                LOGGER.log(INFO, "REPAIR finished, you should look at the previous logs for details. Possible errors could be repaired, you should restart server now!");
            }

            if (messages.size() > 0) {
                StringBuilder output = new StringBuilder("FATAL errors [" + messages.size() + "] in database (some could be repaired, you should restart server):\n");
                for (String message : messages) {
                    output.append(message.trim()).append("\n");
                }
                throw new RuntimeException(output.toString());
            } else {
                LOGGER.log(INFO, "CHECK DB finished, everything is ok!");
            }
        } catch (Exception ex) {
            if (repairDB) {
                // ulozi se alespon to, co se podarilo opravit
                conn.commit();
            } else {
                conn.rollback();
            }
            throw ex;
        }
    }

    public void checkKeywords(Connection conn, List<MetaTable> tables, List<MetaColumn> columns, List<MetaIndex> indexes) throws Exception {
        Set<String> keywords = getDialect().getKeywordSet(conn);
        // check table names and table columns for keyword
        if (tables != null) {
            for (MetaTable table : tables) {
                if (table.isTable()) {
                    checkKeyWord(MetaTable.NAME.of(table), table, keywords);
                    for (MetaColumn column : MetaTable.COLUMNS.of(table)) {
                        checkKeyWord(MetaColumn.NAME.of(column), table, keywords);
                    }
                }
            }
        }
        // check table columns for keyword
        if (columns != null) {
            for (MetaColumn column : columns) {
                checkKeyWord(MetaColumn.NAME.of(column), column.getTable(), keywords);
            }
        }
        // chekc indexes for keywords
        if (indexes != null) {
            for (MetaIndex index : indexes) {
                checkKeyWord(MetaIndex.NAME.of(index), MetaIndex.TABLE.of(index), keywords);
            }
        }

    }

    private List<String> checkTables(Connection conn, List<MetaTable> mappedTables, boolean repairDB) throws Exception {
        ArrayList<String> messages = new ArrayList<String>();
        // 1) CHECK table exists
        LOGGER.log(INFO, "Checking tables (" + mappedTables.size() + ") for existence ...");
        for (MetaTable mappedTable : mappedTables) {
            if (mappedTable.isTable()) { // jen reálné tabulky
                // pro repair mode je treba nastavit povoleni uprav i pokud je jinak zakazano
                if (repairDB) {
                    mappedTable.clearReadOnly();
                    MetaTable.ORM2DLL_POLICY.setValue(mappedTable, Orm2ddlPolicy.CREATE_OR_UPDATE_DDL);
                    mappedTable.setReadOnly(false);
                }
                List<MetaColumn> mappedColumns = mappedTable.get(MetaTable.COLUMNS);
                LOGGER.log(INFO, "Checking table '" + mappedTable.getAlias() + "', columns=" + mappedColumns.size() + " ...");
                Map dbColumns = listDBTableColumns(mappedTable, conn.getMetaData());
                if (dbColumns.size() < 1) {
                    // !!! MISSING TABLE
                    String msg = "MISSING db table '" + mappedTable.getAlias() + "'";
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        StringBuilder sql = new StringBuilder();
                        getDialect().printTable(mappedTable, sql);
                        msg = "  REPAIR: Adding table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                        LOGGER.log(INFO, msg);
                        executeUpdate(sql, mappedTable);
                    }
                }
            }
        }
        // 2) CHECK table missing columns
        LOGGER.log(INFO, "Checking tables (" + mappedTables.size() + ") for missing columns ...");
        for (MetaTable mappedTable : mappedTables) {
            if (mappedTable.isTable()) { // jen reálné tabulky
                // pro repair mode je treba nastavit povoleni uprav i pokud je jinak zakazano
                LOGGER.log(INFO, "Checking table '" + mappedTable.getAlias() + "' for missing columns ...");
                List<String> checkTableMessages = checkTableForMissingColumns(conn, mappedTable, repairDB);
                messages.addAll(checkTableMessages);
            }
        }
        // 3) CHECK table columns properties
        LOGGER.log(INFO, "Checking tables (" + mappedTables.size() + ") for columns properies ...");
        for (MetaTable mappedTable : mappedTables) {
            if (mappedTable.isTable()) { // jen reálné tabulky
                // pro repair mode je treba nastavit povoleni uprav i pokud je jinak zakazano
                LOGGER.log(INFO, "Checking table '" + mappedTable.getAlias() + "' for columns properties ...");
                List<String> checkTableMessages = checkTableForColumnsProperties(conn, mappedTable, repairDB);
                messages.addAll(checkTableMessages);
            }
        }
        // 4) CHECK table primary keys
        LOGGER.log(INFO, "Checking tables (" + mappedTables.size() + ") for missing columns ...");
        for (MetaTable mappedTable : mappedTables) {
            if (mappedTable.isTable()) { // jen reálné tabulky
                // pro repair mode je treba nastavit povoleni uprav i pokud je jinak zakazano
                LOGGER.log(INFO, "Checking table '" + mappedTable.getAlias() + "' for primary keys ...");
                List<String> checkTableMessages = checkTableForPrimaryKeys(conn, mappedTable, repairDB);
                messages.addAll(checkTableMessages);
            }
        }
        // 5) CHECK table foreign keys
        LOGGER.log(INFO, "Checking tables (" + mappedTables.size() + ") for foreign keys ...");
        for (MetaTable mappedTable : mappedTables) {
            if (mappedTable.isTable()) { // jen reálné tabulky
                // pro repair mode je treba nastavit povoleni uprav i pokud je jinak zakazano
                LOGGER.log(INFO, "Checking table '" + mappedTable.getAlias() + "' for foreign keys ...");
                List<String> checkTableMessages = checkTableForForeignKeys(conn, mappedTable, repairDB);
                messages.addAll(checkTableMessages);
            }
        }
        // 6) CHECK table for indexes
        LOGGER.log(INFO, "Checking tables (" + mappedTables.size() + ") for indexes ...");
        for (MetaTable mappedTable : mappedTables) {
            if (mappedTable.isTable()) { // jen reálné tabulky
                // pro repair mode je treba nastavit povoleni uprav i pokud je jinak zakazano
                LOGGER.log(INFO, "Checking table '" + mappedTable.getAlias() + "' for indexes ...");
                List<String> checkTableMessages = checkTableForIndexes(conn, mappedTable, repairDB);
                messages.addAll(checkTableMessages);
            }
        }
        return messages;
    }

    public List<String> checkTableForMissingColumns(Connection conn, MetaTable mappedTable, boolean repairDB) throws Exception {
        ArrayList messages = new ArrayList();
        List<MetaColumn> mappedColumns = mappedTable.get(MetaTable.COLUMNS);
        Map<String, Map<String, Object>> dbColumns = listDBTableColumns(mappedTable, conn.getMetaData());
        if (mappedColumns.size() != dbColumns.size()) {
            // pokud je rozdilny pocet mapovanych a ziskanych sloupcu - v databazi muzou byt sloupce navic, ktere tam byt nemaji!
            String msg = "  DIFFERENT count of mapped (ujorm) and received (db) columns in table '" + mappedTable.getAlias() + "'! Mapped=" + mappedColumns.size() + ",Received=" + dbColumns.size();
            LOGGER.log(WARN, msg);
            messages.add(msg);
        }
        for (MetaColumn mappedColumn : mappedColumns) {
            String columnName = mappedColumn.get(MetaColumn.NAME).toUpperCase();
            LOGGER.log(INFO, "  Checking column " + columnName + " ...");
            if (!dbColumns.containsKey(columnName)) {
                // !!! MISSING COLUMN
                String msg = "  MISSING db column '" + columnName + "' in table '" + mappedTable.getAlias() + "'";
                LOGGER.log(WARN, msg);
                messages.add(msg);
                if (repairDB) {
                    StringBuilder sql = new StringBuilder();
                    getDialect().printAlterTableAddColumn(mappedColumn, sql);
                    msg = "  REPAIR: Adding column '" + columnName + "' to table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                    LOGGER.log(INFO, msg);
                    executeUpdate(sql, mappedTable);
                }
            }
        }
        return messages;
    }

    public List<String> checkTableForColumnsProperties(Connection conn, MetaTable mappedTable, boolean repairDB) throws Exception {
        ArrayList messages = new ArrayList();
        List<MetaColumn> mappedColumns = mappedTable.get(MetaTable.COLUMNS);
        Map<String, Map<String, Object>> dbColumns = listDBTableColumns(mappedTable, conn.getMetaData());
        for (MetaColumn mappedColumn : mappedColumns) {
            String columnName = mappedColumn.get(MetaColumn.NAME).toUpperCase();
            String columnType = mappedColumn.get(MetaColumn.DB_TYPE).name();
            LOGGER.log(INFO, "  Checking column " + columnName + " ...");
            Map<String, Object> dbColumn = dbColumns.get(columnName);
            if (dbColumn == null) {
                LOGGER.log(WARN, "  Missing column " + columnName);
                continue;
            }

            int mappedMaxCharLength = mappedColumn.getMaxLength();
            if (mappedMaxCharLength != -1
            && mappedColumn.getKey().isTypeOf(CharSequence.class)) {
                LOGGER.log(INFO, "    Checking column char max length...");
                Object dbMaxCharLength = dbColumn.get(COLUMN_DEF_CHAR_LENGTH);
                // pokud je maximalni delka v DB omezena a je ruzna od mapovane... (nektere typy neni mozne omezit na urovni DB, napr. decimal v MSSQL, proto musi vyhovet i omezeni=0)
                if (dbMaxCharLength != null && !dbMaxCharLength.equals(mappedMaxCharLength)) {
                    // MISSING OR DIFFERENT max length LENGTH ON COLUMN
                    String msg = "    MISSING or DIFFERENT max length on column '" + columnName + "' in table '" + mappedTable.getAlias() + "': Mapped=" + mappedMaxCharLength + ", Received=" + dbMaxCharLength;
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        boolean uniqueIndexExists = !mappedColumn.get(MetaColumn.UNIQUE_INDEX).isEmpty();
                        if (uniqueIndexExists) {
                            msg = "    It's not possible to repair column length with unique index.";
                            LOGGER.log(WARN, msg);
                        } else {
                            StringBuilder sql;
                            String indexName = createIndexNameForColumn(mappedColumn, false, mappedTable);
                            MetaIndex mappedIndex = null;
                            if (indexName != null) {
                                mappedIndex = createIndexForColumn(indexName, mappedColumn, mappedTable);
                                mappedIndex.writeValue(MetaIndex.COLUMNS, new ArrayList<>(1));
                                mappedIndex.get(MetaIndex.COLUMNS).add(mappedColumn);
                                // DROP indexu
                                sql = new StringBuilder();
                                getDialectEx().printDropIndex(mappedIndex, sql);
                                msg = "  REPAIR: Dropping index '" + indexName + "' to table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                                LOGGER.log(INFO, msg);
                                executeUpdate(sql, mappedTable);
                            }

                            // ZMENA SLOUPCE
                            sql = new StringBuilder();
                            getDialect().printAlterTableColumn(mappedColumn, false, sql);
                            msg = "  REPAIR: Changing column '" + columnName + "' to table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                            LOGGER.log(INFO, msg);
                            executeUpdate(sql, mappedTable);

                            // ADD indexu
                            if (indexName != null) {
                                sql = new StringBuilder();
                                getDialect().printIndex(mappedIndex, sql);
                                msg = "  REPAIR: Adding index '" + indexName + "' to table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                                LOGGER.log(INFO, msg);
                                executeUpdate(sql, mappedTable);
                            }
                        }
                    }
                }
            }

            if (DEFAULT_VALUES_IN_DB_ALLOWED && mappedColumn.hasDefaultValue()) {
                LOGGER.log(INFO, "    Checking column default constraint name ...");
                Map<String, String> constraintInfo = (Map<String, String>) dbColumn.get(COLUMN_DEF_VALUE_CONSTRAINTS);
                if (constraintInfo != null) {
                    String dbDefaultValueConstraintName = constraintInfo.get("constraint");
                    String expectedDefaultValueConstraintName = getDialect().getNameProvider().buildDefaultConstraintForDefaultValueName(mappedTable, mappedColumn);
                    if (!dbDefaultValueConstraintName.equals(expectedDefaultValueConstraintName)) {
                        // !!! DIFFERENT DEFAULT VALUE CONSTRAINT NAME
                        String msg = "    DIFFERENT default value constraint name on column '" + columnName + "' in table '" + mappedTable.getAlias() + "': Mapped=" + expectedDefaultValueConstraintName + ", Received=" + dbDefaultValueConstraintName;
                        LOGGER.log(WARN, msg);
                        messages.add(msg);
                        if (repairDB) {
                            // TODO: automaticka zmena nazvu constraint pro default value (jen pro MSSQL, pracne!)
                            // StringBuilder sql = new StringBuilder();
                            // getDialect().printXXX(mappedColumn, sql);
                            // executeUpdate(sql, conn.createStatement(), mappedTable);
                        }
                    }

                }

                Object mappedDefaultValue = mappedColumn.getJdbcFriendlyDefaultValue();
                LOGGER.log(INFO, "    Checking column default value '" + mappedDefaultValue + "' ...");
                Object dbDefaultValue = dbColumn.get(COLUMN_DEF_DEFAULT_VALUE);
                if (dbDefaultValue instanceof String) {
                    String tempValue = (String) dbDefaultValue;
                    // prevod DB hodnot na standardni hodnoty
                    if (tempValue.startsWith("(N'") && tempValue.endsWith("')")) {
                        tempValue = tempValue.substring(3, tempValue.length() - 2);
                        dbDefaultValue = tempValue;
                    } else if (tempValue.startsWith("('") && tempValue.endsWith("')")) {
                        tempValue = tempValue.substring(2, tempValue.length() - 2);
                        dbDefaultValue = tempValue;
                    } else if (!columnType.equalsIgnoreCase("BOOLEAN") && tempValue.matches("\\(\\(.*\\)\\)")) {
                        Pattern pattern = Pattern.compile("\\(\\((.*)\\)\\)");
                        Matcher matcher = pattern.matcher(tempValue);
                        if (matcher.find()) {
                            dbDefaultValue = matcher.group(1);
                        }
                    } else if (columnType.equalsIgnoreCase("BOOLEAN") && (tempValue.equals("((1))") || tempValue.equals("1"))) {
                        dbDefaultValue = true;
                    } else if (columnType.equalsIgnoreCase("BOOLEAN") && (tempValue.equals("((0))") || tempValue.equals("0"))) {
                        dbDefaultValue = false;
                    } else if (columnType.equalsIgnoreCase("DECIMAL")) {
                        dbDefaultValue = new BigDecimal(tempValue);
                    }
                }

                boolean defaultValuesEqual = true;
                // mozne problemy:
                // 1) mapped == null AND db != null
                // 2) mapped != null AND db == null
                // 3) mapped != null AND mapped != db
                if ((mappedDefaultValue == null && dbDefaultValue != null)
                        || (mappedDefaultValue != null && dbDefaultValue == null)
                        || (mappedDefaultValue != null && !mappedDefaultValue.toString().equals("" + dbDefaultValue))) {
                    defaultValuesEqual = false;
                }
                // vyjimka pro BigDecimal - db muze vracet 0.00, zatimco Ujorm 0 -> pritom se jedna o stejne hodnoty
                if (dbDefaultValue instanceof BigDecimal && mappedDefaultValue instanceof BigDecimal) {
                    BigDecimal decimalDb = (BigDecimal) dbDefaultValue;
                    BigDecimal decimalMapped = (BigDecimal) mappedDefaultValue;
                    defaultValuesEqual = decimalDb.compareTo(decimalMapped) == 0;
                }

                if (!defaultValuesEqual) {
                    // !!! MISSING OR DIFFERENT DEFAULT VALUE
                    String msg = "    MISSING or DIFFERENT default value on column '" + columnName + "' in table '" + mappedTable.getAlias() + "': Mapped=" + mappedDefaultValue + ", Received=" + dbDefaultValue;
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        // TODO: automaticke odebrani a pridani default value
                        // StringBuilder sql = new StringBuilder();
                        // getDialect().printXXX(mappedColumn, sql);
                        // executeUpdate(sql, conn.createStatement(), mappedTable);
                    }
                }
            } else {
                LOGGER.log(INFO, "    Checking column has no default value ...");
                Object dbDefaultValue = dbColumn.get(COLUMN_DEF_DEFAULT_VALUE);
                if (!isDefaultValueNull(dbColumn)) {
                    // !!! DIFFERENT DEFAULT VALUE - THERE SHOULD BE NO DEFFAULT VALUE
                    String msg = "    DIFFERENT default value on column '" + columnName + "' in table '" + mappedTable.getAlias() + "': Mapped=" + null + ", Received=" + dbDefaultValue;
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        // TODO: automaticke odebrani default value
                        // StringBuilder sql = new StringBuilder();
                        // getDialect().printXXX(mappedColumn, sql);
                        // executeUpdate(sql, conn.createStatement(), mappedTable);
                    }
                }

            }
            Integer nullableConstant = (Integer) dbColumn.get(COLUMN_DEF_NULLABLE);
            if (mappedColumn.isMandatory() || mappedColumn.isPrimaryKey()) {
                LOGGER.log(INFO, "    Checking column not null ...");
                if (nullableConstant == null || nullableConstant != 0) {
                    // MISSING NOT NULL ON COLUMN
                    String msg = "    MISSING not null on column '" + columnName + "' in table '" + mappedTable.getAlias() + "'";
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        // TODO: automaticka zmena z nullable na not null
                        // StringBuilder sql = new StringBuilder();
                        // getDialect().printXXX(mappedColumn, sql);
                        // msg = "  REPAIR: Adding XXX '" + columnName + "' to table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                        // LOGGER.log(INFO, msg);
                        // executeUpdate(sql, conn.createStatement(), mappedTable);
                    }
                }
            } else {
                LOGGER.log(INFO, "    Checking column nullable ...");
                if (nullableConstant == null || nullableConstant != 1) {
                    // !!! MISSING NULLABLE ON COLUMN
                    String msg = "    MISSING nullable on column '" + columnName + "' in table '" + mappedTable.getAlias() + "'";
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        // TODO: automaticka zmena z not null na nullable
                        // StringBuilder sql = new StringBuilder();
                        // getDialect().printXXX(mappedColumn, sql);
                        // msg = "  REPAIR: Adding XXX '" + columnName + "' to table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                        // LOGGER.log(INFO, msg);
                        // executeUpdate(sql, conn.createStatement(), mappedTable);
                    }
                }
            }
        }
        return messages;
    }

    private boolean isDefaultValueNull(Map<String, Object> dbColumn) {
        Object dbDefaultValue = dbColumn.get(COLUMN_DEF_DEFAULT_VALUE);
        Object dbColumnName = dbColumn.get(COLUMN_DEF_NAME);

        // specific for MySQL - 'id' column is allowed to have default value
        if (isDialectTypeMySql() && dbColumnName.equals("id") && dbDefaultValue != null && dbDefaultValue.equals("0")) {
            return true;
        }
        return dbDefaultValue == null;
    }

    public List<String> checkTableForPrimaryKeys(Connection conn, MetaTable mappedTable, boolean repairDB) throws Exception {
        ArrayList messages = new ArrayList();
        // check primary keys
        List<MetaColumn> mappedPKeyColumns = Arrays.asList(mappedTable.getFirstPK()); // check is only for first primary key now
        Map<String, Map<String, Object>> dbPKeyColumns = listDBTablePKey(mappedTable, conn.getMetaData());
        if (mappedPKeyColumns.size() != dbPKeyColumns.size()) {
            // pokud je rozdilny pocet mapovanych a ziskanych sloupcu - v databazi muzou byt klice navic, ktere tam byt nemaji!
            String msg = "  DIFFERENT count of mapped (ujorm) and received (db) primary keys in table '" + mappedTable.getAlias() + "'! Mapped=" + mappedPKeyColumns.size() + ",Received=" + dbPKeyColumns.size();
            LOGGER.log(WARN, msg);
            messages.add(msg);
        }
        for (MetaColumn mappedColumn : mappedPKeyColumns) {
            String columnName = mappedColumn.get(MetaColumn.NAME).toUpperCase();
            //In MySQL, the name of a PRIMARY KEY is PRIMARY
            if (isDialectTypeMySql() && columnName.equals(ID_COLUMN_NAME) && dbPKeyColumns.containsKey(MYSQL_PRIMARY_KEY_NAME)) {
                continue;
            }
            String pkeyName = getDialect().getNameProvider().buildPrimaryKeyName(mappedTable, Arrays.asList(mappedColumn));
            LOGGER.log(INFO, "  Checking primary key '" + pkeyName + "' on column " + columnName + " ...");
            if (!dbPKeyColumns.containsKey(pkeyName.toUpperCase())) {
                String dbPKeyName = null;
                for (String dbPKey : dbPKeyColumns.keySet()) {
                    String keyName = dbPKeyColumns.get(dbPKey).get(PKEY_DEF_COLUMN_NAME).toString().toUpperCase();
                    if (columnName.equals(keyName)) {
                        dbPKeyName = dbPKey;
                        break;
                    }
                }
                if (dbPKeyName != null) {
                    // !!! DIFFERENT NAME OF PRIMARY KEY
                    String msg = "  DIFFERENT primary key name on column '" + columnName + "' in table '" + mappedTable.getAlias() + "', Mapped=" + pkeyName + ", Received=" + dbPKeyName;
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        // maybe it's possible to rename primary key, or drop and create...
                    }
                } else {
                    // !!! MISSING PRIMARY KEY
                    String msg = "  MISSING primary key '" + pkeyName + "' on column '" + columnName + "' in table '" + mappedTable.getAlias() + "'";
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        StringBuilder sql = new StringBuilder();
                        MetaTable table = mappedColumn.getTable();
                        getDialectEx().printPrimaryKey(mappedColumn, sql);
                        msg = "  REPAIR: Adding primary key '" + pkeyName + "' on column '" + columnName + "' in table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                        LOGGER.log(INFO, msg);
                        executeUpdate(sql, table);
                    }

                }

            }
        }
        return messages;
    }

    public List<String> checkTableForForeignKeys(Connection conn, MetaTable mappedTable, boolean repairDB) throws Exception {
        ArrayList messages = new ArrayList();
        // check foreign keys
        List<MetaColumn> mappedFKeyColumns = mappedTable.getForeignColumns();
        Map<String, Map<String, Object>> dbFKeyColumns = listDBTableFKey(mappedTable, conn.getMetaData());
        if (mappedFKeyColumns.size() != dbFKeyColumns.size()) {
            // pokud je rozdilny pocet mapovanych a ziskanych sloupcu - v databazi muzou byt klice navic, ktere tam byt nemaji!
            String msg = "  DIFFERENT count of mapped (ujorm) and received (db) foreign keys in table '" + mappedTable.getAlias() + "'! Mapped=" + mappedFKeyColumns.size() + ",Received=" + dbFKeyColumns.size();
            LOGGER.log(WARN, msg);
            messages.add(msg);
        }
        for (MetaColumn mappedColumn : mappedFKeyColumns) {
            String columnName = mappedColumn.get(MetaColumn.NAME).toUpperCase();
            String fkeyName = getDialectEx().buildConstraintName(mappedColumn, mappedTable);
            LOGGER.log(INFO, "  Checking foreign key '" + fkeyName + "' on column " + columnName + " ...");
            if (!dbFKeyColumns.containsKey(fkeyName.toUpperCase())) {
                // !!! MISSING FOREIGN KEY
                String msg = "  MISSING foreign key '" + fkeyName + "' on column '" + columnName + "' in table '" + mappedTable.getAlias() + "'";
                LOGGER.log(WARN, msg);
                messages.add(msg);
                if (repairDB) {
                    StringBuilder sql = new StringBuilder();
                    getDialect().printForeignKey(mappedColumn, sql);
                    msg = "  REPAIR: Adding foreign key '" + fkeyName + "' on column '" + columnName + "' in table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                    LOGGER.log(INFO, msg);
                    executeUpdate(sql, mappedColumn.getTable());
                }
            }
        }
        return messages;
    }

    public List<String> checkTableForIndexes(Connection conn, MetaTable mappedTable, boolean repairDB) throws Exception {
        ArrayList messages = new ArrayList();
        // checking indexes
        List<MetaColumn> mappedPKeyColumns = Arrays.asList(mappedTable.getFirstPK()); // check is only for first primary key now
        Set<String> dbTableIndexes = listDBTableIndexes(mappedTable, conn.getMetaData(), mappedPKeyColumns);
        Collection<MetaIndex> mappedIndexes = mappedTable.getIndexCollection();
        if (mappedIndexes.size() != dbTableIndexes.size()) {
            // pokud je rozdilny pocet mapovanych a ziskanych indexu - v databazi muzou byt indexy navic, ktere tam byt nemaji!
            String msg = "  DIFFERENT count of mapped (ujorm) and received (db) indexes in table '" + mappedTable.getAlias() + "'! Mapped=" + mappedIndexes.size() + ",Received=" + dbTableIndexes.size();
            LOGGER.log(WARN, msg);
            messages.add(msg);
        }
        for (MetaIndex mappedIndex : mappedIndexes) {
            if (!mappedIndex.get(MetaIndex.UNIQUE)) {
                String indexName = mappedIndex.get(MetaIndex.NAME).toUpperCase();
                LOGGER.log(INFO, "  Checking index " + indexName + " ...");
                if (!dbTableIndexes.contains(indexName)) {
                    // !!! MISSING INDEX
                    String msg = "  MISSING db index '" + indexName + "' in table '" + mappedTable.getAlias() + "'";
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        StringBuilder sql = new StringBuilder();
                        getDialect().printIndex(mappedIndex, sql);
                        msg = "  REPAIR: Adding index '" + indexName + "' to table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                        LOGGER.log(INFO, msg);
                        executeUpdate(sql, mappedTable);
                    }
                }
            } else {
                String indexName = getDialect().getNameProvider().getUniqueConstraintName(mappedIndex.get(MetaIndex.COLUMNS)).toUpperCase();
                LOGGER.log(INFO, "  Checking unique index " + indexName + " ...");
                if (!dbTableIndexes.contains(indexName)) {
                    // !!! MISSING UNIQUE INDEX
                    String msg = "  MISSING db unique index '" + indexName + "' in table '" + mappedTable.getAlias() + "'";
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        StringBuilder sql = new StringBuilder();
                        getDialectEx().printUniqueConstraint(MetaIndex.COLUMNS.of(mappedIndex), sql);
                        msg = "  REPAIR: Adding unique index '" + indexName + "' to table '" + mappedTable.getAlias() + "' with SQL:\n" + sql;
                        LOGGER.log(INFO, msg);
                        executeUpdate(sql, mappedTable);
                    }
                }
            }
        }
        return messages;
    }

    private List<? extends String> checkUjormPKSupport(Connection conn, List<MetaTable> mappedTables, boolean repairDB) throws Exception {
        List<String> messages = new ArrayList<>();
        Set<String> tableSequenceIds = new HashSet<>();

        for (MetaTable mappedTable : mappedTables) {
            if (mappedTable.getSequencer().isSequenceTableRequired()) {
                LOGGER.log(INFO, "Checking ujorm_pk_support for table '" + mappedTable.getAlias() + "' ...");

                // TABLE MAX ID
                // construct sql
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT MAX(");
                getDialect().printColumnAlias(mappedTable.getFirstPK(), sql);
                sql.append(") FROM ");
                getDialect().printFullTableName(mappedTable, sql);
                tableSequenceIds.add(getDialect().printFullTableName(mappedTable, true, new StringBuilder()).toString());
                // get from db
                PreparedStatement statement = conn.prepareStatement(sql.toString());
                ResultSet rs = null;
                Long tableMaxID = null;
                try {
                    rs = statement.executeQuery();
                    rs.next();
                    tableMaxID = rs.getLong(1);
                    LOGGER.log(INFO, "  Table max id = " + tableMaxID);
                } finally {
                    MetaDatabase.close(null, statement, rs, true);
                }

                // UJORM MAX ID
                long[] sqMap = mappedTable.getSequencer().getCurrentDBSequence(conn, null);
                Long ujormMaxId = sqMap != null ? sqMap[UjoSequencer.SEQ_LIMIT] : null; // returns X as limit (last assigned number), "X+1" will be next assigned ID
                LOGGER.log(INFO, "  Ujorm max id = " + ujormMaxId);

                if (tableMaxID > 0 && ujormMaxId == null) {
                    // !!! CORRUPTED SEQUENCE
                    String msg = "  CORRUPTED db sequence for table '" + mappedTable.getAlias() + "': there are real data and sequence is not created! table max id: " + tableMaxID;
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        sql = new StringBuilder();
                        int step = mappedTable.getSequencer().getIncrement();
                        ujormMaxId = ((tableMaxID / step) + 1) * step;
                        getDialect().printSequenceInitWithValues(mappedTable.getSequencer(), ujormMaxId, step, sql);

                        msg = "  REPAIR: Adding new sequence for table '" + mappedTable.getAlias() + "' with new ujormMaxId = '" + ujormMaxId + "' with SQL:\n" + sql;
                        LOGGER.log(INFO, msg);

                        String tableName = getDialect().printFullTableName(mappedTable, true, new StringBuilder()).toString();
                        PreparedStatement statement2 = conn.prepareStatement(sql.toString());
                        try {
                            statement2.setString(1, tableName);
                            statement2.executeUpdate();
                        } finally {
                            MetaDatabase.close(null, statement2, null, true);
                        }

                        mappedTable.getSequencer().reset();
                    }
                } else if ((ujormMaxId != null) && (tableMaxID > ujormMaxId)) {
                    // !!! CORRUPTED SEQUENCE
                    String msg = "  CORRUPTED db sequence for table '" + mappedTable.getAlias() + "': ujorm max id > table max id: " + ujormMaxId + ">" + tableMaxID;
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        sql = new StringBuilder();
                        int step = (int) sqMap[UjoSequencer.SEQ_STEP];
                        ujormMaxId = ((tableMaxID / step) + 1) * step;
                        getDialectEx().printSequenceNextValueWithValues(mappedTable.getSequencer(), ujormMaxId, sql);

                        msg = "  REPAIR: Updating sequence for table '" + mappedTable.getAlias() + "' with new ujormMaxId = '" + ujormMaxId + "' with SQL:\n" + sql;
                        LOGGER.log(INFO, msg);

                        String tableName = getDialect().printFullTableName(mappedTable, true, new StringBuilder()).toString();
                        PreparedStatement statement2 = conn.prepareStatement(sql.toString());
                        try {
                            statement2.setString(1, tableName);
                            statement2.executeUpdate();
                        } finally {
                            MetaDatabase.close(null, statement2, null, true);
                        }

                        mappedTable.getSequencer().reset();
                    }
                }

            }
        }

        LOGGER.log(INFO, "Checking ujorm_pk_support for invalid sequences ...");

        StringBuilder sql = new StringBuilder();
        getDialectEx().printSequenceListAllId(findFirstSequencer(), sql);
        // get from db
        PreparedStatement statement = conn.prepareStatement(sql.toString());
        ResultSet rs = null;
        try {
            rs = statement.executeQuery();
            while (rs.next()) {
                String seqTableId = rs.getString(1);
                if (!tableSequenceIds.contains(seqTableId)) {
                    String msg = "  INVALID db sequence '" + seqTableId + "': there is no mapped table for that sequence";
                    LOGGER.log(WARN, msg);
                    messages.add(msg);
                    if (repairDB) {
                        sql = new StringBuilder();
                        getDialect().printSequenceDeleteById(findFirstSequencer(), seqTableId, sql);

                        msg = "  REPAIR: Deleting invalid sequence '" + seqTableId + "' with SQL:\n" + sql;
                        LOGGER.log(INFO, msg);

                        PreparedStatement statement2 = conn.prepareStatement(sql.toString());
                        try {
                            statement2.setString(1, seqTableId);
                            statement2.executeUpdate();
                        } finally {
                            MetaDatabase.close(null, statement2, null, true);
                        }
                    }
                }
            }
        } finally {
            MetaDatabase.close(null, statement, rs, true);
        }
        return messages;
    }

    public Map<String, Map<String, Object>> listDBTableColumns(MetaTable table, final DatabaseMetaData dmd) throws SQLException {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        String schemaDBName = dbIdentifier(MetaTable.SCHEMA.of(table), dmd);
        String tableDBName = dbIdentifier(MetaTable.NAME.of(table), dmd);

        // kontroluje se jen pro MSSQL, v MySQL nemají default hodnoty vlastní názvy
        HashMap<String, HashMap<String, String>> defaultValueConstraints = null;
        if (isDialectTypeMSSql()) {
            PreparedStatement statement = dmd.getConnection().prepareStatement("SELECT o.name [table], c.name [column], object_name(d.constid) [constraint], cm.text [default_value] FROM sysconstraints d, sysobjects o, syscolumns c, syscomments cm WHERE  (o.id = d.id)   AND (c.id = o.id AND c.colid = d.colid)   AND (cm.id = d.constid)   AND (d.[status] & 5 = 5) AND (o.xtype = 'U')  AND (o.name = ?) ORDER BY [table], [column], [constraint];");
            ResultSet rs = null;
            try {
                statement.setString(1, tableDBName);
                rs = statement.executeQuery();
                defaultValueConstraints = new HashMap<>();
                while (rs.next()) {
                    String columnName = rs.getString("column");
                    HashMap<String, String> constraintInfo = new HashMap<>();
                    defaultValueConstraints.put(columnName, constraintInfo);
                    constraintInfo.put("constraint", rs.getString("constraint"));
                    constraintInfo.put("default_value", rs.getString("default_value"));
                }
            } finally {
                MetaDatabase.close(null, statement, rs, true);
            }
        }

        boolean catalog = isCatalog();
        try (ResultSet rs = dmd.getColumns(catalog ? schemaDBName : null, catalog ? null : schemaDBName, tableDBName, null)) {
            while (rs.next()) {
                String columnName = rs.getString(COLUMN_DEF_NAME);
                HashMap<String, Object> columnInfo = new HashMap<>();
                result.put(columnName.toUpperCase(), columnInfo);
                columnInfo.put(COLUMN_DEF_NAME, columnName);
                columnInfo.put(COLUMN_DEF_DEFAULT_VALUE, rs.getString(COLUMN_DEF_DEFAULT_VALUE));
                columnInfo.put(COLUMN_DEF_NULLABLE, rs.getInt(COLUMN_DEF_NULLABLE));
                columnInfo.put(COLUMN_DEF_CHAR_LENGTH, rs.getInt(COLUMN_DEF_CHAR_LENGTH));
                columnInfo.put(COLUMN_DEF_VALUE_CONSTRAINTS, defaultValueConstraints != null ? defaultValueConstraints.get(columnName) : null);
            }
        }
        return result;
    }

    public Map<String, Map<String, Object>> listDBTablePKey(MetaTable table, final DatabaseMetaData dmd) throws SQLException {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        String schemaDBName = dbIdentifier(MetaTable.SCHEMA.of(table), dmd);
        String tableDBName = dbIdentifier(MetaTable.NAME.of(table), dmd);
        boolean catalog = isCatalog();
        try (ResultSet rs = dmd.getPrimaryKeys(catalog ? schemaDBName : null, catalog ? null : schemaDBName, tableDBName)) {
            while (rs.next()) {
                String keyName = rs.getString(PKEY_DEF_NAME);
                String columnName = rs.getString(PKEY_DEF_COLUMN_NAME);
                HashMap<String, Object> info = new HashMap<>();
                result.put(keyName.toUpperCase(), info);
                info.put(PKEY_DEF_NAME, keyName);
                info.put(PKEY_DEF_COLUMN_NAME, columnName);
            }
        }
        return result;
    }

    public Map<String, Map<String, Object>> listDBTableFKey(MetaTable table, final DatabaseMetaData dmd) throws SQLException {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        String schemaDBName = dbIdentifier(MetaTable.SCHEMA.of(table), dmd);
        String tableDBName = dbIdentifier(MetaTable.NAME.of(table), dmd);
        boolean catalog = isCatalog();
        try (ResultSet rs = dmd.getImportedKeys(catalog ? schemaDBName : null, catalog ? null : schemaDBName, tableDBName)) {
            while (rs.next()) {
                String keyName = rs.getString(FKEY_DEF_NAME);
                HashMap<String, Object> info = new HashMap<>();
                result.put(keyName.toUpperCase(), info);
                info.put(FKEY_DEF_NAME, keyName);
            }
        }
        return result;
    }

    public Set<String> listDBTableIndexes(MetaTable table, final DatabaseMetaData dmd, List<MetaColumn> skippedPrimaryKeyColumns) throws SQLException {
        ResultSet rs;
        Set<String> dbTableIndexes = new HashSet<>();
        String schemaDBName = dbIdentifier(MetaTable.SCHEMA.of(table), dmd);
        String tableDBName = dbIdentifier(MetaTable.NAME.of(table), dmd);
        boolean catalog = isCatalog();
        rs = dmd.getIndexInfo(catalog ? schemaDBName : null, catalog ? null : schemaDBName, tableDBName, false // unique
                , false // approximate
                );
        while (rs.next()) {
            String name = rs.getString("INDEX_NAME");
            String targetColumnName = rs.getString("COLUMN_NAME");
            boolean skip = false;
            if (name != null) {
                if (skippedPrimaryKeyColumns != null) {
                    for (MetaColumn pkColumn : skippedPrimaryKeyColumns) {
                        String skippedPrimaryKeyColumnName = pkColumn.get(MetaColumn.NAME);
                        if (skippedPrimaryKeyColumnName.toUpperCase().equals(targetColumnName.toUpperCase())) {
                            // skip primary key column index
                            skip = true;
                            break;
                        }
                    }
                }
                if (!skip) {
                    dbTableIndexes.add(name.toUpperCase());
                }
            }
        }
        rs.close();
        return dbTableIndexes;
    }

    private boolean isDialectTypeMySql() {
        return getDialect() instanceof MySqlDialect;
    }

    private boolean isDialectTypeMSSql() {
        return getDialect() instanceof MSSqlDialect;
    }

    /** Create an Index For the Column. The method is called from the {@link MetaDbServiceEx} class */
    public MetaIndex createIndexForColumn(String idxName, MetaColumn column, MetaTable metaTable) {
        MetaIndex mIndex = new MetaIndex(idxName, metaTable);
        boolean isUniqueIndexExists = MetaColumn.UNIQUE_INDEX.getItemCount(column) > 0;
        MetaIndex.UNIQUE.setValue(mIndex, isUniqueIndexExists);
        return mIndex;
    }

    /** Create an Index For the Column. The method is called from the {@link MetaDbServiceEx} class */
    public String createIndexNameForColumn(MetaColumn column, boolean uniqueIndex, MetaTable metaTable) {
        String metaIdxName;
        if (uniqueIndex) {
            metaIdxName = MetaColumn.UNIQUE_INDEX.getFirstItem(column);
        } else {
            metaIdxName = MetaColumn.INDEX.getFirstItem(column);
        }
        if (metaIdxName.isEmpty() && column.isForeignKey()) {
            metaIdxName = MetaColumn.AUTO_INDEX_NAME;
        }

        assert metaIdxName.length() > 0;

        // automatic indexes ("AUTO" or foreign keys)
        if (MetaColumn.AUTO_INDEX_NAME.equalsIgnoreCase(metaIdxName)) {
            final SqlNameProvider nameProvider = metaTable.getDatabase().getDialect().getNameProvider();
            if (uniqueIndex) {
                metaIdxName = nameProvider.getUniqueConstraintName(column);
            } else {
                metaIdxName = nameProvider.getIndexName(column);
            }
        }
        return metaIdxName;
    }
}
