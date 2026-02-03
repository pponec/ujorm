/*
 *  Copyright 2008 Daan, StuQ.nl
 *  Copyright 2014-2026 Pavel Ponec
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
 *
 */
package org.ujorm.wicket.component.form;

import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Label displaying feedback messages for FormComponents.
 * <p>
 * You can use this Label to show the error message near the actual FormComponent, instead of in the FeedbackPanel
 * It's safe to remove the FeedbackPanel if you use this class for every FormComponent in your Form.
 * <p>
 * You can use this code under Apache 2.0 license, as long as you retain the copyright messages.
 *
 * Tested with Wicket 1.3.4
 * @author Daan, StuQ.nl, modified by Pavel Ponec
 */
public class FeedbackLabel extends MultiLineLabel {

    /** Field component holds a reference to the {@link Component} this FeedbackLabel belongs to */
    private final Component input;
    /** Field text holds a model of the text to be shown in the FeedbackLabel */
    private final IModel text;

    /**
     * Call this constructor if you just want to display the FeedbackMessage of the component.
     * The constructor set the attribute {@link #setOutputMarkupId(boolean)} using the value {@code true}.
     * @param id The non-null id of this component
     * @param input The {@link FormComponent} to show the FeedbackMessage for.
     */
    public FeedbackLabel(String id, Component input) {
        this(id, input, (IModel) null);
    }

    /**
     * Call this constructor if you want to display a custom text
     * @param id The non-null id of this component
     * @param component The {@link FormComponent} to show the custom text for.
     * @param text The custom nullable text to show when the FormComponent has a FeedbackMessage
     */
    public FeedbackLabel(String id, Component component, String text) {
        this(id, component, new Model(text));
    }

    /**
     * Call this constructor if you want to display a custom model (for easy i18n)
     * @param id The non-null id of this component
     * @param component The {@link FormComponent} to show the custom model for.
     * @param iModel The custom nullable model to show when the {@link FormComponent} has a FeedbackMessage
     */
    public FeedbackLabel(String id, Component input, IModel iModel) {
        super(id);
        this.input = input;
        this.text = iModel;
        setOutputMarkupPlaceholderTag(true);
    }

    /**
     * Set the content of this FeedbackLabel, depending on if the component has a FeedbackMessage.
     *
     * The HTML class attribute will be filled with the error level of the feedback message. That way, you can easily
     * style different messages differently. Examples:
     *
     * class = "INFO"
     * class = "ERROR"
     * class = "DEBUG"
     * class = "FATAL"
     *
     * @see Component
     */
    @Override
    protected void onConfigure() {
        super.onConfigure();
        final boolean visible = input.hasFeedbackMessage();
        super.setVisibilityAllowed(visible);
        if (visible) {
            if (this.text != null) {
                this.setDefaultModel(text);
            } else {
                this.setDefaultModel(new Model(input.getFeedbackMessages().first().getMessage()));
            }
            // Assign an error level as a CSS class: (wrong implementation)
            // final String css = input.getFeedbackMessages().first().getLevelAsString();
            // this.add(new CssAppender(css));
        } else {
            this.setDefaultModel(null);
        }
    }

    /**  Insert feedback message */
    public void setFeedbackMessage(IModel<String> message) {
        if (message != null) {
            input.getFeedbackMessages().add(new FeedbackMessage(input, message.getObject(), 0));
        } else {
            input.getFeedbackMessages().clear();
        }
    }

    /** Have got the Feedback any message ? */
    public boolean isFeedbackMessage() {
        return !input.getFeedbackMessages().isEmpty();
    }
}
