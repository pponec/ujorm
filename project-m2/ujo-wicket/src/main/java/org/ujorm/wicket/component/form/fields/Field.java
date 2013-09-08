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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.LabeledWebMarkupContainer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.IValidator;
import org.ujorm.Key;
import org.ujorm.Validator;
import org.ujorm.core.KeyRing;
import org.ujorm.validator.ValidatorUtils;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.form.FeedbackLabel;
import org.ujorm.wicket.component.form.FieldEvent;
import org.ujorm.wicket.component.form.UiValidator;

/**
 * Input textfield with a Label includding a feedback message.
 * @author Pavel Ponec
 */
public class Field extends Panel {
    private static final long serialVersionUID = 20130621L;

    /** Delay for searching fields is 400 [ms] by default */
    protected static final Duration DEFAULT_DELAY = Duration.milliseconds(300);

    /** CSS required style for the Label */
    public static final String CSS_REQUIRED = "required";

    /** Localization property prefix */
    public static final String PROPERTY_PREFIX = "label.";

    /** A form component */
    private FormComponent input;
    protected FeedbackLabel feedback;
    protected IValidator<?> validator;
    protected String cssClass;
    /** Serializable key */
    protected KeyRing key;
    protected List<Behavior> behaviors;

    public Field(Key property) {
        this(property.getName(), property, null);
        this.setOutputMarkupPlaceholderTag(true);
    }

    /**
     * The default constructor
     * @param componentId Required component
     * @param property Optional Ujorm Key
     * @param cssClass Optional CSS class
     */
    public Field(String componentId, Key property, String cssClass) {
        super(componentId, Model.of());
        this.key = KeyRing.of(property);
        this.cssClass = cssClass;
    }

    /** On initialize */
    @Override
    protected void onInitialize() {
        super.onInitialize();
        this.add(new CssAppender(getCssClass()));

        if (cssClass!=null) {
            add(new CssAppender(cssClass));
        }

        add(input = createInput("input", getDefaultModel()));
        add(createLabel(input));
        add(feedback = new FeedbackLabel("message", input, (IModel)null));

        if (behaviors!=null) {
            for (Behavior behavior : behaviors) {
                input.add(behavior);
            }
            behaviors = null;
        }
        feedback.setOutputMarkupId(true);
    }

    /** Create Form inputComponent */
    protected FormComponent createInput(String componentId, IModel model) {
        final FormComponent result = new TextField(componentId, model, key.getFirstKey().getType());

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

    /** Validator setter */
    public Field setValidator(Validator<?> validator) {
        this.validator = new UiValidator(validator, key);
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
        if (behaviors==null) {
            behaviors = new ArrayList<Behavior>();
        }
        behaviors.add(behavior);
    }

    /** Return an Input component */
    public FormComponent getInput() {
        return input;
    }

    /** Add a {@code maxlength} of a text-field for String attributes */
    protected void addMaxLength(final FormComponent result) {
        if (validator instanceof UiValidator
        && key.getFirstKey().isTypeOf(String.class)) {
            int length = ValidatorUtils.getMaxLength(((UiValidator)validator).getValidator());
            if (length >= 0) {
               result.add(new AttributeModifier("maxlength", Model.of(length)));
            }
        }
    }

    /** Create Label and assign the CSS class {@code required} for the mandatory Field */
    protected Component createLabel(final Component inp) {
        final SimpleFormComponentLabel result = new SimpleFormComponentLabel("label", (LabeledWebMarkupContainer)input);
        //result.setDefaultModel(createLabelModel()); // see the: FormComponent.setLabel()

        if (isRequired()) {
            result.add(new CssAppender(CSS_REQUIRED));
        }
        return result;
    }

    /** Create label model */
    protected IModel createLabelModel() {
        final Key<?,?> nativeKey = key.getFirstKey();
        final ResourceModel labelModel = new ResourceModel
                (getResourceLabelKey(nativeKey), nativeKey.getName());
        return labelModel;
    }

    /** Resource Label Key */
    protected String getResourceLabelKey(final Key<?,?> key) {
        return PROPERTY_PREFIX + key.toStringFull();
    }

    /** Is the field required ? */
    protected boolean isRequired() {
        boolean result = validator instanceof UiValidator
            && ValidatorUtils.isMandatoryValidator(((UiValidator) validator).getValidator());
        return result;
    }

    /** Returns assigned key */
    public Key<?,?> getKey() {
        return key.getFirstKey();
    }

    /** Assign a feedback message */
    public void setFeedbackMessage(IModel<String> message) {
        feedback.setFeedbackMessage(message);
    }

    /** Returns a main CSS class */
    protected String getCssClass() {
        return "control-group";
    }

    /** Create an Updating Behavior with "keyup" event
     * @param field Field is not used by default, however it can be a switch for different results for example.
     * @return
     */
    public void onChange(final String action) {
        addBehaviour(createChangeBehaviour(action, "keyup"));
        addBehaviour(createChangeBehaviour(action, "onchange"));
    }

    /** Create new AjaxFormComponentUpdatingBehavior with delay 300 ms. */
    protected AjaxEventBehavior createChangeBehaviour(final String action, final String jsEvent) {
        return new AjaxFormComponentUpdatingBehavior(jsEvent) {
            @Override protected void onUpdate(AjaxRequestTarget target) {
                send(Field.this, Broadcast.BUBBLE, new FieldEvent(action, key, target));
            }

            @Override protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setThrottlingSettings(new ThrottlingSettings("thr2Id", DEFAULT_DELAY, true));
            }
        };
    }
}
