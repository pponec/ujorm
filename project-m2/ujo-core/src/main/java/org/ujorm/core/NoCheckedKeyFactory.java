/*
 *  Copyright 2013-2022 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.core;

import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.extensions.NoCheckedProperty;

/** The factory for creating Key where a validator is off
 * @author Pavel Ponec
 */
public class NoCheckedKeyFactory<UJO extends Ujo> extends KeyFactory<UJO> {

    public NoCheckedKeyFactory(Class<? extends UJO> type, boolean propertyCamelCase, KeyList<?> abstractSuperProperties) {
        super(type, propertyCamelCase, abstractSuperProperties);
    }

    public NoCheckedKeyFactory(Class<? extends UJO> type, boolean propertyCamelCase) {
        super(type, propertyCamelCase);
    }

    public NoCheckedKeyFactory(Class<? extends UJO> type) {
        super(type);
    }

    /** Create a special Key without a Validator checking */
    @Override
    protected <T> Key<UJO,T> createKey(String name, T defaultValue, Validator<T> validator) {
        final NoCheckedProperty<UJO,T> p = new NoCheckedProperty<>(name, defaultValue, validator);
        addKey(p);
        return p;
    }



}
