/*
 *  Copyright 2020-2022 Pavel Ponec
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
 * @see org.ujorm.orm.metaModel.MetaParams
 */
final class ManyCacheKey extends CacheKey {

    /** Values of the primary key */
    final private Object[] pkv;
    final private Class type;

    /**
     * Constructor
     * @param type type of UJO
     * @param pkv Values of the primary key
     */
    public ManyCacheKey(Class type, Object... pkv) {
        this.type = type;
        this.pkv = pkv;
    }

    @Override
    public int size() {
        return pkv.length;
    }

    @Override
    public Object getValue(int index) {
        return pkv[index];
    }

    @Override
    public Class getType() {
        return type;
    }

}
