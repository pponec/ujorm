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
package org.ujorm.hotels.service.impl;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujorm.Key;
import org.ujorm.hotels.service.ModuleParams;
import org.ujorm.hotels.service.ParamService;
import org.ujorm.implementation.quick.SmartUjoLockable;
import org.ujorm.tools.MsgFormatter;

/**
 * Common database service implementations.
 * If the object is not a Spring managed bean,
 * then it is possibile to use it like any other regular UJO object.
 * @author ponec
 */
public abstract class AbstractModuleParams<U extends AbstractModuleParams>
        extends SmartUjoLockable<U>
        implements ModuleParams<U> {
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractModuleParams.class);

    @Inject
    @Named(ParamService.CACHED)
    private ParamService paramService;

    /** If the instance is not a Spring managed bean,
     * then the object is a regular UJO object (independent on a Spring services) */
    @Override
    public Object readValue(final Key<?,?> key) {
        return paramService != null
             ? paramService.getValue((Key)key, getModule())
             : super.readValue(key);
    }

    /** Load default values into database */
    @PostConstruct
    public void init() throws IllegalStateException {
        if (paramService != null) try {
            paramService.init((ModuleParams) this);
            lock();
            LOGGER.info("The parameter module '{}' is initialized", getClass().getName());
        } catch (Exception e) {
            final String msg = MsgFormatter.format("The parameter module '{}' loading failed", getClass().getName());
            LOGGER.info(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

}
