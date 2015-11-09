/*
 * Copyright 2012-2014 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.core;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.ListKey;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.extensions.AbstractCollectionProperty;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.PropertyModifier;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;

/**
 * Serializable key factory is the best tool of Ujorm to create Property implementations.
 * <h3>Sample of usage</h3>
 * <pre class="pre">
 * <span class="keyword-directive">public class</span> Person <span class="keyword-directive">implements</span> Ujo {
 *     <span class="keyword-directive">private static final</span> KeyFactory&lt;Person&gt; factory
 *             = KeyFactory.CamelBuilder.get(Person.<span class="keyword-directive">class</span>);
 *
 *     <span class="keyword-directive">public static final</span> Key&lt;Person,Long&gt; PID = factory.newKey();
 *     <span class="keyword-directive">public static final</span> Key&lt;Person,Integer&gt; AGE = factory.newKey();
 *     <span class="keyword-directive">public static final</span> ListUjoProperty&lt;Person,String&gt; NAMES = factory.newListKey();
 *
 *     <span class="keyword-directive">static</span> {
 *         pf.lock();
 *     }
 *
 *     <span class="comment">/&#42;&#42; Data container &#42;/</span>
 *     <span class="keyword-directive">protected</span> Object[] data;
 *
 *     <span class="keyword-directive">public</span> Object readValue(Key key) {
 *         <span class="keyword-directive">return</span> data==<span class="keyword-directive">null</span> ? data : data[key.getIndex()];
 *     }
 *
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">void</span> writeValue(Key key, Object value) {
 *         <span class="keyword-directive">if</span> (data==<span class="keyword-directive">null</span>) {
 *             data = <span class="keyword-directive">new</span> Object[readKeys().size()];
 *         }
 *         data[key.getIndex()] = value;
 *     }
 *
 *     <span class="keyword-directive">public</span> KeyList&lt;?&gt; readKeys() {
 *         <span class="keyword-directive">return</span> factory.getKeys();
 *     }
 *
 *     <span class="keyword-directive">public</span> <span class="keyword-directive">boolean</span> readAuthorization(UjoAction action, Key key, Object value) {
 *         <span class="keyword-directive">return</span> <span class="keyword-directive">true</span>;
 *     }
 * }
 * </pre>
 *
 * <h3>Using the KeyFactory in interfaces</h3>
 * In some cases can be useful to define Ujo Keys in an interface.
 * See the <a href="http://ujorm.org/javadoc/org/ujorm/package-summary.html#keyFactory2Interface">next simple example</a>
 * how to design the inteface using the class KeyFactory.
 *
 * @author Pavel Ponec
 */
public class KeyFactory<UJO extends Ujo> implements Serializable {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(KeyFactory.class);
    /** Generate key name using the cammel case. */
    protected static final boolean CAMEL_CASE = true;
    /** Requested modifier of key definitions. */
    public static final int PROPERTY_MODIFIER = Modifier.STATIC | Modifier.PUBLIC | Modifier.FINAL;
    /** Transient key list */
    transient private InnerDataStore<UJO> tmpStore;
    /** Property Store */
    private KeyList<UJO> propertyStore;

    @SuppressWarnings("unchecked")
    public KeyFactory(Class<? extends UJO> holder) {
        this(holder, false);
    }

    @SuppressWarnings("unchecked")
    public KeyFactory(Class<? extends UJO> holder, boolean propertyCamelCase) {
        this(holder, propertyCamelCase, null);
    }

    /**
     * Create new Property Factory for objecty of type.
     * @param holder The holder of the Key fields
     * @param propertyCamelCase Property names are created along fild name by a camel case converter.
     * @param abstractSuperKeys Pass a super keys fromo an abstract super class, if any.
     */
    @SuppressWarnings("unchecked")
    public KeyFactory(Class<?> holder, boolean propertyCamelCase, Iterable<? extends Key<?,?>> abstractSuperKeys) {
        this.tmpStore = new InnerDataStore<UJO>(holder, propertyCamelCase, abstractSuperKeys);
    }

    /** Get a KeyList from a super class or interfaces  */
    private Iterable<? extends Key<?,?>> createSuperKeys() {
        Iterable<? extends Key<?,?>> superKeys = tmpStore.superKeys;
        if (superKeys == null) {
            superKeys = getSuperKeys(this.tmpStore.holder);
        } else if (superKeys instanceof KeyList) {
            final KeyList<?> keyList = (KeyList<?>) superKeys;
            assert keyList.getType().isAssignableFrom(tmpStore.holder)
                    : "Type parameters is not child of the SuperProperites type: " + keyList.getTypeName();
        }

        return superKeys != null
             ? superKeys
             : InnerDataStore.EMPYT_KEYS;
    }

    /** Read Keys from the super class of the current hodler
     * @param holder The current holder of the key fields
     * @return Keys from the holder super class
     */
    protected final Iterable<? extends Key<?,?>> getSuperKeys(Class<?> holder) {
        if (holder.isInterface()) {
            final List<Key<?,?>> keyList = new ArrayList<Key<?,?>>();
            for (Class<?> types : holder.getInterfaces()) {
                for (Key<?, ?> key : readKeys(types)) {
                   keyList.add(key);
                }
            }
            return keyList;
        }
        final Class<?> superClass = holder.getSuperclass();
        if (Ujo.class.isAssignableFrom(superClass)) {
            return readKeys(superClass);
        }
        return null;
    }

    /** Read Keys from the super class or interface
     * @param superClass A super class or interface
     * @return Not null value always
     */
    private Iterable<? extends Key<?,?>> readKeys(final Class<?> superClass) throws IllegalArgumentException {
        if (Modifier.isAbstract(superClass.getModifiers())) {
            KeyList<?> r1 = null;
            KeyFactory<?> r2 = null;
            for (Field field : superClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    try {
                        if (r1 == null) {
                            r1 = getFieldValue(KeyList.class, field);
                        }
                        if (r2 == null) {
                            r2 = getFieldValue(KeyFactory.class, field);
                        }
                    } catch (Exception e) {
                        final String msg = String.format("Pass the %s attribute of the superlass %s to the constructor of the class %s, please", KeyList.class.getSimpleName(), superClass, getClass().getSimpleName());
                        throw new IllegalArgumentException(msg, e);
                    }
                }
            }
            return r1 != null ? r1 //
                    : r2 != null ? r2.getKeys() //
                    : InnerDataStore.EMPYT_KEYS;
        } else {
            try {
                return ((Ujo) superClass.newInstance()).readKeys();
            } catch (Exception e) {
                throw new IllegalArgumentException("Can't create instance of " + superClass, e);
            }
        }
    }

    /** Returns a field value */
    private <T> T getFieldValue(Class<T> type, Field field) throws Exception {
        if (type.isAssignableFrom(field.getType())) {
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return (T) field.get(null);
        }
        return null;
    }

    /** Add an new key for an internal use. */
    protected boolean addKey(Property<?, ?> p) {
        checkLock();
        return tmpStore.addKey(p);
    }

    /** Lock the key factory */
    public final void lock() {
        lockAndSize();
    }

    /** Lock the key factory
     * @return count of the direct keys.
     */
    public final int lockAndSize() {
        return getKeys().size();
    }

    /** Get KeyRing */
    public KeyList<UJO> getKeys() throws IllegalStateException {
        if (propertyStore == null) {
            // Synchronize the factory:
            synchronized (tmpStore.holder) {
                if (propertyStore == null) {
                    try {
                        propertyStore = createKeyList();
                        onCreate(propertyStore, tmpStore);
                    } catch (Throwable e) {
                        final String msg = "Can't create the KeyFactory for the " + tmpStore.holder;
                        LOGGER.log(UjoLogger.ERROR, msg, e);
                        throw new IllegalStateException(msg, e);
                    }
                    tmpStore = (InnerDataStore<UJO>) (Object) InnerDataStore.EMPTY;
                }
            }
        }
        return propertyStore;
    }

    /** Create a key List */
    protected KeyList<UJO> createKeyList() throws IllegalStateException {
        final List<Key<UJO,?>> result = new ArrayList<Key<UJO,?>>(tmpStore.propertyList.size() + 8);
        try {
            for (Key key : createSuperKeys()) {
                result.add(key);
            }
            final List<Field> fields = tmpStore.getFields();
            for (Key<UJO, ?> p : tmpStore.getKeys()) {
                result.add(p);
                if (p instanceof Property) {
                    final Property pr = (Property) p;
                    if (PropertyModifier.isLock(pr)) {
                        continue;
                    }
                    if (p.getIndex()<=Property.UNDEFINED_INDEX) {
                        PropertyModifier.setIndex(result.size()-1, pr, false);
                    }
                    final Field field = findField(p, fields);
                    if (p.getName() == null) {
                        PropertyModifier.setName(createKeyName(field, this.tmpStore.camelCase), pr);
                    }
                    if (p.getType() == null) {
                        PropertyModifier.setType(getGenericClass(field, true), pr);
                    }
                    if (p.getDomainType() == null) {
                        PropertyModifier.setDomainType(getGenericClass(field, false), pr);
                    }
                    if (p instanceof AbstractCollectionProperty) {
                        final AbstractCollectionProperty lp = (AbstractCollectionProperty) pr;
                        if (lp.getItemType() == null) {
                            PropertyModifier.setItemType(getGenericClass(field, true), lp);
                        }
                    }
                    PropertyModifier.lock(pr); // Lock all attributes:
                    tmpStore.addAnnotations(p, field); // Save all annotation s annotations.
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Can't initialize a key of the " + tmpStore.holder, e);
        }
        return tmpStore.createKeyList(result);
    }

    /** Create a key name along the field. */
    protected String createKeyName(Field field, boolean camelCase) {
        if (camelCase) {
            final StringBuilder result = new StringBuilder(32);
            final String name = field.getName();
            boolean lower = true;
            for (int i = 0, max = name.length(); i < max; i++) {
                final char c = name.charAt(i);
                if (c == '_') {
                    lower = false;
                } else {
                    result.append(lower ? Character.toLowerCase(c) : c);
                    lower = true;
                }
            }
            return result.toString().intern();
        } else {
            return field.getName().intern();
        }
    }

    /** Find field */
    private Field findField(final Key p, final List<Field> fields) throws Exception {
        for (Field field : fields) {
            if (field.get(null) == p) {
                return field;
            }
        }
        // The case for a composite fields:
        for (Field field : fields) {
            final Object fk = field.get(null);
            if (fk instanceof CompositeKey) {
                final CompositeKey ck = (CompositeKey) fk;
                if (ck.getCompositeCount() == 1
                &&  ck.getFirstKey() == p) {
                    return field;
                }
            }
        }
        final String msg = String.format("Can't get a field for the key index #%d - %s.%s"
                , p.getIndex()
                , tmpStore.holder.getSimpleName()
                , p.getName());
        throw new IllegalStateException(msg);
    }

    /** Create new Key */
    public <T> Key<UJO, T> newKey() {
        return createKey(null, null, null);
    }

    /** Create new Key */
    public <T> Key<UJO, T> newKey(String name) {
        return createKey(name, null, null);
    }

    /** Create new Key with a default value */
    public <T> Key<UJO, T> newKeyDefault(T defaultValue) {
        return createKey(null, defaultValue, null);
    }

    /** Create new Key */
    public <T> Key<UJO, T> newKey(Validator<T> validator) {
        return createKey(null, null, validator);
    }

    /** Create new Key */
    public <T> Key<UJO, T> newKey(String name, T defaultValue) {
        return createKey(name, defaultValue, null);
    }

    /** Create new Key */
    public <T> Key<UJO, T> newKey(String name, Validator<T> validator) {
        return createKey(name, null, validator);
    }

    /** Create new Key with a default value */
    public <T> Key<UJO, T> newKeyDefault(T defaultValue, Validator<T> validator) {
        return createKey(null, defaultValue, validator);
    }

    /** Create new Key */
    public <T> Key<UJO, T> newKey(String name, T defaultValue, Validator<T> validator) {
        return createKey(name, defaultValue, validator);
    }

    /** Create new Key for a value type class */
    public <T> Key<UJO, T> newClassKey(String name, Class<?> defaultClassValue) {
        return createKey(name, (T) defaultClassValue, null);
    }

    /** Create a new CompositeKey with an alias name */
    public <T> CompositeKey<UJO, T> newKeyAlias(String alias) {
        final Key<UJO, T> result = newKey();
        return result.alias(alias);
    }

    /** Create a new CompositeKey with an alias name */
    public <T> CompositeKey<UJO, T> newKeyAlias(String name, String alias) {
        final Key<UJO, T> result = newKey(name);
        return result.alias(alias);
    }

    /** Create a new CompositeKey with an alias name */
    public <T> CompositeKey<UJO, T> newKeyAlias(String name, String alias, Validator<T> validator) {
        final Key<UJO, T> result = newKey(name, validator);
        return result.alias(alias);
    }

    /** Create a new CompositeKey with an alias name */
    public <T> CompositeKey<UJO, T> newKeyAlias(String alias, Validator<T> validator) {
        final Key<UJO, T> result = newKey(validator);
        return result.alias(alias);
    }

    /** Add new Key for a value type class, index must be undefied */
    public <P extends Property<UJO, ?>> P add(P key) {
        if (key.getIndex() >= 0) {
            throw new IllegalArgumentException("Property index must be undefined");
        }
        addKey(key);
        return (P) key;
    }

    /** Common protected factory method */
    protected <T> Key<UJO, T> createKey(String name, T defaultValue, Validator<T> validator) {
        final Property<UJO, T> p = Property.of(name, null, defaultValue, Property.UNDEFINED_INDEX, validator, false);
        addKey(p);
        return p;
    }

    /** Create new Key */
    public final <T> ListKey<UJO, T> newListKey() {
        return newListKey(null);
    }

    /** Create new Key */
    public <T> ListKey<UJO, T> newListKey(String name) {
        checkLock();
        final ListKey<UJO, T> p = ListProperty.newListProperty(name, null, Property.UNDEFINED_INDEX, false);
        tmpStore.addKey(p);
        return p;
    }

    /** Check if the class is locked */
    protected void checkLock() throws IllegalStateException {
        if (propertyStore != null) {
            throw new IllegalStateException(getClass().getSimpleName() + " is locked");
        }
    }

    /** An event on Create */
    protected void onCreate(KeyList<UJO> list, InnerDataStore<UJO> innerData) {
        UjoManager.getInstance().register(list, innerData);
    }

    /** TemporaryStore */
    protected InnerDataStore<UJO> getTmpStore() {
        return tmpStore;
    }

    /* ================== STATIC METHOD ================== */

    /** Returns array of generic parameters
     * @param field Base field
     * @param typeResult Value {@code true} means a key TYPE, other it is required key DOMAIN.
     * @return type
     * @throws IllegalArgumentException
     */
    @PackagePrivate
    static Class getGenericClass(final Field field, final boolean typeResult) throws IllegalArgumentException {
        try {
            final ParameterizedType type = (ParameterizedType) field.getGenericType();
            final Type[] types = type.getActualTypeArguments();
            final Type result = types[typeResult
                    ? types.length - 1
                    : 0];
            return (result instanceof Class)
                    ? (Class) result
                    : Class.class;
        } catch (Exception e) {
            LOGGER.log(UjoLogger.WARN, "The generic scan failed on the field '{}'", field.getName());
            return typeResult
                    ? Object.class
                    : Ujo.class;
        }
    }

    // ================== INNER CLASS ==================
    /** A temporarry data store. */
    protected static final class InnerDataStore<UJO extends Ujo> {

        /** Empty Key List */
        private static final Iterable<? extends Key<?,?>> EMPYT_KEYS = Collections.emptyList();
        /** Empty constant */
        private static final InnerDataStore<Ujo> EMPTY = new InnerDataStore<Ujo>(Ujo.class, false, null);
        /** External keys */
        private final Iterable<? extends Key<?,?>> superKeys;
        /** The Ujo type is serialized holder of the Fields*/
        private final Class<?> holder;
        /** Convert <strong>field names<strong> to a camelCase name.*/
        private final boolean camelCase;
        /** Transient key list */
        private final List<Key<UJO, ?>> propertyList;
        /** Property annotations */
        private final Map<Key<UJO, ?>, Map<Class<? extends Annotation>, Annotation>> annotationsMap;
        /** The Ujo type is serialized holder of the Fields*/
        private Class<?> type;

        /** Constructor */
        public InnerDataStore(Class<?> holder, boolean propertyCamelCase, Iterable<? extends Key<?,?>> abstractSuperKeys) {
            this.holder = holder;
            this.camelCase = propertyCamelCase;
            this.superKeys = abstractSuperKeys;
            this.propertyList = new ArrayList<Key<UJO, ?>>(32);
            this.annotationsMap = new HashMap<Key<UJO, ?>, Map<Class<? extends Annotation>, Annotation>>();
        }

        /** Add all annotation for required key. */
        public void addAnnotations(Key<UJO, ?> p, Field field) {
            final Annotation[] annotations = field.getAnnotations();
            Map<Class<? extends Annotation>, Annotation> annots = annotationsMap.get(field);
            if (annots == null && annotations.length > 0) {
                annots = new HashMap<Class<? extends Annotation>, Annotation>(annotations.length);
                annotationsMap.put(p, annots);
            }
            for (Annotation annotation : annotations) {
                annots.put(annotation.annotationType(), annotation);
            }
        }

        /** Add key to a list */
        public boolean addKey(Key p) {
            return propertyList.add(p);
        }

        /** Get all keys */
        public Iterable<Key<UJO, ?>> getKeys() {
            return propertyList;
        }

        /** Create a new Property List */
        public KeyList<UJO> createKeyList(List<Key<UJO,?>> keys) {
            propertyList.clear();
            propertyList.addAll(keys);
            return KeyRing.of((List) keys);
        }

        /** Returns a count of the Key items */
        public int size() {
            return propertyList.size();
        }

        /** Returns a domain type. */
        @SuppressWarnings("unchecked")
        public Class<?> getDomainType() {
            if (type == null) {
                type = KeyRing.getBaseType(propertyList.toArray(new Key[propertyList.size()]));
            }
            return holder;
        }

        /**
         * @return the cammelCase
         */
        public boolean isCammelCase() {
            return camelCase;
        }

        /** Get all UjoPorperty fields */
        public List<Field> getFields() {
            final Field[] fields = holder.getFields();
            final List<Field> result = new LinkedList<Field>();
            for (int j = 0; j < fields.length; j++) {
                final Field field = fields[j];
                if (field.getModifiers() == UjoManager.PROPERTY_MODIFIER
                        && Key.class.isAssignableFrom(field.getType())) {
                    result.add(field);
                }
            }
            return result;
        }

        /**
         * Returns the Property annotations Set.
         */
        @PackagePrivate
        Map<Class<? extends Annotation>, Annotation> getAnnotations(Key<UJO, ?> p) {
            Map<Class<? extends Annotation>, Annotation> result = annotationsMap.get(p);
            if (result == null) {
                result = Collections.emptyMap();
            }
            return result;
        }

        /**
         * Returns required annotation or {@code null}, if no annotatin was not found.
         */
        @SuppressWarnings("unchecked")
        public <A extends Annotation> A getAnnotation(Key<UJO, ?> p, Class<A> annoType) {
            final Map<Class<? extends Annotation>, Annotation> result = getAnnotations(p);
            return (A) result.get(annoType);
        }
    }

    // --------------- BUILDERS ---------------

    /** The base factory */
    public static final class Builder {

        /** Private constructor */
        private Builder() {
        }

        /** Return an instance of the {@link KeyFactory} class */
        @SuppressWarnings("unchecked")
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<? extends UJO> baseClass) {
            return new KeyFactory(baseClass);
        }

        /** Return an instance of the {@link KeyFactory} class.
         * @param baseClass The domain class
         * @param superKeys Keys form an abstract super class
         */
        @SuppressWarnings("unchecked")
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<? extends UJO> baseClass, KeyList<?> superKeys) {
            return new KeyFactory(baseClass, false, superKeys);
        }
    }

    /** The base factory */
    public static final class CamelBuilder {

        /** Private constructor */
        private CamelBuilder() {
        }

        /** Return an instance of the {@link KeyFactory} class
         * @param baseClass Base class
         * @param propertyCamelCase {@link #CAMEL_CASE}
         * @return Return an instance of the {@link KeyFactory} class
         */
        @SuppressWarnings("unchecked")
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<? extends UJO> baseClass) {
            return new KeyFactory(baseClass, CAMEL_CASE, null);
        }

        /** Return an instance of the {@link KeyFactory} class.
         * @param baseClass The domain class
         * @param propertyCamelCase {@link #CAMEL_CASE}
         * @param superKeys Keys form an abstract super class
         */
        @SuppressWarnings("unchecked")
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<? extends UJO> baseClass, KeyList<?> superKeys) {
            return new KeyFactory(baseClass, CAMEL_CASE, superKeys);
        }
    }

    /** The factory for creating Key where a validator is off */
    public static final class NoCheckBuilder {

        /** Private constructor */
        private NoCheckBuilder() {
        }

        /** Return an instance of the {@link KeyFactory} class
         * @param baseClass Base class
         * @param propertyCamelCase {@link #CAMEL_CASE}
         * @return Return an instance of the {@link KeyFactory} class
         */
        @SuppressWarnings("unchecked")
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<? extends UJO> baseClass) {
            return new NoCheckedKeyFactory(baseClass, CAMEL_CASE, null);
        }

        /** Return an instance of the {@link KeyFactory} class.
         * @param baseClass The domain class
         * @param propertyCamelCase {@link #CAMEL_CASE}
         * @param superKeys Keys form an abstract super class
         */
        @SuppressWarnings("unchecked")
        public static <UJO extends Ujo> KeyFactory<UJO> get(Class<? extends UJO> baseClass, KeyList<?> superKeys) {
            return new NoCheckedKeyFactory(baseClass, CAMEL_CASE, superKeys);
        }
    }

    /** The base factory for the WeakKeyFactory implementation. */
    public static final class WeakBuilder {

        /** Private constructor */
        private WeakBuilder() {
        }

        /** Default constructor with a CamelCase feature building.
         * @param holder The class with a public static Keys.
         */
        public static WeakKeyFactory get(Class<?> holder) {
            return new WeakKeyFactory(holder);
        }

        /** Default constructor with a CamelCase feature building.
         * @param holder The class with a public static Keys.
         */
        public static WeakKeyFactory get(Class<?> holder, boolean propertyCamelCase) {
            return new WeakKeyFactory(holder, propertyCamelCase);
        }
    }
}
