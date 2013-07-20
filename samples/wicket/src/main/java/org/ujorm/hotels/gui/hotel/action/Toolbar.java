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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Hotel;

/**
 * The common action panel
 * @author Pavel Ponec
 */
public class Toolbar extends Panel {

    /** Default criterion */
    public static final Criterion<Hotel> defaultCriterion = Hotel.ACTIVE.whereEq(true);
    /** Model criterion */
    private IModel<Criterion<Hotel>> criterionModel = Model.of();

    public Toolbar(String id) {
        super(id);
    }

    /** Returns a Criterion model for the Hotel table,
     * for example all active hotels:
     * <pre>{@code Hotel.ACTIVE.whereEq(true)}</pre>
     */
    public IModel<Criterion<Hotel>> getCriterion() {
        Criterion<Hotel> result = defaultCriterion;
        if (false) {
            // TODO ... add criterions
        }
        criterionModel.setObject(result);
        return criterionModel;
    }
}
