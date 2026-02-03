/*
 *  Copyright 2009-2026 Pavel Ponec
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

package org.ujorm.orm.metaModel;

import java.io.File;
import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.Key;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.Transient;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.FixingTableSequences;
import org.ujorm.orm.FixingTableSequencesDisabled;
import org.ujorm.orm.ITypeService;
import org.ujorm.orm.InitializationBatch;
import org.ujorm.orm.SqlNameProvider;
import org.ujorm.orm.TypeService;
import org.ujorm.orm.ao.CachePolicy;
import org.ujorm.orm.ao.CheckReport;
import org.ujorm.orm.ao.CommentPolicy;
import org.ujorm.orm.ao.IndexModelBuilder;
import org.ujorm.orm.ao.LoadingPolicy;
import org.ujorm.orm.ao.Orm2ddlPolicy;
import org.ujorm.orm.utility.OrmTools;

/**
 * An ORM configuration parameters is a part of metamodel.
 * The class is a root of a database configuration.
 * @author Pavel Ponec
 */
@Unmodifiable
public final class MetaParams extends AbstractMetaModel {
    private static final Class<MetaParams> CLASS = MetaParams.class;
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(CLASS);
    private static final String INSTANCE_FAILED_MSG = "Instance of the class failed: ";

    /** Property Factory */
    private static final KeyFactory<MetaParams> f = KeyFactory.CamelBuilder.get(CLASS);
    /** Session cache policy.
     * The default value is PROTECTED_CACHE.
     * @see CachePolicy Parameter values */
    public static final Key<MetaParams,CachePolicy> CACHE_POLICY = f.newKey("cachePolicy", CachePolicy.PROTECTED_CACHE);
    /** Special parameter for an automatically assembled table alias prefix.
     * The default value is the empty string. */
    public static final Key<MetaParams,String> TABLE_ALIAS_PREFIX = f.newKey("tableAliasPrefix", "");
    /** Special parameter for an automatically assembled table alias prefix.
     * The default value is the empty String. */
    public static final Key<MetaParams,String> TABLE_ALIAS_SUFFIX = f.newKey("tableAliasSuffix", "");
    /** Sequential cache parameter saves the number of requests to the following sequence when a insert statement into DB.
     * The value of the parameter is used only when creating a new DB, specific ORM changes for each table
     * can be changed any time later in the column 'cache' of table 'ormujo_pk_support' .
     * Default values is 100, the smallest possible value is 1. */
    public static final Key<MetaParams,Integer> SEQUENCE_CACHE = f.newKey("sequenceCache", 100);
    /** The parameter 'lazyLoading' specify a lazy-loading policy in case
     * that UJO objects have got any session. Argument options are described
     * in the enum {@link org.ujorm.orm.ao.LoadingPolicy}.
     * In case the object have assigned no session, the key returns the {@code null} value
     * as well as common javabeans. A default key value is the
     * {@link org.ujorm.orm.ao.LoadingPolicy#ALLOWED_USING_OPEN_SESSION}.
     * @see org.ujorm.orm.ao.LoadingPolicy */
    public static final Key<MetaParams,LoadingPolicy> LOADING_POLICY = f.newKey("loadingPolicy", LoadingPolicy.ALLOWED_USING_OPEN_SESSION);
    /** A policy to defining the database structure by a DDL.
     * The default value is option: CREATE_OR_UPDATE_DDL.
     * @see Orm2ddlPolicy Parameter values
     */
    public static final Key<MetaParams,Orm2ddlPolicy> ORM2DLL_POLICY = f.newKey("orm2ddlPolicy", Orm2ddlPolicy.CREATE_OR_UPDATE_DDL);
    /** A policy for assigning an annotation table comment {@link org.ujorm.orm.annot.Comment} to database.
     * The default value is ON_ANY_CHANGE.
     * @see CommentPolicy  Parameter values
     */
    public static final Key<MetaParams,CommentPolicy> COMMENT_POLICY = f.newKey("commentPolicy", CommentPolicy.ON_ANY_CHANGE);
    /** Framework can save the final configuration file to a new file for an external use. If this parameter is null than the save action is skipped. */
    public static final Key<MetaParams,File> SAVE_CONFIG_TO_FILE = f.newKey("saveConfigToFile");
    /** An initialization batch implementation can be called after building the ORM metamodel.
     * Default value means: run no batch.
     * @see InitializationBatch
     */
    public static final Key<MetaParams,Class<? extends InitializationBatch>> INITIALIZATION_BATCH = f.newClassKey("initializationBatch", InitializationBatch.class);
    /** The instance of the parameter class {@see ITypeService} is used for conversion, reading and writing to/from the ResultSet.
     * You can specify a subtype of the class for a column special features.
     * @see org.ujorm.orm.annot.Column#converter()
     * @see org.ujorm.orm.TypeServiceForOlderJdbc An implementation for an older JDBC API (older version than 4.2)
     */
    public static final Key<MetaParams,Class<? extends ITypeService>> TYPE_SERVICE = f.newClassKey("typeService", TypeService.class);
    /** The class for is used for building a {@link org.ujorm.orm.metaModel.MetaIndex} model.
     * You can specify a subtype of the class {@link org.ujorm.orm.ao.IndexModelBuilder}
     * for an index special features.
     * @see org.ujorm.orm.ao.IndexModelBuilder
     * @see org.ujorm.orm.ao.IndexModelOrderedBuilder
     */
    public static final Key<MetaParams,Class<? extends IndexModelBuilder>> INDEX_MODEL_BUILDER = f.newClassKey("indexModelBuilder", IndexModelBuilder.class);
    /** The instance of the parameter class {@see MetaDbService} is used for creating and validation a database according to the metamodel.
     * You can overwrite some method for your ideas.
     */
    public static final Key<MetaParams,Class<? extends MetaDbService>> META_DB_SERVICE = f.newClassKey("metaDbService", MetaDbService.class);
    /** Default SQL name provider for special names of database is called: {@link SqlNameProvider} */
    public static final Key<MetaParams,Class<? extends SqlNameProvider>> SQL_NAME_PROVIDER = f.newClassKey("sqlNameProvider", SqlNameProvider.class);
    /** The quotation policy in the SQL statement, where default is no quotatio with an EXCEPTION if any keyword is found.
     * @see CheckReport Parameter values
     * @see #QUOTE_SQL_NAMES
     */
    public static final Key<MetaParams,CheckReport> QUOTATION_POLICY = f.newKey("checkKeywords", CheckReport.EXCEPTION);
    /** The maximal count of items for the SQL IN operator, default value is 500 items
     * The limit is used inside the method {@link OrmTools#loadLazyValuesAsBatch(java.lang.Iterable, org.ujorm.Key) loadLazyValuesAsBatch(..)}.
     @see OrmTools#loadLazyValuesAsBatch(java.lang.Iterable, org.ujorm.Key)
     */
    public static final Key<MetaParams,Integer> MAX_ITEM_COUNT_4_IN = f.newKey("maxItemCountForIN", 500);

    /** The parameter value TRUE affects to a Sequence key name in the internal sequence generator.
     * Value TRUE generate a special character "~" instead of default database schema in the sequence table.
     * The benefit of the special character can be evaluated in the case of the renaming of the database schema.
     * In case of change of the parameter value is necessary to convert values in the database column 'ujorm_pk_support.id' by hand.
     * <br>NOTE: The default value is FALSE for backward compatibility, however for new projects is preferred value TRUE.
     */
    public static final Key<MetaParams,Boolean> SEQUENCE_SCHEMA_SYMBOL = f.newKey("sequenceSchemaSymbol", false);

    /** Any action type or CREATE, UPDATE, DELETE on inheritance objects calls the same action to its 'parent' object.
     * If the mode is off than you must take care of all its parents in the code handy.
     * The default failure is TRUE.<br>
     * Note: the parameter does not affect the operations
     * {@link org.ujorm.orm.Session#update(org.ujorm.orm.OrmUjo, org.ujorm.criterion.Criterion) batch update} or
     * {@link org.ujorm.orm.Session#delete(org.ujorm.criterion.Criterion) batch delete} due direct modification of database.
     * @see MetaTable#getParent(org.ujorm.orm.OrmUjo) MetaTable.getParent(..)
     */
    public static final Key<MetaParams,Boolean> INHERITANCE_MODE = f.newKey("inheritanceMode", true);

    /** The parameter is used to limit of the insert statement in case the "sql multirow insert".
     * The default value is 100.
     * @see org.ujorm.orm.Session#save(java.util.List, int) save(List)
     */
    public static final Key<MetaParams,Integer> INSERT_MULTIROW_ITEM_LIMIT = f.newKey("insertMultirowItemLimit", 100);

    /** The parameter contains the special parameters with for different use.
     * @see MoreParams
     */
    public static final Key<MetaParams,MoreParams> MORE_PARAMS = f.newKey("moreParams");

    /** Set a logging level for a full metamodel information in the XML format
     * where the default parameter value is {@code true}.
     * The TRUE value means the INFO level and the FALSE value means the FINE logging level.
     */
    public static final Key<MetaParams,Boolean> LOG_METAMODEL_INFO = f.newKey("logMetamodelInfo", true);

    /** Enable the login for a multi-value SQL statement INSERT including its values
     * where the default parameter value is {@code false}.
     * If JDBC driver does not support the multi-value statement, the parameter is ignored.
     */
    public static final Key<MetaParams,Boolean> LOG_SQL_MULTI_INSERT = f.newKey("logSqlMultiInsert", false);

    /** If the parameter is true, then the OrmHandler closes a {@code default session} after creating database structures.
     * Note, a closing the session of in-memory databases can remove all data structure.
     * The default value is {@code true}.
     *
     * @see See the note of the H2 Database documentation: By default, closing the last connection to a database closes the database.
     * For an in-memory database, this means the content is lost. To keep the database open,
     * add {@code ;DB_CLOSE_DELAY=-1} to the database URL. To keep the content of an in-memory database as long
     * as the virtual machine is alive, use jdbc:h2:mem:test;DB_CLOSE_DELAY=-1.
     */
    public static final Key<MetaParams,Boolean> AUTO_CLOSING_DEFAULT_SESSION = f.newKey("autoClosingDefaultSession", true);

    /** Parameter tries to install a bridge to the <a href="http://logback.qos.ch/">Logback</a> logging framework
     * using a statement <code>SLF4JBridgeHandler.install()</code>;
     */
    public static final Key<MetaParams,Boolean> LOGBACK_LOGGING_SUPPORT = f.newKey("logbackLoggingSupport", false);
    /** Logging JDBC arguments can be cropped using the next argument, default value 128 characters per value,
     * the value {@link Integer.MAX_VALUE} means an unlimited.
     */
    public static final Key<MetaParams,Integer> LOG_VALUE_LENGTH_LIMIT = f.newKey("logValueLengthLimit", 128);

    /** Class for th efixing the table sequences for Ujorm before 1.84 (excluding)
     * @see FixingTableSequencesDisabled */
    public static final Key<MetaParams,Class<? extends FixingTableSequences>> FIXING_TABLE_SEQUENCES = f.newKey("fixingTableSequences", FixingTableSequences.class);

    /** SQL keyword set (upper case) only for case: QUOTATION_POLICY = QUOTE_ONLY_SQL_KEYWORDS */
    @Transient
    public static final Key<MetaParams,Set<String>> KEYWORD_SET = f.newKey("keywordSet", Collections.EMPTY_SET);

    /** An application context for initialization of the customer components of the metamodel. */
    @Transient
    public static final Key<MetaParams,Object> APPL_CONTEXT = f.newKey("applContext");

    /** The key initialization */
    static {
        f.lock();
    }

    /** CheckReport a keyword in the database table or colum name inside the metamodel.
     * The default value is EXCEPTION.
     * @see CheckReport Parameter values
     * @see #QUOTE_SQL_NAMES
     */
    public static final Key<MetaParams,CheckReport> CHECK_KEYWORDS = QUOTATION_POLICY;

    /** An extended index name strategy
     * @see MoreParams#EXTENTED_INDEX_NAME_STRATEGY
     */
    public static final Key<MetaParams, Boolean> EXTENTED_INDEX_NAME_STRATEGY = MORE_PARAMS.add(MoreParams.EXTENTED_INDEX_NAME_STRATEGY);

    /** The type service cache */
    private final Map<Class, ITypeService> typeServices = new HashMap<Class, ITypeService>(2);

    /** Assign an initialization batch */
    private InitializationBatch batch;

    public MetaParams() {
        MORE_PARAMS.setValue(this, new MoreParams());
    }

    @Override
    public void writeValue(Key key, Object value) {

        // Sequence validation:
        if (SEQUENCE_CACHE==key) {
            int val = (Integer) value;
            if (val<1) {
                value = 1;
                final String msg = "The smallest possible value of key '"+key+"' is 1, not " + val;
                LOGGER.log(UjoLogger.WARN, msg);
            }
        }
        super.writeValue(key, value);
    }

    /** Returns a converter instance.
     * Method use an internal cache for smaller converter instance count.
     * @param converterClass A class to create an instance of the converter. If the value is {@code null},
     * then a default converter defined in parameters is used.
     * @return Returns a converter instance.
     */
    public <T extends ITypeService> T getConverter(Class<T> converterClass) {
        if (converterClass==null) {
            converterClass = (Class<T>) TYPE_SERVICE.of(this);
        }
        T result = (T) typeServices.get(converterClass);
        if (result == null) {
            try {
                result = converterClass.newInstance();
                typeServices.put(converterClass, result);
            } catch (RuntimeException | ReflectiveOperationException e) {
                return throwInstantiationException(converterClass, e);
            }
        }
        return result;
    }

    /** Set a parameter value */
    @SuppressWarnings("unchecked")
    public <UJO extends MetaParams, VALUE> MetaParams set(Key<UJO, VALUE> key, VALUE value) {
        key.setValue((UJO) this,value);
        return this;
    }

    /** Set application context. */
    public MetaParams setApplContext(Object applContext) {
        APPL_CONTEXT.setValue(this, applContext);
        return this;
    }

    /** Returns an object to provide the special parameters for a different use. */
    public MoreParams more() {
        return MORE_PARAMS.of(this);
    }

    /** Skip the check test and Quote all SQL columns, tables and alias names.
     * <br>NOTE: The change of the parameter value affects the native SQL statements in Ujorm views.
     * @see #QUOTATION_POLICY
     * @see CheckReport#SKIP_AND_QUOTE_SQL_NAMES
     */
    public boolean isQuotedSqlNames() {
        switch (QUOTATION_POLICY.of(this)) {
            case QUOTE_SQL_NAMES:
            case QUOTE_ONLY_SQL_KEYWORDS:
                return true;
            default:
                return false;
        }
    }

    /** Return quoting policy */
    public CheckReport getQuotationPolicy() {
        return QUOTATION_POLICY.of(this);
    }

    /** Skip the check test and Quote all SQL columns, tables and alias names.
     * <br>NOTE: The change of the parameter value affects the native SQL statements in Ujorm views.
     * @param quote Parameter {@code true} affects to an escaping the database names,
     * else the {@code false} value will affects to checked a database KeyWords in the names.
     * @see #QUOTATION_POLICY
     * @see CheckReport#SKIP_AND_QUOTE_SQL_NAMES
     */
    public void setQuotedSqlNames(boolean quote) {
        QUOTATION_POLICY.setValue(this, quote
                ? CheckReport.QUOTE_SQL_NAMES
                : null // The default value
                );
    }

    /** Assign an initialization batch */
    public void set(Key<MetaParams,Class<? extends InitializationBatch>> key, InitializationBatch batch) {
        checkReadOnly(true);
        this.batch = batch;
    }

    /** Returns an instance of the initialization batch */
    public InitializationBatch getInitializationBatch() throws IllegalStateException {
        if (INITIALIZATION_BATCH.isDefault(this)) {
            return this.batch;
        } else try {
            return INITIALIZATION_BATCH.of(this).newInstance();
        } catch (RuntimeException | ReflectiveOperationException e) {
            return throwInstantiationException(INITIALIZATION_BATCH.of(this), e);
        }
    }

    /** Create new instance of the class IndexModelBuilder and initialize the result by a parameter */
    public IndexModelBuilder getIndexModelBuilder(MetaTable metaTable) throws IllegalStateException {
        try {
            final IndexModelBuilder result = INDEX_MODEL_BUILDER.of(this).newInstance();
            result.init(metaTable);
            return result;
        } catch (RuntimeException | ReflectiveOperationException e) {
            return throwInstantiationException(INDEX_MODEL_BUILDER.of(this), e);
        }
    }

    /** Returns an instance of the initialization batch */
    public FixingTableSequences getFixingTableSequences(@Nullable final MetaDatabase db, @NotNull Connection conn) throws IllegalStateException {
        final Class<? extends FixingTableSequences> resultType = FIXING_TABLE_SEQUENCES.of(this);
        try {
            return resultType.getConstructor(MetaDatabase.class, Connection.class).newInstance(db, conn);
        } catch (RuntimeException | ReflectiveOperationException e) {
            return throwInstantiationException(resultType, e);
        }
    }

    /** Get an application context,  */
    @Nullable
    public <T> T getApplicationContext() {
        return (T) APPL_CONTEXT.of(this);
    }

    /** Throws an unchecked exception due an InstantiationException */
    private <T> T throwInstantiationException(final Class<?> type, final Exception e) throws IllegalUjormException {
        throw new IllegalUjormException(INSTANCE_FAILED_MSG + type, e);
    }
}

