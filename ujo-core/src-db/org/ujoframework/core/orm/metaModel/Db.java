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

import org.ujoframework.UjoProperty;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.core.orm.AbstractMetaModel;
import org.ujoframework.core.orm.DbType;
import org.ujoframework.core.orm.annot.Database;
import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.db.TableUjo;
import org.ujoframework.implementation.db.UjoRelative;

/**
 * A logical database description.
 * @author pavel
 */
public class Db extends AbstractMetaModel {

    /** List of tables */
    public static final ListProperty<Db,DbTable> TABLES = newPropertyList("table", DbTable.class);
    /** Database connection */
    public static final UjoProperty<Db,String> CONNECTION = newProperty("connection", "");
    /** DB class root instance */
    @Transient
    public static final UjoProperty<Db,TableUjo> ROOT = newProperty("root", TableUjo.class);
    /** Database name */
    public static final UjoProperty<Db,String> NAME = newProperty("name", "");
    /** LDPA */
    public static final UjoProperty<Db,String> LDAP = newProperty("ldap", "");

    public Db(TableUjo database) {
        ROOT.setValue(this, database);

        Database annotDB = database.getClass().getAnnotation(Database.class);
        if (annotDB!=null) {
            NAME.setValue(this, annotDB.name());
            CONNECTION.setValue(this, annotDB.jdbc());
            LDAP.setValue(this, annotDB.ldap());
        }
        if (NAME.isDefault(this)) {
            NAME.setValue(this, database.getClass().getSimpleName());
        }


        for (UjoProperty tableProperty : database.readProperties()) {

            if (tableProperty instanceof UjoRelative) {
                UjoRelative tProperty = (UjoRelative) tableProperty;

                DbTable table = new DbTable(this, tProperty);
                TABLES.addItem(this, table);
            }
        }

    }

    /** Change DbType by a Java property */
   @SuppressWarnings("unchecked")
    public void changeDbType(DbColumn column) {
       UjoProperty property = DbColumn.PROPERTY.of(column);

       Class type = property.getType();
        if (String.class==type) {
            DbColumn.TYPE.setValue(column, DbType.VARCHAR);
            changeDefault(column, property, 128);
        }
        else if (Integer.class==type) {
            DbColumn.TYPE.setValue(column, DbType.INT);
            if (DbColumn.MAX_LENGTH.isDefault(column)) {
                DbColumn.MAX_LENGTH.setValue(column, 8);
            }
        }
        else if (String.class==type) {
            DbColumn.TYPE.setValue(column, DbType.VARCHAR);
            if (DbColumn.MAX_LENGTH.isDefault(column)) {
                DbColumn.MAX_LENGTH.setValue(column, 128);
            }
        }
    }

    /** Name of Database. */
    @Override
    public String toString() {
        return NAME.of(this);
    }

}
