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
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaProcedure;
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
    private MetaRoot configuration;
    /** The default ORM session */
    private Session session;

    /** Map a property to a database column model */
    private final HashMap<UjoProperty,MetaRelation2Many> propertyMap = new HashMap<UjoProperty,MetaRelation2Many> ();
    /** Map a Java class to a database table model */
    private final HashMap<Class,MetaTable> entityMap = new HashMap<Class,MetaTable> ();
    /** Map a Java class to a procedure model */
    private final HashMap<Class,MetaProcedure> procedureMap = new HashMap<Class,MetaProcedure> ();

    /** The constructor */
    public OrmHandler() {
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

     /** Get a <strong>default</strong> Session of the OrmHandler.
      * On a multi-thread application use a method {@link #createSession()} rather.
      * @see #createSession()
      */
    public Session getSession() {
        if (session==null) {
            session = createSession();
        }
        return session;
    }

    /** Create new session */
    public Session createSession() {
        return new Session(this);
    }

    /** Load parameters from an external XML file.
     * The initialization must be finished before an ORM definition loading.
     * <br/>Note: in case the parameter starts by the character tilde '~' than the symbol is replaced by a local home directory.
     * See some valid parameter examples:
     * <ul>
     *    <li>http://myproject.org/dbconfig.xml</li>
     *    <li>file:///C:/Documents%20and%20Settings/my/app/dbconfig.xml</li>
     *    <li>~/app/dbconfig.xml</li>
     * </ul>
     */
    public boolean config(String url) throws IllegalArgumentException {
        try {
            if (url.startsWith("~")) {
                final String file = System.getProperty("user.home") + url.substring(1);
                return config(new File(file).toURI().toURL(), true);
            } else {
                return config(new URL(url), true);
            }
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

    /** Save the alternative ORM configuration include parameters (if the parameters are not null).
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
        String databaseId = databaseModel.getSimpleName();
        MetaDatabase paramDb = configuration!=null ? configuration.removeDb(databaseId) : null;

        // Create the ORM DB model:
        UJO root = getInstance(databaseModel);
        MetaDatabase dbModel = new MetaDatabase(this, root, paramDb, databases.getDatabaseCount());
        databases.add(dbModel);

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("DATABASE META-MODEL:\n"+getConfig());
        }
        
        return dbModel;
    }

    /** Load a meta-data and create database tables */
    @SuppressWarnings("unchecked")
    final public <UJO extends OrmUjo> void loadDatabase(final Class<UJO> databaseModel) {
        loadDatabase(new Class[] {databaseModel});
    }

    /** Load a meta-data and create database tables */
    public synchronized <UJO extends OrmUjo> void loadDatabase(final Class<UJO> ... databaseModel) {

        // Load meta-model:
        for (Class<UJO> db : databaseModel) {
            loadDatabaseInternal(db);
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

        // Log the meta-model:
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("DATABASE META-MODEL:\n" + getConfig());
        }

        // Export the meta-model into a XML file:
        File outConfigFile = MetaParams.SAVE_CONFIG_TO_FILE.of(getParameters());
        if (outConfigFile!=null) try {
            databases.print(outConfigFile);
        } catch (IOException e) {
            throw new IllegalStateException("Can't create configuration " + outConfigFile, e);
        }

        // Create DDL:
        switch (MetaParams.ORM2DLL_POLICY.of(getParameters())) {
            case CREATE_DDL:
            case CREATE_OR_UPDATE_DDL:
            case VALIDATE:
                for (MetaDatabase dbModel : getDatabases()) {
                    dbModel.create(getSession());
                }
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
    public void addProcedureModel(MetaProcedure metaProcedure) {
        procedureMap.put(MetaProcedure.DB_PROPERTY.of(metaProcedure).getType(), metaProcedure);
    }

    /** Map a property to the table */
    @SuppressWarnings("unchecked")
    public void addTableModel(MetaTable metaTable) {
        entityMap.put(metaTable.getType(), metaTable);
    }

    /** Map a property to the table */
    @SuppressWarnings("unchecked")
    public void addColumnModel(MetaRelation2Many column) {
        UjoProperty property = column.getProperty();
        MetaRelation2Many oldColumn = findColumnModel(property);

        if (oldColumn == null) {
            propertyMap.put(property, column);
        } else {
            final Class oldType = oldColumn.getTableClass();
            final Class newType = column.getTableClass();

            if (newType.isAssignableFrom(oldType)) {
                // Only a parent can be assigned:
                propertyMap.put(property, column);
            }
        }
    }

    /** Find a Relation/Column model of the paramemeter property.
     * @param pathProperty Parameter can be type of Property of PathProperty (direct or indirect);
     * @return Returns a related model or the NULL if no model was found.
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
        MetaTable result = entityMap.get(dbClass);
        if (result==null) {
            final String msg = "An entity mapping bug: the " + dbClass + " is not mapped to the Database.";
            throw new IllegalStateException(msg);
        }
        return result;
    }

    /** Find a procedure model by the procedureClass.
     * If the procedure model is not found then the IllegalStateException is throwed.
     */
    public MetaProcedure findProcedureModel(Class<? extends DbProcedure> procedureClass) throws IllegalStateException {
        MetaProcedure result = procedureMap.get(procedureClass);
        if (result==null) {
            final String msg = "An procedure mapping bug: the " + procedureClass + " is not mapped to the Database.";
            throw new IllegalStateException(msg);
        }
        return result;
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


    /** Returns a final meta-model in the XML format */
    public String getConfig() {
        return databases.toString();
    }
    
}
