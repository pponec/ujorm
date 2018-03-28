/*
 *  Copyright 2013-2016 Pavel Ponec
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
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.IValidator;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.KeyRing;
import org.ujorm.validator.ValidatorUtils;
import org.ujorm.wicket.CssAppender;
import org.ujorm.wicket.component.form.FeedbackLabel;
import org.ujorm.wicket.component.form.FieldEvent;
import org.ujorm.wicket.component.form.UiValidator;
import org.ujorm.wicket.component.tools.LocalizedModel;

/**
 * Common Input field with a Label including a feedback message.
 * @author Pavel Ponec
 * @param <T> Field value type
 */
public class Field<T> extends GenericPanel<T> {
    private static final long serialVersionUID = 20130621L;

    /** Delay for searching fields is 400 [ms] by default */
    protected static final Duration DEFAULT_DELAY = Duration.milliseconds(300);

    /** CSS required style for the Label */
    public static final String CSS_REQUIRED = "required";

    /** Localization key prefix */
    public static final String PROPERTY_PREFIX = "label.";

    /** A form component */
    private FormComponent<T> input;
    protected FeedbackLabel feedback;
    protected List<IValidator<? super T>> validators;
    protected String cssClass;
    /** Serializable key */
    protected KeyRing<?> key;
    protected List<Behavior> behaviors;
    /** Extended visible model, the {@code null} value means a default manner. */
    private IModel<Boolean> visibleModel;

    public <U extends Ujo> Field(Key<U,T> key) {
        this(key.getName(), key, null);
    }

    /**
     * The default constructor
     * @param componentId Required component
     * @param key Optional Ujorm Key
     * @param cssClass Optional CSS class
     */
    @SuppressWarnings("unchecked")
    public <U extends Ujo> Field(@Nonnull final String componentId, Key<U,T> key, @Nonnull final String cssClass) {
        super(componentId, new Model());
        this.key = KeyRing.of(key);
        this.cssClass = cssClass;
    }

    /** Initialize a super class for special cases */
    protected final void onSuperInitialize() {
        super.onInitialize();
    }

    /** On initialize */
    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new CssAppender(getCssClass()));

        if (cssClass!=null) {
            add(new CssAppender(cssClass));
        }

        add(input = createInput("input", (IModel) getDefaultModel()));
        add(createLabel(input));
        add(feedback = new FeedbackLabel("message", input, (IModel)null));
    }

    /** On configure */
    @Override
    protected void onConfigure() {
        super.onConfigure();

        if (validators != null) {
            for (IValidator<? super T> validator : validators) {
                input.add(validator);
                addMaxLength(input);
            }
            validators = null;
        }

        if (behaviors!=null) {
            for (Behavior behavior : behaviors) {
                input.add(behavior);
            }
            behaviors = null;
        }

        if (visibleModel!=null) {
            setVisible(Boolean.TRUE.equals(visibleModel.getObject()));
        }
    }

    /** Create Form inputComponent */
    protected FormComponent createInput(@Nonnull final String componentId, @Nonnull final IModel<T> model) {
        @SuppressWarnings("unchecked")
        final FormComponent result = new TextField(componentId, model, key.getFirstKey().getType());

        result.setEnabled(isEnabled());
        result.setLabel(createLabelModel());
        return result;
    }

    /** Validator getter */
    @Nonnull
    public List<IValidator<? super T>> getValidators() {
        if (validators != null) {
            return validators;
        }
        if (input != null) {
            return input.getValidators();
        }
        return Collections.emptyList();
    }

    /** Validator setter */
    public Field<T> addValidator(@Nonnull final IValidator<T> validator) {
        if (validators == null) {
            validators = new ArrayList<>();
        }
        validators.add(validator);
        return this;
    }

    /** The UJO Validator setter */
    public Field<T> addValidator(@Nonnull final Validator<T> ujoValidator) {
        return addValidator(new UiValidator(ujoValidator, key));
    }

    /** Returns an {@code input} value from model */
    public T getModelValue() {
        return (T) input.getDefaultModelObject();
    }

    /** Set new value for the {@code input} and reset feedback messages */
    public void setModelValue(final T value) {
        getModel().setObject(value);
        if (input != null) {
            input.modelChanged();
            input.getFeedbackMessages().clear();
        }
    }

    /** add Behaviour */
    public Field<T> addBehaviour(@Nonnull final AjaxEventBehavior behavior) {
        if (behaviors==null) {
            behaviors = new ArrayList<Behavior>();
        }
        behaviors.add(behavior);
        return this;
    }

    /** Add a CSS style according the Key */
    public Field<T> addCssStyle() {
        final Key localKey = key.getFirstKey();
        final Key css = localKey.isComposite()
                ? ((CompositeKey)localKey).getLastKey()
                : localKey;
        return addCssStyle(css);
    }

    /** Add a CSS style to the current field */
    public Field<T> addCssStyle(@Nonnull final CharSequence cssStyle) {
        add(new CssAppender(cssStyle.toString()));
        return this;
    }

    /** Return an Input component */
    public FormComponent getInput() {
        return input;
    }

    /** Add a {@code maxlength} of a text-field for String attributes */
    protected void addMaxLength(@Nonnull final FormComponent result) {
        if (key.getFirstKey().isTypeOf(String.class)) {
            int length = Integer.MAX_VALUE;
            for (IValidator<? super T> item : validators) {
                if (item instanceof UiValidator) {
                   int lh = ValidatorUtils.getMaxLength(((UiValidator)item).getValidator());
                   length = Math.min(lh, length);
                }
            }
            if (length < Integer.MAX_VALUE) {
               result.add(new AttributeModifier("maxlength", Model.of(length)));
            }
        }
    }

    /** Create Label and assign the CSS class {@code required} for the mandatory Field */
    protected Component createLabel(@Nonnull final Component inp) {
        final SimpleFormComponentLabel result = new SimpleFormComponentLabel("label", (LabeledWebMarkupContainer)input);
        //result.setDefaultModel(createLabelModel()); // see the: FormComponent.setLabel()

        if (isRequired()) {
            result.add(new CssAppender(CSS_REQUIRED));
        }
        return result;
    }

    /** Create label model */
    protected IModel<String> createLabelModel() {
        final Key<?,?> nativeKey = getKey();
        final ResourceModel labelModel = new ResourceModel
                (getResourceLabelKey(nativeKey), nativeKey.getName());
        return labelModel;
    }

    /** Resource Label Key */
    protected String getResourceLabelKey(@Nonnull final Key<?,?> key) {
        return PROPERTY_PREFIX + getKeyName();
    }

    /** Is the field required ? */
    protected boolean isRequired() {
        boolean result = validators instanceof UiValidator
            && ValidatorUtils.isMandatoryValidator(((UiValidator) validators).getValidator());
        return result;
    }

    /** Returns assigned key */
    @Nullable
    public final Key<?,?> getKey() {
        return key != null ? key.getFirstKey() : null;
    }

    /** Assign a feedback message */
    public void setFeedbackMessage(@Nonnull final IModel<String> message) {
        feedback.setFeedbackMessage(message);
    }

    /** Clear feedback messages */
    public void clearFeedbackMessages() {
        if (input != null) {
            input.getFeedbackMessages().clear();
        }
    }

    /** Returns a main CSS class */
    protected String getCssClass() {
        return "control-group";
    }

    /** Extended visible model, the {@code null} value means a default manner. */
    public IModel<Boolean> getVisibleModel() {
        return visibleModel;
    }

    /** Extended visible model, the {@code null} value means a default manner. */
    public Field setVisibleModel(@Nonnull final IModel<Boolean> visibleModel) {
        this.visibleModel = visibleModel;
        return this;
    }

    /** Set enabled the input component */
    public void setEnabledInput(final boolean enabled) {
        super.setEnabled(enabled);
        if (getInput() != null) {
            getInput().setEnabled(enabled);
        }
    }

    /** Component is enabled if its input is enabled too */
    @Override
    public boolean isEnabled() {
        if (super.isEnabled()) {
            final FormComponent input = getInput();
            return input == null || input.isEnableAllowed();
        } else {
            return false;
        }
    }

    /** Create an Updating Behavior with "keyup" event */
    public void onChange(final String action) {
        addBehaviour(createChangeBehaviour(action, "keyup"));
        addBehaviour(createChangeBehaviour(action, "change"));
    }

    /** Create new AjaxFormComponentUpdatingBehavior with delay 300 ms. */
    protected AjaxEventBehavior createChangeBehaviour
        ( @Nonnull final String action
        , @Nonnull final String jsEvent) {
        return new AjaxFormComponentUpdatingBehavior(jsEvent) {
            @Override protected void onUpdate(final AjaxRequestTarget target) {
                send(Field.this, Broadcast.BUBBLE, new FieldEvent(action, key, target));
            }

            @Override protected void updateAjaxAttributes
        ( @Nonnull final AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setThrottlingSettings(new ThrottlingSettings("thr2Id", DEFAULT_DELAY, true));
            }
        };
    }

    /** Add a new {@link AjaxFormComponentUpdatingBehavior|Behavior}
     * to updating a component model on blur events. */
    public AjaxEventBehavior addBehaviourOnBlur() {
        return new AjaxFormComponentUpdatingBehavior("blur") {
            @Override protected void onUpdate(AjaxRequestTarget t) {}
        };
    }

    /** Set a focus to the component */
    public void requestFocus(@Nonnull final AjaxRequestTarget target) {
       target.focusComponent(getInput());
    }

    /** Get a simple key name for a localization */
    protected String getKeyName() {
        return getKeyName(getKey());
    }

    /** Get a simple key name for a localization */
    protected String getKeyName(@Nonnull final Key<?,?> key) {
        return LocalizedModel.getSimpleKeyName(key);
    }

    /** A debug information */
    @Override
    public String toString() {
        if (getKey() != null) {
            return getKeyName() + " for the input: " + getInput();
        } else {
            return String.valueOf(getInput());
        }
    }

}
