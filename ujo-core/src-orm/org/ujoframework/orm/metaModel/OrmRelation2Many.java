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
import org.ujoframework.orm.annot.Column;

/**
 * The database/object relation to many.
 * @author Ponec
 */
public class OrmRelation2Many extends AbstractMetaModel {

    /** The database column name.
     * If an appropriate UjoProperty is a relation to another ORM object with more primary keys,
     * then the several names can be separated by a space or comma character.
     */
    @XmlAttribute
    public static final UjoProperty<OrmRelation2Many,String> NAME = newProperty("name", "");
    /** Table property */
    @Transient
    public static final UjoProperty<OrmRelation2Many,UjoProperty> TABLE_PROPERTY = newProperty("tableProperty", UjoProperty.class);
    /** DB table */
    @Transient
    public static final UjoProperty<OrmRelation2Many,OrmTable> TABLE = newProperty("table", OrmTable.class);

    public OrmRelation2Many(OrmTable table, UjoProperty tableProperty) {
        
        Field field = UjoManager.getInstance().getPropertyField(OrmTable.DB_PROPERTY.of(table).getItemType(), tableProperty);
        Column column = field.getAnnotation(Column.class);

        if (true) {
            TABLE.setValue(this, table);
            TABLE_PROPERTY.setValue(this, tableProperty);
        }
        if (column!=null) {
            NAME.setValue(this, column.name());
        }
        changeDefault(this, NAME, tableProperty.getName());
    }

    protected OrmRelation2Many() {
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

    @Override
    public String toString() {
        return NAME.of(this);
    }

}
