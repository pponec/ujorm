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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Common Feedback Field
 * @author Pavel Ponec
 */
public class FeedbackField<T> extends Field<T> {

    private static final long serialVersionUID = 20130621L;

    @SuppressWarnings("unchecked")
    public FeedbackField(String id) {
        super(id, null, null);
        setDefaultModel(new Model(""));
        setOutputMarkupPlaceholderTag(true);
        add(new AjaxButton("closeButton") {
            @Override protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                feedback.setFeedbackMessage(null);
                target.add(FeedbackField.this);
            }
        });
    }

    @Override
    public void onConfigure() {
        super.onConfigure();
        setVisibilityAllowed(feedback.isFeedbackMessage());
    }

    /** Create Label and assign the CSS class {@code required} for the mandatory Field */
    @Override
    protected Component createLabel(final Component inp) {
        final Label result = new Label("label");
        result.setVisibilityAllowed(false);
        return result;
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(String componentId, IModel<T> model) {
        final HiddenField<String> result = new HiddenField<String>(componentId, (IModel) model, String.class);
        result.setLabel(new Model(""));
        return result;
    }

    @Override
    public void setFeedbackMessage(IModel<String> message) {
        // setVisibleAllowed(message != null); // It does not work
        if (feedback != null) {
            feedback.setFeedbackMessage(message);
        }
    }

    /** Returns a main CSS class */
    @Override
    protected String getCssClass() {
        return super.getCssClass() + " alert absolute-alert";
    }

}
