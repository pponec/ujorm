/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.ao;

import org.ujoframework.orm.OrmUjo;
import org.ujoframework.orm.metaModel.OrmPKey;

/**
 * UJO CacheKey
 * @author pavel
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


    /** Has the two objects the same PK values include BO type ? */
    @Override
    public boolean equals(Object obj) {
        final CacheKey cache = (CacheKey) obj;
        if (this.getType()!=cache.getType()) {
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
    public static CacheKey newInstance(OrmUjo ujo, OrmPKey pkey) {
        return new UjoCacheKey(ujo, pkey);
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
