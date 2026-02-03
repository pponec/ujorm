/*
 *  Copyright 2017-2026 Pavel Ponec
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
package org.ujorm.wicket.component.form.fields;

import java.time.LocalDateTime;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.tools.DateTimes;

/**
 * Local datetime field with a Label including a feedback message.
 * @author Pavel Ponec
 */
public class LocalDateTimeField<T> extends Field<T> {

    public <U extends Ujo> LocalDateTimeField(Key<U,T> key) {
        super(key.getName(), key, null);
    }

    public <U extends Ujo> LocalDateTimeField(String componentId, Key<U,T> key, String cssClass) {
        super(componentId, key, cssClass);
    }

    /** Create Form inputComponent */
    @Override
    @SuppressWarnings("unchecked")
    protected FormComponent createInput(final String componentId, final IModel<T> model) {
        final org.apache.wicket.markup.html.form.TextField<LocalDateTime> result
                = new org.apache.wicket.markup.html.form.TextField<>(componentId, LocalDateTime.class);
        result.add(new CssAppender(getInputCssClass()));
        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        result.setDefaultModel(Model.of());
        return result;
    }

    /** Default CSS class have got value {@code datepicker} */
    protected String getInputCssClass() {
        return "localDateTime";
    }

    /** Returns localizadDate pattern */
    protected String getDatePattern() {
        final String key = DateTimes.LOCALE_DATETIME_FORMAT_KEY;
        return getLocalizer().getString(key, this, DateTimes.getDefaultPattern(key));
    }
}
