package org.ujorm.wicket.component.toolbar;
/*
 * Copyright 2013-2015, Pavel Ponec
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
import javax.annotation.Nonnull;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.AbstractChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.ujorm.Ujo;
import org.ujorm.criterion.Criterion;
import org.ujorm.wicket.CommonActions;
import org.ujorm.wicket.UjoEvent;

/**
 * The common Toolbar panel
 * @author Pavel Ponec
 */
abstract public class AbstractToolbar<U extends Ujo> extends GenericPanel<U> {

    /** Delay for searching fields is 400 [ms] by default */
    protected static final Duration DEFAULT_DELAY = Duration.milliseconds(400);

    /** Model criterion */
    private IModel<Criterion<U>> criterionModel = Model.of();

    public AbstractToolbar(String id) {
        super(id);
    }

    /** Buid a default criterion. */
    @Override
    public void onInitialize() {
        super.onInitialize();
        buildCriterion();
    }

    /**
     * Create TextFiled, add 'placeholder', assign Behaviour and set an OutputMarkupId.
     * @param componentId Component ID
     * @return TextField for searching
     */
    protected TextField createSearchFiled(String componentId) {
        return createSearchFiled(componentId, String.class,  componentId);
    }

    /**
     * Create TextFiled, add 'placeholder', assign Behaviour and set an OutputMarkupId.
     * @param componentId Component ID
     * @param type Type of the field
     * @return TextField for searching
     */
    protected TextField createSearchFiled(String componentId, Class<?> type) {
        return createSearchFiled(componentId, type,  componentId);
    }

    /**
     * Create TextFiled, add 'placeholder', assign Behavior and set an OutputMarkupId.
     * @param componentId Component ID
     * @param type Type of the field
     * @param placeholderKey Localization key for a Placeholder
     * @return TextField for searching
     */
    protected final TextField createSearchFiled(String componentId, Class<?> type, String placeholderKey) {
        return createSearchFiled(componentId, type, new ResourceModel(placeholderKey, placeholderKey));
    }

    /**
     * Create TextFiled, add 'placeholder', assign Behavior and set an OutputMarkupId.
     * @param componentId Component ID
     * @param type Type of the field
     * @param placeholder Localization for a Placeholder
     * @return TextField for searching
     */
    protected TextField createSearchFiled(String componentId, Class<?> type, IModel<String> placeholder) {
        final TextField result = new TextField(componentId, new Model(), type);
        result.add(new AttributeModifier("placeholder", placeholder));
        result.setOutputMarkupId(true);
        addChangeBehavior(result);
        return result;
    }

    /** Returns a Criterion model for the OrmUjo table,
     * for example all active hotels:
     * <pre>{@code Hotel.ACTIVE.whereEq(true)}</pre>
     */
    public IModel<Criterion<U>> getCriterion() {
        return criterionModel;
    }

    /** Modify internal Criterion */
    abstract protected void buildCriterion();

    /** Create an Updating Behavior with "keyup" event
     * @param field Field is not used by default, however it can be a switch for different results for example.
     * @return
     */
    protected void addChangeBehavior(@Nonnull final FormComponent field) {
        final AjaxEventBehavior behavior = new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                buildCriterion();
                AbstractToolbar.this.onUpdate(target);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setThrottlingSettings(new ThrottlingSettings
                        ("thrId", DEFAULT_DELAY, true));
            }
        };
        field.add(behavior);
    }

    /** Create an Updating Behavior with "onchange" event for Combo
     * @param field Field is not used by default, however it can be a switch for different results for example.
     * @return
     */
    protected void addChangeBehavior(@Nonnull final AbstractChoice field) {
        final AjaxEventBehavior behavior =  new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override protected void onUpdate(AjaxRequestTarget target) {
                buildCriterion();
                AbstractToolbar.this.onUpdate(target);
            }
        };
        field.add(behavior);
    }

    /** On update event */
    protected void onUpdate(final AjaxRequestTarget target) {
        send(getPage(), Broadcast.BREADTH, new UjoEvent(getDefaultActionName(), target));
    }

    /** Default action name is {@link CommonActions#FILTER} */
    protected String getDefaultActionName() {
        return CommonActions.FILTER;
    }

    /** Implements the method to request focus */
    public void requestFocus(@Nonnull final AjaxRequestTarget target) {
    }
}
