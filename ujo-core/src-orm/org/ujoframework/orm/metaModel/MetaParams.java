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

package org.ujoframework.orm.metaModel;

import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.ao.CachePolicy;
import org.ujoframework.orm.ao.Orm2ddlPolicy;

/**
 * A logical database description.
 * The class is a root of database configuration.
 * @author Pavel Ponec
 */
public class MetaParams extends AbstractMetaModel {
    private static final Class CLASS = MetaParams.class;
    public static final Logger LOGGER = Logger.getLogger(MetaParams.class.getName());


    /** Enable / disable a session cache for the business objects. */
    public static final Property<MetaParams,CachePolicy> CACHE_POLICY = newProperty("cachePolicy", CachePolicy.MANY_TO_ONE);
    /** The parameters enables the the cache implementation by WeakHashMap. The false value use a HashMap implementation. Default value is TRUE. */
    public static final Property<MetaParams,Boolean> CACHE_WEAK_MAP = newProperty("cacheWeakMap", true);
    /** Special prameter for an automatically assembled table alias prefix. */
    public static final Property<MetaParams,String> TABLE_ALIAS_PREFIX = newProperty("tableAliasPrefix", "");
    /** Special prameter for an automatically assembled table alias prefix. */
    public static final Property<MetaParams,String> TABLE_ALIAS_SUFFIX = newProperty("tableAliasSuffix", "");
    /** Sequential cache parameter saves the number of requests to the following sequence when a insert statement into DB.
     * The value of the parameter is used only when creating a new DB, indivuální ORM changes for each table 
     * can be changed any time later in the column 'cache' of table 'ormujo_pk_support' .
     * Default values is 64, the smallest possible value is 1. */
    public static final Property<MetaParams,Integer> SEQUENCE_CACHE = newProperty("sequenceCache", 64);
    /** A policy to defining the database structure by a DDL. */
    public static final Property<MetaParams,Orm2ddlPolicy> ORM2DLL_POLICY = newProperty("Orm2ddlPolicy", Orm2ddlPolicy.CREATE_DDL);
    /** The property initialization */
    static{init(CLASS);}

    @Override
    public void writeValue(UjoProperty property, Object value) {

        // Sequence validation:
        if (SEQUENCE_CACHE==property) {
            int val = (Integer) value;
            if (val<1) {
                value = 1;
                LOGGER.warning("The smallest possible value of property '"+property+"' is 1, not " + val);
            }
        }

        super.writeValue(property, value);
    }

    /** Is the cache enabled? */
    public boolean isCacheEnabled() {
        final boolean result = MetaParams.CACHE_POLICY.of(this)!=CachePolicy.NONE;
        return result;
    }
    
}
