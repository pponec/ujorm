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
package org.ujorm.hotels.services.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.Key;
import org.ujorm.core.UjoCoder;
import org.ujorm.core.UjoManager;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.ParamKey;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.entity.enums.Module;
import org.ujorm.hotels.services.*;
/**
 * Common database service implementations
 * @author ponec
 */
@Service
@Transactional
public class ParamServiceImpl<U extends KeyValue>
extends AbstractServiceImpl
implements ParamService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamServiceImpl.class);

    @Autowired
    private AuthService authService;

    /** Get a value of the key */
    @Override
    public <U extends ModuleParams, T> T getValue(Key<? super U, T> key) {
        try {
            final U instance = (U) key.getDomainType().newInstance();
            return getValue(key, instance.getModule());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't get a value for the key: " + key.getFullName(), e);
        }
    }

    /** Get a value of the key */
    @Override
    public <U extends ModuleParams, T> T getValue(Key<? super U, T> key, Module module) {
        final Criterion<ParamValue> crn1, crn2, crn3, crn4, crn5;
        crn1 = ParamValue.KEY_NAME$.whereEq(key.getName());
        crn2 = ParamValue.KEY_MODULE$.whereEq(module);
        crn3 = ParamValue.CUSTOMER.whereNull();
        crn4 = ParamValue.CUSTOMER.whereEq(authService.getLoggedCustomer());
        crn5 = crn1.and(crn2).and(crn3.or(crn4));
        //
        ParamValue param = null;
        for (ParamValue paramValue : getSession().createQuery(crn5).orderBy(ParamValue.CUSTOMER)) {
            param = paramValue;
            if (paramValue.readFK(ParamValue.CUSTOMER) != null) {
                break;
            }
        }

        return param != null
                ? UjoManager.getInstance().decodeValue(key,  param.getTextValue())
                : key.getDefault();
    }

    /** Save all parameters into database */
    @Override
    public void init(ModuleParams<?> params) {
        final Map<String, ParamKey> paramKeyMap = getParamKeyMap(params);

        // --- KEYS ----

        final UjoCoder converter = UjoManager.getInstance().getCoder();
        final Date now = new Date();
        for (Key key : params.readKeys()) {
            ParamKey paramKey = paramKeyMap.get(key.getName());
            if (paramKey == null) {
                paramKey = new ParamKey(key.getName(), params.getModule());
                paramKeyMap.put(key.getName(), paramKey);
            }
            paramKey.setParamClass(key.getType());
            paramKey.setTextDefaultValue(converter.encodeValue(key.getDefault(), false));
            paramKey.setLastUpdate(now);
            paramKey.setSystemParam(true); // TODO
            paramKey.setNote("-"); // TODO
            getSession().saveOrUpdate(paramKey);
        }

        // --- VALUES ----

        final Map<String, ParamValue> valueMap = getParamValueMap(paramKeyMap.values());
        for (Key key : params.readKeys()) {
            ParamValue paramValue = valueMap.get(key.getName());
            if (paramValue == null) {
                paramValue = new ParamValue(paramKeyMap.get(key.getName()));
                paramValue.setCustomer(null);
                paramValue.setLastUpdate(now);
                getSession().save(paramValue);
            }
        }
    }

    /** Returns a saved ParamKeySet for required module */
    private Map<String, ParamKey> getParamKeyMap(ModuleParams<?> params) {
        final Set<String> keyNames = new HashSet<>(params.readKeys().size());
        for (Key key : params.readKeys()) {
            boolean unique = keyNames.add(key.getName());
            if (!unique) {
                throw new IllegalStateException("The parameter is not unique: " + key.getFullName());
            }
        }
        final Criterion<ParamKey> crn1, crn2, crn3;
        crn1 = ParamKey.NAME.whereIn(keyNames);
        crn2 = ParamKey.MODULE.whereIn(params.getModule());
        crn3 = crn1.and(crn2);

        final Map<String, ParamKey> result = new HashMap<>(keyNames.size());
        for (ParamKey paramKey : getSession().createQuery(crn3)) {
            result.put(paramKey.getName(), paramKey);
        }
        return result;
    }

    /** Returns a ParamValueSet for required module */
    private Map<String, ParamValue> getParamValueMap(Collection<ParamKey> keys) {
        final Map<String, ParamValue> result = new HashMap<>(keys.size());
        final Criterion<ParamValue> crn = ParamValue.PARAM_KEY.whereIn(keys);
        for (ParamValue value : getSession().createQuery(crn).addColumn(ParamValue.KEY_NAME$)) {
            result.put(ParamValue.KEY_NAME$.of(value), value);
        }
        return result;
    }

}
