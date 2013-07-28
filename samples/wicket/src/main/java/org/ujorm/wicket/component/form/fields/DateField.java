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
package org.ujorm.wicket.component.form.fields;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.ujorm.Key;
import org.ujorm.wicket.CssAppender;

/**
 * CheckBox field with a Label includding a feedback message.
 * @author Pavel Ponec
 */
public class DateField extends Field {
    private static final long serialVersionUID = 20130621L;
    /** Default CSS class have got value {@code datepicker} */
    public static final String CSS_DATEPICKER = "datePickerComponent";

    public DateField(Key property) {
        super(property.getName(), property, null);
    }

    public DateField(String componentId, Key property, String cssClass) {
        super(componentId, property, cssClass);
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(String componentId, IModel model) {
        final DateTextField result = new DateTextField(componentId, model, getDatePattern());
        result.add(new CssAppender(getInputCssClass()));

        if (validator != null) {
            IValidator<? super java.util.Date> dateValidator = (IValidator<? super java.util.Date>) validator;
            result.add(dateValidator);
            addMaxLength(result);
        }

        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        return result;
    }

    /** Returns localizadDate pattern */
    protected String getDatePattern() {
        return getString("locale.date.pattern", null, "yyyy-MM-dd");
    }

    /** Returns an {@code input} value from model */
    @Override
    public Object getModelValue() {
        Object result = super.getModelValue();
        return getKey().isTypeOf(java.sql.Date.class)
             ? new java.sql.Date(((java.util.Date)result).getTime())
             : result ;
    }

    /** Default CSS class have got value {@code datepicker} */
    protected String getInputCssClass() {
        return CSS_DATEPICKER;
    }
}
