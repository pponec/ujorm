package org.ujorm.wicket.component.toolbar;
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
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
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
abstract public class AbstractToolbar<U extends Ujo> extends Panel {

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
     * @param placeholderKey Localization key for a Placeholder
     * @return TextField for searching
     */
    protected TextField createSearchFiled(String componentId) {
        return createSearchFiled(componentId, componentId);
    }

    /**
     * Create TextFiled, add 'placeholder', assign Behaviour and set an OutputMarkupId.
     * @param componentId Component ID
     * @param placeholderKey Localization key for a Placeholder
     * @return TextField for searching
     */
    protected TextField createSearchFiled(String componentId, String placeholderKey) {
        TextField result = new TextField(componentId, Model.of(""));
        result.add(new AttributeModifier("placeholder", new ResourceModel(placeholderKey, placeholderKey)));
        result.setOutputMarkupId(true);
        result.add(createChangeBehavior(result));
        return result;
    }

    /** Returns a Criterion model for the Hotel table,
     * for example all active hotels:
     * <pre>{@code Hotel.ACTIVE.whereEq(true)}</pre>
     */
    public IModel<Criterion<U>> getCriterion() {
        return criterionModel;
    }

    /** Modify internal Criteiron */
    abstract protected void buildCriterion();

    /** Create an Updating Behavior with "keyup" event
     * @param field Field is not used by default, however it can be a switch for different results for example.
     * @return
     */
    protected AjaxEventBehavior createChangeBehavior(final FormComponent field) {
        return new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                buildCriterion();
                send(getPage(), Broadcast.BREADTH, new UjoEvent(getDefaultActionName(), target));
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setThrottlingSettings(new ThrottlingSettings
                        ("thrId", DEFAULT_DELAY, true));
            }
        };
    }

    /** Default action name is {@link CommonActions#FILTER} */
    public String getDefaultActionName() {
        return CommonActions.FILTER;
    }
}
