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
import org.ujoframework.implementation.orm.TableUjo;


/**
 * DB table medadata.
 * @author Pavel Ponec
 * @composed 1 - * OrmRelation2Many
 * @composed 1 - * OrmColumn
 * @composed 1 - 1 OrmPKey
 */
public class OrmTable extends AbstractMetaModel {

    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;

    /** The meta-model id */
    @XmlAttribute
    public static final UjoProperty<OrmTable,String> ID = newProperty("id", "", propertyCount++);
    /** DB table name */
    public static final UjoProperty<OrmTable,String> NAME = newProperty("name", "", propertyCount++);
    /** Name of table schema. */
    @Transient
    public static final UjoProperty<OrmTable,String> SCHEMA = newProperty("schema", "", propertyCount++);
    /** Table Columns */
    public static final ListProperty<OrmTable,OrmColumn> COLUMNS = newPropertyList("column", OrmColumn.class, propertyCount++);
    /** Table relations to many */
    public static final ListProperty<OrmTable,OrmRelation2Many> RELATIONS = newPropertyList("relation2m", OrmRelation2Many.class, propertyCount++);
    /** Is it a model of a database view ? */
    @XmlAttribute
    public static final UjoProperty<OrmTable,Boolean> VIEW = newProperty("view", false, propertyCount++);
    /** SQL SELECT statement */
    public static final UjoProperty<OrmTable,String> SELECT = newProperty("select", "", propertyCount++);
    /** SQL SELECT model. Note: this property must not be persistent due a blank spaces in key names! */
    @Transient
    public static final UjoProperty<OrmTable,OrmView> SELECT_MODEL = newProperty("selectModel", OrmView.class, propertyCount++);
    /** Unique Primary Key */
    @Transient
    public static final UjoProperty<OrmTable,OrmPKey> PK = newProperty("pk", OrmPKey.class, propertyCount++);
    /** Database relative <strong>property</strong> (a base definition of table) */
    @Transient
    public static final UjoProperty<OrmTable,RelationToMany> DB_PROPERTY = newProperty("dbProperty", RelationToMany.class, propertyCount++);
    /** Database */
    @Transient
    public static final UjoProperty<OrmTable,OrmDatabase> DATABASE = newProperty("database", OrmDatabase.class, propertyCount++);

    /** The unique table name over all Databases of the one OrmHandler. */
    private final String alias;

    /** No parameter constructor. */
    public OrmTable() {
        alias = "";
    }

    @SuppressWarnings("unchecked")
    public OrmTable(OrmDatabase database, RelationToMany dbProperty, OrmTable parTable) {
        ID.setValue(this, dbProperty.getName());
        DATABASE.setValue(this, database);
        DB_PROPERTY.setValue(this, dbProperty);

        final Field field = UjoManager.getInstance().getPropertyField(OrmDatabase.ROOT.of(database), dbProperty);

        View view1 = field.getAnnotation(View.class);
        View view2 = (View) dbProperty.getItemType().getAnnotation(View.class);
        VIEW.setValue(this, view1!=null || view2!=null);

        if (parTable!=null) {
            changeDefault(this, NAME  , NAME.of(parTable));
            changeDefault(this, SCHEMA, SCHEMA.of(parTable));
            changeDefault(this, SELECT, SELECT.of(parTable));
            changeDefault(this, VIEW  , VIEW.of(parTable));
        }

        if (VIEW.of(this)) {
            if (view1!=null) changeDefault(this, NAME  , view1.name());
            if (view1!=null) changeDefault(this, SCHEMA, view1.schema());
            if (view1!=null) changeDefault(this, SELECT, view1.select());
            if (view2!=null) changeDefault(this, NAME  , view2.name());
            if (view2!=null) changeDefault(this, SCHEMA, view2.schema());
            if (view2!=null) changeDefault(this, SELECT, view2.select());

            if (!SELECT.isDefault(this)) {
                SELECT_MODEL.setValue(this, new OrmView(SELECT.of(this)));
            }
        } else {
            Table table1 = field.getAnnotation(Table.class);
            Table table2 = (Table) dbProperty.getItemType().getAnnotation(Table.class);
            if (table1!=null) changeDefault(this, NAME  , table1.name());
            if (table1!=null) changeDefault(this, SCHEMA, table1.schema());
            if (table2!=null) changeDefault(this, NAME  , table2.name());
            if (table2!=null) changeDefault(this, SCHEMA, table2.schema());
        }
        changeDefault(this, SCHEMA, OrmDatabase.SCHEMA.of(database));
        changeDefault(this, NAME, dbProperty.getName());
        alias = NAME.of(this); // + "_" +  dbProperty.getIndex();

        // -----------------------------------------------

        OrmPKey dpk = new OrmPKey(this);
        PK.setValue(this, dpk);

        OrmHandler dbHandler = database.getOrmHandler();
        UjoManager ujoManager = UjoManager.getInstance();
        for (UjoProperty property : ujoManager.readProperties(dbProperty.getItemType())) {

            if (!ujoManager.isTransientProperty(property)) {

                if (property instanceof RelationToMany) {
                    OrmRelation2Many param = parTable!=null ? parTable.findRelation(property.getName()) : null;
                    OrmRelation2Many column = new OrmRelation2Many(this, property, param);
                    RELATIONS.addItem(this, column);
                    dbHandler.addProperty(property, column);

                } else {
                    OrmColumn param  = parTable!=null ? parTable.findColumn(property.getName()) : null;
                    OrmColumn column = new OrmColumn(this, property, param);
                    COLUMNS.addItem(this, column);
                    dbHandler.addProperty(property, column);

                    if (OrmColumn.PRIMARY_KEY.of(column)) {
                        OrmPKey.COLUMNS.addItem(dpk, column);
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
    public void assignPrimaryKey(final TableUjo table) {
        final Class type = DB_PROPERTY.of(this).getItemType();
        if (type.isInstance(table)) {
            final OrmPKey pk = PK.of(this);
            final boolean ok = pk.assignPrimaryKey(table);
            if (!ok) {
                throw new RuntimeException("DB SEQUENCE is not supported for " + type);
            }
        } else {
            throw new IllegalArgumentException("Argument is not type of " + type);
        }
    }
    
    /** Returns a new instance or the BO. */
    public TableUjo createBO() throws InstantiationException, IllegalAccessException {
        Class type = DB_PROPERTY.of(this).getItemType();
        Object result = type.newInstance();
        return (TableUjo) result;        
    }

    /** Returns the first PK */
    public OrmColumn getFirstPK() {
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
    public final String getAlias() {
        return alias;
    }

    /** Returns the database */
    final public OrmDatabase getDatabase() {
        return DATABASE.of(this);
    }

    /** Compare object by the same instance. */
    @Override
    public boolean equals(Object obj) {
        return this==obj;
    }

    /** Finds the first column by ID or returns null. The method is for internal use only. */
    OrmColumn findColumn(String id) {

        if (isValid(id)) for (OrmColumn column : COLUMNS.of(this)) {
            if (OrmColumn.ID.equals(column, id)) {
                return column;
            }
        }
        return null;
    }

    /** Finds the first relation by ID or returns null. The method is for internal use only. */
    OrmRelation2Many findRelation(String id) {

        if (isValid(id)) for (OrmRelation2Many relation : RELATIONS.of(this)) {
            if (OrmRelation2Many.ID.equals(relation, id)) {
                return relation;
            }
        }
        return null;
    }

}
