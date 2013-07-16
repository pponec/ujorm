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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import static org.ujorm.wicket.CssAppender.*;

/**
 * Common Feedback Field
 * @author Pavel Ponec
 */
public class FeedbackField extends Field {

    private static final long serialVersionUID = 20130621L;

    public FeedbackField(String id) {
        super(id, null, null);
        setDefaultModel(new Model(""));
        setOutputMarkupId(true);
        setOutputMarkupPlaceholderTag(true);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        if (!feedback.isFeedbackMessage()) {
            tag.put(CSS_CLASS, "hidden");
        }
    }

    /** Create Label and assign the CSS class {@code required} for the mandatory Field */
    @Override
    protected Component createLabel(final Component inp) {
        final Label result = new Label("label");
        result.setVisible(false);
        return result;
    }

    /** Create Form inputComponent */
    @Override
    protected FormComponent createInput(String componentId, IModel model) {
        final HiddenField<String> result = new HiddenField<String>(componentId, model, String.class);
        result.setLabel(new Model(""));
        return result;
    }

    @Override
    public void setFeedbackMessage(IModel<String> message) {
        // setVisible(message != null); // It does not work
        if (feedback != null) {
            feedback.setFeedbackMessage(message);
        }
    }
}
