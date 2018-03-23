/*
 *  Copyright 2015-2016 Pavel Ponec
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
package org.ujorm.wicket.component.choice;

import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.IModelComparator;
import org.apache.wicket.model.util.WildcardListModel;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyRing;
import org.ujorm.tools.Assert;

/**
 * Ujo Dropdown choice
 * @author Pavel Ponec
 */
public class UjoDropDown<U extends Ujo> extends DropDownChoice<U> {

    /** Pair of keys: [display/id]*/
    private final KeyRing<U> display;

    /**
     * Constructor
     * @param id Component ID
     * @param choices List of items type of Ujo
     * @param index Required Unique identifier Key
     * @param display Required display Key
     */
    public UjoDropDown(final String id, final List<? extends U> choices, Key<U, ?> index, Key<U, ?> display) {
        this(id, choices, KeyRing.<U>of(index, display));
        Assert.notNull(display, "display");
        Assert.notNull(index, "index");
    }

        /** Constructor
     * @param id Component identifier
     * @param choices Item list
     * @param display Two keys [identifier & display]
     */
    public UjoDropDown(final String id, final List<? extends U> choices, final KeyRing<U> display) {
        this(id, new WildcardListModel<U>(choices), display);
    }

    /** Constructor
     * @param id Component identifier
     * @param choices Item list
     * @param display Two keys [identifier & display]
     */
    public UjoDropDown(final String id, final IModel<List<? extends U>> choices, final KeyRing<U> display) {
        super(id, choices, new IChoiceRenderer<U>() {
            @Override public Object getDisplayValue(final U bo) {
                return display.getLastKey().of(bo);
            }
            @Override public String getIdValue(final U bo, final int index) {
                return String.valueOf(display.getFirstKey().of(bo));
            }
            @Override public U getObject(String id, IModel<? extends List<? extends U>> choices) {
                final List<? extends U> choiceList = choices.getObject();
                for (int i = 0, max = choiceList.size(); i < max; i++) {
                    final U choice = choiceList.get(i);
                    if (getIdValue(choice, i).equals(id)) {
                        return choice;
                    }
                }
                return null;
            }
        });
        this.display = display;
    }

    /** Create a new model comparator
     * @return New instance of the Model comparator */
    @Override
    public IModelComparator getModelComparator() {
        return new IModelComparator() {
            @Override public boolean compare(final Component component, final Object newObject) {
                if (display.getType().isInstance(newObject)) {
                    final U thisValue = (U) component.getDefaultModelObject();
                    final Key<U, Object> ID = (Key<U, Object>) display.getFirstKey();
                    return thisValue != null && ID.equals(thisValue, ID.of((U) newObject));
                } else {
                    return UjoDropDown.super.getModelComparator().compare(component, newObject);
                }
            }
        };
    }

}
