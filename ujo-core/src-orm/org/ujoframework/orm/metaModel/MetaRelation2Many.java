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
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.annot.Column;

/**
 * The database/object relation to many.
 * @author Pavel Ponec
 */
public class MetaRelation2Many extends AbstractMetaModel {

    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    /** The meta-model ID. */
    @XmlAttribute
    public static final UjoProperty<MetaRelation2Many,String> ID = newProperty("id", "", propertyCount++);
    /** The database column name.
     * If an appropriate UjoProperty is a relation to another ORM object with more primary keys,
     * then the several names can be separated by a space or comma character.
     */
    public static final UjoProperty<MetaRelation2Many,String> NAME = newProperty("name", "", propertyCount++);
    /** Table property */
    @Transient
    public static final UjoProperty<MetaRelation2Many,UjoProperty> TABLE_PROPERTY = newProperty("tableProperty", UjoProperty.class, propertyCount++);
    /** DB table */
    @Transient
    public static final UjoProperty<MetaRelation2Many,MetaTable> TABLE = newProperty("table", MetaTable.class, propertyCount++);

    public MetaRelation2Many() {
    }
    
    public MetaRelation2Many(MetaTable table, UjoProperty tableProperty, MetaRelation2Many param) {
        
        Field field = UjoManager.getInstance().getPropertyField(MetaTable.DB_PROPERTY.of(table).getItemType(), tableProperty);
        Column column = field.getAnnotation(Column.class);

        if (true) {
            ID.setValue(this, tableProperty.getName());
            TABLE.setValue(this, table);
            TABLE_PROPERTY.setValue(this, tableProperty);
        }
        if (param!=null) {
            changeDefault(this, NAME, NAME.of(param));
        }
        if (column!=null) {
            NAME.setValue(this, column.name());
        }
        changeDefault(this, NAME, tableProperty.getName());
    }

    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    /** It is a DB column */
    public boolean isColumn() {
        return false;
    }

    /** Is it a Foreign Key ? */
    public boolean isForeignKey() {
        return false;
    }

    /** Returns a column property */
    public final UjoProperty getProperty() {
        return TABLE_PROPERTY.of(this);
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

    @Override
    public String toString() {
        return NAME.of(this);
    }

}
