/*
 * Copyright 2013-2014, Pavel Ponec
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.hotels.services.ModuleParams;
import org.ujorm.hotels.services.ParamService;
import org.ujorm.implementation.quick.SmartUjo;
/**
 * Common database service implementations
 * @author ponec
 */
@Transactional
public abstract class ModuleParamsImpl<U extends ModuleParamsImpl>
extends SmartUjo<U>
implements ModuleParams<U>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamServiceImpl.class);

    @Autowired
    private ParamService paramService;

    /** Load default values into database */
    public void init() {
        paramService.init((ModuleParams)this);
    }

}