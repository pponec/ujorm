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

package org.ujorm.extensions;

import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;

/**
 * Spring Key Factory
 * @author Pavel Ponec
 */
public class WeakKeyFactory extends KeyFactory<Ujo> {

    public WeakKeyFactory(Class<? extends Ujo> type) {
        super(type);
    }

    /** Create new Key */
    @Override
    public final <T> WeakKey<T> newKey() {
        return createKey(null, null);
    }

    /** Create new Key */
    @Override
    public final <T> WeakKey<T> newKey(String name) {
        return createKey(name, null);
    }

    /** Create new Key */
    @Override
    protected <T> WeakKey<T> createKey(String name, T defaultValue) {
        final WeakKeyImpl<T> p = new WeakKeyImpl<T>(name);
        addKey(p);
        return p;
    }

    
}
