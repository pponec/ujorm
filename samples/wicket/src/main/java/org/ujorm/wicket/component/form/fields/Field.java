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

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidator;
import org.ujorm.Key;
import org.ujorm.core.KeyRing;
import org.ujorm.validator.ValidatorUtils;
import org.ujorm.wicket.component.form.FeedbackLabel;
import org.ujorm.wicket.component.form.UjoValidator;
import static org.ujorm.wicket.CommonConstants.*;

/**
 * Input textfield with a Label includding a feedback message.
 * @author Pavel Ponec
 */
public class Field extends Panel {
    private static final long serialVersionUID = 20130621L;

    /** CSS required style for the Label */
    public static final String CSS_REQUIRED = "required";

    /** Localization property prefix */
    public static final String PROPERTY_PREFIX = "label.";

    /** Input */
    private FormComponent<?> input;
    protected FeedbackLabel feedback;
    protected IValidator<?> validator;
    protected WebMarkupContainer div;
    protected String cssClass;
    /** Serializable key */
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
            div.add(new AttributeAppender(CSS_CLASS, new Model(cssClass), " "));
        }


        div.add(input = createInput("input", getDefaultModel()));
        div.add(createLabel(input));
        div.add(feedback = new FeedbackLabel("message", input, (IModel)null));

        for (AjaxEventBehavior behavior : behaviors) {
            input.add(behavior);
        }
        feedback.setOutputMarkupId(true);
    }

    /** Create Form inputComponent */
    protected FormComponent createInput(String componentId, IModel model) {
        final FormComponent result = new TextField("input", model, key.getFirstKey().getType());
        result.add(new AttributeModifier("type", "text"));
        
        if (validator != null) {
            result.add(validator);
            addMaxLength(result);
        }

        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
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

    /** Set new value for the {@code input} and reset feedback messages */
    public void setModelValue(Object value) {
        input.getFeedbackMessages().clear();
        input.setDefaultModelObject(value);
    }

    /** add Behaviour */
    public void addBehaviour(AjaxEventBehavior behavior) {
        behaviors.add(behavior);
    }

    /** Return an Input component */
    public Component getInput() {
        return input;
    }

    /** Add a {@code maxlength} of a text-field for String attributes */
    protected void addMaxLength(final FormComponent result) {
        if (validator instanceof UjoValidator
        && key.getFirstKey().isTypeOf(String.class)) {
            int length = ValidatorUtils.getMaxLength(((UjoValidator)validator).getValidator());
            if (length >= 0) {
               result.add(new AttributeModifier("maxlength", Model.of(length)));
            }
        }
    }

    /** Create Label and assign the CSS class {@code required} for the mandatory Field */
    protected Component createLabel(final Component inp) {
        final SimpleFormComponentLabel result = new SimpleFormComponentLabel("label", input);
        //result.setDefaultModel(createLabelModel()); // see the: FormComponent.setLabel()

        if (isMandatory()) {
            result.add(new AttributeAppender(CSS_CLASS, CSS_REQUIRED));
        }
        return result;
    }

    /** Create label model */
    protected IModel createLabelModel() {
        final ResourceModel labelModel = new ResourceModel(PROPERTY_PREFIX
            + key.getFirstKey().getName()
            , key.getFirstKey().getName());
        return labelModel;
    }

    /** Is the field required ? */
    protected boolean isMandatory() {
        boolean result = validator instanceof UjoValidator
            && ValidatorUtils.isMandatoryValidator(((UjoValidator) validator).getValidator());
        return result;
    }


}
