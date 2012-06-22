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

import org.ujorm.core.PropertyFactory;

/**
 * Spring Key Factory
 * @author Pavel Ponec
 */
public class SpringKeyFactory extends PropertyFactory<AbstractAplicationContextAdapter> {

    public SpringKeyFactory(Class<? extends AbstractAplicationContextAdapter> type) {
        super(type);
    }

    /** Create new UjoProperty */
    @Override
    public final <T> SpringKey<T> newProperty() {
        return createProperty(null, null);
    }

    /** Create new UjoProperty */
    @Override
    public final <T> SpringKey<T> newProperty(String name) {
        return createProperty(name, null);
    }

    /** Create new UjoProperty */
    @Override
    protected <T> SpringKey<T> createProperty(String name, T defaultValue) {
        final SpringKey<T> p = new SpringKeyImpl<T>(name);
        addProperty(p);
        return p;
    }

    
}
