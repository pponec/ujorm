/*
 *  Copyright 2012-2013 Pavel Ponec
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
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;

public class KeyModel<UJO extends Ujo, T> implements IModel<T> {

    private static final long serialVersionUID = 1L;
    /** Object model */
    private final UJO modelObject;
    /** Serializable key. */
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

    /** Rerurn Key
     * @deprecated Use the {@link #getKey()}.
     */    
    @Deprecated
    final public Key<UJO, T> getProperty() {
        return getKey();
    }
    
    /**
     * Rerurn Key
     */
    @SuppressWarnings("unchecked")
    final public Key<UJO, T> getKey() {
        return (Key<UJO, T>) property.getFirstKey();
    }

    @Override
    public T getObject() {
        return getKey().of(modelObject);
    }

    @Override
    public void setObject(T object) {
        getKey().setValue(modelObject, object);
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
    
    /**
     * Create a Wicket model from a domain model type of Ujo or JavaBean.
     * @param <T> The Model object type
     * @param bo A model business object
     * @param property Property expression for property access
     * @return An instance type of PropertyModel or KeyModel according to the [@code bo} parameter type. 
     * @throws IllegalArgumentException 
     */
    @SuppressWarnings("unchecked")
    public static <T> IModel<T> of(Object bo, String property) throws IllegalArgumentException {
        if (bo instanceof Ujo) {
            final Ujo ubo = (Ujo) bo;
            final Key key = ubo.readKeys().find(property);
            return (IModel<T>) KeyModel.of(ubo, key);
        } else {
            return new PropertyModel<T>(bo, property);
        }
    }    
}
