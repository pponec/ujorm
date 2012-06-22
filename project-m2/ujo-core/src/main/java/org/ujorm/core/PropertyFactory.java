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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.extensions.AbstracCollectionProperty;
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
    transient private List<UjoProperty<UJO, ?>> propertyList;

    /** Property Store */
    private UjoPropertyList<UJO> propertyStore;

    /** The Ujo type is serializad */
    private Class<? extends UJO> type;

    @SuppressWarnings("unchecked")
    public PropertyFactory(Class<? extends UJO> type) {
        this.type = type;
        this.propertyList = new ArrayList<UjoProperty<UJO, ?>>();
        try {
            final Class<?> superClass = type.getSuperclass();
            if (Ujo.class.isAssignableFrom(superClass)
            && !Modifier.isAbstract(superClass.getModifiers())) {
                for (UjoProperty p : ((Ujo) superClass.newInstance()).readProperties()) {
                    propertyList.add(p);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't create instance of " + type.getSuperclass(), e);
        }
    }

    /** Add an new property for an internal use. */
    protected boolean addProperty(UjoProperty p) {
        checkLock();
        return propertyList.add(p);
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
            propertyList = null;
            onCreate(propertyStore);
        }
        return propertyStore;
    }

    /** Create a property List */
    protected UjoPropertyList<UJO> createPropertyList() {
        final List<Field> fields = getFields();
        try {
            for (UjoProperty<UJO, ?> p : propertyList) {
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
                    if (p instanceof AbstracCollectionProperty) {
                        final AbstracCollectionProperty lp = (AbstracCollectionProperty) pr;
                        if (lp.getItemType() == null) {
                            PropertyModifier.setItemType(getGenericClass(field,1), lp);
                        }
                    }                    
                    PropertyModifier.lock(pr); // Lock all attributes:
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(PropertyFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        final UjoPropertyList<UJO> result = PropertyStore.of(type, (List) propertyList);
        return result;
    }

    /** Find field */
    private Field findField(UjoProperty p, List<Field> fields) throws Exception {
        for (Field field : fields) {
            if (field.get(null) == p) {
                return field;
            }
        }
        throw new IllegalStateException("Can't find a field: " + p);
    }

    /** Find field */
    private List<Field> getFields() {
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
        final Property<UJO,T> p = Property.newInstance(name, null, defaultValue, propertyList.size(), false);
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
        final ListProperty<UJO,T> p = ListProperty.newListProperty(name, null, propertyList.size(), false);
        propertyList.add(p);
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
        return getInstance(baseClass, PropertyFactory.class);
    }

    /** Returns new factory instance along the parameter class {@code factory}
     * @param baseClass base class
     * @param factory New implementaton of the factory with consturctor parameter type of {@code Class<UJO>}.
     * @throws IllegalArgumentException
     */
    public static <UJO extends Ujo, T extends PropertyFactory<UJO>> T getInstance(Class<UJO> baseClass, Class<T> factory) throws IllegalArgumentException {
        try {
            return factory.getConstructor(Class.class).newInstance(factory);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't create instence of the factory " + factory, e);
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

}
