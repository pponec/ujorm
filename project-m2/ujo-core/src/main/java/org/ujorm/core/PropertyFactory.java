/*
 * Copyright 2012 ponec.
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.Ujo;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.extensions.AbstracCollectionProperty;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.PropertyModifier;

/**
 * Non serializable property factory
 * @author ponec
 */
public class PropertyFactory<UJO extends Ujo> implements Serializable {

    /** Transient property list */
    transient private List<UjoProperty<UJO, ?>> propertyList;

    /** Property Store */
    private UjoPropertyList<UJO> propertyStore;

    /** The Ujo type is serializad */
    private Class<UJO> type;

    public PropertyFactory(Class<UJO> type) {
        this.type = type;
        this.propertyList = new ArrayList<UjoProperty<UJO, ?>>();
        try {
            final Class superClass = type.getSuperclass();
            if (Ujo.class.isAssignableFrom(superClass)) {
                for (UjoProperty p : ((Ujo) superClass.newInstance()).readProperties()) {
                    propertyList.add(p);
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Can§§'t create instance of " + type.getSuperclass());
        }
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

    /** Lock the factory */
    public void lock() {
        getPropertyList();
    }

    /** Create a property List */
    protected UjoPropertyList<UJO> createPropertyList() {
        final List<Field> fields = getFields();
        try {
            for (UjoProperty<UJO, ?> p : propertyList) {
                Field field = findField(p, fields);
                if (p.getName() == null) {
                    PropertyModifier.setName(field.getName(), (Property) p);
                }
                if (p.getType() == null) {
                    PropertyModifier.setType(UjoManager.getGenericClass(field, 1), (Property) p);
                }
                if (p instanceof AbstracCollectionProperty) {
                    final AbstracCollectionProperty lp = (AbstracCollectionProperty) p;
                    if (lp.getItemType() == null) {
                        PropertyModifier.setItemType(UjoManager.getGenericClass(field,1), lp);
                    }
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
    public <T> Property<UJO,T> newProperty() {
        return newProperty(null, null);
    }

    /** Create new UjoProperty */
    public <T> Property<UJO,T> newProperty(String name) {
        return newProperty(name, null);
    }

    /** Create new UjoProperty */
    public <T> Property<UJO,T> newPropertyDef(T defaultValue) {
        return newProperty(null, defaultValue);
    }

    /** Create new UjoProperty */
    public <T> Property<UJO,T> newProperty(String name, T defaultValue) {
        if (propertyStore!=null) {
            throw new IllegalStateException("Factory s locked");
        }
        final Property<UJO,T> p = Property.newInstance(name, null, defaultValue, propertyList.size(), false);
        propertyList.add(p);
        return p;
    }

    /** An event on Create */
    protected void onCreate(UjoPropertyList<UJO> list) {
        // UjoManager.getInstance().init(list);
    }

    /* ================== STATIC METHOD ================== */

    public static <UJO extends Ujo> PropertyFactory<UJO> getInstance(Class baseClass) {
        return new PropertyFactory<UJO>(baseClass);
    }

}
