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

package org.ujorm.wicket.component.form;

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidator;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;

/**
 * Input field including a label and a feedback message.
 * @author Pavel Ponec
 */
public class Field extends Panel {
    private static final long serialVersionUID = 20130621L;

    /** Localization property prefix */
    public static final String PROPERTY_PREFIX = "label.";

    protected FormComponent<?> input;
    protected FeedbackLabel feedback;
    protected IValidator<?> validator;
    protected WebMarkupContainer div;
    protected String cssClass;
    protected KeyRing key;
    protected List<AjaxEventBehavior> behaviors = new ArrayList<AjaxEventBehavior>();

    public Field(Key property) {
        this(property.getName(), property, null);
    }

    public Field(String componentId, Key property, String cssClass) {
        super(componentId, Model.of());
        this.key = KeyRing.of(property);
        this.cssClass = cssClass;
    }

    /** On initialize */
    @Override
    protected void onInitialize() {
        super.onInitialize();

        this.setOutputMarkupId(true);
        this.setOutputMarkupPlaceholderTag(true);

        add(div = new WebMarkupContainer("editField"));
        if (cssClass!=null) {
            div.add(new AttributeAppender("class", new Model(cssClass), " "));
        }

        final ResourceModel labelModel = new ResourceModel(PROPERTY_PREFIX
                + key.getFirstKey().getName()
                , key.getFirstKey().getName());

        div.add(new Label("label", labelModel));
        div.add(input = createInput("input", getDefaultModel()));
        div.add(feedback = new FeedbackLabel("message", input, (IModel)null));

        for (AjaxEventBehavior behavior : behaviors) {
            input.add(behavior);
        }
        feedback.setOutputMarkupId(true);
    }

    /** Create Form inputComponent */
    protected FormComponent createInput(String componentId, IModel model) {
        final FormComponent result = new TextField("input", model, key.getFirstKey().getType());
        
        if (validator != null) {
            result.add(validator);
        }

        result.setEnabled(isEnabled());
        return result;
    }

    /** Validator getter */
    public IValidator<?> getValidator() {
        return validator;
    }

    /** Validator setter */
    public Field setValidator(IValidator<?> validator) {
        this.validator = validator;
        return this;
    }

    /** Returns an {@code input} value from model */
    public Object getModelValue() {
        return input.getDefaultModelObject();
    }

    /** Set a model for the {@code input} */
    public void setModelValue(Object value) {
        input.setDefaultModelObject(value);
    }

    /** add Behaviour */
    public void addBehaviour(AjaxEventBehavior behavior) {
        behaviors.add(behavior);
    }
}
