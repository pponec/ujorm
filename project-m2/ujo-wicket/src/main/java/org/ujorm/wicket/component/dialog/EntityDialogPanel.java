/*
 * Copyright 2013, Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.wicket.component.dialog;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.ujorm.Ujo;
import org.ujorm.wicket.component.form.FieldProvider;
import org.ujorm.wicket.component.form.fields.FeedbackField;

/**
 * Entity Dialog Content
 * @author Pavel Ponec
 */
public class EntityDialogPanel<T extends Ujo> extends AbstractDialogPanel<T> {
    private static final long serialVersionUID = 20130621L;

    /** Input fields provider */
    protected final FieldProvider<T> fields;
    private final FeedbackField feedbackField;

    public EntityDialogPanel(ModalWindow modalWindow, IModel<T> model, boolean autoClose) {
        super(modalWindow, model, autoClose);
        // Create a feedback:
        repeater.add(feedbackField = new FeedbackField(repeater.newChildId()));
        // Create a field factory:
        form.add((fields = new FieldProvider(repeater)).getRepeatingView());
    }

    /** Input fields provider
     * @see #fields
     */
    public FieldProvider<T> getFields() {
        return fields;
    }

    /**
     * Show dialog and assign a data from domain object
     * @param title Dialog title
     * @param body Dialog body
     * @param actionButtonProperty Action button key
     * @param target Target
     */
    @Override
    public void show(AjaxRequestTarget target, IModel<String> title, IModel<T> body, String actionButtonProperty) {
        fields.setDomain(body.getObject());
        super.show(target, title, body, actionButtonProperty);
    }

    /** Returns a base model object / entity */
    @Override
    public T getBaseModelObject() {
        return fields.getDomain();
    }

    /** {@inheritDoc} */
    @Override
    protected void setFeedback(IModel<String> message) {
        feedbackField.setFeedbackMessage(message);
    }

}
