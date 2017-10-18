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
package org.ujorm.hotels.service;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.entity.enums.Module;

/**
 * Service to manage parameters
 * @author Ponec
 */
public interface ParamService {
    public static final String CACHED = "ParamServiceCached";
    public static final String NATURAL = "ParamServiceNatural";

    /** Get a value of the key the */
    public <U extends ModuleParams, T> T getValue(Key<? super U, T> key);

    /** Get a value of the key where the module have got special parameter for getter performance */
    public <U extends ModuleParams, T> T getValue(Key<? super U, T> key, Module module);

    /** Get a value of the key where the module have got special parameter for getter performance */
    public <U extends ModuleParams, T> T getValue(Key<? super U, T> key, Module module, Customer customer);

    /** Get all parameters for a required Customer and an additional Customer */
    public List<? super ParamValue> getValues(@Nullable Customer customer, Criterion<ParamValue> criterion);

    /** Get all parameters for a logged Customer using an extended criterion */
    public List<? super ParamValue> getValues(@Nonnull Criterion<ParamValue> criterion);

    /** Get all parameters for a required Customer */
    public List<? super ParamValue> getValues(@Nullable Customer customer);

    /** Get all parameters for a logged Customer */
    public List<? super ParamValue> getValues();

    /** Save a modified text value of the parameter to database
     * @param param An persistent format of the parameter.
     * @param Logged user in case of the {@link @PersonalParam}
     */
    public void updateValue(ParamValue param, Customer user);

    /** Save a modified parameter text value of a logged user
     * @param param Undefined customer save an default parameters
     */
    public void updateValue(ParamValue param);

    /** Clear all the cached parameters */
    public void clearCache();

    /** Save all parameters to database */
    public void init(ModuleParams<?> params);

}
