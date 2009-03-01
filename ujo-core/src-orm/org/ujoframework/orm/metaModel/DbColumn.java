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
import java.util.Collections;
import java.util.List;
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
public class DbColumn extends DbRelation2m {

    /** DB primary key */
    public static final UjoProperty<DbColumn,Boolean> PRIMARY_KEY = newProperty("primaryKey", false);
    /** Database Type */
    public static final UjoProperty<DbColumn,DbType> DB_TYPE = newProperty("dbType", DbType.Automatic);
    /** Column NOT-NULL */
    public static final UjoProperty<DbColumn,Boolean> MANDATORY = newProperty("mandatory", false);
    /** Column value length */
    public static final UjoProperty<DbColumn,Integer> MAX_LENGTH = newProperty("maxLength", -1);
    /** Column value precision */
    public static final UjoProperty<DbColumn,Integer> PRECISION = newProperty("precision", -1);
    /** DB Default value */
    public static final UjoProperty<DbColumn,String> DEFAULT_VALUE = newProperty("default", "");
    /** The column is included in the index of the name */
    public static final UjoProperty<DbColumn,String> INDEX_NAME = newProperty("indexName", "");
    /** DB primary key generator */
    public static final UjoProperty<DbColumn,GenerationType> PRIMARY_KEY_GEN = newProperty("primaryKeyGenerator", GenerationType.MEMO_SEQUENCE);

    public DbColumn(DbTable table, UjoProperty tableProperty) {
        super(table, tableProperty);

        Field field = UjoManager.getInstance().getPropertyField(DbTable.DB_PROPERTY.of(table).getItemType(), tableProperty);
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
            DbTable.DATABASE.of(table).changeDbType(this);
        }
        if (MAX_LENGTH.isDefault(this)) {
            DbTable.DATABASE.of(table).changeDbLength(this);
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
        final boolean result = DbColumn.TABLE_PROPERTY.of(this).isTypeOf(TableUjo.class);
        return result;
    }

    /** Returns foreign columns if the column is a Foreign colum. */
    @SuppressWarnings("unchecked")
    public List<DbColumn> getForeignColumns() {
        List<DbColumn> result;
        Class type = DbColumn.TABLE_PROPERTY.of(this).getType();
        DbTable table = DbHandler.getInstance().findTableModel(type);
        if (table!=null) {
            DbPK pk = DbTable.PK.of(table);
            result = pk.COLUMNS.getList(pk);
        } else {
            result = Collections.emptyList();
        }
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
        final  DbTable table = TABLE.of(this);
        return DbTable.NAME.of(table) + '.' + NAME.of(this);
    }


}
