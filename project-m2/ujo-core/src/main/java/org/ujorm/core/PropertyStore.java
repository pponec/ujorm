/*
 *  Copyright 2011-2012 Pavel Ponec
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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.annot.Immutable;
import org.ujorm.extensions.PathProperty;

/**
 * The Immutable and Serializable UjoProperty Collection include some service methods.
 * Object have got implemented the hashCode() and equals() methods.
 * @author Pavel Ponec
 */
@Immutable
public class PropertyStore<UJO extends Ujo> implements UjoPropertyList<UJO>, Serializable {

    static final long serialVersionUID = 1L;
    /** Property Separator */
    protected static final char PROPERTY_SEPARATOR = '.';
    /** A text to mark a descending sort of a property in a deserializaton proccess. */
    protected static final String DESCENDING_SYMBOL = String.valueOf(PROPERTY_SEPARATOR) + PROPERTY_SEPARATOR;
    /** The Ujo type is serializad */
    private Class<UJO> type;
    /** Property size */
    private int size;
    /** Transient properties */
    private UjoProperty<UJO, ?>[] properties;
    /** Default hash code. */
    transient private int hashCode;

    /**
     * Constructor
     * @param baseClass Not null base class for all properties
     * @param properties Property array
     * @see #of(java.lang.Class, org.ujorm.UjoProperty<T,?>[])
     */
    public PropertyStore(Class<UJO> baseClass, UjoProperty<UJO, ?>... properties) {
        if (baseClass == null) {
            throw new IllegalArgumentException("The baseClass must be defined");
        }
        this.type = baseClass;
        this.properties = properties;
        this.size = properties.length;
    }

    /** Get The Base Class */
    public Class<UJO> getType() {
        return type;
    }

    /**
     * Find a direct property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return .
     */
    @Override
    public UjoProperty<UJO, ?> findDirectProperty(final String name, final boolean throwException) throws IllegalArgumentException {
        int nameHash = name.hashCode();

        for (UjoProperty prop : properties) {
            if (prop.getName().hashCode() == nameHash // speed up
                    && prop.getName().equals(name)) {
                return prop;
            }
        }

        if (throwException) {
            throw new IllegalArgumentException("A property called \"" + name + "\" was not found in the " + type);
        } else {
            return null;
        }
    }

    @Override
    final public UjoProperty<UJO, ?> findDirectProperty(final Ujo ujo, final String name, final boolean throwException) throws IllegalArgumentException {
        return findDirectProperty(ujo, name, UjoAction.DUMMY, true, throwException);
    }

    /**
     * Find a direct property by property name from parameter.
     * @param ujo An Ujo object
     * @param name A property name.
     * @param action Action type UjoAction.ACTION_* .
     * @param result Required result of action.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     */
    @SuppressWarnings("deprecation")
    @Override
    public UjoProperty<UJO, ?> findDirectProperty(final Ujo ujo, final String name, final UjoAction action, final boolean result, final boolean throwException) throws IllegalArgumentException {
        if (ujo == null) {
            return null;
        }
        int nameHash = name.hashCode();

        for (final UjoProperty prop : properties) {
            if (prop.getName().hashCode() == nameHash // speed up
                    && prop.getName().equals(name)
                    && (action.getType() == UjoAction.ACTION_XML_ELEMENT
                    ? !getUjoManager().isXmlAttribute(prop)
                    : ujo.readAuthorization(action, prop, null)) == result) {
                return prop;
            }
        }

        if (throwException) {
            throw new IllegalArgumentException("A property name \"" + name + "\" was not found in the " + ujo.getClass());
        } else {
            return null;
        }
    }

    /**
     * Find direct or indirect property by property name from parameter.
     *
     * @param name A property name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @deprecated Uset the method {@link #find(java.lang.String, boolean)}
     */
    public UjoProperty<UJO, ?> findIndirect(String names, boolean throwException) throws IllegalArgumentException {
        return find(names, throwException);
    }

    /**
     * Find <strong>indirect</strong> property by the name. Empty result can trhow NULL value if parameter throwException==false.
     * @param names Not null property name inclukde composite properties (indirect properties).
     * @param throwException
     * @return new UjoProperty
     */
    @SuppressWarnings("unchecked")
    @Override
    public UjoProperty find(String names, boolean throwException) {
        if (names.indexOf(PropertyStore.PROPERTY_SEPARATOR) < 0) {
            return findDirectProperty(names, throwException);
        }

        Class ujoType = type;
        int j, i = 0;
        List<UjoProperty> props = new ArrayList<UjoProperty>(8);
        names += ".";
        while ((j = names.indexOf('.', i + 1)) >= 0) {
            final String name = names.substring(i, j);
            final UjoProperty p = UjoManager.getInstance().readProperties(ujoType).findDirectProperty(name, true);
            props.add(p);
            ujoType = p.getType();
            i = j + 1;
        }
        switch (props.size()) {
            case 0:
                if (throwException) {
                    throw new IllegalStateException("Invalid property name: " + names);
                } else {
                    return null;
                }
            case 1:
                return props.get(0);
            default:
                return new PathProperty(props);
        }
    }

    /**
     * Find (both direct or indirect) property by property name from parameter.
     * @param name A property name by sample "user.address.street".
     * @return .
     */
    @Override
    public UjoProperty<UJO, ?> find(String name) throws IllegalArgumentException {
        return find(name, true);
    }

    /** Get The First Properties */
    @Override
    public UjoProperty<UJO, ?> getFirstProperty() {
        return get(0);
    }

    /** Get The Last Properties */
    @Override
    public UjoProperty<UJO, ?> getLastProperty() {
        return get(size - 1);
    }

    /** Get The Last Properties 
     * @deprecated Use the method {@link #getLastProperty()} rather.
     */
    @Override
    final public UjoProperty<UJO, ?> last() {
        return getLastProperty();
    }

    /** Get one Property */
    @Override
    public UjoProperty<UJO, ?> get(int i) {
        return properties[i];
    }

    /** Returns or create UjoManager.
     * In your own implementation keep in a mind a simple serialization freature of the current object.
     */
    protected UjoManager getUjoManager() {
        return UjoManager.getInstance();
    }

    /** Size */
    @Override
    public int size() {
        return size;
    }

    /** Is the collection empty? */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** Test collection if it contains a property parameter  */
    @SuppressWarnings("element-type-mismatch")
    public boolean contains(Object property) {
        return Arrays.asList(properties).contains(property);
    }

    /** Create Property Interator */
    @Override
    public Iterator<UjoProperty<UJO, ?>> iterator() {
        return new Iterator<UjoProperty<UJO, ?>>() {

            int i = -1;

            public boolean hasNext() {
                return (i + 1) < size;
            }

            public UjoProperty<UJO, ?> next() {
                return get(++i);
            }

            /**
             * The method is not supported.
             */
            @Deprecated
            public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException("The REMOVE operation is not supported.");
            }
        };
    }

    /** Convert Properties to a new Array */
    @Override
    public UjoProperty<UJO, ?>[] toArray() {
        final UjoProperty<UJO, ?>[] result = new UjoProperty[size];
        System.arraycopy(this.properties, 0, result, 0, result.length);
        return result;
    }

    /** Create new Instance of the Base Ujo object */
    @Override
    public UJO newBaseUjo() throws IllegalStateException {
        try {
            @SuppressWarnings("unchecked")
            UJO result = (UJO) type.newInstance();
            return result;

        } catch (Exception e) {
            throw new IllegalStateException("Can't create instance for " + type, e);
        }
    }

    /** Returns a class name of the related UJO */
    @Override
    public String getTypeName() {
        return type.getName();
    }

    /** Returns true if list contains property from the parameter. */
    @Override
    public boolean contains(UjoProperty<UJO, ?> o) {
        for (UjoProperty p : properties) {
            if (p.equals(o)) {
                return true;
            }
        }
        return false;
    }

    /** Returns the property names */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(32);
        for (UjoProperty p : properties) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(p);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof PropertyStore) {
            final PropertyStore<UJO> o = (PropertyStore<UJO>) obj;
            if (this.size() != o.size()) {
                return false;
            }
            if (!this.getType().equals(o.getType())) {
                return false;
            }
            for (int i = 0; i < size; ++i) {
                final UjoProperty p1 = this.get(i);
                final UjoProperty p2 = o.get(i);
                if (!p1.equals(p2)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /** Calculate hashCode */
    @Override
    public int hashCode() {
        if (hashCode==0) {
            int hash = 7;
            hash = 83 * hash + this.type.hashCode();
            hash = 83 * hash + Arrays.deepHashCode(createPropertyNames());
            hashCode = hash;
        }
        return hashCode;
    }

    // -------------- SERIALIZATION METHOD(S) --------------

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(this.type);
        out.writeObject(createPropertyNames());
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.type = (Class) in.readObject();
        final String[] nameProperties = (String[]) in.readObject();
        this.properties = restoreProperties(type, nameProperties);
        this.size = properties.length;
    }

    /** Serializable property names */
    private String[] createPropertyNames() {
        final String[] nameProperties = new String[properties.length];
        for (int i = properties.length - 1; i >= 0; --i) {
            final UjoProperty property = properties[i];
            nameProperties[i] = properties[i].isAscending() ? property.getName() : (property.getName() + DESCENDING_SYMBOL);
        }
        return nameProperties;
    }

    /** Create Properties */
    private UjoProperty<UJO, ?>[] restoreProperties(Class type, String[] nameProperties) {
        final UjoPropertyList propertyList = getUjoManager().readProperties(type);
        final UjoProperty<UJO, ?>[] ps = new UjoProperty[nameProperties.length];
        for (int i = 0; i < nameProperties.length; i++) {
            final String pNameRaw = nameProperties[i];
            final boolean descending = pNameRaw.endsWith(DESCENDING_SYMBOL);
            final String pName = descending ? pNameRaw.substring(0, pNameRaw.length() - DESCENDING_SYMBOL.length()) : pNameRaw;
            final UjoProperty property = propertyList.find(pName, true).descending(descending);
            ps[i] = property;
        }
        properties = ps;
        return properties;
    }

    // -------------- STATIC METHOD(S) --------------

    /** Create a new instance, the parameters is cloned. */
    public static <UJO extends Ujo> PropertyStore<UJO> of(Class<UJO> baseClass, UjoProperty<? super UJO, ?>... properties) {
        UjoProperty[] ps = new UjoProperty[properties.length];
        System.arraycopy(properties, 0, ps, 0, ps.length);
        return new PropertyStore<UJO>(baseClass, ps);
    }

    /** Create a new instance */
    public static <UJO extends Ujo> PropertyStore<UJO> of(Class<UJO> baseClass, Collection<UjoProperty<? super UJO, ?>> properties) {
        final UjoProperty<UJO, ?>[] ps = new UjoProperty[properties.size()];
        int i = 0;
        for (UjoProperty<? super UJO, ?> p : properties) {
            ps[i++] = (UjoProperty<UJO, ?>) p;
        }
        return new PropertyStore<UJO>(baseClass, (UjoProperty[]) ps);
    }
}
