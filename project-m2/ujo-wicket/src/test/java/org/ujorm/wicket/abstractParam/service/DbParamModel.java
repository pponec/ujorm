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

package org.ujorm.wicket.abstractParam.service;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.orm.OrmUjo;

/**
 * Param Value
 * @author Pavel Ponec
 */
public class DbParamModel <V extends OrmUjo, K extends OrmUjo, C extends OrmUjo> {

    final public Key<V, Object> ID = null;
    final public Key<V, K> PARAM_KEY = null;
    final public Key<V, C> CUSTOMER = null;
    final public Key<V, String> TEXT_VALUE = null;
    final public Key<V, Date> LAST_UPDATE = null;
    //
    final public Key<V, Object> KEY_ID$ = null;
    final public Key<V, String> KEY_NAME$ = null;
    final public Key<V, IModule> KEY_MODULE$ = null;
    final public Key<V, Boolean> KEY_SYSTEM$ = null;
    final public Key<V, Object> CUSTOMER_ID$ = null;
    //
    public final Key<K, Boolean> ParamKey_SYSTEM_PARAM = null;
    public final Key<K, Date> ParamKey_LAST_UPDATE = null;
    public final Key<K, Object> ParamKey_ID = null;
    public final Key<K, IModule> ParamKey_MODULE = null;
    public final Key<K, String> ParamKey_CLASS_NAME = null;
    public final Key<K, String> ParamKey_CLASS_PACKAGE = null;
    public final Key<K, String> ParamKey_NAME = null;
    public final Key<K, String> ParamKey_NOTE = null;
    public final Key<K, String> ParamKey_TEXT_DEFAULT_VALUE = null;
    //
    final public Key<C, String> CUSTOMER_LOGIN = null;
    final public Key<C, Object> CUSTOMER_ID = null;
    //
    final private Class<V> typeValue;
    final private Class<K> typeKey;
    final private Class<C> typeCustomer;

    public DbParamModel(Class<V> typeValue, Class<K> typeKey, Class<C> typeCustomer) {
        this.typeValue = typeValue;
        this.typeKey = typeKey;
        this.typeCustomer = typeCustomer;
    }

    public Class<V> getTypeValue() {
        return typeValue;
    }

    public Class<K> getTypeKey() {
        return typeKey;
    }

    public Class<C> getTypeCustomer() {
        return typeCustomer;
    }

    public V createValueInstance() {
        try {
            return getTypeValue().newInstance();
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalStateException("Instance failed for: " + getTypeValue(), e);
        }
    }

    public K createKeyInstance(String paramName, IModule module) {
        try {
            K result = getTypeKey().newInstance();
            ParamKey_NAME.setValue(result, paramName);
            ParamKey_MODULE.setValue(result, module);
            return result;
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalStateException("Instance failed for: " + getTypeKey(), e);
        }
    }

    public KeyList readKeys() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }





}
