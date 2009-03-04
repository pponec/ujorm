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
import org.ujoframework.orm.DbHandler;


/**
 * DB table medadata.
 * @author pavel
 */
public class DbTable extends AbstractMetaModel {

    /** DB table name */
    @XmlAttribute
    public static final UjoProperty<DbTable,String> NAME = newProperty("name", "");
    /** Unique Primary Key */
    @Transient
    public static final UjoProperty<DbTable,DbPK> PK = newProperty("pk", DbPK.class);
    /** Database relative <strong>property</strong> (a base definition of table) */
    @Transient
    public static final UjoProperty<DbTable,RelationToMany> DB_PROPERTY = newProperty("dbProperty", RelationToMany.class);
    /** Table Columns */
    public static final ListProperty<DbTable,DbColumn> COLUMNS = newPropertyList("column", DbColumn.class);
    /** Table relations to many */
    public static final ListProperty<DbTable,DbRelation2m> RELATIONS = newPropertyList("relation2m", DbRelation2m.class);
    /** Database */
    @Transient
    public static final UjoProperty<DbTable,DbModel> DATABASE = newProperty("database", DbModel.class);


    @SuppressWarnings("unchecked")
    public DbTable(DbModel database, RelationToMany dbProperty) {
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

        DbPK dpk = new DbPK();
        PK.setValue(this, dpk);

        DbHandler dbHandler = DbHandler.getInstance();
        UjoManager ujoManager = UjoManager.getInstance();
        for (UjoProperty property : ujoManager.readProperties(dbProperty.getItemType())) {

            if (!ujoManager.isTransientProperty(property)) {


                if (property instanceof RelationToMany) {
                    DbRelation2m column = new DbRelation2m(this, property);
                    RELATIONS.addItem(this, column);
                    dbHandler.addProperty(property, column);

                } else {
                    DbColumn column = new DbColumn(this, property);
                    COLUMNS.addItem(this, column);
                    dbHandler.addProperty(property, column);

                    if (DbColumn.PRIMARY_KEY.of(column)) {
                        DbPK.COLUMNS.addItem(dpk, column);
                    }
                }
            }
        }
    }

    /** Returns a table name include a name of database. */
    public String getFullName() {
        final DbModel db = DATABASE.of(this);
        final String dbName = DbModel.NAME.of(db);
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
            DbPK pk = PK.of(this);
            boolean ok = pk.assignPrimaryKey(table);
            if (!ok) {
                throw new RuntimeException("DB SEQUENCE is not supported for " + type);
            }
        } else {
            throw new IllegalArgumentException("Argument is not type of " + type);
        }
    }

    /** Compare two objects by its PrimaryKey */
    public boolean equals(Ujo ujo1, Ujo ujo2) {
        final DbPK pk = PK.of(this);
        return pk.equals(ujo1, ujo2);
    }

    /** Table Name */
    @Override
    public String toString() {
        return NAME.of(this);
    }

}
