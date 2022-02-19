/*
 *  Copyright 2014-2022 Pavel Ponec
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jetbrains.annotations.Nullable;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.wicket.component.tools.DateTimes;

/**
 * Key column for a Date data type
 * @author Pavel Ponec
 * @param <U extends Ujo> The Model object type
 */
public class KeyColumnLocalDateTime<U extends Ujo> extends KeyColumn<U, LocalDateTime> {
    private static final long serialVersionUID = 1L;

    /** Default CSS class for  */
    public static String DEFAULT_CSS_CLASS = "date";

    /** Default 'date' format key for localizations */
    private final String localeDateFormatKey;

    /** Constructor with the default value localeDateFormatKey = {@code "locale.date.pattern"}. */
    public KeyColumnLocalDateTime(KeyRing<U> key, KeyRing<U> keySortable) {
        this(key, keySortable, DateTimes.LOCALE_DATE_FORMAT_KEY);
    }

    public KeyColumnLocalDateTime(KeyRing<U> key, KeyRing<U> keySortable, String localeDateFormatKey) {
        super(key, keySortable);
        this.localeDateFormatKey = localeDateFormatKey;
        setCssClass(DEFAULT_CSS_CLASS);
    }

    /** Create the Label for a Value component */
    @Override
    @Nullable
    protected Component createValueCoponent(final String componentId, final IModel<?> valueModel, final U ujo) {
        final Label result = new Label(componentId);
        result.setDefaultModel(new Model() {
            @Override public Serializable getObject() {
                final LocalDateTime modelObject = (LocalDateTime) valueModel.getObject();
                if (modelObject != null) {
                    final DateTimeFormatter form = DateTimeFormatter.ofPattern(getDatePattern(result));
                    return form.format(modelObject);
                } else {
                    return null;
                }
            }
        });
        return result;
    }

    /** Returns localizadDate pattern */
    protected String getDatePattern(final Component component) {
        return component.getLocalizer().getString(localeDateFormatKey, component, DateTimes.getDefaultPattern(localeDateFormatKey));
    }

    // =============== STATIC FACTORY METHODS ===============

    /**
     * A factory method
     * @param key Domain Key
     * @param sorted Sorted column
     * @param cssClass Class for a value {@code Boolean.TRUE} where a default value is {@link #DEFAULT_CSS_OK_CLASS}.
     * @return New instance of the KeyColumn class
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, boolean sorted, String cssClass) {
        return of(key, sorted, cssClass, DateTimes.LOCALE_DATE_FORMAT_KEY);
    }

    /**
     * A factory method
     * @param key Domain Key
     * @param sorted Sorted column
     * @param cssClass Class for a value {@code Boolean.TRUE} where a default value is {@link #DEFAULT_CSS_OK_CLASS}.
     * @return New instance of the KeyColumn class
     */
    public static <U extends Ujo, T> KeyColumn<U,T> of(Key<U,T> key, boolean sorted, String cssClass, String localeDateFormatKey) {
        final KeyRing serializableKey = KeyRing.of(key);
        final KeyColumn<U, T> result = new KeyColumnLocalDateTime
                ( serializableKey
                , sorted ? serializableKey : null
                , localeDateFormatKey);
        result.setCssClass(cssClass);
        return result;
    }

}
