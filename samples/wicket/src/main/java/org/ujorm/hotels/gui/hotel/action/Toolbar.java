package org.ujorm.hotels.gui.hotel.action;
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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.time.Duration;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.wicket.CommonActions;
import org.ujorm.wicket.UjoEvent;
import static org.ujorm.core.UjoManager.*;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public class Toolbar extends Panel {

    /** Delay for searching fields is 400 [ms] by default */
    protected static final Duration DEFAULT_DELAY = Duration.milliseconds(400);
    /** Event action */
    public static final String FILTER_ACTION = CommonActions.FILTER;

    /** Default criterion */
    public static final Criterion<Hotel> defaultCriterion = Hotel.ACTIVE.whereEq(true);
    /** Model criterion */
    private IModel<Criterion<Hotel>> criterionModel = Model.of();
    /** searchHotel */
    private TextField searchHotel;
    /** searchHotel */
    private TextField searchCity;

    public Toolbar(String id) {
        super(id);

        final Form form = new Form("form");
        this.add(form);
        form.add(searchHotel = createSearchFiled("searchHotel"));
        form.add(searchCity = createSearchFiled("searchCity"));
        buildCriterion();
    }

    /** Create TextFiled, add 'placeholder', assign Behaviour and set an OutputMarkupId. */
    private TextField createSearchFiled(String key) {
        TextField result = new TextField(key, Model.of(""));
        result.add(new AttributeModifier("placeholder", new ResourceModel(key, key)));
        result.setOutputMarkupId(true);
        result.add(createChangeBehavior());
        return result;
    }

    /** Modify Criteiron */
    private void buildCriterion() {
        Criterion<Hotel> result = defaultCriterion;

        if (isFilled(searchHotel.getValue())) {
            result = result.and(Hotel.NAME.where(Operator.STARTS_CASE_INSENSITIVE
                   , searchHotel.getValue()));
        }
        if (isFilled(searchCity.getValue())) {
            result = result.and(Hotel.CITY.add(City.NAME).where(Operator.STARTS_CASE_INSENSITIVE
                   , searchCity.getValue()));
        }

        criterionModel.setObject(result);
    }

    /** Returns a Criterion model for the Hotel table,
     * for example all active hotels:
     * <pre>{@code Hotel.ACTIVE.whereEq(true)}</pre>
     */
    public IModel<Criterion<Hotel>> getCriterion() {
        return criterionModel;
    }

    /** Create an Updating Behavior with "keyup" event */
    private AjaxFormComponentUpdatingBehavior createChangeBehavior() {
        return new AjaxFormComponentUpdatingBehavior("keyup") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                buildCriterion();
                send(getPage(), Broadcast.BREADTH, new UjoEvent(FILTER_ACTION, target));
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setThrottlingSettings(new ThrottlingSettings
                        ("thrId", DEFAULT_DELAY, true));
            }
        };
    }
}
