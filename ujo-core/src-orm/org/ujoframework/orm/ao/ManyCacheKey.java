/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.ao;


import org.ujoframework.implementation.orm.TableUjo;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmTable;

/**
 * UJO CacheKey
 * @author pavel
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
