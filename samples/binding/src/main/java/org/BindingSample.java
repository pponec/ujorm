/**
  * Copyright (C) 2008, Paval Ponec, contact: http://ujoframework.org/
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation version 2 of the License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You may obtain a copy of the License at
  * http://www.gnu.org/licenses/gpl-2.0.txt
  */

package org;

import java.util.HashMap;
import java.util.Map;
import org.impl.*;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 * Example
 * @author Ponec
 */
public class BindingSample {

    private Map<UjoProperty, UIComponent> binding = new HashMap<UjoProperty, UIComponent>();

    /** Map an UICOmponent to the Person properties.
     * Note the type safe binding!
     */
    public void simpleMapping() {
        bind(Person.FIRSTNAME, new UIComponentString());
        bind(Person.SURNAME, new UIComponentString());
        bind(Person.AGE, new UIComponentInt());
        bind(Person.MALE, new UIComponentBoolean());
        bind(Person.CASH, new UIComponentDouble());
    }

    /** PFor further simplification is possible to use the auto-mapping. */
    public void autoMapping() {
        bind(Person.FIRSTNAME);
        bind(Person.SURNAME);
        bind(Person.AGE);
        bind(Person.MALE);
        bind(Person.CASH);

        // How to set some attributes?
        component(Person.FIRSTNAME).setStyle("width", "100px");
        component(Person.SURNAME).setStyle("width", "100px");
        component(Person.CASH).setStyle("color", "red");
    }

    /** Load data to a GUI panel */
    @SuppressWarnings("unchecked")
	public void initForm(Person person) {
        for (UjoProperty p : binding.keySet()) {
            UIComponent component = binding.get(p);
            component.setValue(p.getValue(person));
        }
    }

    /** Load data from GUI panel to the Person instance */
    @SuppressWarnings("unchecked")
	public void loadInputValues(Ujo person) {
        for (UjoProperty p : binding.keySet()) {
            UIComponent component = binding.get(p);
            p.setValue(person, component.getValue());
        }
    }

    /** Type safe <build>component binding</build> */
    public <T> void bind(UjoProperty<?, T> property, UIComponent<T> component) {
        binding.put(property, component);
    }

    /** Auto-binding implementation */
    public void bind(UjoProperty property) {
        if (property.isTypeOf(String.class)) {
             binding.put(property, new UIComponentString());
        } else if (property.isTypeOf(Integer.class)) {
             binding.put(property, new UIComponentInt());
        } else if (property.isTypeOf(Boolean.class)) {
             binding.put(property, new UIComponentBoolean());
        } else if (property.isTypeOf(Double.class)) {
             binding.put(property, new UIComponentDouble());
        }
    }

    /** Type safe <build>component binding</build> */
    @SuppressWarnings("unchecked")
	public <T> UIComponent<T> component(UjoProperty<?, T> property) {
        return binding.get(property);
    }
}
