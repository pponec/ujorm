/*
 * Copyright 2012-2012 Pavel Ponec
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.extensions.AbstractCollectionProperty;
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.PropertyModifier;

/**
 * Non serializable property factory
 * @author ponec
 */
public class PropertyFactory<UJO extends Ujo> implements Serializable {

    /** Requested modifier of property definitions. */
    public static final int PROPERTY_MODIFIER = Modifier.STATIC|Modifier.PUBLIC|Modifier.FINAL;

    /** Transient property list */
    transient private InnerDataStore<UJO> tmpStore;

    /** Property Store */
    private UjoPropertyList<UJO> propertyStore;

    @SuppressWarnings("unchecked")
    public PropertyFactory(Class<? extends UJO> type) {
        this(type, false);
    }

    @SuppressWarnings("unchecked")
    public PropertyFactory(Class<? extends UJO> type, boolean propertyCamelCase) {
        this.tmpStore = new InnerDataStore<UJO>(type, propertyCamelCase);
        try {
            final Class<?> superClass = type.getSuperclass();
            if (Ujo.class.isAssignableFrom(superClass)
            && !Modifier.isAbstract(superClass.getModifiers())) {
                for (UjoProperty p : ((Ujo) superClass.newInstance()).readProperties()) {
                    tmpStore.addProperty(p);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't create instance of " + type.getSuperclass(), e);
        }
    }

    /** Add an new property for an internal use. */
    protected boolean addProperty(UjoProperty p) {
        checkLock();
        return tmpStore.addProperty(p);
    }

    /** Lock the property factory */
    public final void lock() {
        lockAndSize();
    }

    /** Lock the property factory
     * @return count of the direct properties.
     */
    public final int lockAndSize() {
        return getPropertyList().size();
    }

    /** Get PropertyStore */
    public UjoPropertyList<UJO> getPropertyList() {
        if (propertyStore==null) {
            propertyStore = createPropertyList();
            tmpStore = null;
            onCreate(propertyStore);
        }
        return propertyStore;
    }

    /** Create a property List */
    protected UjoPropertyList<UJO> createPropertyList() throws IllegalStateException {
        final List<Field> fields = tmpStore.getFields();
        try {
            for (UjoProperty<UJO, ?> p : tmpStore.getProperties()) {
                if (p instanceof Property) {
                    final Property pr = (Property) p;
                    if (PropertyModifier.isLock(pr)) {
                        continue;
                    }
                    Field field = findField(p, fields);
                    if (p.getName() == null) {
                        PropertyModifier.setName(field.getName(), pr);
                    }
                    if (p.getType() == null) {
                        PropertyModifier.setType(getGenericClass(field, 1), pr);
                    }
                    if (p instanceof AbstractCollectionProperty) {
                        final AbstractCollectionProperty lp = (AbstractCollectionProperty) pr;
                        if (lp.getItemType() == null) {
                            PropertyModifier.setItemType(getGenericClass(field,1), lp);
                        }
                    }
                    PropertyModifier.lock(pr); // Lock all attributes:
                    tmpStore.addAnnotation(p, field); // Save all annotation s annotations.
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Can't initialize a property of the " + tmpStore.type, e);
        }
        return tmpStore.createPropertyList();
    }

    /** Find field */
    private Field findField(UjoProperty p, List<Field> fields) throws Exception {
        for (Field field : fields) {
            if (field.get(null) == p) {
                return field;
            }
        }
        throw new IllegalStateException("Can't get a field for the property index #" + p.getIndex());
    }

    /** Create new UjoProperty */
    public <T> UjoProperty<UJO,T> newProperty() {
        return createProperty(null, null);
    }

    /** Create new UjoProperty */
    public <T> UjoProperty<UJO,T> newProperty(String name) {
        return createProperty(name, null);
    }

    /** Create new UjoProperty with a default value */
    public <T> UjoProperty<UJO,T> newPropertyDefault(T defaultValue) {
        return createProperty(null, defaultValue);
    }

    /** Create new UjoProperty */
    public <T> UjoProperty<UJO,T> newProperty(String name, T defaultValue) {
        return createProperty(name, defaultValue);
    }

    /** Common protected factory method */
    protected <T> UjoProperty<UJO,T> createProperty(String name, T defaultValue) {
        final Property<UJO,T> p = Property.newInstance(name, null, defaultValue, tmpStore.size(), false);
        addProperty(p);
        return p;
    }

    /** Create new UjoProperty */
    public final <T> ListProperty<UJO,T> newListProperty() {
        return newListProperty(null);
    }

    /** Create new UjoProperty */
    public <T> ListProperty<UJO,T> newListProperty(String name) {
        checkLock();
        final ListProperty<UJO,T> p = ListProperty.newListProperty(name, null, tmpStore.size(), false);
        tmpStore.addProperty(p);
        return p;
    }

    /** Check if the class is locked */
    protected void checkLock() throws IllegalStateException {
        if (propertyStore!=null) {
            throw new IllegalStateException(getClass().getSimpleName() + " is locked");
        }
    }

    /** An event on Create */
    protected void onCreate(UjoPropertyList<UJO> list) {
        // UjoManager.getInstance().init(list);
    }

    /* ================== STATIC METHOD ================== */

    /** Return an instance of the {@link PropertyFactory} class */
    public static <UJO extends Ujo> PropertyFactory<UJO> getInstance(Class<UJO> baseClass) {
        return new PropertyFactory(baseClass);
    }

    /** Returns new factory instance along the parameter class {@code factory}
     * @param baseClass base class
     * @param factoryClass A class with implementaton of the factory with consturctor parameter type of {@code Class<UJO>}.
     * @throws IllegalArgumentException
     */
    public static <UJO extends Ujo, T extends PropertyFactory<UJO>> T getInstance(Class<UJO> baseClass, Class<T> factoryClass) throws IllegalArgumentException {
        try {
            return factoryClass.getConstructor(Class.class).newInstance(baseClass);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't create instence of the factory " + factoryClass, e);
        }
    }

    /** Regurns array of generic parameters */
    @PackagePrivate static Class getGenericClass(final Field field, final int position) throws IllegalArgumentException {
        try {
            final ParameterizedType type = (ParameterizedType) field.getGenericType();
            final Type result = type.getActualTypeArguments()[position];
            return (result instanceof Class) ? (Class) result : Class.class;
        } catch (Exception e) {
            final String msg = String.format("The field '%s' generic scan failed", field.getName());
            throw new IllegalArgumentException(msg, e);
        }
    }

    // ================== INNER CLASS ==================

    private static final class InnerDataStore<UJO extends Ujo> {

        /** The Ujo type is serializad */
        private final Class<? extends UJO> type;

        /** Convert field name to a camelCase */
        private final boolean camelCase;

        /** Transient property list */
        private final List<UjoProperty<UJO,?>> propertyList;

        /** Property annotations */
        private final Map<UjoProperty<UJO,?>, Map<Class<? extends Annotation>,Annotation>> annotationsMap;

        /** Constructor */
        public InnerDataStore(Class<? extends UJO> type, boolean propertyCamelCase) {
            this.type = type;
            this.camelCase = propertyCamelCase;
            this.propertyList = new ArrayList<UjoProperty<UJO,?>>(32);
            this.annotationsMap = new HashMap<UjoProperty<UJO,?>,Map<Class<? extends Annotation>,Annotation>>();
        }

        /** Add all annotation for required property. */
        public void addAnnotation(UjoProperty<UJO,?> p, Field field) {
            Map<Class<? extends Annotation>,Annotation> annots = annotationsMap.get(field);
            if (annots==null) {
                annots = new HashMap<Class<? extends Annotation>,Annotation>(8);
                annotationsMap.put(p, annots);
            }
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                annots.put(annotation.getClass(), annotation);
            }
        }

        /** Add property to a list */
        public boolean addProperty(UjoProperty p) {
            return propertyList.add(p);
        }

        /** Get all properties */
        public Iterable<UjoProperty<UJO,?>> getProperties() {
            return propertyList;
        }

        /** Create a new Property List */
        public UjoPropertyList<UJO> createPropertyList() {
            return PropertyStore.of(type, (List) propertyList);
        }

        /** Returns a count of the UjoProperty items */
        public int size() {
            return propertyList.size();
        }

        /**
         * @return the cammelCase
         */
        public boolean isCammelCase() {
            return camelCase;
        }

        /** Get all UjoPorperty fields */
        public List<Field> getFields() {
            final Field[] fields = type.getFields();
            final List<Field> result = new LinkedList<Field>();
            for (int j = 0; j < fields.length; j++) {
                final Field field = fields[j];
                if (field.getModifiers()==UjoManager.PROPERTY_MODIFIER
                &&  UjoProperty.class.isAssignableFrom(field.getType()) ){
                    result.add(field);
                }
            }
            return result;
        }

        /**
         * Property annotations Set
         */
        public  Map<Class<? extends Annotation>,Annotation> getAnnotations(UjoProperty<UJO,?> p) {
            return annotationsMap.get(p);
        }

    }

}
