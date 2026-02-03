/*
 *  Copyright 2014-2026 Pavel Ponec
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
package org.ujorm.wicket.component.grid;

import java.io.Serializable;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.wicket.component.tools.LocalizedModel;

/**
 * Key column for a Date data type
 * @author Pavel Ponec
 * @param <U> Ujo
 * @param <T> Enum type
 */
public class KeyColumnEnum<U extends Ujo, T extends Enum<T>> extends KeyColumn<U, T> {
    private static final long serialVersionUID = 1L;

    public KeyColumnEnum(KeyRing<U> key, KeyRing<U> keySortable) {
        super(key, keySortable);
    }

    /** Create the Label for a Value component */
    @Override
    protected Component createValueCoponent(final String componentId, final IModel<?> valueModel, final U ujo) {
        final Label result = new Label(componentId);
        result.setDefaultModel(new Model() {
            @Override public Serializable getObject() {
                final T value = (T) valueModel.getObject();
                final String resourceKey = resourceKey(value);
                final String defaultValue = value != null ? value.name() : "";
                return result.getString(resourceKey, valueModel, defaultValue);
            }
        });
        return result;
    }

    /**
     * Translates the {@code object} into resource key that will be used to lookup the value shown
     * to the user
     * @param value
     * @return resource key
     */
    protected String resourceKey(T value) {
        return value != null
             ? value.getClass().getSimpleName() + '.' + value.name()
             : ("value." + LocalizedModel.getSimpleKeyName(getKey()) + ".null");
    }

    // =============== STATIC FACTORY METHODS ===============

    /**
     * A factory method
     * @param key Domain Key
     * @param sorted Sorted column
     * @return New instance of the KeyColumn class
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, boolean sorted, String cssClass) {
        final KeyRing serializableKey = KeyRing.of(key);
        final KeyColumnEnum result = new KeyColumnEnum
                ( serializableKey
                , sorted ? serializableKey : null);
        result.setCssClass(cssClass);
        return result;
    }

    /**
     * A factory method
     * @param key Domain Key
     * @param sort Key of sorting
     * @return
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, Key<U,T> sort, String cssClass) {
        final KeyColumnEnum result = new KeyColumnEnum
                ( KeyRing.of(key)
                , KeyRing.of(sort));
        result.setCssClass(cssClass);
        return result;
    }
}
