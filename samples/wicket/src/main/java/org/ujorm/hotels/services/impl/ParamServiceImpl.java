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

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.ParamValue;
import org.ujorm.hotels.entity.enums.Module;
import org.ujorm.hotels.services.*;
/**
 * Common database service implementations
 * @author ponec
 */
@Transactional
public class ParamServiceImpl<U extends KeyValue>
extends AbstractServiceImpl
implements ParamService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamServiceImpl.class);

    @Autowired
    private AuthService authService;

    /** Save all parameters into database */
    @Override
    public void init(ModuleParams moduleParams) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Get a value of the key */
    public <U extends ParamService, T> T getValue(Key<? super U, T> key, Module module) {
        final Criterion<ParamValue> crn1, crn2, crn3, crn4, crn5, crn6, crn7;
        crn1 = ParamValue.KEY_NAME$.whereEq(key.getName());
        crn2 = ParamValue.KEY_MODULE$.whereEq(module);
        crn3 = ParamValue.KEY_SYSTEM$.whereEq(true);
        crn4 = ParamValue.CUSTOMER.whereNull();
        crn5 = ParamValue.KEY_SYSTEM$.whereEq(false);
        crn6 = ParamValue.CUSTOMER.whereEq(authService.getLoggedCustomer());
        //
        final Criterion<ParamValue> crnA, crnB, crnC, crnD;
        crnA = crn1.and(crn2);

        return null;
    }


}
