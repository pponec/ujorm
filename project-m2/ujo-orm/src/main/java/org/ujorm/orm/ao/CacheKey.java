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

import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.metaModel.MetaPKey;

/**
 * UJO CacheKey
 * @author Pavel Ponec
 * @see org.ujorm.orm.metaModel.MetaParams
 */
abstract public class CacheKey {

    /** Hash Code */
    private int hash = 0;

    /** Count of PKs */
    abstract public int size();

    /** Returns a PK on the selected index. PK must not be null. */
    abstract public Object getValue(int index);

    /** OrmUjo class */
    abstract public Class getType();


    /** Has the two objects the same PK values ? */
    @Override
    public boolean equals(Object obj) {
        final CacheKey cache = (CacheKey) obj;
        if (cache==null || this.getType()!=cache.getType()) {
            return false;
        }
        for (int i=size()-1; i>=0; --i) {
            final Object v1 = this.getValue(i);
            final Object v2 = cache.getValue(i);
            if (!v1.equals(v2)) {
                return false;
            }
        }
        return true;
    }

    /** Returns hash code */
    @Override
    public int hashCode() {
        if (hash==0) {
            int h = 7 + getType().hashCode();
            for (int i=size()-1; i>=0; --i) {
                h = 67 * h + getValue(i).hashCode();
            }
            hash = h!=0 ? h : 1 ; // no zero result
        }
        return hash;
    }

    // --------------- FACTORY -----------------------

    /** Constructor for the OrmUjo */
    public static CacheKey newInstance(OrmUjo bo, MetaPKey pkey) {
        return new UjoCacheKey(bo, pkey);
    }

    /** Constructor for one keyk */
    public static CacheKey newInstance(Class type, Object pk) {
        return new OneCacheKey(type, pk);
    }

    /** Constructor for many keys */
    public static CacheKey newInstance(Class type, Object... pks) {
        return new ManyCacheKey(type, pks);
    }

}
