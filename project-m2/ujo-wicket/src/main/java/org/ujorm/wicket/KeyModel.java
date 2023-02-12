/*
 *  Copyright 2012-2022 Pavel Ponec
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

import org.jetbrains.annotations.NotNull;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;

public class KeyModel<U extends Ujo, T> implements IModel<T> {

    private static final long serialVersionUID = 1L;
    /** Object model */
    private final U domain;
    /** Serializable key. */
    private final KeyRing<U> key;

    /**
     * Protected Construct with a wrapped (IModel) or unwrapped (non-IModel) object and a key expression
     * that works on the given model. To create instance use the method {@link #of(org.ujorm.Ujo, org.ujorm.Key of(...)) }
     *
     * @param modelObject The model object, which may or may not implement IModel
     * @param key Instance of the Key
     * @see #of(org.ujorm.Ujo, org.ujorm.Key)
     */
    @SuppressWarnings("unchecked")
    protected KeyModel(final U modelObject, final Key<U, T> key) {
        this.domain = modelObject;
        this.key = KeyRing.of(key);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return key.toString();
    }

    /** Rerurn Key
     * @deprecated Use the {@link #getKey()}.
     */
    @Deprecated
    public final Key<U, T> getProperty() {
        return getKey();
    }

    /**
     * Rerurn Key
     */
    @SuppressWarnings("unchecked")
    public final Key<U, T> getKey() {
        return key.getFirstKey();
    }

    @Override
    public T getObject() {
        return getKey().of(domain);
    }

    @Override
    public void setObject(final T object) {
        getKey().setValue(domain, object);
    }

    /** Return a class of the base domainObject */
    public Class<? super U> getBaseClass() {
        return key.getType();
    }

    @Override
    public void detach() {
    }

    // ------ STATIC ------

    /**
     * Type-infering factory method
     * @param parent object that contains the key
     * @param key key path
     * @return {@link PropertyModel} instance
     */
    public static <U extends Ujo, T> KeyModel<U, T> of(@NotNull final U parent, @NotNull final Key<U, T> key) {
        return new KeyModel<U, T>(parent, key);
    }

    /**
     * Type-infering factory method
     * @param domain object that contains the key
     * @param key the first key (path)
     * @return {@link PropertyModel} instance
     */
    @SuppressWarnings("unchecked")
    public static <U extends Ujo, T> KeyModel<U, T> of(@NotNull final IModel<U> domain, @NotNull final KeyRing<U> key) {
        return of(domain.getObject(), key.getFirstKey());
    }

    /**
     * Create a Wicket model from a domain model type of Ujo or JavaBean.
     * @param <T> The Model object type
     * @param bo A model business object
     * @param key Property expression for key access
     * @return An instance type of PropertyModel or KeyModel according to the [@code bo} parameter type.
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("unchecked")
    public static <T> IModel<T> of(Object bo, String key) throws IllegalArgumentException {
        if (bo instanceof Ujo) {
            final Ujo ubo = (Ujo) bo;
            final Key k = ubo.readKeys().find(key);
            return (IModel<T>) KeyModel.of(ubo, k);
        } else {
            return new PropertyModel<T>(bo, key);
        }
    }
}
