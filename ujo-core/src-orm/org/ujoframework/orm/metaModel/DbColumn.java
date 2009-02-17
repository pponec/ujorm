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
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.annot.Column;

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

    /** It is a DB column */
    @Override
    public boolean isColumn() {
        return true;
    }

    /** Is it a Foreign Key ? */
    @Override
    public boolean isForeignKey() {
        final boolean result = TableUjo.class.isAssignableFrom( DbColumn.TABLE_PROPERTY.of(this).getType() );
        return result;
    }

    @Override
    public String toString() {
        return NAME.of(this);
    }


}
