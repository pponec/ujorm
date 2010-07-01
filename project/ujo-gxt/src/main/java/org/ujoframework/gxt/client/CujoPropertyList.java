/*
 * Ujo4GXT - GXT module for the UJO Framework
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujoframework.gxt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Client Ujo Property List
 * @author PropertyList
 */
public class CujoPropertyList implements Iterable<CujoProperty> {

    private Class<? extends Cujo> type;
    private List<CujoProperty> properties = new ArrayList<CujoProperty>();
    private boolean lock = false;

    public CujoPropertyList(Class<? extends Cujo> type) {
        this.type = type;
        CujoManager.add(type, this);
    }

    public CujoProperty[] getProperties() {
        if (!lock) {
            lock = true;
        }
        return properties.toArray(new CujoProperty[properties.size()]);
    }

    public Class<? extends Cujo> getType() {
        return type;
    }

    public CujoProperty findProperty(String name) {
        int hash = name.hashCode();
        for (CujoProperty p : properties) {
            if (p.getName().hashCode() == hash &&
                p.getName().equals(name)) {
                return p;
            }
        }
        throw new NoSuchElementException("No property " + name + " was found in the " + type);
    }

    @Override
    public Iterator<CujoProperty> iterator() {
        return properties.iterator();
    }

    public CujoModel createColumnModel() {
        CujoModel result = new CujoModel(this);
        return result;

    }

    public <UJO extends Cujo, VALUE> CujoProperty<UJO, VALUE> newProperty(String name, Class<VALUE> type) {
        return newProperty(name, type, null);
    }

    public <UJO extends Cujo, VALUE extends CEnum> CProperty<UJO, VALUE> newPropertyEnum(String name, String enumKey) {
        if (lock) {
            throw new UnsupportedOperationException("The " + getClass() + " is locked");
        }
        CPropertyEnum<UJO, VALUE> result = new CPropertyEnum<UJO, VALUE>(name, CEnum.class, null, properties.size(), enumKey);
        properties.add(result);
        return result;
    }


    @SuppressWarnings("unchecked")
    public <UJO extends Cujo, VALUE> CujoProperty<UJO, VALUE> newPropertyDef(String name, VALUE defaultValue) {
        return newProperty(name, (Class) defaultValue.getClass(), defaultValue);
    }

    private <UJO extends Cujo, VALUE> CujoProperty<UJO, VALUE> newProperty(String name, Class<VALUE> type, VALUE defaultValue) {
        if (lock) {
            throw new UnsupportedOperationException("The " + getClass() + " is locked");
        }
        CujoProperty<UJO, VALUE> result = new CProperty<UJO, VALUE>(name, type, defaultValue, properties.size());
        properties.add(result);
        return result;
    }

    @Override
    public String toString() {
        return type.getName();
    }


}
