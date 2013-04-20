/*
 *  Copyright 2013-2013 Pavel Ponec
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
package org.ujorm.orm.template;

import java.io.IOException;
import org.ujorm.Key;
import org.ujorm.core.annot.Immutable;
import org.ujorm.orm.*;
import org.ujorm.orm.impl.ColumnWrapperImpl;
import org.ujorm.orm.impl.TableWrapperImpl;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * The class for building any SQL statement using Ujorm Keys
 *
 * @author Pavel Ponec
 */
@Immutable
public class AliasTable<UJO extends OrmUjo> {
    
    /** Default handler */
    private final OrmHandler handler;
    /** Meta table */
    private final TableWrapper table;

    public AliasTable(Class<UJO> table, String alias, OrmHandler handler) {
        this(new TableWrapperImpl(handler.findTableModel(table), alias), handler);
    }

    protected AliasTable(TableWrapper table, OrmHandler handler) {
        this.handler = handler;
        this.table = table;
    }

    /** Returns SQL Dialect */
    protected SqlDialect getDialect() {
        return table.getModel().getDatabase().getDialect();
    }

    /** Get table Alias */
    public TableWrapper getTableModel() {
        return table;
    }

    /** Returns Table witn Alias */
    public String table() throws IllegalStateException {
        try {
            StringBuilder result = new StringBuilder(32);
            getDialect().printTableAliasDefinition(table, result);
            return result.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e.getClass().getSimpleName(), e);
        }
    }
    
    /** Print one column including alias */
    public <T> String column(Key<UJO, T> key) throws IllegalStateException {
        return printColumn
                ( findColumnModel(key)
                , null
                , new StringBuilder(32))
                .toString();
    }

    /** Returns one column including an <strong>default</strong> special alias name after the 'AS' phrase.  */
    public <T> String columnAs(Key<UJO, T> key) throws IllegalStateException {
        return columnAs(key, null);
    }
    
    /** Returns one column including the <strong>required</strong> alias name after the 'AS' phrase.
     * @param key Related key
     * @param columnAlias an alias String or a Key instance for the key Name, the {@code null} value use an Key name.
     * @return  Returns one column including the <strong>required</strong> alias name after the 'AS' phrase.
     * @throws IllegalStateException 
     */
    public <T> String columnAs(Key<UJO, T> key, CharSequence columnAlias) throws IllegalStateException {
        return printColumn
                ( findColumnModel(key)
                , columnAlias != null ? columnAlias : key.getName()
                , new StringBuilder(64)).toString();
    }

    /** Call the column() method for all Keys of Ujorm */
    public String allColumns() throws IllegalStateException {
        return allColumns(false);
    }
    
    /** Call the column() method for all Keys of Ujorm */
    public String allColumns(boolean includeKeyAlias) throws IllegalStateException {
        final StringBuilder result = new StringBuilder(128);
        for (ColumnWrapper column : this.table.getModel().getColumns()) {
            if (!column.getModel().isColumn()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(", ");
            }
            printColumn(column.getModel(), includeKeyAlias ? column.getKey().getName() : null, result);
        }
        return result.toString();
    }
    
    /** Return the same result as {@link #table} */
    @Override
    public String toString() {
        return table();
    }

    // ------------ HELPER METHODS ------------
    
    /** Returns one column including an default special alias after the 'AS' phrase.
     * @param key Related key
     * @param columnAlias an alias name or a Key instance, the {@code null} value use an Key name.
     * @return Returns one column including an default special alias.
     * @throws IllegalStateException 
     */
    protected StringBuilder printColumn(MetaColumn column, final CharSequence columnAlias, StringBuilder out) throws IllegalStateException {
        try {
            getDialect().printColumnAlias(new ColumnWrapperImpl(column, table), out);
            if (columnAlias != null) {
                out.append(" AS ").append(columnAlias);
            }
            return out;
        } catch (IOException e) {
            throw new IllegalStateException(e.getClass().getSimpleName(), e);
        }
    }    

    /** Find Column Model or throw an IllegalArgumentException. */
    protected MetaColumn findColumnModel(Key<UJO, ?> key) throws IllegalArgumentException {
        return (MetaColumn) handler.findColumnModel((Key) key, true);
    }
    
    // ------------ STATIC METHODS ------------
    
    /** Create new Alias with required name */
    public static <UJO extends OrmUjo> AliasTable<UJO> of(Class<UJO> table, String alias, OrmHandler handler) {
        return new AliasTable<UJO>(table, alias, handler);
    }

    /** Create new Alias with default name */
    public static <UJO extends OrmUjo> AliasTable<UJO> of(Class<UJO> table, OrmHandler handler) {
        return new AliasTable<UJO>(handler.findTableModel(table), handler);
    }
}