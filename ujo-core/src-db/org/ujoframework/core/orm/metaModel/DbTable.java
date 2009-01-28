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
package org.ujoframework.core.orm.metaModel;

import java.lang.reflect.Field;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.orm.annot.Table;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.db.UjoRelative;
import org.ujoframework.implementation.map.MapUjo;


/**
 * DB table medadata.
 * @author pavel
 */
public class DbTable extends MapUjo {


    /** DB table name */
    public static final UjoProperty<DbTable,String> NAME = newProperty("name", String.class);
    /** Unique Primary Key */
    public static final UjoProperty<DbTable,DbPK> PK = newProperty("pk", DbPK.class);
    /** Database relative property (a base definition of table) */
    public static final UjoProperty<DbTable,UjoRelative> DB_RELATIVE = newProperty("dbRelative", UjoRelative.class);
    /** Columns */
    public static final ListProperty<DbTable,DbColumn> COLUMNS = newPropertyList("columns", DbColumn.class);
    /** Database */
    public static final UjoProperty<DbTable,Db> DATABASE = newProperty("database", Db.class);

    public DbTable(Db database, UjoRelative dbRelative) {
        DATABASE.setValue(this, database);
        DB_RELATIVE.setValue(this, dbRelative);

        Field field = UjoManager.getPropertyField(database.getClass(), dbRelative);
        Table table = field.getAnnotation(Table.class);
        NAME.setValue(this, table!=null ? table.name() : dbRelative.getName());

        for (UjoProperty property : UjoManager.getInstance().readProperties(dbRelative.getItemType())) {
            if (property instanceof UjoRelative) {
                DbColumn column = new DbColumn(this, (UjoRelative)property);
                COLUMNS.addItem(this, column);
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
