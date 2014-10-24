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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
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
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.ParamKey;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.entity.enums.Module;
import org.ujorm.hotels.services.*;
import org.ujorm.hotels.services.annot.PersonalParam;
import org.ujorm.orm.Session;
import org.ujorm.orm.annot.Comment;
/**
 * Common database service implementations
 * @author Pavel Ponec
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
        } catch (/*ReflectiveOperationException*/ Exception e) {
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
        for (ParamValue paramValue : getSession().createQuery(crn5).orderBy
                ( ParamValue.KEY_NAME$
                , ParamValue.KEY_SYSTEM$.descending())) {
            param = paramValue;
        }

        return param != null
             ? UjoManager.getInstance().decodeValue(key,  param.getTextValue())
             : key.getDefault();
    }

    /** Get all parameters for a required Customer
     * @todo add next argument removeObsolete type of Boolean to exclude obsolete parameter keys
     */
    @Override
    public List<ParamValue> getValues(@Nullable Customer customer) {
        final Criterion<ParamValue> crn1,crn2,crn3;
        crn1 = ParamValue.CUSTOMER.whereNull();
        crn2 = ParamValue.CUSTOMER.whereEq(customer);
        crn3 = crn1.or(crn2);

        final Key<ParamValue,Integer> KEY_ID = ParamValue.KEY_ID$;
        final Map<String,ParamValue> values = createQuery(crn3)
                .orderBy(KEY_ID, ParamValue.KEY_SYSTEM$.descending())
                .addColumn(KEY_ID)
                .map(KEY_ID, new HashMap(128));
        final Map<Integer,ParamKey> keys = createQuery(ParamKey.ID.forAll()).map();
        for (ParamValue value : values.values()) {
            value.setParamKey(keys.get(KEY_ID.of(value)));
        }
        return new ArrayList(values.values());
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
            paramKey.setSystemParam(isSystemParam(key));
            paramKey.setNote(getComment(key));
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

    /** Get a comment of the parameter Key */
    private String getComment(Key key) {
        final Comment comment = UjoManager.findAnnotation(key, Comment.class);
        return comment != null ? comment.value() : "";
    }

    /** Is the key a system parameter */
    private boolean isSystemParam(Key key) {
        final PersonalParam user = UjoManager.findAnnotation(key, PersonalParam.class);
        return user == null;
    }

    /** Returns a saved ParamKeySet for required module */
    private Map<String, ParamKey> getParamKeyMap(ModuleParams<?> params) {
        final Set<String> keyNames = new HashSet<String>(params.readKeys().size());
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

        final Map<String, ParamKey> result = new HashMap<String, ParamKey>(keyNames.size());
        for (ParamKey paramKey : getSession().createQuery(crn3)) {
            result.put(paramKey.getName(), paramKey);
        }
        return result;
    }

    /** Returns a ParamValueSet for required module */
    private Map<String, ParamValue> getParamValueMap(Collection<ParamKey> keys) {
        final Map<String, ParamValue> result = new HashMap<String, ParamValue>(keys.size());
        final Criterion<ParamValue> crn = ParamValue.PARAM_KEY.whereIn(keys);
        for (ParamValue value : getSession().createQuery(crn).addColumn(ParamValue.KEY_NAME$)) {
            result.put(ParamValue.KEY_NAME$.of(value), value);
        }
        return result;
    }

    /** Update the new text value of the parameter */
    @Override
    public void updateValue(ParamValue param) {
        final Session session = getSession();
        final ParamValue dbParam = new ParamValue();
        dbParam.setId(param.getId());
        dbParam.writeSession(session);
        dbParam.setTextValue(param.getTextValue());
        session.update(param);
    }

}
