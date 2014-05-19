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

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.ujorm.Key;

/**
 * Text field for a password including a feedback message.
 * @author Pavel Ponec
 */
public class PasswordField extends Field {

    private static final long serialVersionUID = 20130621L;

    public PasswordField(Key key) {
        super(key.getName(), key, null);
    }

    public PasswordField(String componentId, Key key, String cssClass) {
        super(componentId, key, cssClass);
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(String componentId, IModel model) {
        final FormComponent result = new PasswordTextField(componentId, model);

        if (validator != null) {
            result.add(validator);
            addMaxLength(result);
        }

        result.setRequired(false);
        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        return result;

    }
}
