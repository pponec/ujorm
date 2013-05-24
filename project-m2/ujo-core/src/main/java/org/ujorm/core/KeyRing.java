/*
 *  Copyright 2011-2013 Pavel Ponec
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
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.UjoProperty;
import org.ujorm.core.annot.Immutable;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.extensions.PathProperty;

/**
 * The Immutable and Serializable Key Collection including some service methods.
 * Object have got implemented the hashCode() and equals() methods.
 * @author Pavel Ponec
 * @pop.todo KeyRing, KeyStock, KeyBundle, KeyRing. KeyPack
 */
@Immutable
@SuppressWarnings("deprecation")
public class KeyRing<UJO extends Ujo> implements KeyList<UJO>, Serializable {

    static final long serialVersionUID = 1L;
    /** Property Separator */
    protected static final char PROPERTY_SEPARATOR = '.';
    /** A text to mark a descending sort of a property in a deserializaton proccess. */
    protected static final String DESCENDING_SYMBOL = "" + PROPERTY_SEPARATOR + PROPERTY_SEPARATOR;
    /** The the domain class of reelated Keys. The value can be {@code null} if the Key array is empty. */
    private Class<UJO> type;
    /** Property size */
    private int size;
    /** Transient keys */
    protected Key<UJO, ?>[] keys;
    /** Default hash code. */
    transient private int hashCode;

    /**
     * Constructor
     * @param baseClass Not null base class for all keys
     * @param keys Property array
     * @see #of(java.lang.Class, org.ujorm.Key<T,?>[])
     */
    public KeyRing(Key<UJO, ?>... keys) {
        this(null, keys);
    }

    /**
     * Constructor
     * @param domainClass If the parameter is null then the value is calculated from keys.
     * @param keys Property array
     * @see #of(java.lang.Class, org.ujorm.Key<T,?>[])
     */
    protected KeyRing(Class<UJO> domainClass, Key<UJO, ?>... keys) {
        this.type = domainClass;
        this.keys = keys;
        this.size = keys.length;
    }

    /** The the domain class of reelated Keys.
     * The value can be {@code null} if the Key array is empty. */
    public Class<UJO> getType() {
        if (type==null) {
            type = getBaseType(keys);
        }
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
    public Key<UJO, ?> findDirectKey(final String name, final boolean throwException) throws IllegalArgumentException {
        int nameHash = name.hashCode();

        for (Key prop : keys) {
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
    final public Key<UJO, ?> findDirectKey(final Ujo ujo, final String name, final boolean throwException) throws IllegalArgumentException {
        return findDirectKey(ujo, name, UjoAction.DUMMY, true, throwException);
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
    public Key<UJO, ?> findDirectKey(final Ujo ujo, final String name, final UjoAction action, final boolean result, final boolean throwException) throws IllegalArgumentException {
        if (ujo == null) {
            return null;
        }
        int nameHash = name.hashCode();

        for (final Key prop : keys) {
            if (prop.getName().hashCode() == nameHash // speed up
                    && prop.getName().equals(name)
                    && (getUjoManager().isXmlAttribute(prop)) != result) {
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
     * Find <strong>indirect</strong> property by the name. Empty result can trhow NULL value if parameter throwException==false.
     * @param names Not null property name inclukde composite keys (indirect keys).
     * @param throwException
     * @return new Key
     */
    @SuppressWarnings("unchecked")
    @Override
    public Key find(String names, boolean throwException) {
        if (names.indexOf(KeyRing.PROPERTY_SEPARATOR) < 0) {
            return findDirectKey(names, throwException);
        }

        Class ujoType = type;
        int j, i = 0;
        List<Key> props = new ArrayList<Key>(8);
        names += ".";
        while ((j = names.indexOf('.', i + 1)) >= 0) {
            final String name = names.substring(i, j);
            final Key p = UjoManager.getInstance().readProperties(ujoType).findDirectKey(name, true);
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
    public Key<UJO, ?> find(String name) throws IllegalArgumentException {
        return find(name, true);
    }

    /** Get The First Keys */
    @Override
    public Key<UJO, ?> getFirstKey() {
        return get(0);
    }

    /** Get The Last Keys */
    @Override
    public Key<UJO, ?> getLastKey() {
        return get(size - 1);
    }

    /** Get one Property */
    @Override
    public Key<UJO, ?> get(int i) {
        return keys[i];
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

    /** Create Property Interator */
    @Override
    public Iterator<UjoProperty<UJO, Object>> iterator() {
        return new Iterator<UjoProperty<UJO, Object>>() {
            int i = -1;

            public boolean hasNext() {
                return (i + 1) < size;
            }

            public UjoProperty<UJO,Object> next() {
                return (UjoProperty<UJO,Object>) get(++i);
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

    /** Convert Keys to a new Array */
    @Override
    public Key[] toArray() {
        final Key[] result = new Key[size];
        System.arraycopy(this.keys, 0, result, 0, result.length);
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

//    /** Test collection if it contains a property parameter  */
//    @SuppressWarnings("element-type-mismatch")
//    public boolean contains(Object property) {
//        return Arrays.asList(keys).contains(property);
//    }
//

    /** Returns true if list contains property from the parameter. */
    @Override
    public boolean contains(Key<?, ?> o) {
        for (Key p : keys) {
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
        for (Key p : keys) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(p);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof KeyRing) {
            final KeyRing<UJO> o = (KeyRing<UJO>) obj;
            if (this.size() != o.size()) {
                return false;
            }
            if (!this.getType().equals(o.getType())) {
                return false;
            }
            for (int i = 0; i < size; ++i) {
                final Key p1 = this.get(i);
                final Key p2 = o.get(i);
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

    /** Serialization method */
    @SuppressWarnings("unused")
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(this.getType());
        out.writeObject(createPropertyNames());
    }

    /** Deserialization method */
    @SuppressWarnings("unused")
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.type = (Class<UJO>) in.readObject();
        final String[] nameProperties = (String[]) in.readObject();
        this.keys = restoreProperties(type, nameProperties);
        this.size = keys.length;
    }

    /** Create a new text Array of property names */
    private String[] createPropertyNames() {
        final String[] nameProperties = new String[keys.length];
        for (int i = keys.length - 1; i >= 0; --i) {
            final Key property = keys[i];
            nameProperties[i] = keys[i].isAscending() ? property.getName() : (property.getName() + DESCENDING_SYMBOL);
        }
        return nameProperties;
    }

    /** Create Keys */
    private Key<UJO, ?>[] restoreProperties(Class type, String[] nameProperties) {
        final KeyList propertyList = getUjoManager().readProperties(type);
        final Key<UJO, ?>[] ps = new Key[nameProperties.length];
        for (int i = 0; i < nameProperties.length; i++) {
            final String pNameRaw = nameProperties[i];
            final boolean descending = pNameRaw.endsWith(DESCENDING_SYMBOL);
            final String pName = descending ? pNameRaw.substring(0, pNameRaw.length() - DESCENDING_SYMBOL.length()) : pNameRaw;
            final Key property = propertyList.find(pName, true).descending(descending);
            ps[i] = property;
        }
        keys = ps;
        return keys;
    }

    // -------------- STATIC METHOD(S) --------------

    /** Create a new instance, the parameters is cloned.
     * @param domainClass Mandatory doomain class
     * @param keys Nullable value
     * @return If the keys are {@code null}, than the result is the {@code null} too.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> KeyRing<UJO> of(Class<UJO> domainClass, Key<? super UJO, ?>... keys) {
        if (keys == null) {
            return null;
        }
        final Key[] ps = new Key[keys.length];
        System.arraycopy(keys, 0, ps, 0, ps.length);
        return new KeyRing<UJO>(domainClass, ps);
    }

    /** Returns all direct properties form a domain class
     * @param domainClass Mandatory doomain class
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> KeyRing<UJO> of(Class<UJO> domainClass) {
        try {
            final KeyList result = domainClass.newInstance().readKeys();
            return result instanceof KeyRing
            ? (KeyRing) result
            : of(domainClass, (Collection) result);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Create a new instance
     * @param domainClass Mandatory doomain class
     * @param keys Nullable value
     * @return If the keys are {@code null}, than the result is the {@code null} too.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> KeyRing<UJO> of(Class<UJO> domainClass, Collection<Key<? super UJO, ?>> keys) {
        if (keys == null) {
            return null;
        }
        final Key[] ps = new Key[keys.size()];
        int i = 0;
        for (Key<? super UJO, ?> p : keys) {
            ps[i++] = (Key<UJO, ?>) p;
        }
        return new KeyRing<UJO>(domainClass, (Key[]) ps);
    }

    /** Create a new instance, the parameters is cloned. */
    public static <UJO extends Ujo> KeyRing<UJO> of(Key<? super UJO, ?>... keys) {
        return of(null, keys);
    }

    /** Create a new instance */
    public static <UJO extends Ujo> KeyRing<UJO> of(Collection<Key<? super UJO, ?>> keys) {
        return of(null, keys);
    }

    /** Returns the Common Base Type or value {code null}, of keys are empty.
     * @return If any key is from a child domain class, than the farthest child is returned.
     */
    @PackagePrivate static <UJO extends Ujo> Class<UJO> getBaseType(Key<UJO, ?>... keys) {
        Class<UJO> result = null;
        for (Key<UJO, ?> key : keys) {
            if (result==null || result.isAssignableFrom(getDomainType(key))) {
                result = key.getDomainType();
            }
        }
        return result;
    }

    /** Returns a domain type, the result is not null always. */
    private static Class<?> getDomainType(Key<?,?> key) {
        Class<?> result = key.getDomainType();
        return result!=null ? result : Ujo.class;
    }


}
