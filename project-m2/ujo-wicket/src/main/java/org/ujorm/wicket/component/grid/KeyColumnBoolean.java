/*
 *  Copyright 2013-2014 Pavel Ponec
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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.wicket.CssAppender;

/**
 * Key column for a boolean data type
 * @author Pavel Ponec
 * @param <U extends Ujo>
 *            The Model object type
 */
public class KeyColumnBoolean<U extends Ujo> extends KeyColumn<U, Boolean> {

    private static final long serialVersionUID = 1L;

    /** Default CSS class for an OK value */
    protected static String DEFAULT_LOGICAL_CLASS = "logical";
    /** Default CSS class for an {@code true} value */
    protected static String DEFAULT_CSS_AFFIRMATIVE_CLASS = "ok";

    /** Class for the TRUE value */
    protected final String cssAffirmativeClass;

    public KeyColumnBoolean(Key<U,?> key, Key<U,?> keySortable, String cssClass) {
        this(KeyRing.<U>of(key), KeyRing.<U>of(keySortable), cssClass, DEFAULT_CSS_AFFIRMATIVE_CLASS);
    }

    public KeyColumnBoolean(KeyRing<U> key, KeyRing<U> keySortable, String cssClass, String cssAffirmativeClass) {
        super(key, keySortable);
        setCssClass(cssClass);
        this.cssAffirmativeClass = cssAffirmativeClass;
    }

    /** Create a Value component */
    @Override
    protected IModel<?> createValueModel(final U ujo) {
        final Object value = keySerializable.getFirstValue(ujo);
        String result = value == null
                ? ""
                : Boolean.TRUE.equals(value)
                ? "ok"
                : "-";
        return Model.of(result);
    }

    /** Append the CSS class */
    @Override
    protected void appendCssClass(Component value, U ujo) {
        super.appendCssClass(value, ujo);

        if (cssAffirmativeClass != null
        &&  Boolean.TRUE.equals(keySerializable.getFirstValue(ujo))) {
            value.add(new CssAppender(cssAffirmativeClass));
        }
    }

    /** Returns a CSS class of the {@code true} value */
    public String getCssAffirmativeClass() {
        return cssAffirmativeClass;
    }

    // =============== STATIC FACTORY METHODS ===============

    /**
     * A factory method where a default key {@link #getCssAffirmativeClass()} have got value {@link #DEFAULT_CSS_AFFIRMATIVE_CLASS}
     * @param key Domain Key
     * @param sorted A sorting request
     * @return New instance of the KeyColumn class
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, boolean sorted) {
        return of(key, sorted, DEFAULT_CSS_AFFIRMATIVE_CLASS);
    }

    /**
     * A factory method
     * @param key Domain Key
     * @param sorted Sorted column
     * @param cssAffirmativeClass Class for a value {@code Boolean.{@code true}} where a default value is {@link #DEFAULT_CSS_AFFIRMATIVE_CLASS}.
     * @return New instance of the KeyColumn class
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, boolean sorted, String cssTrueClass) {
        final KeyRing serializableKey = KeyRing.of(key);
        return new KeyColumnBoolean
                ( serializableKey
                , sorted ? serializableKey : null
                , DEFAULT_LOGICAL_CLASS
                , cssTrueClass);
    }

    /**
     * A factory method
     * @param key Domain Key
     * @param sort Key of sorting
     * @param cssAffirmativeClass Class for a value {@code Boolean.TRUE} where a default value is {@link #DEFAULT_CSS_AFFIRMATIVE_CLASS}.
     * @return
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, Key<U,T> sort, String cssTrueClass) {
        return new KeyColumnBoolean
                ( KeyRing.of(key)
                , KeyRing.of(sort)
                , DEFAULT_LOGICAL_CLASS
                , cssTrueClass);
    }


}
