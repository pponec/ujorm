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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.entity.enums.ModuleEnum;

/**
 * Service to manage parameters
 * @author Ponec
 */
public interface ParamService {
    String CACHED = "ParamServiceCached";
    String NATURAL = "ParamServiceNatural";

    /** Get a value of the key the */
    <U extends ModuleParams, T> T getValue(Key<? super U, T> key);

    /** Get a value of the key where the module have got special parameter for getter performance */
    <U extends ModuleParams, T> T getValue(Key<? super U, T> key, ModuleEnum module);

    /** Get a value of the key where the module have got special parameter for getter performance */
    <U extends ModuleParams, T> T getValue(Key<? super U, T> key, ModuleEnum module, Customer customer);

    /** Get all parameters for a required Customer and an additional Customer */
    List<? super ParamValue> getValues(@Nullable Customer customer, Criterion<ParamValue> criterion);

    /** Get all parameters for a logged Customer using an extended criterion */
    List<? super ParamValue> getValues(@NotNull Criterion<ParamValue> criterion);

    /** Get all parameters for a required Customer */
    List<? super ParamValue> getValues(@Nullable Customer customer);

    /** Get all parameters for a logged Customer */
    List<? super ParamValue> getValues();

    /** Save a modified text value of the parameter to database
     * @param param An persistent format of the parameter.
     * @param Logged user in case of the {@link @PersonalParam}
     */
    void updateValue(ParamValue param, Customer user);

    /** Save a modified parameter text value of a logged user
     * @param param Undefined customer save an default parameters
     */
    void updateValue(ParamValue param);

    /** Clear all the cached parameters */
    void clearCache();

    /** Save all parameters to database */
    void init(ModuleParams<?> params);

}
