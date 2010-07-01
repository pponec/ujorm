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

package org.ujoframework.orm.metaModel;

import java.lang.reflect.Field;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.extensions.Property;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.annot.Column;

/**
 * The database/object relation to many.
 * @author Pavel Ponec
 */
public class MetaRelation2Many extends AbstractMetaModel {
    private static final Class CLASS = MetaRelation2Many.class;

    /** The meta-model ID. */
    @XmlAttribute
    public static final Property<MetaRelation2Many,String> ID = newProperty("id", "");
    /** The database column name.
     * If an appropriate UjoProperty is a relation to another ORM object with more primary keys,
     * then the several names can be separated by a space or comma character.
     */
    public static final Property<MetaRelation2Many,String> NAME = newProperty("name", "");
    /** Table property */
    @Transient
    public static final Property<MetaRelation2Many,UjoProperty> TABLE_PROPERTY = newProperty("tableProperty", UjoProperty.class);
    /** DB table */
    @Transient
    public static final Property<MetaRelation2Many,MetaTable> TABLE = newProperty("table", MetaTable.class);
    /** The property initialization */
    static{init(CLASS);}


    public MetaRelation2Many() {
    }
    
    public MetaRelation2Many(MetaTable table, UjoProperty tableProperty, MetaRelation2Many param) {
        
        Field field = UjoManager.getInstance().getPropertyField(table.getType(), tableProperty);
        Column column = field!=null ? field.getAnnotation(Column.class) : null;

        if (true) {
            ID.setValue(this, tableProperty.getName());
            TABLE.setValue(this, table);
            TABLE_PROPERTY.setValue(this, tableProperty);
        }
        if (column!=null) {
            changeDefault(this, NAME, column.name());
            changeDefault(this, NAME, column.value());
        }
        if (param!=null) {
            changeDefault(this, NAME, NAME.of(param));
        }
        changeDefault(this, NAME, tableProperty.getName());
    }

    /** It is a DB column (either a value of a foreign key), 
     * not a relation to many.
     */
    public boolean isColumn() {
        return false;
    }

    /** Is it a Foreign Key ? */
    public boolean isForeignKey() {
        return false;
    }

    /** Returns a column property */
    final public UjoProperty getProperty() {
        return TABLE_PROPERTY.of(this);
    }

    final public MetaTable getTable() {
        return TABLE.of(this);
    }

    /** Returns a class of column table. */
    final public Class<OrmUjo> getTableClass() {
        final Class<OrmUjo> result = TABLE.of(this).getType();
        return result;
    }

    /** Get property value */
    @SuppressWarnings("unchecked")
    final public Object getValue(Ujo ujo) {
        final Object result = getProperty().of(ujo);
        return result;
    }

    /** OrmHandler */
    public OrmHandler getHandler() {
        final OrmHandler result = TABLE.of(this).getDatabase().getOrmHandler();
        return result;
    }

    /** Column name. */
    @Override
    public String toString() {
        return NAME.of(this);
    }

}
