/*
 * Copyright 2014-2015, Pavel Ponec
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.wicket.util.lang.Args;
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
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.ModuleParams;
import org.ujorm.hotels.service.ParamService;
import org.ujorm.hotels.service.param.annot.PersonalParam;
import org.ujorm.orm.Session;
import org.ujorm.orm.annot.Comment;
import static org.ujorm.hotels.entity.ParamValue.*;

/**
 * Common database service implementations
 * @author Pavel Ponec
 */
@Transactional
@Service(ParamService.NATURAL)
public class ParamServiceImpl
extends AbstractServiceImpl<ParamValue>
implements ParamService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamServiceImpl.class);

    @Autowired
    private AuthService authService;

    /** Get a value of the key */
    @Override
    public final <U extends ModuleParams, T> T getValue(Key<? super U, T> key) {
        try {
            @SuppressWarnings("unchecked")
            final U instance = (U) key.getDomainType().newInstance();
            return getValue(key, instance.getModule());
        } catch (/*ReflectiveOperationException*/ Exception e) {
            throw new IllegalStateException("Can't get a value for the key: " + key.getFullName(), e);
        }
    }

    /** Get a value of the key for the logged user */
    @Override
    public final <U extends ModuleParams, T> T getValue(Key<? super U, T> key, Module module) {
        return getValue(key, module, authService.getLoggedCustomer());
    }

    /** Get a value of the key for the logged user */
    @Override
    public <U extends ModuleParams, T> T getValue(Key<? super U, T> key, Module module, Customer customer) {
        final Criterion<ParamValue> crn1, crn2, crn3, crn4, crn5;
        crn1 = ParamValue.KEY_NAME$.whereEq(key.getName());
        crn2 = ParamValue.KEY_MODULE$.whereEq(module);
        crn3 = ParamValue.CUSTOMER.whereNull();
        crn4 = ParamValue.CUSTOMER.whereEq(customer);
        crn5 = crn1.and(crn2).and(crn3.or(crn4));
        //
        ParamValue param = null;
        for (ParamValue paramValue : getSession().createQuery(crn5).orderBy(ParamValue.ID)) {
            param = paramValue;
        }

        return param != null
             ? UjoManager.getInstance().decodeValue(key,  param.getTextValue())
             : key.getDefault();
    }

    /** Get all parameters for a logged Customer
     * @todo add next argument removeObsolete type of Boolean to exclude obsolete parameter keys
     */
    @Override
    public final List<ParamValue> getValues() {
        return getValues(authService.getLoggedCustomer());
    }

    /** Get all parameters for a required Customer
     * @todo add next argument removeObsolete type of Boolean to exclude obsolete parameter keys
     */
    @Override
    public List<ParamValue> getValues(@Nullable Customer customer) {
        final Criterion<ParamValue> criterion
                = authService.isAdmin()
                ? ParamValue.ID.forAll()
                : ParamValue.KEY_SYSTEM$.whereEq(false);
        return getValues(customer, criterion);
    }

    /** Get all parameters for a logged Customer */
    @Override
    public List<ParamValue> getValues(@Nonnull Criterion<ParamValue> criterion) {
        return getValues(authService.getLoggedCustomer(), criterion);
    }

    /** Get all parameters for a required Customer.
     * NOTE: The SQL standard's core functionality does not explicitly define a default sort order for Nulls
     * so the ordering by the {@code ParamValue.ID} is necessary in this implementation
     * @todo add next argument removeObsolete type of Boolean to exclude obsolete parameter keys
     */
    @Override
    public List<ParamValue> getValues(@Nullable Customer customer, @Nonnull Criterion<ParamValue> criterion) {
        Args.notNull(criterion, "criterion");

        final Criterion<ParamValue> crn1,crn2,crn3;
        crn1 = ParamValue.CUSTOMER.whereNull();
        crn2 = ParamValue.CUSTOMER.whereEq(customer);
        crn3 = criterion.and(crn1.or(crn2));

        final Key<ParamValue,Integer> KEY_ID = ParamValue.KEY_ID$;
        final Map<Integer,ParamValue> values = createQuery(crn3)
                .orderBy(ParamValue.ID) // Default is the first!
                .addColumn(KEY_ID)
                .map(KEY_ID, new HashMap<Integer,ParamValue>(128));
        final Map<Integer,ParamKey> keys = getSession().createQuery(ParamKey.ID.forAll()).map();
        for (ParamValue value : values.values()) {
            value.setParamKey(keys.get(KEY_ID.of(value)));
        }
        return new ArrayList<ParamValue>(values.values());
    }

    /** Save a modified parameter text value of a logged user
     * @param param Undefined customer save an default parameters
     */
    @Override
    public final void updateValue(ParamValue param) {
        param.writeSession(getSession());
        if (param.getCustomer()!=null && param.isPersonalParam()) {
            Args.isTrue(param.getCustomer().getId().equals(authService.getLoggedCustomer().getId())
            , "User " + authService.getLoggedCustomer().getId() + " is modyfing foreign parameters " + param);
        }
        updateValue(param, this.authService.getLoggedCustomer());
    }

    /** Save a modified text value of the parameter to database */
    /**
     *
     * @param param
     * @param user
     */
    @Override
    public void updateValue(ParamValue param, Customer user) {
        final Session session = getSession();
        final ParamValue dbParam;

        if (param.getCustomer()==null && param.isPersonalParam()) {
            // Make INSERT:
            dbParam = param;
            dbParam.writeSession(null);
            dbParam.setId(null);
            dbParam.setCustomer(user);
        }
        else {
            // Make UPDATE:
            dbParam = new ParamValue();
            dbParam.setId(param.getId());
            dbParam.writeSession(session);
        }

        dbParam.setTextValue(param.getTextValue());
        dbParam.setLastUpdate(LocalDateTime.now());
        session.saveOrUpdate(dbParam);

        // Log the value change:
        final String msg = String.format
            ( "User '%s' [%s] changed the %s parameter '%s.%s' to a new value: '%s'."
            , user.getLogin()
            , user.getId()
            , param.get(KEY_SYSTEM$) ? "system" : "private"
            , param.get(KEY_MODULE$).name()
            , param.get(KEY_NAME$)
            , param.getTextValue());
        LOGGER.info(msg);
    }

    /** The method makes nothing */
    @Override
    public void clearCache() {
    }

    /** Save all parameters into database */
    @Override
    public void init(ModuleParams<?> params) {
        final Map<String, ParamKey> paramKeyMap = getParamKeyMap(params);

        // --- KEYS ----

        final UjoCoder converter = UjoManager.getInstance().getCoder();
        final LocalDateTime now = LocalDateTime.now();
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

}
