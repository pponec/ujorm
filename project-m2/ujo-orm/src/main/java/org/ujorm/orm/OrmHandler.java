/*
 *  Copyright 2009-2014 Pavel Ponec
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

package org.ujorm.orm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.core.UjoTools;
import org.ujorm.core.annot.Immutable;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaProcedure;
import org.ujorm.orm.metaModel.MetaRelation2Many;
import org.ujorm.orm.metaModel.MetaRoot;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.orm.template.AliasTable;

/**
 * The basic class for an ORM support.
 * @author Pavel Ponec
 * @composed 1 - 1 MetaRoot
 * @assoc - - - AbstractMetaModel
 */
@Immutable
public class OrmHandler implements OrmHandlerProvider {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(OrmHandler.class);

    /** List of databases */
    private final MetaRoot databases = new MetaRoot();
    /** Temporary configuration */
    private MetaRoot configuration;
    /** The default ORM session */
    private Session defaultSession;

    /** Map a <strong>key</strong> to a database <strong>column model</strong> */
    private final HashMap<Key,MetaRelation2Many> propertyMap = new HashMap<Key,MetaRelation2Many> ();
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

     /** Get a <strong>default</strong> Session of the OrmHandler.
      * On a multi-thread application use a method {@link #createSession()} rather.
      * @see #createSession()
      */
    public Session getDefaultSession() {
        if (defaultSession==null) {
            defaultSession = createSession();
        }
        return defaultSession;
    }

     /** Get a <strong>default</strong> Session of the OrmHandler.
      * On a multi-thread application use a method {@link #createSession()} rather.
      * @see #createSession()
      * @deprecated Method was replaced by the name {@link #getDefaultSession() }
      */
    public Session getSession() {
        return getDefaultSession();
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

    /** Save the alternative ORM configuration including parameters (if the parameters are not null).
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

    /** Is the parameter a persistent key? */
    public boolean isPersistent(Key key) {

        final boolean resultFalse
        =  key.isTypeOf(List.class)
        || UjoManager.getInstance().isTransient(key)
        ;
        return !resultFalse;
    }

    /** LoadInternal a database model from parameter */
    private <UJO extends OrmUjo> MetaDatabase loadDatabaseInternal(Class<UJO> databaseModel) {

        // Load a configuration parameters:
        String databaseId = databaseModel.getSimpleName();
        MetaDatabase paramDb = configuration!=null ? configuration.removeDb(databaseId) : null;

        // Create the ORM DB model:
        UJO root = getInstance(databaseModel);
        MetaDatabase dbModel = new MetaDatabase(this, root, paramDb, databases.getDatabaseCount());
        databases.add(dbModel);

        return dbModel;
    }

    /** Load a meta-data, lock it and create database tables.
     * There is not allowed to make any change to the created meta-model.
     */
    @SuppressWarnings("unchecked")
    final public <UJO extends OrmUjo> void loadDatabase(final Class<UJO> databaseModel) {
        loadDatabase(new Class[] {databaseModel});
    }

    /** Load a meta-data, lock it and create database tables.
     * There is not allowed to make any change to the created meta-model.
     */
    public final synchronized <UJO extends OrmUjo> void loadDatabase(final Class<UJO> ... databaseModel) {

        if (isReadOnly()) {
            throw new IllegalArgumentException("The meta-model is locked and can´t be changed.");
        }

        // Load meta-model:
        for (Class<UJO> db : databaseModel) {
            loadDatabaseInternal(db);
        }

        // Initialize Column Type codes:
        MetaParams params = getParameters();
        for (MetaRelation2Many r : propertyMap.values()) {
            if (r.isColumn()) {
                ((MetaColumn)r).initTypeCode();
            }
        }

        // Lock the meta-model:
        databases.lock();

        // Log the meta-model:
        final Level level = MetaParams.LOG_METAMODEL_INFO.of(params)
            ? UjoLogger.INFO
            : UjoLogger.DEBUG;
        if (LOGGER.isLoggable(level)) {
            final String msg = "DATABASE META-MODEL:\n" + getConfig();
           LOGGER.log(level, msg);
        }

        // Export the meta-model into a XML file:
        final File outConfigFile = MetaParams.SAVE_CONFIG_TO_FILE.of(getParameters());
        if (outConfigFile!=null) try {
            databases.print(outConfigFile);
        } catch (IOException e) {
            throw new IllegalStateException("Can't create configuration " + outConfigFile, e);
        }

        for (MetaDatabase dbModel : getDatabases()) {
            dbModel.create(getDefaultSession());
        }

        // Run an initializaton batch:
        final InitializationBatch batch = params.getInitializationBatch();
        if (batch != null) {
            Session session = null;
            try {
                session = createSession();
                LOGGER.log(UjoLogger.INFO, "The initializaton batch is running: {}", batch.getClass().getName());
                batch.run(session);
                session.commit();
                session.close();
            } catch (Exception e) {
                final String msg = "The batch failed: " + batch.getClass().getName();
                LOGGER.log(UjoLogger.ERROR, msg, e);
                throw new IllegalStateException(msg, e);
            } finally {
                if (session != null) {
                    session.rollback();
                    session.close();
                }
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
        final List<MetaDatabase> dbs = getDatabases();
        final boolean result = UjoTools.isFilled(dbs) ? dbs.get(0).readOnly() : false;
        return result;
    }

    /** Returns database model */
    public MetaRoot getDatabaseModel() {
        return databases;
    }

    /** Map a key to the table */
    @SuppressWarnings("unchecked")
    public void addProcedureModel(MetaProcedure metaProcedure) {
        procedureMap.put(MetaProcedure.DB_PROPERTY.of(metaProcedure).getType(), metaProcedure);
    }

    /** Map a key to the table */
    @SuppressWarnings("unchecked")
    public void addTableModel(MetaTable metaTable) {
        entityMap.put(metaTable.getType(), metaTable);
    }

    /** Map a key to the table */
    @SuppressWarnings("unchecked")
    public void addColumnModel(MetaRelation2Many column) {
        Key key = column.getKey();
        MetaRelation2Many oldColumn = findColumnModel(key);

        if (oldColumn == null) {
            propertyMap.put(key, column);
        } else {
            final Class oldType = oldColumn.getTableClass();
            final Class newType = column.getTableClass();

            if (newType.isAssignableFrom(oldType)) {
                // Only a parent can be assigned:
                propertyMap.put(key, column);
            }
        }
    }

    /**
     * Find a key annotation by the required type.
     * @param key The key must be a <strong>public static final</strong> field of the related Ujo class.
     * @param annotation Annotation type
     * @return  An annotation instance or the {@code null} value
     * @deprecated Uset the {@link UjoManager#findAnnotation(org.ujorm.Key, java.lang.Class)} rather
     *
     */
    @Deprecated
    public <T extends Annotation> T findAnnotation(Key<?,?> key, Class<T> annotation) {
        return UjoManager.findAnnotation(key, annotation);
    }

    /** Find a Relation/Column model of the parameter key.
     * The column result is type of {@link MetaColumn}.
     * @param compositeKey Parameter can be type of Key of CompositeKey (direct or indirect);
     * @return Returns an object type of {@link MetaColumn} for database column
     * or a related model type of {@link MetaRelation2Many}
     * or the NULL if no model was found.
     */
    final public <T extends MetaRelation2Many> T findColumnModel(Key compositeKey) {
        return findColumnModel(compositeKey, false);
    }

    /** Find a Relation/Column model of the parameter key.
     * The column result is type of {@link MetaColumn}.
     * @param compositeKey Parameter can be type of CompositeKey (direct or indirect);
     * @param throwException Throw the IllegalArgument exception of no Model was not found
     * @return Returns an object type of {@link MetaColumn} for database column
     * or a related model type of {@link MetaRelation2Many}
     * or the NULL if no model was found.
     */
    public <T extends MetaRelation2Many> T findColumnModel(Key compositeKey, boolean throwException) throws IllegalArgumentException {
        if (compositeKey!=null && compositeKey.isComposite()) {
            compositeKey = ((CompositeKey)compositeKey).getLastKey();
                    }
        final MetaRelation2Many result = propertyMap.get(compositeKey);
        if (throwException && result == null) {
            String propertyName = compositeKey != null ? compositeKey.getFullName() : String.valueOf(compositeKey);
            throw new IllegalArgumentException("The key " + propertyName + " have got no meta-model.");
        }
        return (T) result;
    }

    /** Find a table model by the dbClass.
     * If the table model is not found then the IllegalStateException is throwed.
     */
    public final MetaTable findTableModel(final Class<? extends OrmUjo> dbClass) throws IllegalStateException {
        return findTableModel(dbClass, true);
    }

    /**
     * Find a table model by the dbClass.
     * @param throwException Throw the IllegalArgument exception of no Model was not found
     * @return Returns a related model throw the IllegalArgumentException exception.
     */
    public MetaTable findTableModel(final Class<? extends OrmUjo> dbClass, final boolean throwException) throws IllegalStateException {
        final MetaTable result = entityMap.get(dbClass);
        if (result==null && throwException) {
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

    /** Find all <strong>persistent<strong> keys with the required type or subtype.
     * @param type The parameter value Object.clas returns all persistent keys.
     */
    public List<Key> findPropertiesByType(Class type) {
        List<Key> result = new ArrayList<Key>();
        for (Key p : propertyMap.keySet()) {
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

    /** Returns the same instance */
    public OrmHandler getOrmHandler() {
        return this;
    }

    /** Create a new instance of the class {@link AliasTable} */
    public <UJO extends OrmUjo> AliasTable<UJO> tableOf(Class<UJO> entity, String alias) {
        return AliasTable.of(entity, alias, this);
    }

    /** Create a new instance of the class {@link AliasTable} */
    public <UJO extends OrmUjo> AliasTable<UJO> tableOf(Class<UJO> entity) {
        return AliasTable.of(entity, this);
    }

    /** Create a new instance of the class {@link AliasTable}.
     * Use the new method {@link OrmHandler#tableOf(java.lang.Class, java.lang.String)} rather */
    @Deprecated
    public <UJO extends OrmUjo> AliasTable<UJO> alias(Class<UJO> entity, String alias) {
        return tableOf(entity, alias);
    }
}