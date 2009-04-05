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
import org.ujoframework.orm.annot.Table;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.OrmHandler;


/**
 * DB table medadata.
 * @author pavel
 * @composed 1 - * OrmRelation2Many
 * @composed 1 - * OrmColumn
 * @composed 1 - 1 OrmPKey
 */
public class OrmTable extends AbstractMetaModel {

    /** DB table name */
    @XmlAttribute
    public static final UjoProperty<OrmTable,String> NAME = newProperty("name", "");
    /** Unique Primary Key */
    @Transient
    public static final UjoProperty<OrmTable,OrmPKey> PK = newProperty("pk", OrmPKey.class);
    /** Database relative <strong>property</strong> (a base definition of table) */
    @Transient
    public static final UjoProperty<OrmTable,RelationToMany> DB_PROPERTY = newProperty("dbProperty", RelationToMany.class);
    /** Table Columns */
    public static final ListProperty<OrmTable,OrmColumn> COLUMNS = newPropertyList("column", OrmColumn.class);
    /** Table relations to many */
    public static final ListProperty<OrmTable,OrmRelation2Many> RELATIONS = newPropertyList("relation2m", OrmRelation2Many.class);
    /** Database */
    @Transient
    public static final UjoProperty<OrmTable,OrmDatabase> DATABASE = newProperty("database", OrmDatabase.class);


    @SuppressWarnings("unchecked")
    public OrmTable(OrmDatabase database, RelationToMany dbProperty) {
        DATABASE.setValue(this, database);
        DB_PROPERTY.setValue(this, dbProperty);

        final Field field  = UjoManager.getInstance().getPropertyField(database, dbProperty);
        final Table table1 = field.getAnnotation(Table.class);
        final Table table2 = (Table) dbProperty.getItemType().getAnnotation(Table.class);
        
        if (table1!=null) {
            NAME.setValue(this, table1.name());
        }
        if (table2!=null) {
            NAME.setValue(this, table2.name());
        }
        if (NAME.isDefault(this)) {
            NAME.setValue(this, dbProperty.getName());
        }

        OrmPKey dpk = new OrmPKey();
        PK.setValue(this, dpk);

        OrmHandler dbHandler = OrmHandler.getInstance();
        UjoManager ujoManager = UjoManager.getInstance();
        for (UjoProperty property : ujoManager.readProperties(dbProperty.getItemType())) {

            if (!ujoManager.isTransientProperty(property)) {


                if (property instanceof RelationToMany) {
                    OrmRelation2Many column = new OrmRelation2Many(this, property);
                    RELATIONS.addItem(this, column);
                    dbHandler.addProperty(property, column);

                } else {
                    OrmColumn column = new OrmColumn(this, property);
                    COLUMNS.addItem(this, column);
                    dbHandler.addProperty(property, column);

                    if (OrmColumn.PRIMARY_KEY.of(column)) {
                        OrmPKey.COLUMNS.addItem(dpk, column);
                    }
                }
            }
        }
    }

    /** Returns a table name include a name of database. */
    public String getFullName() {
        final OrmDatabase db = DATABASE.of(this);
        final String dbName = OrmDatabase.NAME.of(db);
        final String tableName = NAME.of(this);

        if (isValid(dbName)) {
           //return dbName + "." + tableName; // TODO
           return tableName;
        } else {
           return tableName;
        }
    }


    /** Assign a PK from framework */
    public void assignPrimaryKey(TableUjo table) {
        Class type = DB_PROPERTY.of(this).getItemType();
        if (type.isInstance(table)) {
            OrmPKey pk = PK.of(this);
            boolean ok = pk.assignPrimaryKey(table);
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

    /** Compare two objects by its PrimaryKey */
    public boolean equals(Ujo ujo1, Ujo ujo2) {
        final OrmPKey pk = PK.of(this);
        return pk.equals(ujo1, ujo2);
    }


    /** Returns the first PK */
    public OrmColumn getFirstPK() {
        return PK.of(this).getFirstColumn();
    }

    /** Is the instance a database relation model? */
    public boolean isPersistent() {
        return DATABASE.of(this)!=null;
    }

    /** Database model is not persistent. A side efect is that the DATABASE property has hot a null value. */
    public void setNotPersistent() {
        DATABASE.setValue(this, null);
    }

    /** Returns the database */
    final public OrmDatabase getDatabase() {
        return DATABASE.of(this);
    }

}
