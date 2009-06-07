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
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.annot.XmlAttribute;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.annot.Table;
import org.ujoframework.orm.annot.View;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.OrmUjo;


/**
 * DB table metamodel.
 * @author Pavel Ponec
 * @composed 1 - * MetaRelation2Many
 * @composed 1 - * MetaColumn
 * @composed 1 - 1 MetaPKey
 */
public class MetaTable extends AbstractMetaModel {

    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    /** The meta-model id */
    @XmlAttribute
    public static final UjoProperty<MetaTable,String> ID = newProperty("id", "", propertyCount++);
    /** DB table name */
    public static final UjoProperty<MetaTable,String> NAME = newProperty("name", "", propertyCount++);
    /** The unique table/view name over all Databases in scope one OrmHandler */
    public static final UjoProperty<MetaTable,String> ALIAS = newProperty("alias", "", propertyCount++);
    /** Name of table schema. */
    @Transient
    public static final UjoProperty<MetaTable,String> SCHEMA = newProperty("schema", "", propertyCount++);
    /** Table Columns */
    public static final ListProperty<MetaTable,MetaColumn> COLUMNS = newPropertyList("column", MetaColumn.class, propertyCount++);
    /** Table relations to many */
    public static final ListProperty<MetaTable,MetaRelation2Many> RELATIONS = newPropertyList("relation2m", MetaRelation2Many.class, propertyCount++);
    /** Is it a model of a database view ? */
    @XmlAttribute
    public static final UjoProperty<MetaTable,Boolean> VIEW = newProperty("view", false, propertyCount++);
    /** SQL SELECT statement */
    public static final UjoProperty<MetaTable,String> SELECT = newProperty("select", "", propertyCount++);
    /** SQL SELECT model. Note: this property must not be persistent due a blank spaces in key names! */
    @Transient
    public static final UjoProperty<MetaTable,MetaView> SELECT_MODEL = newProperty("selectModel", MetaView.class, propertyCount++);
    /** Unique Primary Key */
    @Transient
    public static final UjoProperty<MetaTable,MetaPKey> PK = newProperty("pk", MetaPKey.class, propertyCount++);
    /** Database relative <strong>property</strong> (a base definition of table) */
    @Transient
    public static final UjoProperty<MetaTable,RelationToMany> DB_PROPERTY = newProperty("dbProperty", RelationToMany.class, propertyCount++);
    /** Database */
    @Transient
    public static final UjoProperty<MetaTable,MetaDatabase> DATABASE = newProperty("database", MetaDatabase.class, propertyCount++);

    /** No parameter constructor. */
    public MetaTable() {
    }

    @SuppressWarnings("unchecked")
    public MetaTable(MetaDatabase database, RelationToMany dbProperty, MetaTable parTable) {
        ID.setValue(this, dbProperty.getName());
        DATABASE.setValue(this, database);
        DB_PROPERTY.setValue(this, dbProperty);

        final Field field = UjoManager.getInstance().getPropertyField(MetaDatabase.ROOT.of(database), dbProperty);

        View view1 = field.getAnnotation(View.class);
        View view2 = (View) dbProperty.getItemType().getAnnotation(View.class);
        VIEW.setValue(this, view1!=null || view2!=null);

        if (parTable!=null) {
            changeDefault(this, NAME  , NAME.of(parTable));
            changeDefault(this, ALIAS , ALIAS.of(parTable));
            changeDefault(this, SCHEMA, SCHEMA.of(parTable));
            changeDefault(this, SELECT, SELECT.of(parTable));
            changeDefault(this, VIEW  , VIEW.of(parTable));
        }

        if (VIEW.of(this)) {
            if (view1!=null) changeDefault(this, NAME  , view1.name());
            if (view1!=null) changeDefault(this, ALIAS , view1.alias());
            if (view1!=null) changeDefault(this, SCHEMA, view1.schema());
            if (view1!=null) changeDefault(this, SELECT, view1.select());
            if (view2!=null) changeDefault(this, NAME  , view2.name());
            if (view2!=null) changeDefault(this, ALIAS , view2.alias());
            if (view2!=null) changeDefault(this, SCHEMA, view2.schema());
            if (view2!=null) changeDefault(this, SELECT, view2.select());

            if (!SELECT.isDefault(this)) {
                SELECT_MODEL.setValue(this, new MetaView(SELECT.of(this)));
            }
        } else {
            Table table1 = field.getAnnotation(Table.class);
            Table table2 = (Table) dbProperty.getItemType().getAnnotation(Table.class);
            if (table1!=null) changeDefault(this, NAME  , table1.name());
            if (table1!=null) changeDefault(this, ALIAS , table1.alias());
            if (table1!=null) changeDefault(this, SCHEMA, table1.schema());
            if (table2!=null) changeDefault(this, NAME  , table2.name());
            if (table2!=null) changeDefault(this, ALIAS , table2.alias());
            if (table2!=null) changeDefault(this, SCHEMA, table2.schema());
        }
        changeDefault(this, SCHEMA, MetaDatabase.SCHEMA.of(database));
        changeDefault(this, NAME, dbProperty.getName());
        String aliasPrefix = MetaParams.TABLE_ALIAS_PREFIX.of(database.getParams());
        String aliasSuffix = MetaParams.TABLE_ALIAS_SUFFIX.of(database.getParams());
        changeDefault(this, ALIAS, aliasPrefix+NAME.of(this)+aliasSuffix);

        // -----------------------------------------------

        MetaPKey dpk = new MetaPKey(this);
        PK.setValue(this, dpk);

        OrmHandler dbHandler = database.getOrmHandler();
        UjoManager ujoManager = UjoManager.getInstance();
        for (UjoProperty property : ujoManager.readProperties(dbProperty.getItemType())) {

            if (!ujoManager.isTransientProperty(property)) {

                if (property instanceof RelationToMany) {
                    MetaRelation2Many param = parTable!=null ? parTable.findRelation(property.getName()) : null;
                    MetaRelation2Many column = new MetaRelation2Many(this, property, param);
                    RELATIONS.addItem(this, column);
                    dbHandler.addProperty(property, column);

                } else {
                    MetaColumn param  = parTable!=null ? parTable.findColumn(property.getName()) : null;
                    MetaColumn column = new MetaColumn(this, property, param);
                    COLUMNS.addItem(this, column);
                    dbHandler.addProperty(property, column);

                    if (MetaColumn.PRIMARY_KEY.of(column)) {
                        MetaPKey.COLUMNS.addItem(dpk, column);
                    }
                }
            }
        }
    }

    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    /** Assign a PK from framework */
    public void assignPrimaryKey(final OrmUjo bo) {
        final Class type = DB_PROPERTY.of(this).getItemType();
        if (type.isInstance(bo)) {
            final MetaPKey pk = PK.of(this);
            final boolean ok = pk.assignPrimaryKey(bo);
            if (!ok) {
                throw new RuntimeException("DB SEQUENCE is not supported for " + type);
            }
        } else {
            throw new IllegalArgumentException("Argument is not type of " + type);
        }
    }
    
    /** Returns a new instance or the BO. */
    public OrmUjo createBO() throws InstantiationException, IllegalAccessException {
        Class type = DB_PROPERTY.of(this).getItemType();
        Object result = type.newInstance();
        return (OrmUjo) result;
    }

    /** Returns the first PK */
    public MetaColumn getFirstPK() {
        return PK.of(this).getFirstColumn();
    }

    /** Is the instance a database relation model? */
    public boolean isPersistent() {
        return DATABASE.of(this)!=null;
    }

    /** Is the instance a database relation model? */
    public boolean isView() {
        return VIEW.of(this);
    }

    /** Is the instance a database persistent table? A false value neans that the object is a relation model or view. */
    public boolean isTable() {
        return isPersistent() && !isView();
    }

    /** Is the query from a SQL select model ? */
    public boolean isSelectModel() {
        return SELECT_MODEL.of(this)!=null;
    }

    /** Database model is not persistent. A side efect is that the DATABASE property has hot a null value. */
    public void setNotPersistent() {
        DATABASE.setValue(this, null);
    }

    /** Returns a unique table name over all Databases of the one OrmHandler. */
    public String getAlias() {
        return ALIAS.of(this);
    }

    /** Returns the database */
    final public MetaDatabase getDatabase() {
        return DATABASE.of(this);
    }

    /** Compare object by the same instance. */
    @Override
    public boolean equals(Object obj) {
        return this==obj;
    }

    /** Finds the first column by ID or returns null. The method is for internal use only. */
    MetaColumn findColumn(String id) {

        if (isValid(id)) for (MetaColumn column : COLUMNS.of(this)) {
            if (MetaColumn.ID.equals(column, id)) {
                return column;
            }
        }
        return null;
    }

    /** Finds the first relation by ID or returns null. The method is for internal use only. */
    MetaRelation2Many findRelation(String id) {

        if (isValid(id)) for (MetaRelation2Many relation : RELATIONS.of(this)) {
            if (MetaRelation2Many.ID.equals(relation, id)) {
                return relation;
            }
        }
        return null;
    }

}
