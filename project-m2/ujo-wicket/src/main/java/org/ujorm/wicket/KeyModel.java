/*
 *  Copyright 2012-2012 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.wicket;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;

public class KeyModel<UJO extends Ujo, T> implements IModel<T> {

    private static final long serialVersionUID = 1L;
    /** Object model */
    private final UJO modelObject;
    /** Property expression for property access. */
    private final KeyRing<UJO> property;

    /**
     * Protected Construct with a wrapped (IModel) or unwrapped (non-IModel) object and a property expression
     * that works on the given model. To create instance use the method {@link #of(org.ujorm.Ujo, org.ujorm.Key of(...)) }
     *
     * @param modelObject The model object, which may or may not implement IModel
     * @param property Instance of the Key
     * @see #of(org.ujorm.Ujo, org.ujorm.Key) 
     */
    @SuppressWarnings("unchecked")
    protected KeyModel(final UJO modelObject, final Key<UJO, T> property) {
        this.modelObject = modelObject;
        this.property = KeyRing.of(property);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return property.toString();
    }

    /**
     * Rerurn Key
     */
    @SuppressWarnings("unchecked")
    public Key<UJO, T> getProperty() {
        return (Key<UJO, T>) property.getFirstKey();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        return (T) property.getFirstKey().of((UJO) modelObject);
    }

    @Override
    public void setObject(T object) {
        @SuppressWarnings("unchecked")
        final Key<UJO,Object> key = (Key<UJO,Object>) this.property.getFirstKey();
        key.setValue((UJO) modelObject, object);
    }

    /** Return a class of the base domainObject */
    public Class<? super UJO> getBaseClass() {
        return property.getType();
    }

    @Override
    public void detach() {
    }

    // ------ STATIDC ------

    /**
     * Type-infering factory method
     * @param parent object that contains the property
     * @param property property path
     * @return {@link PropertyModel} instance
     */
    public static <UJO extends Ujo, T> KeyModel<UJO, T> of(UJO parent, Key<UJO, T> property) {
        return new KeyModel<UJO, T>(parent, property);
    }

    /**
     * Type-infering factory method
     * @param parent object that contains the property
     * @param property the first property (path)
     * @return {@link PropertyModel} instance
     */
    @SuppressWarnings("unchecked")
    public static <UJO extends Ujo, T> KeyModel<UJO, T> of(IModel<UJO> parent, KeyRing<UJO> property) {
        return (KeyModel<UJO, T>) of(parent.getObject(), property.getFirstKey());
    }
}
