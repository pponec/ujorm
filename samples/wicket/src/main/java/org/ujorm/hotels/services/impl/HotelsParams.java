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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.hotels.entity.enums.Module;
import org.ujorm.hotels.services.annot.PersonalParam;
/**
 * Common database service implementations
 * @author ponec
 */
@Transactional
public class HotelsParams<U extends HotelsParams> extends AbstractModuleParamsImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(HotelsParams.class);

    /** Factory */
    private static final KeyFactory<HotelsParams> f = newFactory(HotelsParams.class);

    public static final Key<HotelsParams, String> TEST1 = f.newKey("Test1", "A");
    public static final Key<HotelsParams, String> TEST2 = f.newKey("Test2", "B");
    @PersonalParam
    public static final Key<HotelsParams, String> TEST3 = f.newKey("Test3", "C");

    static { f.lock(); }


    @Override
    public Module getModule() {
        return Module.HOTELS;
    }


}
