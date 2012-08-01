/*
 *  Copyright 2012-2012 Pavel Ponec
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

package org.ujorm.spring;

import org.ujorm.core.KeyFactory;

/**
 * Spring Key Factory
 * @author Pavel Ponec
 */
public class SpringKeyFactory extends KeyFactory<AbstractAplicationContextAdapter> {

    public SpringKeyFactory(Class<? extends AbstractAplicationContextAdapter> type) {
        super(type);
    }

    /** Create new Key */
    @Override
    public final <T> SpringKey<T> newKey() {
        return createKey(null, null);
    }

    /** Create new Key */
    @Override
    public final <T> SpringKey<T> newKey(String name) {
        return createKey(name, null);
    }

    /** Create new Key */
    @Override
    protected <T> SpringKey<T> createKey(String name, T defaultValue) {
        final SpringKeyImpl<T> p = new SpringKeyImpl<T>(name);
        addKey(p);
        return p;
    }

    
}
