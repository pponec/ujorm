/*
 *  Copyright 2009-2010 Pavel Ponec
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManager;
import org.ujoframework.core.UjoManagerXML;
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.orm.metaModel.MetaDatabase;
import org.ujoframework.orm.metaModel.MetaRoot;
import org.ujoframework.orm.annot.Db;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaRelation2Many;
import org.ujoframework.orm.metaModel.MetaTable;

/**
 * The basic class for an ORM support.
 * @author Pavel Ponec
 * @composed 1 - 1 MetaRoot
 * @assoc - - - AbstractMetaModel
 */
public class OrmHandler {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(OrmHandler.class.getName());
    /** Default handler */
    private static OrmHandler handler = new OrmHandler();

    /** List of databases */
    private MetaRoot databases = new MetaRoot();
    /** Temporary configuration */
    private MetaRoot configuration = null;
    /** The default ORM session */
    private Session session;

    /** Map a property to a database column model */
    private HashMap<UjoProperty,MetaRelation2Many> propertyMap = new HashMap<UjoProperty,MetaRelation2Many> ();

    /** The constructor */
    public OrmHandler() {
        session = createSession();
    }

    /** The constructor with a database metamodel initialization. */
    public <UJO extends OrmUjo> OrmHandler(final Class<UJO> databaseModel) {
        this();
        loadDatabase(databaseModel);
    }

    /** The constructor with a database metamodel initialization. */
    public <UJO extends OrmUjo> OrmHandler(final Class<UJO> ... databaseModels) {
        this();
        loadDatabase(databaseModels);
    }


    /** A candidate to removing */
    // @Deprecated
    public static OrmHandler getInstance() {
        return handler;
    }

     /** Get Session */
    public Session getSession() {
        return session;
    }

    /** Create new session */
    public Session createSession() {
        return new Session(this);
    }

    /** Load parameters from an external XML file.
     * The initialization must be finished before an ORM definition loading.
     */
    public boolean config(String url) throws IllegalArgumentException {
        try {
            return config(new URL(url), true);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Configuration file is not valid " + url , e);
        }
    }

    /** Save the ORM parameters.
     * The assigning must be finished before an ORM definition loading.
     */
    public void config(MetaParams params) throws IllegalArgumentException {
        MetaRoot.PARAMETERS.setValue(databases, params);
    }

    /** Save the ORM configuration include parameters (if the parameters are not null).
     * The assigning must be finished before an ORM definition loading.
     */
    public void config(MetaRoot config) throws IllegalArgumentException {
         this.configuration = config;

        // The parameters assigning:
        MetaParams params = MetaRoot.PARAMETERS.of(configuration);
        if (params!=null) {
            config(params);
        }
    }

    /** Load parameters from an external XML file.
     * The initialization must be finished before an ORM definition loading.
     */
    public boolean config(URL url, boolean throwsException) throws IllegalArgumentException {
        try {
            final MetaRoot conf = UjoManagerXML.getInstance().parseXML
            ( new BufferedInputStream(url.openStream())
            , MetaRoot.class
            , this
            );

            config(conf);
            return true;

        } catch (Exception e) {
            if (throwsException) {
               throw new IllegalArgumentException("Configuration file is not valid ", e);
            } else {
               return false;
            }
        }
    }

    /** Is the parameter a persistent property? */
    public boolean isPersistent(UjoProperty property) {
        
        final boolean resultFalse
        =  property.isTypeOf(List.class)
        || UjoManager.getInstance().isTransientProperty(property)
        ;
        return !resultFalse;
    }

    /** LoadInternal a database model from paramater */
    private <UJO extends OrmUjo> MetaDatabase loadDatabaseInternal(Class<UJO> databaseModel) {

        // Load a configuration parameters:
        Db annotDb = databaseModel.getAnnotation(Db.class);
        String schemaDb = annotDb!=null ? annotDb.schema() : null;
        MetaDatabase paramDb = configuration!=null ? configuration.removeDb(schemaDb) : null;

        // Create the ORM DB model:
        UJO model = getInstance(databaseModel);
        MetaDatabase dbModel  = new MetaDatabase(this, model, paramDb);
        databases.add(dbModel);

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("DATABASE META-MODEL:\n"+databases.toString());
        }
        
        return dbModel;
    }

    /** Load a meta-data and create database tables */
    @SuppressWarnings("unchecked")
    final public <UJO extends OrmUjo> void loadDatabase(final Class<UJO> databaseModel) {
        loadDatabase(new Class[] {databaseModel});
    }

    /** Load a meta-data and create database tables */
    public <UJO extends OrmUjo> void loadDatabase(final Class<UJO> ... databaseModels) {

        for (Class<UJO> databaseModel : databaseModels) {
            MetaDatabase dbModel = loadDatabaseInternal(databaseModel);

            switch (MetaParams.ORM2DLL_POLICY.of(session.getParameters())) {
                case CREATE_DDL:
                    dbModel.create(session);
                    break;
            }
        }

        // Initialize Column Type codes:
        MetaParams params = getParameters();
        for (MetaRelation2Many r : propertyMap.values()) {
            if (r.isColumn()) {
                ((MetaColumn)r).initTypeCode(params);
            }
        }

        // Lock the meta-model:
        databases.setReadOnly(true);

        // Print the meta-model:
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("DATABASE META-MODEL:\n" + databases.toString());
        }

        File outConfigFile = MetaParams.SAVE_CONFIG_TO_FILE.of(getParameters());
        if (outConfigFile!=null) try {
            databases.print(outConfigFile);
        } catch (IOException e) {
            throw new IllegalStateException("Can't create configuration " + outConfigFile, e);
        }
    }

    /** Create an instance from the class */
    private <UJO extends OrmUjo> UJO getInstance(Class<UJO> databaseModel) {
        try {
            return databaseModel.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't create instance of " + databaseModel, e);
        }
    }

    /** Do the handler have a read-only state? */
    public boolean isReadOnly() {
        List<MetaDatabase> dbs = getDatabases();
        boolean result = dbs.size()>0 && dbs.get(0).readOnly();
        return result;
    }


    /** Map a property to the table */
    @SuppressWarnings("unchecked")
    public void addProperty(UjoProperty property, MetaRelation2Many newColumn) {

        MetaRelation2Many oldColumn = findColumnModel(property);

        if (oldColumn == null) {
            propertyMap.put(property, newColumn);
        } else {
            final Class oldType = oldColumn.getTableClass();
            final Class newType = newColumn.getTableClass();

            if (newType.isAssignableFrom(oldType)) {
                // Only a parent can be assigned:
                propertyMap.put(property, newColumn);
            }
        }
    }

    /** Find a Relation/Column model of the paramemeter property.
     * @param pathProperty Parameter can be type of Property of PathProperty (direct or indirect);
     * @return Related model or the null if model was not found.
     */
    public MetaRelation2Many findColumnModel(UjoProperty pathProperty) {
        if (pathProperty!=null) while (!pathProperty.isDirect()) {
            pathProperty = ((PathProperty)pathProperty).getLastProperty();
        }
        final MetaRelation2Many result = propertyMap.get(pathProperty);
        return result;
    }

    /** Find a table model by the dbClass. 
     * If the table model is not found then the IllegalStateException is throwed.
     */
    public MetaTable findTableModel(Class<? extends OrmUjo> dbClass) throws IllegalStateException {
        for (MetaDatabase db : MetaRoot.DATABASES.getList(databases)) {
            for (MetaTable table : MetaDatabase.TABLES.getList(db)) {
                // Class has a unique instance in the same classloader:
                if (table.getType()==dbClass) {
                    return table;
                }
            }
        }
        throw new IllegalStateException
            ("An entity mapping bug: the " + dbClass + " is not mapped to the Database.")
            ;
    }

    /** Returns parameters */
    public MetaParams getParameters() {
        return MetaRoot.PARAMETERS.of(databases);
    }

    /** Returns true, if a database meta-model is loaded. */
    public boolean isDatabaseLoaded() {
        int itemCount = MetaRoot.DATABASES.getItemCount(databases);
        return itemCount>0;
    }

    /** Returns all database */
    public List<MetaDatabase> getDatabases() {
        return MetaRoot.DATABASES.of(databases);
    }

    /** Find all <strong>persistent<strong> properties with the required type or subtype.
     * @param type The parameter value Object.clas returns all persistent properties.
     */
    public List<UjoProperty> findPropertiesByType(Class type) {
        List<UjoProperty> result = new ArrayList<UjoProperty>();
        for (UjoProperty p : propertyMap.keySet()) {
            if (p.isTypeOf(type)) {
                result.add(p);
            }
        }
        return result;
    }

}
