/*
 *  Copyright 2013 Pavel Ponec
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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;

/**
 * Key column for a boolean data type
 * @author Pavel Ponec
 * @param <UJO extends Ujo>
 *            The Model object type
 */
public class KeyColumnBoolean<UJO extends Ujo> extends KeyColumn<UJO, Boolean> {

    private static final long serialVersionUID = 1L;

    /** Default CSS class for an OK value */
    protected static String DEFAULT_LOGICAL_CLASS = "logical";
    /** Default CSS class for an OK value */
    protected static String DEFAULT_CSS_OK_CLASS = "ok";

    /** Class for OK value */
    protected final String cssOkClass;

    public KeyColumnBoolean(Key<UJO,?> key, Key<UJO,?> keySortable, String cssClass) {
        this(KeyRing.of(key), KeyRing.of(keySortable), cssClass, DEFAULT_CSS_OK_CLASS);
    }

    public KeyColumnBoolean(KeyRing<UJO> key, KeyRing<UJO> keySortable, String cssClass, String cssOkClass) {
        super(key, keySortable, cssClass);
        this.cssOkClass = cssOkClass;
    }

    /** Create a Value component */
    @Override
    protected IModel<?> createValueModel(final UJO ujo) {
        final Object value = keySerializable.getFirstValue(ujo);
        String result = value == null
                ? ""
                : Boolean.TRUE.equals(value)
                ? "ok"
                : "-";
        return Model.of(result);
    }

    /** Append css class */
    @Override
    protected void appendCssClass(Component value, UJO ujo) {
        super.appendCssClass(value, ujo);

        if (cssOkClass != null
        &&  Boolean.TRUE.equals(keySerializable.getFirstValue(ujo))) {
            value.add(new AttributeAppender("class", new Model(cssOkClass), " "));
        }
    }

    /** Returns a CSS class of the {@code true} value */
    public String getCssOkClass() {
        return cssOkClass;
    }

    // =============== STATIC FACTORY METHODS ===============

    /**
     * A factory method where a default property {@link #getCssOkClass()} have got value {@link #DEFAULT_CSS_OK_CLASS}
     * @param key Domain Key
     * @param sorted A sorting request
     * @return New instance of the KeyColumn class
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, boolean sorted) {
        return of(key, sorted, DEFAULT_CSS_OK_CLASS);
    }

    /**
     * A factory method
     * @param key Domain Key
     * @param sorted Sorted column
     * @param cssOkClass Class for a value {@code Boolean.TRUE} where a default value is {@link #DEFAULT_CSS_OK_CLASS}.
     * @return New instance of the KeyColumn class
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, boolean sorted, String cssOkClass) {
        final KeyRing serializableKey = KeyRing.of(key);
        return new KeyColumnBoolean
                ( serializableKey
                , sorted ? serializableKey : null
                , DEFAULT_LOGICAL_CLASS
                , cssOkClass);
    }

    /**
     * A factory method
     * @param key Domain Key
     * @param sort Key of sorting
     * @param cssOkClass Class for a value {@code Boolean.TRUE} where a default value is {@link #DEFAULT_CSS_OK_CLASS}.
     * @return
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, Key<U,T> sort, String cssOkClass) {
        return new KeyColumnBoolean
                ( KeyRing.of(key)
                , KeyRing.of(sort)
                , DEFAULT_LOGICAL_CLASS
                , cssOkClass);
    }

    
}
