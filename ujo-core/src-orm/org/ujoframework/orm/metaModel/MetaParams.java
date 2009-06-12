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
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.ao.CachePolicy;

/**
 * A logical database description.
 * The class is a root of database configuration.
 * @author Pavel Ponec
 */
public class MetaParams extends AbstractMetaModel {

    public static final Logger LOGGER = Logger.getLogger(MetaParams.class.getName());
    /** Property count */
    protected static int propertyCount = AbstractMetaModel.propertyCount;


    /** Enable / disable a session cache */
    public static final UjoProperty<MetaParams,CachePolicy> CACHE_POLICY = newProperty("cachePolicy", CachePolicy.MANY_TO_ONE, propertyCount++);
    /** Is the enabled cache implemented by WeakHashMap? The false value implements an HashMap instance. Default value is TRUE. */
    public static final UjoProperty<MetaParams,Boolean> CACHE_WEAK = newProperty("cacheWeak", true, propertyCount++);
    /** Special prameter for an automatically assembled table alias prefix. */
    public static final UjoProperty<MetaParams,String> TABLE_ALIAS_PREFIX = newProperty("tableAliasPrefix", "", propertyCount++);
    /** Special prameter for an automatically assembled table alias prefix. */
    public static final UjoProperty<MetaParams,String> TABLE_ALIAS_SUFFIX = newProperty("tableAliasSuffix", "", propertyCount++);
    /** Sequence increment, default values is 64 */
    public static final UjoProperty<MetaParams,Integer> SEQUENCE_INCREMENT = newProperty("sequsenceIncrement", 64, propertyCount++);

    

    /** Property Count */
    @Override
    public int readPropertyCount() {
        return propertyCount;
    }

    /** Is the cache enabled? */
    public boolean isCacheEnabled() {
        final boolean result = MetaParams.CACHE_POLICY.of(this)!=CachePolicy.NONE;
        return result;
    }
    
}
