/*
 *  Copyright 2009-2013 Pavel Ponec
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

import java.lang.reflect.Field;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.core.annot.Immutable;
import org.ujorm.core.annot.Transient;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.annot.Column;

/**
 * The database/object relation to many.
 * @author Pavel Ponec
 */
@Immutable
public class MetaRelation2Many extends AbstractMetaModel {
    private static final Class<MetaRelation2Many> CLASS = MetaRelation2Many.class;

    /** Property Factory */
    private static final KeyFactory<MetaRelation2Many> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** The meta-model ID. */
    @XmlAttribute
    public static final Key<MetaRelation2Many,String> ID = fa.newKey("id", "");
    /** The database column name.
     * If an appropriate Key is a relation to another ORM object with more primary keys,
     * then the several names can be separated by a space or comma character.
     */
    public static final Key<MetaRelation2Many,String> NAME = fa.newKey("name", "");
    /** Table key */
    @Transient
    public static final Key<MetaRelation2Many,Key> TABLE_KEY = fa.newKey("tableProperty");
    /** DB table */
    @Transient
    public static final Key<MetaRelation2Many,MetaTable> TABLE = fa.newKey("table");
    /** The property initialization */
    static{fa.lock();}


    public MetaRelation2Many() {
    }

    public MetaRelation2Many(MetaTable table, Key tableProperty, MetaRelation2Many param) {

        Field field = UjoManager.getInstance().getPropertyField(table.getType(), tableProperty, false);
        Column column = field!=null ? field.getAnnotation(Column.class) : null;

        if (true) {
            ID.setValue(this, tableProperty.getName());
            TABLE.setValue(this, table);
            TABLE_KEY.setValue(this, tableProperty);
        }
        if (column!=null) {
            changeDefault(this, NAME, column.name());
            changeDefault(this, NAME, column.value());
        }
        if (param!=null) {
            changeDefault(this, NAME, NAME.of(param));
        }
        changeDefault(this, NAME, tableProperty.getName());

        assert !getKey().isComposite() : String.format("The key %s must be direct.", getKey().toStringFull());
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
    final public Key getKey() {
        return TABLE_KEY.of(this);
    }

    /** Is it newer the composite Key */
    final public boolean isCompositeKey() {
        return false;
    }

    /** Returns true if the property type is a type or subtype of the parameter class. */
    final public boolean isTypeOf(Class type) {
        return getKey().isTypeOf(type);
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
        final Object result = getKey().of(ujo);
        return result;
    }

    /** OrmHandler */
    public OrmHandler getHandler() {
        final OrmHandler result = TABLE.of(this).getDatabase().getOrmHandler();
        return result;
    }

    /** Property name */
    @Override
    public String toString() {
        return TABLE_KEY.of(this).toStringFull();
    }

    /** Two models are the same if its key names are the same for the same domain type. */
    @Override
    public boolean equals(Object relation) {
        if (relation instanceof ColumnWrapper) {
            final Key argKey = ((ColumnWrapper) relation).getKey();
            return getKey().getName().equals(argKey.getName())
                && getKey().getDomainType() == argKey.getDomainType();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getKey().getName().hashCode();
    }

}
