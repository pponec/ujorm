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

package org.ujoframework.orm;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.orm.metaModel.Db;
import org.ujoframework.orm.metaModel.DbRoot;
import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.metaModel.DbColumn;
import org.ujoframework.orm.metaModel.DbRelation2m;
import org.ujoframework.orm.metaModel.DbTable;

/**
 * The basic class for an ORM support.
 * @author pavel
 */
public class DbHandler {

    public static final Logger LOGGER = Logger.getLogger(DbHandler.class.getName());


    private static DbHandler handler = new DbHandler();

    private Session session = new Session();
    private DbRoot databases = new DbRoot();

    /** Map a property to a database column model */
    private HashMap<UjoProperty,DbRelation2m> propertyMap = new HashMap<UjoProperty,DbRelation2m> ();

    /** The Sigleton constructor */
    protected DbHandler() {
    }

    public static DbHandler getInstance() {
        return handler;
    }

     /** Get Session */
    public Session getSession() {
        return session;
    }

    /** Is the parameter a persistent property? */
    public boolean isPersistent(UjoProperty property) {
        
        final boolean resultFalse
        =  List.class.isAssignableFrom(property.getType())
        || UjoManager.getInstance().isTransientProperty(property)
        ;
        return !resultFalse;
    }

    /** Load a database model from paramater */
    public <UJO extends TableUjo> Db loadDatabase(Class<UJO> databaseModel) {
        UJO model = getInstance(databaseModel);
        Db dbModel  = new Db(model);
        databases.add(dbModel);

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info(databases.toString());
        }
        
        return dbModel;
    }

    /** Load a metada and create database */
    public <UJO extends TableUjo> Db createDatabase(Class<UJO> databaseModel) {

        Db dbModel = loadDatabase(databaseModel);
        dbModel.create();

        return dbModel;
    }




    /** Create an instance from the class */
    private <UJO extends TableUjo> UJO getInstance(Class<UJO> databaseModel) {
        try {
            return databaseModel.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't create instance of " + databaseModel, e);
        }
    }


    /** Find a table of the paramemeter property. */
    public DbTable findTable(UjoProperty property) {
        final DbRelation2m column = findColumn(property);
        return column!=null ? DbRelation2m.TABLE.of(column) : null ;
    }

    /** Find a table of the paramemeter property. */
    public DbRelation2m findColumn(UjoProperty property) {
        final DbRelation2m result = propertyMap.get(property);
        return result;
    }


    /** Map a property to the table */
    @SuppressWarnings("unchecked")
    public void addProperty(UjoProperty property, DbRelation2m newColumn) {

        DbRelation2m oldColumn = findColumn(property);

        if (oldColumn == null) {
            propertyMap.put(property, newColumn);
        } else {
            final DbTable oldTable = DbColumn.TABLE.of(oldColumn);
            final DbTable newTable = DbColumn.TABLE.of(newColumn);

            final Class oldType = DbTable.DB_PROPERTY.of(oldTable).getItemType();
            final Class newType = DbTable.DB_PROPERTY.of(newTable).getItemType();

            if (newType.isAssignableFrom(oldType)) {
                // Only a parent can be assigned:
                propertyMap.put(property, newColumn);
            }
        }
    }

    /** Find a table model by the dbClass */
    public DbTable findTableModel(Class<TableUjo> dbClass) {
        for (Db db : DbRoot.DATABASES.getList(databases)) {
            for (DbTable table : Db.TABLES.getList(db)) {
                if (DbTable.DB_PROPERTY.of(table).getItemType()==dbClass) {
                    return table;
                }
            }
        }
        return null;
    }

}
