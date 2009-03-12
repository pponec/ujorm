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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.DbHandler;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.orm.annot.GenerationType;

/**
 * Database column metadata
 * @author pavel
 */
public class OrmColumn extends OrmRelation2Many {

    /** DB primary key */
    public static final UjoProperty<OrmColumn,Boolean> PRIMARY_KEY = newProperty("primaryKey", false);
    /** Database Type */
    public static final UjoProperty<OrmColumn,DbType> DB_TYPE = newProperty("dbType", DbType.Automatic);
    /** Column NOT-NULL */
    public static final UjoProperty<OrmColumn,Boolean> MANDATORY = newProperty("mandatory", false);
    /** Column value length */
    public static final UjoProperty<OrmColumn,Integer> MAX_LENGTH = newProperty("maxLength", -1);
    /** Column value precision */
    public static final UjoProperty<OrmColumn,Integer> PRECISION = newProperty("precision", -1);
    /** DB Default value */
    public static final UjoProperty<OrmColumn,String> DEFAULT_VALUE = newProperty("default", "");
    /** The column is included in the index of the name */
    public static final UjoProperty<OrmColumn,String> INDEX_NAME = newProperty("indexName", "");
    /** DB primary key generator */
    public static final UjoProperty<OrmColumn,GenerationType> PRIMARY_KEY_GEN = newProperty("primaryKeyGenerator", GenerationType.MEMO_SEQUENCE);


    /** Foreign column names. */
    private String[] foreignNames = null;
    private static final String[] EMPTY_NAMES = new String[0];


    public OrmColumn(OrmTable table, UjoProperty tableProperty) {
        super(table, tableProperty);

        Field field = UjoManager.getInstance().getPropertyField(OrmTable.DB_PROPERTY.of(table).getItemType(), tableProperty);
        Column column = field.getAnnotation(Column.class);

        if (column!=null) {
            PRIMARY_KEY.setValue(this, column.pk());
            MANDATORY  .setValue(this, column.mandatory() || column.pk());
            MAX_LENGTH .setValue(this, column.maxLenght());
            PRECISION  .setValue(this, column.precision());
            DB_TYPE    .setValue(this, column.type());
            INDEX_NAME .setValue(this, column.indexName());
        }
        if (DB_TYPE.isDefault(this)) {
            OrmTable.DATABASE.of(table).changeDbType(this);
        }
        if (MAX_LENGTH.isDefault(this)) {
            OrmTable.DATABASE.of(table).changeDbLength(this);
        }
    }

    /** It is a DB column (either a value of a foreign key) */
    @Override
    public boolean isColumn() {
        return true;
    }

    /** Is it a Foreign Key ? */
    @Override
    public boolean isForeignKey() {
        final boolean result = OrmColumn.TABLE_PROPERTY.of(this).isTypeOf(TableUjo.class);
        return result;
    }

    /** Returns an original foreign columns in case a foreign column. */
    @SuppressWarnings("unchecked")
    public List<OrmColumn> getForeignColumns() {
        List<OrmColumn> result;
        Class type = OrmColumn.TABLE_PROPERTY.of(this).getType();
        OrmTable table = DbHandler.getInstance().findTableModel(type);
        if (table!=null) {
            OrmPKey pk = OrmTable.PK.of(table);
            result = OrmPKey.COLUMNS.getList(pk);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /** Returns names of foreign columns.
     * <br>TODO: Is a time to an optimalization ?
     */
    @SuppressWarnings("unchecked")
    private String[] getForeignColumnNames() {
        if (foreignNames==null) {
            final Class type = OrmColumn.TABLE_PROPERTY.of(this).getType();
            final OrmTable foreignTable = DbHandler.getInstance().findTableModel(type);
            if (foreignTable!=null && isForeignKey()) {
                final OrmPKey pk = OrmTable.PK.of(foreignTable);
                final List<OrmColumn> dbColumns = OrmPKey.COLUMNS.getList(pk);
                final StringTokenizer tokenizer = new StringTokenizer(dbColumns.size()==1 ? NAME.of(this) : "", ", ");

                ArrayList<String> fNames = new ArrayList<String>(dbColumns.size());
                for (int i=0; i<dbColumns.size(); i++) {
                    String name;
                    if (tokenizer.hasMoreTokens()) {
                        name = tokenizer.nextToken();
                    } else {
                        name = "fk_"
                          // + OrmTable.NAME.of(foreignTable)
                             + OrmColumn.NAME.of(this)
                             + "_"
                             + OrmColumn.NAME.of(dbColumns.get(i))
                             ;
                    }
                    fNames.add(name);
                }
                foreignNames = fNames.toArray(new String[fNames.size()]);
            } else {
                foreignNames = EMPTY_NAMES;
            }
        }
        return foreignNames;
    }

    /** Returns a name of foreign column by index */
    public String getForeignColumnName(int index) {
        final String result = getForeignColumnNames()[index];
        return result;
    }

    /** Returns a property value from a table */
    @SuppressWarnings("unchecked")
    public Object getValue(TableUjo table) {
        final UjoProperty property = TABLE_PROPERTY.of(this);
        final Object result = property.of(table);
        return result;
    }

    /** Returns a TABLE and COLUMN names. */
    @Override
    public String toString() {
        final  OrmTable table = TABLE.of(this);
        return OrmTable.NAME.of(table) + '.' + NAME.of(this);
    }


}
