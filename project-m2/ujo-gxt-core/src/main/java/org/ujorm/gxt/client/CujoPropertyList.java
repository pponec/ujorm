/*
 * Ujo4GXT - GXT module for the Ujorm
 * Copyright(c) 2010 Pavel Ponec
 * License: GNU/GPL v3 (see detail on http://www.gnu.org/licenses/gpl.html).
 *          If you need a commercial license, please contact support@ujorm.com.
 * Support: support@ujorm.com - for both technical or business information
 */

package org.ujorm.gxt.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Client Ujo Property List
 * @author Pavel Ponec
 */
public class CujoPropertyList implements Iterable<CujoProperty> {

    private Class<? extends Cujo> type;
    private List<CujoProperty> keys = new ArrayList<CujoProperty>();
    private boolean lock = false;

    public CujoPropertyList(Class<? extends Cujo> type) {
        this.type = type;
        CujoManager.add(type, this);
    }

    public CujoProperty[] getProperties() {
        if (!lock) {
            lock = true;
        }
        return keys.toArray(new CujoProperty[keys.size()]);
    }

    public Class<? extends Cujo> getType() {
        return type;
    }

    /** Find a property by the name. If property was not found than method throws an exception. */
    public CujoProperty findProperty(String name) {
        return findProperty(name, true);
    }

    /** Find a property by the name
     * @param name Property name
     * @param throwsException In case property found the TRUE value trhow an exeption or return a NULL value.
     */
    public CujoProperty findProperty(String name, boolean throwsException) {
        int hash = name.hashCode();
        for (CujoProperty p : keys) {
            if (p.getName().hashCode() == hash &&
                p.getName().equals(name)) {
                return p;
            }
        }
        if (throwsException) {
            throw new NoSuchElementException("No property " + name + " was found in the " + type);
        } else {
            return null;
        }
    }

    /**
     * Find direct or indirect property by property name from parameter.
     *
     * @param name A property name by sample "user.address.street".
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return .
     */
    public CujoProperty findIndirect(String name, boolean throwException) throws IllegalArgumentException {
        final CujoProperty result = CujoManager.findIndirectProperty(type, name, throwException);
        return result;
    }



    @Override
    public Iterator<CujoProperty> iterator() {
        return keys.iterator();
    }

    public CujoModel createColumnModel() {
        CujoModel result = new CujoModel(this);
        return result;

    }

    public <UJO extends Cujo, VALUE> CujoProperty<UJO, VALUE> newProperty(String name, Class<VALUE> type) {
        return newProperty(name, type, null);
    }

    /** A ListProperty Factory
     * Method assigns a next property index.
     */
    public <UJO extends Cujo, ITEM> CListUjoProperty<UJO,ITEM> newPropertyList(String name, Class<ITEM> type) {
        if (lock) {
            throw new UnsupportedOperationException("The " + getClass() + " is locked");
        }
        return CListProperty.newListProperty(name, type, keys.size());
    }


    public <UJO extends Cujo, VALUE extends CEnum> CProperty<UJO, VALUE> newPropertyEnum(String name, String enumKey) {
        if (lock) {
            throw new UnsupportedOperationException("The " + getClass() + " is locked");
        }
        CPropertyEnum<UJO, VALUE> result = new CPropertyEnum<UJO, VALUE>(name, CEnum.class, null, keys.size(), enumKey);
        keys.add(result);
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
        CujoProperty<UJO, VALUE> result = new CProperty<UJO, VALUE>(name, type, defaultValue, keys.size());
        keys.add(result);
        return result;
    }

    @Override
    public String toString() {
        return type.getName();
    }


}
