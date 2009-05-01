/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.ao;

/**
 * UJO CacheKey
 * @author pavel
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
