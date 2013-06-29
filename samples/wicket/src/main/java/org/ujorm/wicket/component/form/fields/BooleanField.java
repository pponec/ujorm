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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.ujorm.Key;

/**
 * CheckBox field with a Label includding a feedback message.
 * @author Pavel Ponec
 */
public class BooleanField extends Field {

    private static final long serialVersionUID = 20130621L;

    public BooleanField(Key property) {
        super(property.getName(), property, null);
    }

    public BooleanField(String componentId, Key property, String cssClass) {
        super(componentId, property, cssClass);
    }

    /** Create Form inputComponent */
    protected FormComponent createInput(String componentId, IModel model) {
        FormComponent result = super.createInput(componentId, model);
        result.add(new AttributeModifier("type", "checkbox"));
        return result;
    }
}
