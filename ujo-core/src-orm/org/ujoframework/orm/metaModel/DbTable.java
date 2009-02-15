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
    public static final UjoProperty<DbTable,RelationToMany> DB_RELATIVE = newProperty("dbRelative", RelationToMany.class);
    /** Table Columns */
    public static final ListProperty<DbTable,DbColumn> COLUMNS = newPropertyList("column", DbColumn.class);
    /** Table relations to many */
    public static final ListProperty<DbTable,DbRelation2m> RELATIONS = newPropertyList("relation2m", DbRelation2m.class);
    /** Database */
    @Transient
    public static final UjoProperty<DbTable,Db> DATABASE = newProperty("database", Db.class);

    public DbTable(Db database, RelationToMany propertyTable) {
        DATABASE.setValue(this, database);
        DB_RELATIVE.setValue(this, propertyTable);

        final Field field = UjoManager.getInstance().getPropertyField(database, propertyTable);
        final Table table = field.getAnnotation(Table.class);
        if (table!=null) {
            NAME.setValue(this, table.name());
        }
        if (NAME.isDefault(this)) {
            NAME.setValue(this, propertyTable.getName());
        }

        DbPK dpk = new DbPK();
        PK.setValue(this, dpk);

        UjoManager ujoManager = UjoManager.getInstance();
        for (UjoProperty property : ujoManager.readProperties(propertyTable.getItemType())) {

            if (!ujoManager.isTransientProperty(property)) {

                if (property instanceof RelationToMany) {
                    DbRelation2m relation = new DbRelation2m(this, property);
                    RELATIONS.addItem(this, relation);
                } else {

                    DbColumn column = new DbColumn(this, property);
                    COLUMNS.addItem(this, column);

                    if (DbColumn.PRIMARY_KEY.of(column)) {
                        DbPK.COLUMNS.addItem(dpk, column);
                    }
                }
            }
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
