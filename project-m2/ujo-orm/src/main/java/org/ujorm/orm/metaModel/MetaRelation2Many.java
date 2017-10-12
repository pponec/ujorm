/*
 *  Copyright 2009-2015 Pavel Ponec
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
import javax.annotation.concurrent.Immutable;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.core.annot.Transient;
import org.ujorm.core.annot.XmlAttribute;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.ColumnWrapper;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.annot.Column;
import org.ujorm.tools.Assert;

/**
 * The database/object relation to many.
 * It is no model of persistent column (nor foreign key).
 * @see #isColumn()
 * @see #isForeignKey()
 * @see MetaColumn
 * @author Pavel Ponec
 */
@Immutable
public class MetaRelation2Many extends AbstractMetaModel {
    private static final Class<MetaRelation2Many> CLASS = MetaRelation2Many.class;

    /** Key Factory */
    private static final KeyFactory<MetaRelation2Many> fa = KeyFactory.CamelBuilder.get(CLASS);
    /** The meta-model ID. */
    @XmlAttribute
    public static final Key<MetaRelation2Many,String> ID = fa.newKey("id", "");
    /** The database column name.
     * If an appropriate Key is a relation to another ORM object with more primary keys,
     * then the several names can be separated by a space or comma character.
     */
    public static final Key<MetaRelation2Many,String> NAME = fa.newKey("name", "");
    /** The direct table key */
    @Transient
    public static final Key<MetaRelation2Many,Key> TABLE_KEY = fa.newKey("tableKey");
    /** DB table */
    @Transient
    public static final Key<MetaRelation2Many,MetaTable> TABLE = fa.newKey("table");
    /** The factory initialization */
    static{fa.lock();}

    /** Table alias for a better performance, the resource is: {@code TABLE.of(this).getAlias()} */
    private final String tableAlias;

    public MetaRelation2Many() {
        this.tableAlias = MetaTable.ALIAS.getDefault();
    }

    /**
     * Meta-model for a relation to many
     * @param table Related Table
     * @param tableKey The direct Key
     * @param param XML content
     */    public MetaRelation2Many(MetaTable table, Key tableKey, MetaRelation2Many param) {
        this.tableAlias = table.getAlias();
        Field field = UjoManager.getInstance().getPropertyField(table.getType(), tableKey, false);
        Column column = field!=null ? field.getAnnotation(Column.class) : null;

        if (true) {
            ID.setValue(this, tableKey.getName());
            TABLE.setValue(this, table);
            TABLE_KEY.setValue(this, tableKey);
        }
        if (column!=null) {
            changeDefault(this, NAME, column.name());
            changeDefault(this, NAME, column.value());
        }
        if (param!=null) {
            changeDefault(this, NAME, NAME.of(param));
        }
        changeDefault(this, NAME, tableKey.getName());

        Assert.isFalse(getKey().isComposite(), "The key {} must be direct.", getKey().getFullName());
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

    /** Returns the ID of the column  */
    public final String getId() {
        return ID.of(this);
    }

    /** Returns a column direct key  */
    public final Key getKey() {
        return TABLE_KEY.of(this);
    }

    /** Is it newer the composite Key */
    public final boolean isCompositeKey() {
        return false;
    }

    /** Returns true if the key type is a type or subtype of the parameter class. */
    public final boolean isTypeOf(Class type) {
        return getKey().isTypeOf(type);
    }

    /** Returns a table model */
    public final MetaTable getTable() {
        return TABLE.of(this);
    }

    /** Returns an alias of the table model */
    //@javax.validation.constraints.NotNull
    public final String getTableAlias() {
        return tableAlias;
    }

    /** Returns a class of column table. */
    public final Class<OrmUjo> getTableClass() {
        final Class<OrmUjo> result = TABLE.of(this).getType();
        return result;
    }

    /** Get key value */
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

    /** Key name */
    @Override
    public String toString() {
        return TABLE_KEY.of(this).getFullName();
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
