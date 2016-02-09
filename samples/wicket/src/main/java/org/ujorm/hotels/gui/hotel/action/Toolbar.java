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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import org.ujorm.hotels.entity.City;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.wicket.CommonActions;
import org.ujorm.wicket.component.toolbar.AbstractToolbar;
import static org.ujorm.core.UjoManager.*;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public final class Toolbar<U extends Hotel> extends AbstractToolbar<U> {
    /** Event action */
    public static final String FILTER_ACTION = CommonActions.FILTER;

    /** Default criterion */
    public static final Criterion<Hotel> defaultCriterion = Hotel.ACTIVE.whereEq(true);
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

    /** Build a Criterion for a Ujorm QUERY.
     * @see #getCriterion()
     */
    @Override
    protected void buildCriterion() {
        Criterion<Hotel> result = defaultCriterion;

        if (isFilled(searchHotel.getValue())) {
            result = result.and(Hotel.NAME.where(Operator.STARTS_CASE_INSENSITIVE
                   , searchHotel.getValue()));
        }
        if (isFilled(searchCity.getValue())) {
            result = result.and(Hotel.CITY.add(City.NAME).where(Operator.STARTS_CASE_INSENSITIVE
                   , searchCity.getValue()));
        }

        final Criterion<U> resultCast = result.cast();
        getCriterion().setObject(resultCast);
    }

    /** Default action name is {@link CommonActions#FILTER} */
    @Override
    public String getDefaultActionName() {
        return FILTER_ACTION;
    }
}
