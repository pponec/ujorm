/*
 *  Copyright 2011-2022 Pavel Ponec
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.extensions.PathProperty;
import org.ujorm.tools.Check;
import org.ujorm.tools.msg.MsgFormatter;

/**
 * The Immutable and Serializable Key Collection including some service methods.
 * Object have got implemented the hashCode() and equals() methods.
 * @author Pavel Ponec
 * @pop.todo KeyRing, KeyStock, KeyBundle, KeyRing. KeyPack
 */
@Unmodifiable
@SuppressWarnings("deprecation")
public class KeyRing<U extends Ujo> implements KeyList<U>, Serializable {
    static final long serialVersionUID = 2018_03_17L;

    /** Property Separator */
    protected static final char PROPERTY_SEPARATOR = '.';
    /** A text to mark a descending sort of a key in a de-serialization process. */
    protected static final String DESCENDING_SYMBOL = ""
            + PROPERTY_SEPARATOR
            + PROPERTY_SEPARATOR
            + PROPERTY_SEPARATOR
            ;
    /** The the domain class of related Keys. The value can be {@code null} if the Key array is empty. */
    private Class<U> type;
    /** Transient keys */
    transient protected Key<U, ?>[] keys;
    /** Default hash code. */
    transient private int hashCode;

    /**
     * Constructor
     * @param keys Property array
     * @see #of(Key[])
     */
    public KeyRing(Key<U, ?>... keys) {
        this(null, keys);
    }

    /**
     * Constructor
     * @param domainClass If the parameter is null then the value is calculated from keys.
     * @param keys Property array
     * @see #of(Key[])
     */
    protected KeyRing(Class<U> domainClass, Key<U, ?>... keys) {
        this.type = domainClass;
        this.keys = keys;
    }

    /** The the domain class of related Keys.
     * The value can be {@code null} if the Key array is empty. */
    @Override
    public Class<U> getType() {
        if (type==null) {
            type = getBaseType(keys);
        }
        return type;
    }

    /**
     * Find a direct key by its name from the parameter.
     *
     * @param name A key name.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     * @return .
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Key<U, T> findDirectKey(final String name, final boolean throwException) throws IllegalArgumentException {
        int nameHash = name.hashCode();

        for (Key prop : keys) {
            if (prop.getName().hashCode() == nameHash // speed up
                    && prop.getName().equals(name)) {
                return prop;
            }
        }

        if (throwException) {
            throwException(name, type, null);
        }
        return null;
    }

    @Override
    public final <T> Key<U, T> findDirectKey(final Ujo ujo, final String name, final boolean throwException) throws IllegalArgumentException {
        return findDirectKey(ujo, name, UjoAction.DUMMY, true, throwException);
    }

    /**
     * Find a direct key by key name from parameter.
     * @param ujo An Ujo object
     * @param name A key name.
     * @param action Action type UjoAction.ACTION_* .
     * @param result Required result of action.
     * @param throwException If result not found an Exception is throwed, or a null can be returned.
     */
    @SuppressWarnings("deprecation")
    @Override
    public <T> Key<U, T> findDirectKey(final Ujo ujo, final String name, final UjoAction action, final boolean result, final boolean throwException) throws IllegalArgumentException {
        if (ujo == null) {
            return null;
        }
        int nameHash = name.hashCode();

        for (final Key prop : keys) {
            if (prop.getName().hashCode() == nameHash // speed up
            &&  prop.getName().equals(name)
            && (getUjoManager().isXmlAttribute(prop) != result)) {
                return prop;
            }
        }

        if (throwException) {
            throwException(name, ujo.getClass(), null);
        }
        return null;
    }

    /**
     * Find <strong>indirect</strong> key by the name. Empty result can trhow NULL value if parameter throwException==false.
     * @param names Not null key name include composite keys (indirect keys).
     * @param throwException
     * @return new Key
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Key<U,T> find(String names, boolean throwException) {
        if (names.indexOf(KeyRing.PROPERTY_SEPARATOR) < 0) {
            return findDirectKey(names, throwException);
        }

        Class ujoType = getType();
        int j, i = 0;
        List<Key> props = new ArrayList<>(8);
        names += ".";
        try {
            while ((j = names.indexOf('.', i + 1)) >= 0) {
                final String name = names.substring(i, j);
                final Key p = UjoManager.getInstance().readKeys(ujoType).findDirectKey(name, throwException);
                if (p == null) {
                    return null;
                }
                props.add(p);
                ujoType = p.getType();
                i = j + 1;
            }
        } catch (Exception e) {
            throwException(names, type, e);
        }

        switch (props.size()) {
            case 0:
                if (throwException) {
                    throwException(names, type, null);
                } else {
                    return null;
                }
            case 1:
                return props.get(0);
            default:
                return new PathProperty(CompositeKey.DEFAULT_ALIAS, props);
        }
    }

    /**
     * Find (both direct or indirect) key by key name from parameter.
     * @param name A key name by sample "user.address.street".
     * @return .
     */
    @Override
    public <T> Key<U,T> find(String name) throws IllegalArgumentException {
        return find(name, true);
    }

    /** Get The First Keys */
    @Override
    public <T> Key<U, T> getFirstKey() {
        return get(0);
    }

    /** Get The Last Keys */
    @Override
    public <T> Key<U,T> getLastKey() {
        return get(keys.length - 1);
    }

    /** Get The First value */
    public final Object getFirstValue(@NotNull final U ujo) {
        return getValue(ujo, 0);
    }

    /** Get The First value */
    public final Object getLastValue(@NotNull final U ujo) {
        return getValue(ujo, keys.length - 1);
    }

    /** Get The First value */
    public final Object getValue(@NotNull final U ujo, int i) {
        return get(i).of(ujo);
    }

    /** Get one Property */
    @Override
    public <T> Key<U,T> get(int i) {
        return (Key<U,T>) keys[i];
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
        return keys.length;
    }

    /** Is the collection empty? */
    @Override
    public boolean isEmpty() {
        return keys.length == 0;
    }

    /** Create Key Iterator */
    @Override
    public final Iterator<Key<U, Object>> iterator() {
        return new Iterator<Key<U, Object>>() {
            int i = -1;

            @Override public boolean hasNext() {
                return (i + 1) < keys.length;
            }

            @Override public Key<U,Object> next() {
                return get(++i);
            }

            /** The method is not supported. */
            @Deprecated
            @Override public void remove() throws UnsupportedOperationException {
                throw new UnsupportedOperationException("The REMOVE operation is not supported.");
            }
        };
    }

    /** Convert Keys to a new Array */
    @Override
    public Key[] toArray() {
        final Key[] result = new Key[keys.length];
        System.arraycopy(keys, 0, result, 0, result.length);
        return result;
    }

    /** Create new Instance of the Base Ujo object */
    @Override
    public U newBaseUjo() throws IllegalStateException {
        try {
            @SuppressWarnings("unchecked")
            final U result = type.newInstance();
            return result;

        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException("Can't create instance for " + type, e);
        }
    }

    /** Returns a class name of the related UJO */
    @Override
    public String getTypeName() {
        return type.getName();
    }

//    /** Test collection if it contains a key parameter  */
//    @SuppressWarnings("element-type-mismatch")
//    public boolean contains(Object key) {
//        return Arrays.asList(keys).contains(key);
//    }

    /** Returns true if list contains key from the parameter. */
    @Override
    public boolean contains(final Key<?, ?> o) {
        for (Key p : keys) {
            if (p.equals(o)) {
                return true;
            }
        }
        return false;
    }

    /** Returns the key names */
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
            final KeyRing<U> o = (KeyRing<U>) obj;
            if (this.size() != o.size()) {
                return false;
            }
            if (!this.getType().equals(o.getType())) {
                return false;
            }
            for (int i = keys.length -1 ; i >= 0; --i) {
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
    private void writeObject(@NotNull final ObjectOutputStream out) throws IOException {
        out.writeObject(this.getType());
        out.writeObject(createPropertyNames());
        out.writeObject(createAliasNames());
    }

    /** De-serialization method */
    @SuppressWarnings({"unused", "unchecked"})
    private void readObject(@NotNull final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.type = (Class<U>) in.readObject();
        final String[] nameProperties = (String[]) in.readObject();
        final String[][] spaces = (String[][]) in.readObject();
        this.keys = restoreProperties(type, nameProperties, spaces);
    }

    /** Create a new text Array of key names */
    private String[] createPropertyNames() {
        final String[] nameProperties = new String[keys.length];
        for (int i = keys.length - 1; i >= 0; --i) {
            final Key key = keys[i];
            nameProperties[i] = keys[i].isAscending()
                    ?  key.getName()
                    : (key.getName() + DESCENDING_SYMBOL);
        }
        return nameProperties;
    }

    /** Create a text Array for alias names */
    private String[][] createAliasNames() {
        final String[][] result = new String[keys.length][];
        for (int i = keys.length - 1; i >= 0; --i) {
            final Key key = keys[i];
            if (key.isComposite()) {
                final CompositeKey cKey = (CompositeKey) key;
                if (cKey.hasAlias()) {
                    result[i] = new String[cKey.getKeyCount()];
                    for (int j = 0; j < cKey.getKeyCount(); j++) {
                        result[i][j] = cKey.getAlias(j);
                    }
                }
            }
        }
        return result;
    }

    /** Create Keys */
    @SuppressWarnings("unchecked")
    private Key<U, ?>[] restoreProperties(@NotNull final Class type, @NotNull final String[] nameProperties, @NotNull final String[][] spaces) {
        final Key<U, ?>[] result = new Key[nameProperties.length];
        final KeyList<?> propertyList = getUjoManager().readKeys(type);
        for (int i = 0; i < nameProperties.length; i++) {
            final String pNameRaw = nameProperties[i];
            final boolean descending = pNameRaw.endsWith(DESCENDING_SYMBOL);
            final String pName = descending
                    ? pNameRaw.substring(0, pNameRaw.length() - DESCENDING_SYMBOL.length())
                    : pNameRaw;
            final Key key = propertyList.find(pName, true).descending(descending);
            result[i] = spaces[i] == null
                    ? key
                    : new PathProperty(key, spaces[i], key.isAscending());
        }
        return result;
    }

    // -------------- STATIC METHOD(S) --------------

    /** Create a new instance, the parameters is cloned.
     * @param key Nullable value
     * @return If the keys are {@code null}, than the result is the {@code null} too.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> KeyRing<UJO> of(@Nullable final Key<? super UJO, ?> key) {
        return key != null
             ? new KeyRing<>((Class<UJO>) key.getDomainType(), new Key[] {key})
             : null;
    }

    /** Create a new instance, the parameters is cloned.
     * @param domainClass The domain class where a not null value is recommended for better performance.
     * @param keys Nullable value
     * @return If the keys are {@code null}, than the result is the {@code null} too.
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> KeyRing<UJO> of(@Nullable final Class<UJO> domainClass, @Nullable Key<? super UJO, ?>... keys) {
        if (keys == null) {
            return null;
        }
        final Key[] ps = new Key[keys.length];
        System.arraycopy(keys, 0, ps, 0, ps.length);
        return new KeyRing<>(domainClass, ps);
    }

    /** Returns all domain keys excluding the argument keys.
     * @param excludedKeys Array of the <strong>direct</strong> excluded keys.
     */
    public static <UJO extends Ujo> KeyRing<UJO> ofExcluding(@NotNull final Key<?, ?>... excludedKeys) {
        return ofExcluding(getBaseType((Key<UJO, ?>[]) excludedKeys), excludedKeys);
    }


    /** Returns all domain keys excluding the argument keys.
     * @param domainClass The domain class where a not null value is recommended for better performance.
     * @param excludedKeys Array of the <strong>direct</strong> excluded keys.
     */
    public static <UJO extends Ujo> KeyRing<UJO> ofExcluding(@NotNull final Class<UJO> domainClass, Key<?, ?>... excludedKeys) {
        final List<Key<? super UJO, ?>> keys = new ArrayList<>();
        main:
        for (Key<UJO,?> key : of(domainClass)) {
            for (Key<?, ?> ex : excludedKeys) {
                if (key == ex) {
                    continue main;
                }
            }
            keys.add(key);
        }
        return of(keys);
    }

    /** Returns all direct properties form a domain class
     * @param domainClass Mandatory domain class
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo> KeyRing<UJO> of(@NotNull final Class<UJO> domainClass) {
        try {
            final KeyList result = domainClass.newInstance().readKeys();
            return result instanceof KeyRing
            ? (KeyRing) result
            : of(domainClass, (Collection) result);
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new IllegalUjormException(e);
        }
    }

    /** Create a new instance
     * @param domainClass The domain class where a not null value is recommended for better performance.
     * @param keys Nullable value
     * @return If the keys are {@code null}, than the result is the {@code null} too.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <UJO extends Ujo> KeyRing<UJO> of(@Nullable final Class<UJO> domainClass, @NotNull final Collection<Key<? super UJO, ?>> keys) {
        if (Check.isEmpty(keys)) {
            return null;
        }
        final Key[] ps = new Key[keys.size()];
        int i = 0;
        for (Key<? super UJO, ?> p : keys) {
            ps[i++] = p;
        }
        return new KeyRing<>(domainClass, ps);
    }

    /** Create a new instance, the parameters is cloned. */
    public static <UJO extends Ujo> KeyRing<UJO> of(@NotNull final Key<? super UJO, ?>... keys) {
        return of(null, keys);
    }

    /** Create a new instance */
    public static <UJO extends Ujo> KeyRing<UJO> of(@NotNull final Collection<Key<? super UJO, ?>> keys) {
        return of(null, keys);
    }

    /** Returns the Common Base Type or value {code null}, of keys are empty.
     * @return If any key is from a child domain class, than the farthest child is returned.
     */
    @PackagePrivate static <UJO extends Ujo> Class<UJO> getBaseType(@NotNull final Key<UJO, ?>... keys) {
        Class<UJO> result = null;
        for (Key<UJO, ?> key : keys) {
            if (result==null || result.isAssignableFrom(getDomainType(key))) {
                result = key.getDomainType();
            }
        }
        return result;
    }

    /** Returns a domain type,
     * The result is not null always where an undefine value have got result the {@link Ujo}
     */
    private static Class<?> getDomainType(@NotNull final Key<?,?> key) {
        Class<?> result = key.getDomainType();
        return result!=null ? result : Ujo.class;
    }

    /** Throws an {@link IllegalArgumentException} exception with the text:<br>
     * "The 'keyname' of the class was not found"
     */
    private void throwException
        ( @NotNull final String keyName
        , @NotNull final Class type
        , @NotNull final Throwable e) throws IllegalUjormException {
        final String msg = MsgFormatter.format("The key '{}' of the {} was not found", keyName, type);
        throw new IllegalUjormException(msg, e);
    }

}
