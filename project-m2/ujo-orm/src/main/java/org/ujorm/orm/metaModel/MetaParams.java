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

package org.ujorm.orm.metaModel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.annot.Immutable;
import org.ujorm.core.annot.Transient;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.AbstractMetaModel;
import org.ujorm.orm.ITypeService;
import org.ujorm.orm.TypeService;
import org.ujorm.orm.ao.CachePolicy;
import org.ujorm.orm.ao.CheckReport;
import org.ujorm.orm.ao.CommentPolicy;
import org.ujorm.orm.ao.Orm2ddlPolicy;
import org.ujorm.orm.utility.OrmTools;

/**
 * A logical database description.
 * The class is a root of database configuration.
 * @author Pavel Ponec
 */
@Immutable
final public class MetaParams extends AbstractMetaModel {
    private static final Class<MetaParams> CLASS = MetaParams.class;
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(MetaParams.class);

    /** Property Factory */
    private static final KeyFactory<MetaParams> f = KeyFactory.CamelBuilder.get(CLASS);
    /** Session cache policy. 
     * The default value is PROTECTED_CACHE.
     * @see CachePolicy Parameter values */
    public static final Key<MetaParams,CachePolicy> CACHE_POLICY = f.newKey("cachePolicy", CachePolicy.PROTECTED_CACHE);
    /** Special prameter for an automatically assembled table alias prefix.
     * The default value is the empty string. */
    public static final Key<MetaParams,String> TABLE_ALIAS_PREFIX = f.newKey("tableAliasPrefix", "");
    /** Special prameter for an automatically assembled table alias prefix.
     * The default value is the empty String. */
    public static final Key<MetaParams,String> TABLE_ALIAS_SUFFIX = f.newKey("tableAliasSuffix", "");
    /** Sequential cache parameter saves the number of requests to the following sequence when a insert statement into DB.
     * The value of the parameter is used only when creating a new DB, indivuální ORM changes for each table 
     * can be changed any time later in the column 'cache' of table 'ormujo_pk_support' .
     * Default values is 100, the smallest possible value is 1. */
    public static final Key<MetaParams,Integer> SEQUENCE_CACHE = f.newKey("sequenceCache", 100);
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
    /** The instance of the parameter class {@see ITypeService} is used for conversion, reading and writting to/from the ResultSet.
     * You can specify a sybtype of the class for a commiono special fetures.
     * @see org.ujorm.orm.annot.Column#converter() 
     */
    public static final Key<MetaParams,Class<? extends ITypeService>> TYPE_SERVICE = f.newClassKey("typeService", TypeService.class);
    /** CheckReport a keyword in the database table or colum name inside the meta-model.
     * The default value is EXCEPTION.
     * @see CheckReport Parameter values
     * @see #QUOTE_SQL_NAMES
     */
    public static final Key<MetaParams,CheckReport> CHECK_KEYWORDS = f.newKey("checkKeywords", CheckReport.EXCEPTION);
    /** The maximal count of items for the SQL IN operator, default value is 500 items
     * The limit is used inside the method {@link OrmTools#loadLazyValuesAsBatch(java.lang.Iterable, org.ujorm.Key) loadLazyValuesAsBatch(..)}.
     @see OrmTools#loadLazyValuesAsBatch(java.lang.Iterable, org.ujorm.Key)
     */
    public static final Key<MetaParams,Integer> MAX_ITEM_COUNT_4_IN = f.newKey("maxItemCountForIN", 500);

    /** The parameter value TRUE affects to a Sequence key name in the internal sequence generator.
     * Value TRUE generate a special character "~" instead of default database schema in the sequence table.
     * The benefit of the special chatacter can be evaluated in the case of the renaming of the database schema.
     * In case of change of the parameter value is necessary to convert values in the database column 'ujorm_pk_support.id' by hand.
     * The default value is FALSE.
     */
    public static final Key<MetaParams,Boolean> SEQUENCE_SCHEMA_SYMBOL = f.newKey("sequenceSchemaSymbol", false);

    /** Any action type or CREATE, UPDATE, DELETE on inheritance objects calls the same action to its 'parrent' object.
     * If the mode is off than you must take care of all its parents in the code handy. 
     * The default falue is TRUE.<br />
     * Note: the parameter does not affect the opearations
     * {@link org.ujorm.orm.Session#update(org.ujorm.orm.OrmUjo, org.ujorm.criterion.Criterion) batch update} or
     * {@link org.ujorm.orm.Session#delete(org.ujorm.criterion.Criterion) batch delete} due direct modification of database.
     * @see MetaTable#getParent(org.ujorm.orm.OrmUjo) MetaTable.getParent(..)
     */
    public static final Key<MetaParams,Boolean> INHERITANCE_MODE = f.newKey("inheritanceMode", true);

    /** The parameter is used to limit of the insert sttatemtn in case the "sql multirow insert".
     * The default value is 100.
     * @see org.ujorm.orm.Session#save(java.util.List, int) save(List)
     */
    public static final Key<MetaParams,Integer> INSERT_MULTIROW_ITEM_LIMIT = f.newKey("insertMultirowItemLimit", 100);

    /** The parameter contains the special parameters with for different use. 
     * @see MoreParams
     */
    public static final Key<MetaParams,MoreParams> MORE_PARAMS = f.newKey("moreParams");

    /** Set a logging level for a full meta-model information in the XML format.
     * The TRUE value means the INFO level and the FALSE value means the FINE logging level.
     */
    public static final Key<MetaParams,Boolean> LOG_METAMODEL_INFO = f.newKey("metamodelLogInfo", true);

    /** Pamameter tries to install a brighe to the <a href="http://logback.qos.ch/">Logback</a> logging framework
     * using a statement <code>SLF4JBridgeHandler.install()</code>;
     */
    public static final Key<MetaParams,Boolean> LOGBACK_LOGGING_SUPPORT = f.newKey("logbackLoggingSupport", false);

    /** An application context for initializaton of the customer componets of the meta-model. */
    @Transient
    public static final Key<MetaParams,Object> APPL_CONTEXT = f.newKey("applContext");

    /** The property initialization */
    static{f.lock();}

    /** The type service cache */
    private final Map<Class, ITypeService> typeServices = new HashMap<Class, ITypeService>(2);

    public MetaParams() {
        MORE_PARAMS.setValue(this, new MoreParams());
    }

    @Override
    public void writeValue(Key property, Object value) {

        // Sequence validation:
        if (SEQUENCE_CACHE==property) {
            int val = (Integer) value;
            if (val<1) {
                value = 1;
                final String msg = "The smallest possible value of property '"+property+"' is 1, not " + val;
                LOGGER.log(Level.WARNING, msg);
            }
        }
        super.writeValue(property, value);
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
            } catch (Exception e) {
                throw new IllegalStateException("Can't create a type service for the " + converterClass, e);
            }
        }
        return result;
    }

    /** Set a parameter value */
    @SuppressWarnings("unchecked")
    public <UJO extends MetaParams, VALUE> MetaParams set(Key<UJO, VALUE> property, VALUE value) {
        property.setValue((UJO) this,value);
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
     * @see #CHECK_KEYWORDS
     * @see CheckReport#SKIP_AND_QUOTE_SQL_NAMES
     */        
    public boolean isQuotedSqlNames() {
        return CheckReport.QUOTE_SQL_NAMES==CHECK_KEYWORDS.of(this);
    }
}
