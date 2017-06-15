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

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.wicket.component.form.FieldEvent;

/**
 * CheckBox field with a Label including a feedback message.
 * @author Pavel Ponec
 */
public class BooleanField<T extends Boolean> extends Field<T> {

    private static final long serialVersionUID = 20130621L;

    public <U extends Ujo> BooleanField(Key<U,T> key) {
        super(key.getName(), key, null);
    }

    public <U extends Ujo> BooleanField(String componentId, Key<U,T> key, String cssClass) {
        super(componentId, key, cssClass);
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(final String componentId, final IModel<T> model) {
        final CheckBox result = new CheckBox(componentId, (IModel) model);
        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        return result;
    }

    /** Create an "onchange" event */
    @Override
    public void onChange(final String action) {
        addBehaviour(createChangeBehaviour(action, "change"));
    }

    /** Create an AjaxFormComponentUpdatingBehavior with no delay. */
    @Override
    protected AjaxEventBehavior createChangeBehaviour(final String action, final String jsEvent) {
        return new AjaxFormComponentUpdatingBehavior(jsEvent) {
            @Override protected void onUpdate(AjaxRequestTarget target) {
                send(BooleanField.this, Broadcast.BUBBLE, new FieldEvent(action, key, target));
            }
        };
    }
}
