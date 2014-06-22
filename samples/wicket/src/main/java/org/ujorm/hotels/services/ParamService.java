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
package org.ujorm.hotels.services;

import org.ujorm.Key;
import org.ujorm.hotels.entity.enums.Module;
import org.ujorm.hotels.services.*;

/**
 * Service to manage parameters
 * @author ponec
 */
public interface ParamService {

    /** Get a value of the key the */
    public <U extends ModuleParams, T> T getValue(Key<? super U, T> key);

    /** Get a value of the key where the momdule have got special parameter for getter performance */
    public <U extends ModuleParams, T> T getValue(Key<? super U, T> key, Module module);

    /** Save all parameters into database */
    public void init(ModuleParams<?> params);

}
