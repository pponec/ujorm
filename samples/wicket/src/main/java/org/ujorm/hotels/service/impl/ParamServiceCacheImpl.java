/*
 * Copyright 2014, Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.hotels.service.impl;

import java.io.Serializable;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.ujorm.Key;
import org.ujorm.core.UjoTools;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.ParamKey;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.entity.enums.ModuleEnum;
import org.ujorm.hotels.service.ModuleParams;
import org.ujorm.hotels.service.ParamService;
import org.ujorm.hotels.service.param.annot.PersonalParam;
/**
 * Common Parameter service service provider including a cache
 * @author Pavel Ponec
 */
@Service(ParamService.CACHED)
public class ParamServiceCacheImpl extends ParamServiceImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamServiceCacheImpl.class);

    /** See the configuration file {@code ehcache.xml} */
    private static final String CACHE_IDENTIFIER = "parameterObjectCache";

    @Inject
    @Named("cacheManager")
    private CacheManager cacheManager;

    /** Get an instance of the Cache */
    private Cache getObjectCache() {
        return cacheManager.getCache(CACHE_IDENTIFIER);
    }

    /** Cache the value */
    @Override
    public <U extends ModuleParams, T> T getValue(final Key<? super U, T> key, final ModuleEnum module, final Customer aCustomer) {
        final T result;
        final boolean personalParam = UjoTools.findAnnotation(key, PersonalParam.class) != null;
        final Customer customer = personalParam ? aCustomer : null;
        final Cache cache = getObjectCache();
        final CacheKey cacheKey = new CacheKey(key.getName(), module, customer);
        final ValueWrapper wrapper = cache.get(cacheKey);

        if (wrapper != null) {
            result = (T) wrapper.get();
        } else {
            LOGGER.info("No cached value for the parameter: " + key.getFullName());
            result = super.getValue(key, module, customer);
            cache.put(cacheKey, result);
        }

        return result;
    }

    /** Update the parametr value and evict the parameter value from the current cache */
    @Override
    public void updateValue(final ParamValue param, final Customer user) {
        super.updateValue(param, user);
        evictParam(param.getParamKey(), user);
    }

    /** Evict a parameter value from the current cache */
    protected void evictParam(final ParamKey param, final Customer customer) {
        final CacheKey ck = new CacheKey
        ( param.getName()
        , param.getModule()
        , param.getSystemParam() ? null : customer);
        getObjectCache().evict(ck);
    }

    /** The method clear cache */
    @Override
    public void clearCache() {
        getObjectCache().clear();
    }

    /** Cache key object */
    static class CacheKey implements Serializable {
        private final int customerId;
        private final String keyName;
        private final ModuleEnum module;
        private int hash;

        public CacheKey(final String keyName, final ModuleEnum module, final Customer customer) {
            Integer custId = customer != null ? customer.getId() : null;
            this.customerId = custId != null ? custId : Integer.MIN_VALUE;
            this.keyName = keyName;
            this.module = module;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                int result = 7;
                result = 53 * result + this.customerId;
                result = 53 * result + this.keyName.hashCode();
                result = 53 * result + this.module.hashCode();
                hash = result;
            }
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CacheKey other = (CacheKey) obj;
            if (this.customerId != other.customerId) {
                return false;
            }
            if (!this.keyName.equals(other.keyName)) {
                return false;
            }
            if (this.module != other.module) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "CacheKey"
                    + "{ customerId=" + customerId
                    + ", keyName=" + keyName
                    + ", module=" + module + '}';
        }
    }

}
