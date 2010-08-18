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
    public <UJO extends Cujo,VALUE> VALUE get(CujoProperty<UJO,VALUE> property) {
        Object result = super.get(property.getName());
        return result!=null ? (VALUE) result : property.getDefault();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <UJO extends Cujo,VALUE> void set(CujoProperty<UJO,VALUE> property, VALUE value) {
        super.set(property.getName(), value);
    }

    @SuppressWarnings("unchecked")
    protected static CujoPropertyList list(Class<? extends Cujo> type) {
        return new CujoPropertyList(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
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

}
