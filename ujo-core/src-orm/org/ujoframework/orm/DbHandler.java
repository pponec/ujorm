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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.orm.metaModel.Db;
import org.ujoframework.orm.metaModel.DbRoot;
import org.ujoframework.implementation.orm.TableUjo;

/**
 * The basic class for an ORM support.
 * @author pavel
 */
public class DbHandler {

    public static final Logger LOGGER = Logger.getLogger(DbHandler.class.getName());


    private static DbHandler handler = new DbHandler();

    private Session session = new Session();
    private DbRoot databases = new DbRoot();

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

        LOGGER.log(Level.INFO, databases.toString());
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





}
