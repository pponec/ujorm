/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */


package org.ujoframework.gxt.client;

import com.extjs.gxt.ui.client.data.BaseModelData;
import java.io.Serializable;

/**
 * Abstract Client Ujo
 * @author Pavel Ponec
 */
abstract public class AbstractCujo extends BaseModelData implements Cujo, Serializable {

    @Override
    abstract public CujoPropertyList readProperties();

    @Override
    @SuppressWarnings("unchecked")
    final public <UJO extends Cujo,VALUE> VALUE get(CujoProperty<UJO,VALUE> property) {
        Object result = this.get(property.getName());
        return result!=null ? (VALUE) result : property.getDefault();
    }

    @Override
    @SuppressWarnings("unchecked")
    final public <UJO extends Cujo,VALUE> void set(CujoProperty<UJO,VALUE> property, VALUE value) {
        this.set(property.getName(), value);
    }

    @SuppressWarnings("unchecked")
    protected static CujoPropertyList list(Class<? extends Cujo> type) {
        return new CujoPropertyList(type);
    }
    
    /** Returns ID value */
    public <T> T getId() {
        return (T) get("id");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
         return toStringRaw();
    }

    /** Final toString method */
    final public String toStringRaw() {
        StringBuilder result = new StringBuilder(128);
        int i=0;
        for (CujoProperty p : readProperties()) {
            if (i++>0) result.append(", ");
            result.append(p.getName());
            result.append("=");
            result.append(get(p));
        }
        return result.toString();
    }

    /** Create new instance */
    @Override
    public <T extends Cujo> T createInstance() {
        throw new UnsupportedOperationException("You must implement the method in a child " + getClass());
    }
}
