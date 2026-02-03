/*
 *  Copyright 2020-2026 Pavel Ponec
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

package org.ujorm.orm.ao;

/**
 * UJO CacheKey
 * @author Pavel Ponec
 */
final class OneCacheKey extends CacheKey {

    final private Object pk;
    final private Class type;

    public OneCacheKey(Class type, Object pk) {
        this.type = type;
        this.pk = pk;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Object getValue(int index) {
        return pk;
    }

    @Override
    public Class getType() {
        return type;
    }

}
