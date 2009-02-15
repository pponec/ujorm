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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.annot.Column;

/**
 * Database column metadata
 * @author pavel
 */
public class DbColumn extends AbstractMetaModel {

    /** DB column name */
    @XmlAttribute
    public static final UjoProperty<DbColumn,String> NAME = newProperty("name", "");
    /** DB primary key */
    public static final UjoProperty<DbColumn,Boolean> PRIMARY_KEY = newProperty("pk", false);
    /** Database Type */
    public static final UjoProperty<DbColumn,DbType> TYPE = newProperty("dbType", DbType.Automatic);
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
    /** Ujo column property */
    @Transient
    public static final UjoProperty<DbColumn,UjoProperty> PROPERTY = newProperty("property", UjoProperty.class);
    /** DB table */
    @Transient
    public static final UjoProperty<DbColumn,DbTable> TABLE = newProperty("table", DbTable.class);


    public DbColumn(DbTable table, UjoProperty propertyColumn) {
        
        Field field = UjoManager.getInstance().getPropertyField(DbTable.DB_RELATIVE.of(table).getItemType(), propertyColumn);
        Column column = field.getAnnotation(Column.class);

        if (column!=null) {
            NAME      .setValue(this, column.name());
            PRIMARY_KEY        .setValue(this, column.pk());
            MANDATORY .setValue(this, column.mandatory());
            MAX_LENGTH.setValue(this, column.maxLenght());
            PRECISION .setValue(this, column.precision());
            TYPE      .setValue(this, column.type());
            INDEX_NAME.setValue(this, column.indexName());
        }

        if (true) {
            TABLE.setValue(this, table);
            PROPERTY.setValue(this, propertyColumn);
        }
        if (NAME.isDefault(this)) {
            NAME.setValue(this, propertyColumn.getName());
        }
        if (TYPE.isDefault(this)) {
            DbTable.DATABASE.of(table).changeDbType(this);
        }
    }

    /** Get property value */
    @SuppressWarnings("unchecked")
    final public Object getValue(Ujo ujo) {
        final Object result = PROPERTY.of(this).of(ujo);
        return result;
    }

    @Override
    public String toString() {
        return NAME.of(this);
    }


}
