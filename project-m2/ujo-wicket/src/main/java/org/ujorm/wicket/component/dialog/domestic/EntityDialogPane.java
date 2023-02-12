/*
 * Copyright 2013-2014, Pavel Ponec
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
package org.ujorm.wicket.component.dialog.domestic;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.ujorm.Ujo;
import org.ujorm.wicket.component.form.FieldProvider;
import org.ujorm.wicket.component.form.FieldProviderFactory;
import org.ujorm.wicket.component.form.fields.FeedbackField;

/**
 * Entity Dialog Content
 * @author Pavel Ponec
 */
public class EntityDialogPane<U extends Ujo> extends AbstractDialogPane<U> {
    private static final long serialVersionUID = 20130621L;

    /**
     * Input fields provider
     * @see Use the method {@link #getFieldBuilder()}
     */
    private final FieldProvider<U> fields;
    /** Feedback Field */
    private final FeedbackField feedbackField;

    /** Constructor with an enabled autoClosing */
    public EntityDialogPane(ModalWindow modalWindow, IModel<? super U> model) {
        this(modalWindow, model, true);
    }

    /** Common constructor */
    public EntityDialogPane(ModalWindow modalWindow, IModel<? super U> model, boolean autoClose) {
        this(modalWindow, model, new FieldProviderFactory<U>(), autoClose);
    }

    /** Common constructor */
    public EntityDialogPane(ModalWindow modalWindow, IModel<? super U> model, FieldProviderFactory<U> fieldProviderFactory, boolean autoClose) {
        super(modalWindow, model, autoClose);
        // Create a feedback:
        feedbackField = fieldProviderFactory.createDefaultFeedbackField(repeater);
        repeater.add(feedbackField);
        // Create a field factory:
        this.fields = fieldProviderFactory.createDefaultFieldProvider(repeater);
        form.add(fields.getRepeatingView());
    }

    /** Input fields provider
     * @see #fields
     * @deprecated Use the {@link #getFieldBuilder() } rather
     */
    @Deprecated
    public final FieldProvider<U> getFields() {
        return getFieldBuilder();
    }

    /** Input fields provider
     * @see #fields
     */
    public FieldProvider<U> getFieldBuilder() {
        return fields;
    }

    /** An action in show before to assign a data from domain object */
    @Override
    protected void onShowBefore(AjaxRequestTarget target) {
        super.onShowBefore(target);
        fields.setDomain(getModelObject());
        fields.requestFocus(target);
    }

//  public void show(AjaxRequestTarget target, IModel<String> title, IModel<U> body, String actionButtonProperty) {
//      fields.setDomain(body.getObject());
//      fields.requestFocus(target);
//      super.show(target, title, body, actionButtonProperty);
//  }

    /** Returns a base model object / entity */
    @Override
    public U getBaseModelObject() {
        return fields.getDomain();
    }

    /** {@inheritDoc} */
    @Override
    protected void setFeedback(IModel<String> message) {
        feedbackField.setFeedbackMessage(message);
    }

}
