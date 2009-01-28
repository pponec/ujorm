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
package org.ujoframework.core.orm.ao;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.ListProperty;
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
    /** Java UJO class */
    public static final UjoProperty<DbTable,Class> TABLE_CLASS = newProperty("tableClass", Class.class);
    /** Columns */
    public static final ListProperty<DbTable,DbColumn> COLUMNS = newPropertyList("columns", DbColumn.class);
    /** Database */
    public static final UjoProperty<DbTable,Db> DATABASE = newProperty("database", Db.class);


    /** Compare two objects by its PrimaryKey */
    public boolean equals(Ujo ujo1, Ujo ujo2) {
        final DbPK pk = PK.of(this);
        return pk.equals(ujo1, ujo2);
    }


}
