/*
 *  Copyright 2017 Pavel Ponec
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

import com.googlecode.wicket.jquery.core.Options;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.tools.DateTimes;

/**
 * LocalDate field with a Label including a feedback message.
 * @author Pavel Ponec
 */
public class LocalDateField<T> extends Field<T> {
    private static final long serialVersionUID = 2016_06_15L;
    /** Default CSS class have got value {@code datepicker} */
    public static final String CSS_DATEPICKER = "datePickerComponent";

    public <U extends Ujo> LocalDateField(Key<U,T> key) {
        super(key.getName(), key, null);
    }

    public <U extends Ujo> LocalDateField(String componentId, Key<U,T> key, String cssClass) {
        super(componentId, key, cssClass);
    }

    /** Create Form inputComponent */
    @Override
    @SuppressWarnings("unchecked")
    protected FormComponent createInput(final String componentId, final IModel<T> model) {
        final DateTextField result = new com.googlecode.wicket.jquery.ui.form.datepicker.AjaxDatePicker
                (componentId, (IModel) model, getDatePattern(), createJQueryOptions());
        result.add(new CssAppender(getInputCssClass()));

        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        return result;
    }

    /** Returns localizadDate pattern */
    protected String getDatePattern() {
        final String key = DateTimes.LOCALE_DATE_FORMAT_KEY;
        return getLocalizer().getString(key, this, DateTimes.getDefaultPattern(key));
    }

    /** Default CSS class have got value {@code datepicker} */
    protected String getInputCssClass() {
        return CSS_DATEPICKER;
    }

    /** Create jQuery options: */
    protected Options createJQueryOptions() {
        final Options options = new Options();
      options.set("dateFormat", "'" + new DateConverter().toJQueryUIDateFormat(getDatePattern()) + "'");
      //options.set("showButtonPanel", "true");
      return options;
    }
}
