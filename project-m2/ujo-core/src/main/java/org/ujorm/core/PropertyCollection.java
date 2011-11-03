/*
 *  Copyright 2011-2011 Pavel Ponec
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ujorm.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;

/**
 * The Immutable and Serializable UjoProperty collection.
 * @author Pavel Ponec
 */
public class PropertyCollection<UJO extends Ujo> implements Iterable<UjoProperty<UJO,?>>, Serializable {

    static final long serialVersionUID = 1L;

    private final Class<UJO> baseClass;
    private final String[] tProperties;
    transient private List<UjoProperty<UJO,?>> properties;

    public PropertyCollection(Class<UJO> baseClass, UjoProperty<UJO, ?> ... properties) {
        this.baseClass = baseClass;
        this.properties = Arrays.asList(properties);

        tProperties = new String[properties.length];
        for (int i = 0; i < properties.length; i++) {
            tProperties[i] = properties[i].getName();
        }
    }

    /** Get Class */
    public Class<UJO> getBaseClass() {
        return baseClass;
    }

    /** Create Properties */
    private List<UjoProperty<UJO,?>> getProperties() {
        final UjoPropertyList propertyList = UjoManager.getInstance().readProperties(baseClass);

        if (properties==null) {
            List<UjoProperty<UJO,?>> ps = new ArrayList<UjoProperty<UJO,?>>(tProperties.length);
            for (int i = 0; i < tProperties.length; i++) {
                ps.add(propertyList.findIndirect(tProperties[i], true));
            }
            properties = ps;
        }
        return properties;
    }

    /** Get The First Properties */
    public UjoProperty<UJO,?> getFirstProperty() {
        return get(0);
    }

    /** Get The Last Properties */
    public UjoProperty<UJO,?> getLastProperty() {
        return get(tProperties.length-1);
    }

    /** Get First Properties */
    public UjoProperty<UJO,?> get(int i) {
        return getProperties().get(i);
    }


    /** Size */
    public int size() {
        return tProperties.length;
    }

    public boolean contains(Object o) {
        return getProperties().contains(o);
    }

    public Iterator<UjoProperty<UJO, ?>> iterator() {
        return getProperties().iterator();
    }

    public UjoProperty[] toArray() {
        return getProperties().toArray(new  UjoProperty[tProperties.length]);
    }

    /** Create new Instance */
    public static <T extends Ujo> PropertyCollection<T> newInstance(Class<T> baseClass, UjoProperty<T, ?> ... properties) {
        return new PropertyCollection<T>(baseClass, properties);
    }

    /** Returns the property names */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(32);
        for (String p : tProperties) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(p);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertyCollection) {
            PropertyCollection<UJO> o = (PropertyCollection<UJO>) obj;
            if (this.size()!=o.size()) {
                return false;
            }
            if (!this.getBaseClass().equals(o.getBaseClass())) {
                return false;
            }
            for (int i = size()-1; i>=0; --i) {
                UjoProperty p1 = this.get(i);
                UjoProperty p2 = o.get(i);
                if (!p1.equals(p2)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.baseClass != null ? this.baseClass.hashCode() : 0);
        hash = 83 * hash + Arrays.deepHashCode(this.tProperties);
        return hash;
    }

}
